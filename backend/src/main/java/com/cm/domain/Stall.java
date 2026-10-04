package com.cm.domain;

import jakarta.persistence.*;

/**
 * 摊位（场地中的物理位置）。
 * zone：A 高人流主通道 / B 中人流 / C 低人流边角 / K 亲子专区 / F 食品专区
 * powerCircuit：供电回路编号，同一回路摊位功率之和不得超过容量
 * noiseSuitable：是否适合有噪音/油烟的经营内容
 */
@Entity
@Table(name = "stall", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "code"}))
public class Stall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** 摊位编号，如 A-01 */
    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 10)
    private String zone;

    /** 摊位面积(平方米) */
    @Column(nullable = false)
    private Double sizeSqm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TrafficLevel trafficLevel = TrafficLevel.MEDIUM;

    @Column(nullable = false)
    private Boolean powerAvailable = false;

    /** 该摊位在所属回路上可取用的最大功率(W) */
    @Column(nullable = false)
    private Integer maxPowerWatt = 0;

    @Column(nullable = false)
    private String powerCircuit = "N";

    @Column(nullable = false)
    private Boolean noiseSuitable = false;

    /** 是否亲子摊位（靠近亲子专区） */
    @Column(nullable = false)
    private Boolean familySpot = false;

    /** 供摆放示意的坐标（百分比 0~100） */
    private Double posX;
    private Double posY;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Double getSizeSqm() { return sizeSqm; }
    public void setSizeSqm(Double sizeSqm) { this.sizeSqm = sizeSqm; }
    public TrafficLevel getTrafficLevel() { return trafficLevel; }
    public void setTrafficLevel(TrafficLevel trafficLevel) { this.trafficLevel = trafficLevel; }
    public Boolean getPowerAvailable() { return powerAvailable; }
    public void setPowerAvailable(Boolean powerAvailable) { this.powerAvailable = powerAvailable; }
    public Integer getMaxPowerWatt() { return maxPowerWatt; }
    public void setMaxPowerWatt(Integer maxPowerWatt) { this.maxPowerWatt = maxPowerWatt; }
    public String getPowerCircuit() { return powerCircuit; }
    public void setPowerCircuit(String powerCircuit) { this.powerCircuit = powerCircuit; }
    public Boolean getNoiseSuitable() { return noiseSuitable; }
    public void setNoiseSuitable(Boolean noiseSuitable) { this.noiseSuitable = noiseSuitable; }
    public Boolean getFamilySpot() { return familySpot; }
    public void setFamilySpot(Boolean familySpot) { this.familySpot = familySpot; }
    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }
    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }
}
