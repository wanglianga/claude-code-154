package com.community.market.domain.entity;

import com.community.market.domain.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠记录：按报名生成。
 * expectedAmount = 营业额合计 × 承诺比例；
 * actualAmount 为实际上缴捐赠；二者不一致进入 DISPUTED 并生成异常。
 */
@Data
@Entity
@Table(name = "donation_record")
public class DonationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long applicationId;

    /** 承诺比例快照（百分比） */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    /** 营业额合计快照 */
    @Column(precision = 12, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    /** 应捐金额 */
    @Column(precision = 12, scale = 2)
    private BigDecimal expectedAmount = BigDecimal.ZERO;

    /** 实际捐赠金额 */
    @Column(precision = 12, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DonationStatus status = DonationStatus.PENDING;

    /** 捐赠凭证（收据号/转账单号） */
    @Column(length = 100)
    private String voucherNo;

    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
