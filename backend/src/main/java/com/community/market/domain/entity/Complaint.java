package com.community.market.domain.entity;

import com.community.market.domain.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 投诉记录，归集到同一活动档案 */
@Data
@Entity
@Table(name = "complaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    /** 被投诉摊位的报名 ID（可为空，表示对活动整体） */
    private Long applicationId;

    @Column(length = 50)
    private String reporterName;

    @Column(length = 20)
    private String reporterPhone;

    /** 投诉类型：噪音/卫生/卖品不符/价格/其他 */
    @Column(length = 30)
    private String complaintType;

    @Column(length = 500, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    private Long handlerId;

    @Column(length = 500)
    private String handleResult;

    private LocalDateTime handledAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
