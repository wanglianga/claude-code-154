package com.cm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    /** 摊主信用分，初始 100，异常扣分、良好履约加分 */
    @Column(nullable = false)
    private Integer creditScore = 100;

    /** 历史累计捐赠金额（分），跨活动累计，影响摊位位置 */
    @Column(nullable = false)
    private Long totalDonationCents = 0L;

    /** 是否存在食品风险记录（资质不全等），影响后续活动摊位位置 */
    @Column(nullable = false)
    private Boolean foodRiskFlag = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Long getTotalDonationCents() { return totalDonationCents; }
    public void setTotalDonationCents(Long totalDonationCents) { this.totalDonationCents = totalDonationCents; }
    public Boolean getFoodRiskFlag() { return foodRiskFlag; }
    public void setFoodRiskFlag(Boolean foodRiskFlag) { this.foodRiskFlag = foodRiskFlag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
