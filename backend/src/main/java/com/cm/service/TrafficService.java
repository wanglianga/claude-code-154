package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TrafficService {

    private final TrafficRecordRepository traffic;
    private final EventRepository events;

    public TrafficService(TrafficRecordRepository traffic, EventRepository events) {
        this.traffic = traffic;
        this.events = events;
    }

    public List<TrafficRecord> list(Long eventId) {
        return traffic.findByEventIdOrderByCreatedAtAsc(eventId);
    }

    @Transactional
    public TrafficRecord add(Long eventId, String period, int count, String note, Long staffId) {
        events.findById(eventId).orElseThrow(() -> new ApiException(404, "活动不存在"));
        if (count < 0) {
            throw new ApiException("客流数不能为负");
        }
        TrafficRecord t = new TrafficRecord();
        t.setEventId(eventId);
        t.setPeriod(period);
        t.setVisitorCount(count);
        t.setNote(note);
        t.setStaffId(staffId);
        return traffic.save(t);
    }
}
