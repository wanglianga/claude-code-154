package com.cm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 临时换品记录：现场实际售卖内容与报名不符时，摊主登记的临时变更 */
@Entity
@Table(name = "goods_change")
public class GoodsChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    /** 原报名售卖内容 */
    @Column(length = 300)
    private String originalGoods;

    /** 临时更换后内容 */
    @Column(length = 300, nullable = false)
    private String changedGoods;

    @Column(length = 300)
    private String reason;

    @Column(name = "staff_id")
    private Long staffId;

    /** 工作人员是否确认允许 */
    @Column(nullable = false)
    private Boolean approved = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getOriginalGoods() { return originalGoods; }
    public void setOriginalGoods(String originalGoods) { this.originalGoods = originalGoods; }
    public String getChangedGoods() { return changedGoods; }
    public void setChangedGoods(String changedGoods) { this.changedGoods = changedGoods; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
