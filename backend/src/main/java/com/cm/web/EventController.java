package com.cm.web;

import com.cm.domain.EventStatus;
import com.cm.dto.Views;
import com.cm.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return eventService.list().stream().map(Views::event).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return Views.event(eventService.get(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        return Views.event(eventService.create(body));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        return Views.event(eventService.update(id, body));
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Roles.requireAdmin();
        return Views.event(eventService.updateStatus(id, EventStatus.valueOf(body.get("status"))));
    }

    // ---------------- 摊位布局 ----------------

    @GetMapping("/{id}/stalls")
    public List<Map<String, Object>> stalls(@PathVariable Long id) {
        return eventService.listStalls(id).stream().map(Views::stall).toList();
    }

    @PostMapping("/{id}/stalls")
    public Map<String, Object> addStall(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        return Views.stall(eventService.saveStall(id, body));
    }

    @PutMapping("/stalls/{stallId}")
    public Map<String, Object> editStall(@PathVariable Long stallId, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        body.put("id", stallId);
        Long eventId = Long.valueOf(body.get("eventId").toString());
        return Views.stall(eventService.saveStall(eventId, body));
    }

    @DeleteMapping("/stalls/{stallId}")
    public Map<String, Object> deleteStall(@PathVariable Long stallId) {
        Roles.requireAdmin();
        eventService.deleteStall(stallId);
        return Map.of("success", true);
    }
}
