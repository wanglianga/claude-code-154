package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final EventRepository events;
    private final StallAssignmentRepository assignments;

    public ComplaintService(ComplaintRepository complaints, EventRepository events,
                            StallAssignmentRepository assignments) {
        this.complaints = complaints;
        this.events = events;
        this.assignments = assignments;
    }

    public List<Complaint> listByEvent(Long eventId) {
        return complaints.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    /** 投诉登记（工作人员代记，或居民通过公开页提交） */
    @Transactional
    public Complaint create(Long eventId, Long assignmentId, String complainant,
                            String contact, String content) {
        events.findById(eventId).orElseThrow(() -> new ApiException(404, "活动不存在"));
        if (assignmentId != null) {
            assignments.findById(assignmentId).orElseThrow(() -> new ApiException(404, "摊位分配不存在"));
        }
        if (content == null || content.isBlank()) {
            throw new ApiException("投诉内容不能为空");
        }
        Complaint c = new Complaint();
        c.setEventId(eventId);
        c.setAssignmentId(assignmentId);
        c.setComplainant(complainant == null || complainant.isBlank() ? "匿名居民" : complainant);
        c.setContact(contact);
        c.setContent(content);
        c.setStatus(ComplaintStatus.OPEN);
        return complaints.save(c);
    }

    @Transactional
    public Complaint handle(Long complaintId, ComplaintStatus status, String result, Long staffId) {
        Complaint c = complaints.findById(complaintId)
                .orElseThrow(() -> new ApiException(404, "投诉不存在"));
        c.setStatus(status);
        c.setHandleResult(result);
        c.setHandledBy(staffId);
        c.setHandledAt(LocalDateTime.now());
        return complaints.save(c);
    }
}
