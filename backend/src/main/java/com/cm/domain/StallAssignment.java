package com.cm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 摊位分配记录（一个通过审核的报名对应一条分配）。
 * 保存系统综合评分、是否自动分配，便于复盘与人工调整。
 */
@Entity
@Table(name = "stall_assignment")
public class StallAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(name = "stall_id", nullable = false)
    private Long stallId;

    /** 入场核销短码（6 位），印/展示在入场二维码中 */
    @Column(nullable = false, unique = true, length = 8)
    private String checkinCode;

    /** 综合分配评分：信用、历史捐赠、公益比例越高分越高，食品风险扣分 */
    private Integer score;

    @Column(length = 300)
    private String reason;

    @Column(nullable = false)
    private Boolean autoAssigned = true;

    /** 摊主是否已核销入场 */
    @Column(nullable = false)
    private Boolean checkedIn = false;

    private LocalDateTime checkInAt;

    /** 核销/入场工作人员 */
    private Long checkInBy;

    /** 摊主是否提前离场 */
    @Column(nullable = false)
    private Boolean earlyLeave = false;

    /** 物资领取清单是否已领取 */
    @Column(nullable = false)
    private Boolean suppliesClaimed = false;

    private LocalDateTime suppliesClaimedAt;

    private LocalDateTime leaveAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }
    public String getCheckinCode() { return checkinCode; }
    public void setCheckinCode(String checkinCode) { this.checkinCode = checkinCode; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Boolean getAutoAssigned() { return autoAssigned; }
    public void setAutoAssigned(Boolean autoAssigned) { this.autoAssigned = autoAssigned; }
    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public LocalDateTime getCheckInAt() { return checkInAt; }
    public void setCheckInAt(LocalDateTime checkInAt) { this.checkInAt = checkInAt; }
    public Long getCheckInBy() { return checkInBy; }
    public void setCheckInBy(Long checkInBy) { this.checkInBy = checkInBy; }
    public Boolean getEarlyLeave() { return earlyLeave; }
    public void setEarlyLeave(Boolean earlyLeave) { this.earlyLeave = earlyLeave; }
    public LocalDateTime getLeaveAt() { return leaveAt; }
    public void setLeaveAt(LocalDateTime leaveAt) { this.leaveAt = leaveAt; }
    public Boolean getSuppliesClaimed() { return suppliesClaimed; }
    public void setSuppliesClaimed(Boolean suppliesClaimed) { this.suppliesClaimed = suppliesClaimed; }
    public LocalDateTime getSuppliesClaimedAt() { return suppliesClaimedAt; }
    public void setSuppliesClaimedAt(LocalDateTime suppliesClaimedAt) { this.suppliesClaimedAt = suppliesClaimedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
