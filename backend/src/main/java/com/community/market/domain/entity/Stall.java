package com.community.market.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 摊位主数据：活动场地上的物理摊位。
 * x/y/row 用于平面图；trafficLevel 人流(1低~3高)；noiseSensitive 是否噪音敏感区。
 */
@Data
@Entity
@Table(name = "stall",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "code"}))
public class Stall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** 摊位编号，如 A-01 */
    @Column(nullable = false, length = 20)
    private String code;

    /** 区域：入口区/中心区/边角区/食品区 */
    @Column(length = 30)
    private String zone;

    private Integer rowNo;
    private Integer colNo;

    /** 摊位大小：1小 2中 3大 */
    private Integer sizeLevel = 2;

    /** 人流等级：1低 2中 3高 */
    private Integer trafficLevel = 2;

    /** 是否可供电 */
    private boolean powerAvailable;

    /** 可承载功率（瓦） */
    private Integer powerCapacity = 0;

    /** 是否噪音敏感（邻近休息区/居民区，不适合音响叫卖） */
    private boolean noiseSensitive;

    /** 状态：FREE 空闲 / ASSIGNED 已分配 */
    @Column(length = 20)
    private String status = "FREE";

    /** 当前分配的报名 ID */
    private Long assignedApplicationId;
}
