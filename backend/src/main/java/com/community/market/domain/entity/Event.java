package com.community.market.domain.entity;

import com.community.market.domain.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate eventDate;

    @Column(length = 200)
    private String location;

    /** 公益目标说明，如：为本社区困难家庭助学筹款 */
    @Column(length = 500)
    private String publicGoal;

    /** 公益筹款目标金额 */
    @Column(precision = 12, scale = 2)
    private BigDecimal goalAmount = BigDecimal.ZERO;

    /** 现场客流（工作人员统计） */
    private Integer visitorCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.RECRUITING;

    private LocalDateTime createdAt = LocalDateTime.now();
}
