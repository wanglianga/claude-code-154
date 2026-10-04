package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 初始化测试账号、一场示例活动、摊位布局与若干待审核报名 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final EventRepository events;
    private final StallRepository stalls;
    private final VendorApplicationRepository applications;
    private final ApplicationService applicationService;

    public DataSeeder(UserRepository users, EventRepository events,
                      StallRepository stalls, VendorApplicationRepository applications,
                      ApplicationService applicationService) {
        this.users = users;
        this.events = events;
        this.stalls = stalls;
        this.applications = applications;
        this.applicationService = applicationService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        User admin = user("admin", "admin123", "陈主任", "13800000001", UserRole.ADMIN, 100, 0L, false);
        User staff = user("staff", "staff123", "周工作员", "13800000002", UserRole.STAFF, 100, 0L, false);
        User v1 = user("vendor1", "vendor123", "张阿姨", "13900000001", UserRole.VENDOR, 100, 52000L, false);
        User v2 = user("vendor2", "vendor123", "李哥", "13900000002", UserRole.VENDOR, 92, 18000L, false);
        User v3 = user("vendor3", "vendor123", "王姐", "13900000003", UserRole.VENDOR, 85, 0L, false);
        User v4 = user("vendor4", "vendor123", "星公益组织", "13900000004", UserRole.VENDOR, 100, 268000L, false);
        User v5 = user("vendor5", "vendor123", "赵小贩", "13900000005", UserRole.VENDOR, 62, 0L, true);
        users.saveAll(List.of(admin, staff, v1, v2, v3, v4, v5));

        Event event = new Event();
        event.setName("2026 金秋社区公益集市");
        event.setDescription("手作、二手、美食与公益义卖齐聚，公益捐赠用于社区困难家庭帮扶。");
        event.setLocation("幸福社区中心广场");
        event.setStartTime(LocalDateTime.of(2026, 10, 17, 9, 0));
        event.setEndTime(LocalDateTime.of(2026, 10, 17, 17, 0));
        event.setStatus(EventStatus.PLANNED);
        event.setDonationGoal(new BigDecimal("8000.00"));
        event.setDonationPurpose("社区困难家庭生活物资与老年食堂加餐");
        event.setTotalPowerWatt(9000);
        events.save(event);
        Long e = event.getId();

        // A 区：高人流主通道；部分带供电
        stall(e, "A-01", "A", TrafficLevel.HIGH, 6, 8, 12, 0, "N", false, false);
        stall(e, "A-02", "A", TrafficLevel.HIGH, 6, 24, 12, 800, "PA", false, false);
        stall(e, "A-03", "A", TrafficLevel.HIGH, 6, 40, 12, 800, "PA", false, false);
        stall(e, "A-04", "A", TrafficLevel.HIGH, 6, 56, 12, 0, "N", false, false);
        stall(e, "A-05", "A", TrafficLevel.HIGH, 6, 72, 12, 0, "N", false, false);
        // B 区：中人流
        stall(e, "B-01", "B", TrafficLevel.MEDIUM, 6, 8, 45, 1000, "PB", false, false);
        stall(e, "B-02", "B", TrafficLevel.MEDIUM, 6, 24, 45, 0, "N", false, false);
        stall(e, "B-03", "B", TrafficLevel.MEDIUM, 6, 40, 45, 1000, "PB", false, false);
        stall(e, "B-04", "B", TrafficLevel.MEDIUM, 6, 56, 45, 0, "N", false, false);
        stall(e, "B-05", "B", TrafficLevel.MEDIUM, 6, 72, 45, 0, "N", false, false);
        // C 区：低人流边角，面积更大
        stall(e, "C-01", "C", TrafficLevel.LOW, 9, 8, 78, 0, "N", false, false);
        stall(e, "C-02", "C", TrafficLevel.LOW, 9, 28, 78, 0, "N", false, false);
        stall(e, "C-03", "C", TrafficLevel.LOW, 9, 48, 78, 1500, "PC", false, false);
        // F 区：食品专区（带供电、容忍油烟噪音）
        stall(e, "F-01", "F", TrafficLevel.MEDIUM, 9, 90, 40, 1500, "PF", true, false);
        stall(e, "F-02", "F", TrafficLevel.MEDIUM, 9, 90, 62, 1500, "PF", true, false);
        stall(e, "F-03", "F", TrafficLevel.MEDIUM, 9, 90, 84, 1500, "PF", true, false);
        // K 区：亲子专区
        stall(e, "K-01", "K", TrafficLevel.HIGH, 6, 72, 45, 0, "N", false, true);
        stall(e, "K-02", "K", TrafficLevel.HIGH, 6, 88, 18, 0, "N", false, true);

        // 预置待审核报名，便于直接演示审核与分配（提交时附规则校验提示）
        seedApp(app(e, v1.getId(), "暖手心作铺", Category.HANDMADE,
                "手工编织围巾、发卡、布艺挂件", false, null, null,
                new BigDecimal("10.00"), 0, 1, 2, true, false, "微信", "微信@暖心"));
        seedApp(app(e, v2.getId(), "时光旧物仓", Category.SECONDHAND,
                "旧书、复古玩具、小家电", false, null, null,
                new BigDecimal("5.00"), 500, 1, 2, false, false, "支付宝", "139****0002"));
        seedApp(app(e, v3.getId(), "王姐卤味", Category.FOOD,
                "现卤豆干、鸡爪、凉茶（现场加热）", false, null, null,
                new BigDecimal("8.00"), 1200, 2, 2, false, true, "聚合码", null));
        seedApp(app(e, v4.getId(), "星公益义卖角", Category.CHARITY,
                "残疾人手工艺品、公益文创，所得全额捐赠", true, null, null,
                new BigDecimal("100.00"), 0, 2, 4, false, false, "现金+微信", null));
        seedApp(app(e, v5.getId(), "赵记烤冷面", Category.FOOD,
                "烤冷面、铁板鱿鱼（大功率电炉）", false, null, null,
                new BigDecimal("0.00"), 1800, 1, 2, false, true, "现金", null));
    }

    /** 保存报名并附带活动规则自动校验提示 */
    private void seedApp(VendorApplication a) {
        a.setRuleHints(String.join("；", applicationService.checkRules(a)));
        applications.save(a);
    }

    private User user(String username, String pwd, String name, String phone,
                      UserRole role, int credit, long totalDonationCents, boolean foodRisk) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(pwd);
        u.setName(name);
        u.setPhone(phone);
        u.setRole(role);
        u.setCreditScore(credit);
        u.setTotalDonationCents(totalDonationCents);
        u.setFoodRiskFlag(foodRisk);
        return u;
    }

    private void stall(long eventId, String code, String zone, TrafficLevel traffic, double size,
                       double x, double y, int power, String circuit, boolean noise, boolean family) {
        Stall s = new Stall();
        s.setEventId(eventId);
        s.setCode(code);
        s.setZone(zone);
        s.setTrafficLevel(traffic);
        s.setSizeSqm(size);
        s.setPosX(x);
        s.setPosY(y);
        s.setPowerAvailable(power > 0);
        s.setMaxPowerWatt(power);
        s.setPowerCircuit(circuit);
        s.setNoiseSuitable(noise);
        s.setFamilySpot(family);
        stalls.save(s);
    }

    private VendorApplication app(long eventId, long vendorId, String stallName, Category category,
                                  String goods, boolean foodQualified, String certNo, String certAttach,
                                  BigDecimal percent, int power, int tables, int chairs,
                                  boolean family, boolean noisy, String payMethod, String payAccount) {
        VendorApplication a = new VendorApplication();
        a.setEventId(eventId);
        a.setVendorId(vendorId);
        a.setStallName(stallName);
        a.setCategory(category);
        a.setGoodsDescription(goods);
        a.setFoodQualified(foodQualified);
        a.setFoodCertNo(certNo);
        a.setFoodCertAttachment(certAttach);
        a.setDonationPercent(percent);
        a.setPowerNeedWatt(power);
        a.setTableNeed(tables);
        a.setChairNeed(chairs);
        a.setFamilyStall(family);
        a.setNoisy(noisy);
        a.setPaymentMethod(payMethod);
        a.setPaymentAccount(payAccount);
        return a;
    }
}
