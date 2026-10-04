package com.community.market.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款流水：现场工作人员/摊主上报的营业额流水，
 * 是核对捐赠金额（承诺比例 × 营业额）的依据。
 */
@Data
@Entity
@Table(name = "payment_record")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long applicationId;

    /** 收款方式 */
    @Column(length = 20)
    private String paymentMethod;

    /** 本笔/本班营业额 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /** 流水凭证号（支付平台单号/现金交接单号） */
    @Column(length = 100)
    private String receiptNo;

    private Long recorderId;

    @Column(length = 200)
    private String remark;

    private LocalDateTime recordedAt = LocalDateTime.now();
}
