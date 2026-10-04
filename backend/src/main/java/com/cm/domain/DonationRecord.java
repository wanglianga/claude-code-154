package com.cm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠记录：按报名承诺比例 × 现场营业额得出应捐金额，
 * 摊主实际缴纳后工作人员核实；系统自动比对流水，标记是否一致。
 */
@Entity
@Table(name = "donation_record")
public class DonationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "assignment_id", nullable = false, unique = true)
    private Long assignmentId;

    /** 总营业额（来自收款流水汇总） */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salesTotal = BigDecimal.ZERO;

    /** 承诺捐赠比例（快照） */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent = BigDecimal.ZERO;

    /** 按比例应捐金额 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedAmount = BigDecimal.ZERO;

    /** 摊主实际缴纳金额 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DonationStatus status = DonationStatus.PENDING_PAY;

    @Column(length = 100)
    private String receiptNo;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(length = 300)
    private String note;

    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public BigDecimal getSalesTotal() { return salesTotal; }
    public void setSalesTotal(BigDecimal salesTotal) { this.salesTotal = salesTotal; }
    public BigDecimal getPercent() { return percent; }
    public void setPercent(BigDecimal percent) { this.percent = percent; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
