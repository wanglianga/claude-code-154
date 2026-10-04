package com.cm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 活动档案中的异常事件：食品资质不完整、售卖不符、同类冲突、用电超载、
 * 提前离场、捐赠与流水不一致等。处理结果会联动摊主信用分与食品风险标记。
 */
@Entity
@Table(name = "event_issue")
public class EventIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "assignment_id")
    private Long assignmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType type;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(length = 300)
    private String resolution;

    /** 本次异常对摊主信用分的扣减（正数） */
    @Column(nullable = false)
    private Integer creditPenalty = 0;

    private Long reportedBy;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public IssueType getType() { return type; }
    public void setType(IssueType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Integer getCreditPenalty() { return creditPenalty; }
    public void setCreditPenalty(Integer creditPenalty) { this.creditPenalty = creditPenalty; }
    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
