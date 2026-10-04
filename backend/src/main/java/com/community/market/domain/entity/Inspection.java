package com.community.market.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 巡检记录：工作人员现场检查实际售卖内容、临时换品 */
@Data
@Entity
@Table(name = "inspection")
public class Inspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long applicationId;

    private Long staffId;

    /** 实际售卖内容 */
    @Column(length = 500)
    private String actualGoods;

    /** 是否与报名相符 */
    private boolean matched;

    /** 是否发生临时换品 */
    private boolean goodsChanged;

    /** 换品说明 */
    @Column(length = 500)
    private String changeDescription;

    /** 用电实测（瓦），用于判断超载 */
    private Integer powerUsedWatt;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt = LocalDateTime.now();
}
