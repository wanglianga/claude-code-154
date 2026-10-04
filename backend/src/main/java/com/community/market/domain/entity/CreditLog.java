package com.community.market.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 摊主信用变动流水：历史捐赠、投诉、食品风险、履约情况都会影响信用与后续摊位位置 */
@Data
@Entity
@Table(name = "credit_log")
public class CreditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vendorId;

    private Long eventId;

    private Long applicationId;

    /** 变动分值，正负均可 */
    private Integer delta;

    /** 变动后分值 */
    private Integer scoreAfter;

    /** 原因 */
    @Column(length = 200)
    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();
}
