package com.cm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动结束后对居民公开的公示档案：总捐赠、用途、摊主履约、异常处理。
 * 由社区管理员发布，公开展示页直接读取已发布记录。
 */
@Entity
@Table(name = "public_report")
public class PublicReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    /** 公示总捐赠金额（发布时快照） */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDonation = BigDecimal.ZERO;

    /** 总客流（快照） */
    @Column(nullable = false)
    private Integer totalVisitors = 0;

    /** 履约优良摊位数 / 异常摊位数（快照） */
    @Column(nullable = false)
    private Integer goodPerformers = 0;
    @Column(nullable = false)
    private Integer issueVendorCount = 0;

    /** 捐赠用途说明 */
    @Column(length = 500)
    private String donationPurpose;

    /** 异常处理情况说明 */
    @Column(length = 1000)
    private String issueSummary;

    /** 下次活动招募说明 */
    @Column(length = 500)
    private String nextPlan;

    private Long publishedBy;
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public BigDecimal getTotalDonation() { return totalDonation; }
    public void setTotalDonation(BigDecimal totalDonation) { this.totalDonation = totalDonation; }
    public Integer getTotalVisitors() { return totalVisitors; }
    public void setTotalVisitors(Integer totalVisitors) { this.totalVisitors = totalVisitors; }
    public Integer getGoodPerformers() { return goodPerformers; }
    public void setGoodPerformers(Integer goodPerformers) { this.goodPerformers = goodPerformers; }
    public Integer getIssueVendorCount() { return issueVendorCount; }
    public void setIssueVendorCount(Integer issueVendorCount) { this.issueVendorCount = issueVendorCount; }
    public String getDonationPurpose() { return donationPurpose; }
    public void setDonationPurpose(String donationPurpose) { this.donationPurpose = donationPurpose; }
    public String getIssueSummary() { return issueSummary; }
    public void setIssueSummary(String issueSummary) { this.issueSummary = issueSummary; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String nextPlan) { this.nextPlan = nextPlan; }
    public Long getPublishedBy() { return publishedBy; }
    public void setPublishedBy(Long publishedBy) { this.publishedBy = publishedBy; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
