package com.cm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 客流计数记录：工作人员按时段/入口上报，复盘时汇总客流走势与总量 */
@Entity
@Table(name = "traffic_record")
public class TrafficRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;
    /** 时段标签，如 10:00-11:00 */
    @Column(length = 20)
    private String period;

    @Column(nullable = false)
    private Integer visitorCount = 0;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(length = 200)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Integer getVisitorCount() { return visitorCount; }
    public void setVisitorCount(Integer visitorCount) { this.visitorCount = visitorCount; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
