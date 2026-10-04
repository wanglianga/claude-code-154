package com.cm.web;

import com.cm.domain.ComplaintStatus;
import com.cm.dto.Views;
import com.cm.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private final ComplaintService complaints;

    public ComplaintController(ComplaintService complaints) {
        this.complaints = complaints;
    }

    @GetMapping("/events/{eventId}/complaints")
    public List<Map<String, Object>> list(@PathVariable Long eventId) {
        Roles.requireStaff();
        return complaints.listByEvent(eventId).stream().map(Views::complaint).toList();
    }

    /** 工作人员登记投诉 */
    @PostMapping("/events/{eventId}/complaints")
    public Map<String, Object> create(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        Long assignmentId = body.get("assignmentId") == null || body.get("assignmentId").toString().isBlank()
                ? null : Long.valueOf(body.get("assignmentId").toString());
        return Views.complaint(complaints.create(eventId, assignmentId,
                (String) body.get("complainant"), (String) body.get("contact"), (String) body.get("content")));
    }

    /** 处理投诉 */
    @PostMapping("/complaints/{id}/handle")
    public Map<String, Object> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        ComplaintStatus status = ComplaintStatus.valueOf(body.get("status").toString());
        return Views.complaint(complaints.handle(id, status,
                (String) body.get("handleResult"), Roles.user().getId()));
    }
}
