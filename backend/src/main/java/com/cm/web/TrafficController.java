package com.cm.web;

import com.cm.dto.Views;
import com.cm.service.TrafficService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TrafficController {

    private final TrafficService traffic;

    public TrafficController(TrafficService traffic) {
        this.traffic = traffic;
    }

    @GetMapping("/events/{eventId}/traffic")
    public List<Map<String, Object>> list(@PathVariable Long eventId) {
        Roles.requireStaff();
        return traffic.list(eventId).stream().map(Views::traffic).toList();
    }

    @PostMapping("/events/{eventId}/traffic")
    public Map<String, Object> add(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        int count = Integer.parseInt(body.get("visitorCount").toString());
        return Views.traffic(traffic.add(eventId, (String) body.get("period"), count,
                (String) body.get("note"), Roles.user().getId()));
    }
}
