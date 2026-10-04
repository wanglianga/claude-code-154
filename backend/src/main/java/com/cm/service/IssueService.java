package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 活动档案中的异常登记与处理；处理结果联动摊主信用分与食品风险标记 */
@Service
public class IssueService {

    private final EventIssueRepository issues;
    private final StallAssignmentRepository assignments;
    private final VendorApplicationRepository applications;
    private final UserRepository users;

    public IssueService(EventIssueRepository issues, StallAssignmentRepository assignments,
                        VendorApplicationRepository applications, UserRepository users) {
        this.issues = issues;
        this.assignments = assignments;
        this.applications = applications;
        this.users = users;
    }

    public List<EventIssue> listByEvent(Long eventId) {
        return issues.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    /** 登记异常，立即扣减摊主信用分 */
    @Transactional
    public EventIssue report(Long eventId, Long assignmentId, IssueType type,
                             String description, Long staffId, int penalty) {
        EventIssue i = new EventIssue();
        i.setEventId(eventId);
        i.setAssignmentId(assignmentId);
        i.setType(type);
        i.setDescription(description);
        i.setReportedBy(staffId);
        i.setCreditPenalty(penalty);
        i.setStatus(IssueStatus.OPEN);
        issues.save(i);
        applyPenalty(i, penalty, false);
        return i;
    }

    @Transactional
    public EventIssue resolve(Long issueId, String resolution, boolean penalize, Long staffId) {
        EventIssue i = issues.findById(issueId).orElseThrow(() -> new ApiException(404, "异常不存在"));
        if (i.getStatus() != IssueStatus.OPEN) {
            throw new ApiException("该异常已处理");
        }
        i.setResolution(resolution);
        i.setResolvedBy(staffId);
        i.setResolvedAt(LocalDateTime.now());
        i.setStatus(penalize ? IssueStatus.PENALIZED : IssueStatus.RESOLVED);
        if (penalize && i.getCreditPenalty() != null && i.getCreditPenalty() > 0) {
            applyPenalty(i, i.getCreditPenalty(), true);
        }
        return issues.save(i);
    }

    private void applyPenalty(EventIssue i, int penalty, boolean alreadyResolved) {
        if (i.getAssignmentId() == null || penalty <= 0) {
            return;
        }
        StallAssignment as = assignments.findById(i.getAssignmentId()).orElse(null);
        if (as == null) return;
        VendorApplication a = applications.findById(as.getApplicationId()).orElse(null);
        if (a == null) return;
        User v = users.findById(a.getVendorId()).orElse(null);
        if (v == null) return;
        v.setCreditScore(Math.max(0, (v.getCreditScore() == null ? 100 : v.getCreditScore()) - penalty));
        if (i.getType() == IssueType.FOOD_QUALIFICATION) {
            v.setFoodRiskFlag(true);
        }
        users.save(v);
    }
}
