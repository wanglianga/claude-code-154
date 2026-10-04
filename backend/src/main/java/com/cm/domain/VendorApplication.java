package com.cm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 摊主报名（同一活动同一摊主唯一）。
 * 汇总售卖品类、食品资质、公益捐赠比例、用电需求、桌椅需求、亲子摊位、收款方式。
 */
@Entity
@Table(name = "vendor_application",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "vendor_id"}))
public class VendorApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    /** 摊位/招牌名称 */
    @Column(nullable = false, length = 100)
    private String stallName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Category category;

    @Column(length = 500)
    private String goodsDescription;

    // ---- 食品资质 ----
    @Column(nullable = false)
    private Boolean foodQualified = false;

    /** 食品经营许可证 / 健康证编号 */
    @Column(length = 100)
    private String foodCertNo;

    /** 资质附件材料说明（文件名/URL） */
    @Column(length = 300)
    private String foodCertAttachment;

    // ---- 公益 ----
    /** 承诺捐赠占营业额比例（%，0~100） */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal donationPercent = BigDecimal.ZERO;

    // ---- 现场需求 ----
    /** 用电需求功率(W)，0 表示不需要 */
    @Column(nullable = false)
    private Integer powerNeedWatt = 0;

    /** 需求桌子数量 */
    @Column(nullable = false)
    private Integer tableNeed = 1;

    /** 需求椅子数量 */
    @Column(nullable = false)
    private Integer chairNeed = 2;

    @Column(nullable = false)
    private Boolean familyStall = false;

    /** 是否产生噪音（现场制作、扩音等） */
    @Column(nullable = false)
    private Boolean noisy = false;

    /** 收款方式：现金 / 微信 / 支付宝 / 聚合码 */
    @Column(nullable = false, length = 50)
    private String paymentMethod = "微信";

    /** 收款流水账号（脱敏备注，如微信昵称/手机号后四位） */
    @Column(length = 100)
    private String paymentAccount;

    // ---- 审核 ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(length = 300)
    private String reviewComment;

    private Long reviewedBy;
    private LocalDateTime reviewedAt;

    /** 审核规则快照：提交时系统自动校验的命中项提示 */
    @Column(length = 500)
    private String ruleHints;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getStallName() { return stallName; }
    public void setStallName(String stallName) { this.stallName = stallName; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getGoodsDescription() { return goodsDescription; }
    public void setGoodsDescription(String goodsDescription) { this.goodsDescription = goodsDescription; }
    public Boolean getFoodQualified() { return foodQualified; }
    public void setFoodQualified(Boolean foodQualified) { this.foodQualified = foodQualified; }
    public String getFoodCertNo() { return foodCertNo; }
    public void setFoodCertNo(String foodCertNo) { this.foodCertNo = foodCertNo; }
    public String getFoodCertAttachment() { return foodCertAttachment; }
    public void setFoodCertAttachment(String foodCertAttachment) { this.foodCertAttachment = foodCertAttachment; }
    public BigDecimal getDonationPercent() { return donationPercent; }
    public void setDonationPercent(BigDecimal donationPercent) { this.donationPercent = donationPercent; }
    public Integer getPowerNeedWatt() { return powerNeedWatt; }
    public void setPowerNeedWatt(Integer powerNeedWatt) { this.powerNeedWatt = powerNeedWatt; }
    public Integer getTableNeed() { return tableNeed; }
    public void setTableNeed(Integer tableNeed) { this.tableNeed = tableNeed; }
    public Integer getChairNeed() { return chairNeed; }
    public void setChairNeed(Integer chairNeed) { this.chairNeed = chairNeed; }
    public Boolean getFamilyStall() { return familyStall; }
    public void setFamilyStall(Boolean familyStall) { this.familyStall = familyStall; }
    public Boolean getNoisy() { return noisy; }
    public void setNoisy(Boolean noisy) { this.noisy = noisy; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentAccount() { return paymentAccount; }
    public void setPaymentAccount(String paymentAccount) { this.paymentAccount = paymentAccount; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getRuleHints() { return ruleHints; }
    public void setRuleHints(String ruleHints) { this.ruleHints = ruleHints; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
