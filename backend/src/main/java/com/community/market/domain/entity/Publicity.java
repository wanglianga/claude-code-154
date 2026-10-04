package com.community.market.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动公示：活动结束后对居民公开的总捐赠、用途、履约与异常处理。
 */
@Data
@Entity
@Table(name = "publicity")
public class Publicity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long eventId;

    /** 公示标题 */
    @Column(length = 200)
    private String title;

    /** 总捐赠金额（汇总快照） */
    @Column(precision = 12, scale = 2)
    private BigDecimal totalDonation = BigDecimal.ZERO;

    /** 总营业额（快照） */
    @Column(precision = 12, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    /** 善款用途说明 */
    @Column(length = 1000)
    private String fundUsage;

    /** 摊主履约情况综述（到场率、提前离场等） */
    @Column(length = 1000)
    private String fulfillmentSummary;

    /** 异常处理情况综述 */
    @Column(length = 1000)
    private String incidentSummary;

    /** 公益组织要求公示的明细（JSON 文本，逐摊位捐赠明细） */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String donationDetailJson;

    private boolean published;

    private Long createdBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
