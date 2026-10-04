package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventRepository events;
    private final StallRepository stalls;

    public EventService(EventRepository events, StallRepository stalls) {
        this.events = events;
        this.stalls = stalls;
    }

    public List<Event> list() {
        return events.findAllByOrderByStartTimeDesc();
    }

    public Event get(Long id) {
        return events.findById(id).orElseThrow(() -> new ApiException(404, "活动不存在"));
    }

    @Transactional
    public Event create(Map<String, Object> body) {
        Event e = new Event();
        applyFields(e, body);
        e.setStatus(EventStatus.PLANNED);
        return events.save(e);
    }

    @Transactional
    public Event update(Long id, Map<String, Object> body) {
        Event e = get(id);
        applyFields(e, body);
        return events.save(e);
    }

    @Transactional
    public Event updateStatus(Long id, EventStatus status) {
        Event e = get(id);
        e.setStatus(status);
        return events.save(e);
    }

    private void applyFields(Event e, Map<String, Object> b) {
        if (b.containsKey("name")) e.setName((String) b.get("name"));
        if (b.containsKey("description")) e.setDescription((String) b.get("description"));
        if (b.containsKey("location")) e.setLocation((String) b.get("location"));
        if (b.get("startTime") != null) e.setStartTime(LocalDateTime.parse(((String) b.get("startTime")).replace(' ', 'T')));
        if (b.get("endTime") != null) e.setEndTime(LocalDateTime.parse(((String) b.get("endTime")).replace(' ', 'T')));
        if (b.get("donationGoal") != null) e.setDonationGoal(new BigDecimal(b.get("donationGoal").toString()));
        if (b.containsKey("donationPurpose")) e.setDonationPurpose((String) b.get("donationPurpose"));
        if (b.get("totalPowerWatt") != null) e.setTotalPowerWatt(Integer.valueOf(b.get("totalPowerWatt").toString()));
    }

    // ---------------- 摊位 ----------------

    public List<Stall> listStalls(Long eventId) {
        get(eventId);
        return stalls.findByEventIdOrderByCode(eventId);
    }

    @Transactional
    public Stall saveStall(Long eventId, Map<String, Object> body) {
        get(eventId);
        Stall s;
        Object idObj = body.get("id");
        if (idObj != null) {
            s = stalls.findById(Long.valueOf(idObj.toString()))
                    .orElseThrow(() -> new ApiException(404, "摊位不存在"));
        } else {
            s = new Stall();
            s.setEventId(eventId);
        }
        s.setCode((String) body.getOrDefault("code", s.getCode()));
        s.setZone((String) body.getOrDefault("zone", s.getZone()));
        if (body.get("sizeSqm") != null) s.setSizeSqm(Double.valueOf(body.get("sizeSqm").toString()));
        if (body.get("trafficLevel") != null) s.setTrafficLevel(TrafficLevel.valueOf(body.get("trafficLevel").toString()));
        if (body.get("powerAvailable") != null) s.setPowerAvailable(Boolean.valueOf(body.get("powerAvailable").toString()));
        if (body.get("maxPowerWatt") != null) s.setMaxPowerWatt(Integer.valueOf(body.get("maxPowerWatt").toString()));
        if (body.get("powerCircuit") != null) s.setPowerCircuit((String) body.get("powerCircuit"));
        if (body.get("noiseSuitable") != null) s.setNoiseSuitable(Boolean.valueOf(body.get("noiseSuitable").toString()));
        if (body.get("familySpot") != null) s.setFamilySpot(Boolean.valueOf(body.get("familySpot").toString()));
        if (body.get("posX") != null) s.setPosX(Double.valueOf(body.get("posX").toString()));
        if (body.get("posY") != null) s.setPosY(Double.valueOf(body.get("posY").toString()));
        if (s.getMaxPowerWatt() != null && s.getMaxPowerWatt() > 0
                && (s.getPowerCircuit() == null || "N".equals(s.getPowerCircuit()))) {
            s.setPowerCircuit("P" + s.getZone());
        }
        return stalls.save(s);
    }

    @Transactional
    public void deleteStall(Long stallId) {
        stalls.deleteById(stallId);
    }
}
