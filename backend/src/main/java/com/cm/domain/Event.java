package com.cm.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 一场社区公益集市活动：公益目标与现场秩序在此一并管理 */
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String location;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EventStatus status = EventStatus.PLANNED;

    /** 公益目标金额（元） */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal donationGoal = BigDecimal.ZERO;

    /** 捐赠用途说明（活动结束后向居民公示） */
    @Column(length = 500)
    private String donationPurpose;

    /** 全场总供电容量(W)，用于分配时的用电超载约束 */
    @Column(nullable = false)
    private Integer totalPowerWatt = 8000;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public BigDecimal getDonationGoal() { return donationGoal; }
    public void setDonationGoal(BigDecimal donationGoal) { this.donationGoal = donationGoal; }
    public String getDonationPurpose() { return donationPurpose; }
    public void setDonationPurpose(String donationPurpose) { this.donationPurpose = donationPurpose; }
    public Integer getTotalPowerWatt() { return totalPowerWatt; }
    public void setTotalPowerWatt(Integer totalPowerWatt) { this.totalPowerWatt = totalPowerWatt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
