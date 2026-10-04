package com.community.market.domain.entity;

import com.community.market.domain.enums.IncidentStatus;
import com.community.market.domain.enums.IncidentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 现场异常档案：资质不完整、卖品不符、同类冲突、用电超载、
 * 提前离场、捐赠流水不符、公益组织要求公示明细等。
 */
@Data
@Entity
@Table(name = "incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentType type;

    @Column(nullable = false, length = 500)
    private String description;

    /** 严重程度：1 提示 2 警告 3 严重 */
    private Integer severity = 2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.OPEN;

    private Long raisedBy;

    @Column(length = 500)
    private String resolution;

    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
