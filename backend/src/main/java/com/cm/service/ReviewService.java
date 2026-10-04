package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** 复盘统计、统一活动档案、居民公示发布 */
@Service
public class ReviewService {

    private final EventRepository events;
    private final VendorApplicationRepository applications;
    private final StallAssignmentRepository assignments;
    private final StallRepository stalls;
    private final UserRepository users;
    private final SalesRecordRepository sales;
    private final DonationRecordRepository donations;
    private final ComplaintRepository complaints;
    private final EventIssueRepository issues;
    private final InspectionRepository inspections;
    private final GoodsChangeRepository goodsChanges;
    private final TrafficRepositoryAdapter trafficAdapter;
    private final PublicReportRepository reports;

    public ReviewService(EventRepository events, VendorApplicationRepository applications,
                         StallAssignmentRepository assignments, StallRepository stalls,
                         UserRepository users, SalesRecordRepository sales,
                         DonationRecordRepository donations, ComplaintRepository complaints,
                         EventIssueRepository issues, InspectionRepository inspections,
                         GoodsChangeRepository goodsChanges,
                         TrafficRecordRepository traffic, PublicReportRepository reports) {
        this.events = events;
        this.applications = applications;
        this.assignments = assignments;
        this.stalls = stalls;
        this.users = users;
        this.sales = sales;
        this.donations = donations;
        this.complaints = complaints;
        this.issues = issues;
        this.inspections = inspections;
        this.goodsChanges = goodsChanges;
        this.trafficAdapter = new TrafficRepositoryAdapter(traffic);
        this.reports = reports;
    }

    /** 统一活动档案：报名、审核、摊位、收款、捐赠、投诉、异常、公示全部归档在一起 */
    public Map<String, Object> archive(Long eventId) {
        Event e = must(eventId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", com.cm.dto.Views.event(e));

        List<Map<String, Object>> apps = new ArrayList<>();
        Map<Long, User> vendorCache = new HashMap<>();
        Map<Long, StallAssignment> asByApp = new HashMap<>();
        assignments.findByEventId(eventId).forEach(a -> asByApp.put(a.getApplicationId(), a));
        Map<Long, Stall> stallMap = new HashMap<>();
        stalls.findByEventIdOrderByCode(eventId).forEach(s -> stallMap.put(s.getId(), s));

        for (VendorApplication a : applications.findByEventIdOrderByCreatedAtDesc(eventId)) {
            User v = vendorCache.computeIfAbsent(a.getVendorId(),
                    id -> users.findById(id).orElse(null));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("application", com.cm.dto.Views.application(a));
            row.put("vendor", v == null ? null : com.cm.dto.Views.user(v));
            StallAssignment as = asByApp.get(a.getId());
            if (as != null) {
                row.put("assignment", com.cm.dto.Views.assignment(as));
                Stall st = stallMap.get(as.getStallId());
                row.put("stall", st == null ? null : com.cm.dto.Views.stall(st));
                row.put("sales", sales.findByAssignmentIdOrderByCreatedAtDesc(as.getId()).stream()
                        .map(com.cm.dto.Views::sales).toList());
                row.put("donation", donations.findByAssignmentId(as.getId())
                        .map(com.cm.dto.Views::donation).orElse(null));
                row.put("inspections", inspections.findByAssignmentIdOrderByCreatedAtDesc(as.getId()).stream()
                        .map(com.cm.dto.Views::inspection).toList());
                row.put("goodsChanges", goodsChanges.findByAssignmentIdOrderByCreatedAtDesc(as.getId()).stream()
                        .map(com.cm.dto.Views::goodsChange).toList());
                row.put("issues", issues.findByAssignmentId(as.getId()).stream()
                        .map(com.cm.dto.Views::issue).toList());
            }
            apps.add(row);
        }
        m.put("records", apps);
        m.put("complaints", complaints.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(com.cm.dto.Views::complaint).toList());
        m.put("issues", issues.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(com.cm.dto.Views::issue).toList());
        m.put("traffic", trafficAdapter.list(eventId).stream()
                .map(com.cm.dto.Views::traffic).toList());
        reports.findByEventId(eventId).ifPresent(r -> m.put("publicReport", com.cm.dto.Views.report(r)));
        m.put("dashboard", dashboard(eventId));
        return m;
    }

    /** 复盘仪表盘：公益金额、客流、投诉、摊主履约、招商建议 */
    public Map<String, Object> dashboard(Long eventId) {
        must(eventId);
        Map<String, Object> m = new LinkedHashMap<>();

        List<VendorApplication> apps = applications.findByEventIdOrderByCreatedAtDesc(eventId);
        List<StallAssignment> ass = assignments.findByEventId(eventId);
        List<DonationRecord> ds = donations.findByEventId(eventId);
        List<Complaint> cs = complaints.findByEventIdOrderByCreatedAtDesc(eventId);
        List<EventIssue> is = issues.findByEventIdOrderByCreatedAtDesc(eventId);
        List<TrafficRecord> ts = trafficAdapter.list(eventId);
        List<SalesRecord> ss = sales.findByEventIdOrderByCreatedAtDesc(eventId);

        BigDecimal salesTotal = ss.stream().map(SalesRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal donatedExpected = ds.stream().map(DonationRecord::getExpectedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal donatedActual = ds.stream()
                .filter(d -> d.getStatus() == DonationStatus.CONFIRMED || d.getStatus() == DonationStatus.PUBLICIZED)
                .map(DonationRecord::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int visitors = ts.stream().mapToInt(TrafficRecord::getVisitorCount).sum();

        // 报名品类分布
        Map<String, Integer> categoryDist = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            categoryDist.put(c.getLabel(), (int) apps.stream().filter(a -> a.getCategory() == c).count());
        }
        // 审核分布
        Map<String, Integer> reviewDist = new LinkedHashMap<>();
        for (ApplicationStatus st : ApplicationStatus.values()) {
            reviewDist.put(st.getLabel(), (int) apps.stream().filter(a -> a.getStatus() == st).count());
        }
        // 异常类型分布
        Map<String, Integer> issueDist = new LinkedHashMap<>();
        for (IssueType t : IssueType.values()) {
            int n = (int) is.stream().filter(x -> x.getType() == t).count();
            if (n > 0) issueDist.put(t.getLabel(), n);
        }
        // 每回路用电负载
        Map<String, Integer> powerLoad = new LinkedHashMap<>();
        Map<Long, Stall> stallMap = new HashMap<>();
        stalls.findByEventIdOrderByCode(eventId).forEach(s -> stallMap.put(s.getId(), s));
        for (StallAssignment a : ass) {
            VendorApplication ap = applications.findById(a.getApplicationId()).orElse(null);
            Stall st = stallMap.get(a.getStallId());
            if (ap != null && st != null && ap.getPowerNeedWatt() != null && ap.getPowerNeedWatt() > 0) {
                powerLoad.merge(st.getPowerCircuit(), ap.getPowerNeedWatt(), Integer::sum);
            }
        }

        // 摊主履约明细与评分
        List<Map<String, Object>> performance = new ArrayList<>();
        int goodCount = 0;
        int issueVendorCount = 0;
        Set<Long> vendorsWithOpenIssues = new HashSet<>();
        for (StallAssignment a : ass) {
            VendorApplication ap = applications.findById(a.getApplicationId()).orElse(null);
            if (ap == null) continue;
            User v = users.findById(ap.getVendorId()).orElse(null);
            Stall st = stallMap.get(a.getStallId());
            DonationRecord d = donations.findByAssignmentId(a.getId()).orElse(null);
            List<EventIssue> mine = issues.findByAssignmentId(a.getId());
            long complaintsAboutMe = cs.stream().filter(c -> a.getId().equals(c.getAssignmentId())).count();
            boolean mismatchInspection = inspections.findByAssignmentIdOrderByCreatedAtDesc(a.getId()).stream()
                    .anyMatch(x -> x.getResult() == InspectionResult.MISMATCH);

            int fulfill = 100;
            if (!Boolean.TRUE.equals(a.getCheckedIn())) fulfill -= 40;
            if (Boolean.TRUE.equals(a.getEarlyLeave())) fulfill -= 20;
            fulfill -= mine.size() * 10;
            if (d != null && d.getStatus() == DonationStatus.MISMATCH) fulfill -= 15;
            if (mismatchInspection) fulfill -= 10;
            fulfill = Math.max(0, fulfill);

            boolean good = Boolean.TRUE.equals(a.getCheckedIn())
                    && !Boolean.TRUE.equals(a.getEarlyLeave())
                    && mine.isEmpty() && !mismatchInspection
                    && (d == null || d.getStatus() == DonationStatus.CONFIRMED
                        || d.getStatus() == DonationStatus.PUBLICIZED
                        || d.getExpectedAmount().signum() == 0);
            if (good) goodCount++;
            if (!mine.isEmpty() || mismatchInspection || complaintsAboutMe > 0) issueVendorCount++;
            mine.stream().filter(x -> x.getStatus() == IssueStatus.OPEN).findAny()
                    .ifPresent(x -> vendorsWithOpenIssues.add(ap.getVendorId()));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("assignmentId", a.getId());
            row.put("vendorName", v == null ? "?" : v.getName());
            row.put("vendorId", ap.getVendorId());
            row.put("stallName", ap.getStallName());
            row.put("category", ap.getCategory().getLabel());
            row.put("stallCode", st == null ? null : st.getCode());
            row.put("checkedIn", a.getCheckedIn());
            row.put("earlyLeave", a.getEarlyLeave());
            row.put("suppliesClaimed", a.getSuppliesClaimed());
            row.put("salesTotal", d == null ? BigDecimal.ZERO : d.getSalesTotal());
            row.put("expectedDonation", d == null ? BigDecimal.ZERO : d.getExpectedAmount());
            row.put("actualDonation", d == null ? BigDecimal.ZERO : d.getActualAmount());
            row.put("donationStatus", d == null ? null : d.getStatus());
            row.put("donationStatusLabel", d == null ? "未生成" : d.getStatus().getLabel());
            row.put("issueCount", mine.size());
            row.put("complaintCount", complaintsAboutMe);
            row.put("fulfillmentScore", fulfill);
            performance.add(row);
        }
        performance.sort((x, y) -> Integer.compare((int) y.get("fulfillmentScore"), (int) x.get("fulfillmentScore")));

        Event event = events.findById(eventId).orElseThrow();
        BigDecimal goal = event.getDonationGoal() == null ? BigDecimal.ZERO : event.getDonationGoal();
        double goalPercent = goal.signum() == 0 ? 0 :
                donatedActual.multiply(BigDecimal.valueOf(100)).divide(goal, 1, java.math.RoundingMode.HALF_UP).doubleValue();

        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("applicationCount", apps.size());
        stat.put("approvedCount", (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.APPROVED).count());
        stat.put("assignedCount", ass.size());
        stat.put("checkedInCount", ass.stream().filter(a -> Boolean.TRUE.equals(a.getCheckedIn())).count());
        stat.put("earlyLeaveCount", ass.stream().filter(a -> Boolean.TRUE.equals(a.getEarlyLeave())).count());
        stat.put("salesTotal", salesTotal);
        stat.put("donatedExpected", donatedExpected);
        stat.put("donatedActual", donatedActual);
        stat.put("donationGoal", goal);
        stat.put("goalPercent", goalPercent);
        stat.put("visitorCount", visitors);
        stat.put("complaintCount", cs.size());
        stat.put("complaintOpen", cs.stream().filter(c -> c.getStatus() != ComplaintStatus.RESOLVED).count());
        stat.put("issueCount", is.size());
        stat.put("issueOpen", is.stream().filter(x -> x.getStatus() == IssueStatus.OPEN).count());
        stat.put("goodPerformerCount", goodCount);
        stat.put("issueVendorCount", issueVendorCount);

        m.put("stat", stat);
        m.put("categoryDistribution", categoryDist);
        m.put("reviewDistribution", reviewDist);
        m.put("issueDistribution", issueDist);
        m.put("powerLoad", powerLoad);
        m.put("performance", performance);
        m.put("suggestions", suggestions(apps, stat, issueDist, vendorsWithOpenIssues, powerLoad));
        reports.findByEventId(eventId).ifPresent(r -> m.put("published", true));
        return m;
    }

    /** 下次招商建议：依据本次数据规则化生成 */
    private List<String> suggestions(List<VendorApplication> apps, Map<String, Object> stat,
                                     Map<String, Integer> issueDist, Set<Long> riskVendors,
                                     Map<String, Integer> powerLoad) {
        List<String> tips = new ArrayList<>();
        int food = (int) apps.stream().filter(a -> a.getCategory() == Category.FOOD).count();
        int handmade = (int) apps.stream().filter(a -> a.getCategory() == Category.HANDMADE).count();
        int secondhand = (int) apps.stream().filter(a -> a.getCategory() == Category.SECONDHAND).count();
        int charity = (int) apps.stream().filter(a -> a.getCategory() == Category.CHARITY).count();
        if (charity <= 1) tips.add("公益义卖摊位偏少，下次可定向邀请公益组织与残障手作团队，提升公益氛围与捐赠规模");
        if (food > 0 && issueDist.getOrDefault(IssueType.FOOD_QUALIFICATION.getLabel(), 0) > 0) {
            tips.add("本期出现食品资质问题，下期报名须强制上传食品经营许可证与健康照，审核通过后方可入食品专区");
        }
        if (issueDist.getOrDefault(IssueType.MISMATCH.getLabel(), 0) > 0) {
            tips.add("存在售卖与报名不符，下期加强开场巡检与临时换品审批，摊位号牌公示主营品类");
        }
        if (issueDist.getOrDefault(IssueType.EARLY_LEAVE.getLabel(), 0) > 0) {
            tips.add("存在提前离场，建议收取履约保证金，全程到场且无异常者下期优先高人流位置");
        }
        if (issueDist.getOrDefault(IssueType.DONATION_MISMATCH.getLabel(), 0) > 0) {
            tips.add("出现捐赠与流水不一致，下期统一使用社区聚合收款码，活动结束自动按比例分账并现场公示");
        }
        powerLoad.forEach((circuit, watt) -> {
            if (watt >= 1800) tips.add("回路 " + circuit + " 负载 " + watt + "W 接近容量，下期需增加供电回路或限制高功率设备报名");
        });
        if (secondhand < 2) tips.add("二手环保摊位不足，可联合物业发起居民旧物征集专区");
        if (handmade >= 3) tips.add("手作摊位反响稳定，下期可设置手作体验区，延长客流停留时间");
        if (!riskVendors.isEmpty()) tips.add("有 " + riskVendors.size() + " 名摊主存在未处理异常/食品风险，下期报名应列入观察名单并限制摊位位置");
        long openComplaints = (long) stat.get("complaintOpen");
        if (openComplaints > 0) tips.add("尚有投诉未办结，公示前须全部办结并向投诉人反馈");
        if (tips.isEmpty()) tips.add("本期秩序与公益目标完成良好，下期可扩大招募规模并复制现有分区与供电方案");
        return tips;
    }

    /** 发布居民公示 */
    @Transactional
    public PublicReport publish(Long eventId, String donationPurpose, String issueSummary,
                                String nextPlan, Long adminId) {
        Event e = must(eventId);
        Map<String, Object> dash = dashboard(eventId);
        Map<String, Object> stat = castMap(dash.get("stat"));

        PublicReport r = reports.findByEventId(eventId).orElseGet(PublicReport::new);
        r.setEventId(eventId);
        r.setTotalDonation((BigDecimal) stat.get("donatedActual"));
        r.setTotalVisitors((Integer) stat.get("visitorCount"));
        r.setGoodPerformers((Integer) stat.get("goodPerformerCount"));
        r.setIssueVendorCount((Integer) stat.get("issueVendorCount"));
        r.setDonationPurpose(donationPurpose != null && !donationPurpose.isBlank() ? donationPurpose : e.getDonationPurpose());
        r.setIssueSummary(issueSummary);
        r.setNextPlan(nextPlan);
        r.setPublishedBy(adminId);
        r.setPublishedAt(LocalDateTime.now());
        reports.save(r);

        // 捐赠记录标记为已公示，活动状态置为已公示
        donations.findByEventId(eventId).forEach(d -> {
            if (d.getStatus() == DonationStatus.CONFIRMED) d.setStatus(DonationStatus.PUBLICIZED);
            donations.save(d);
        });
        e.setStatus(EventStatus.PUBLISHED);
        events.save(e);
        return r;
    }

    /** 居民公开页数据（仅已公示活动可见），脱敏后展示履约与捐赠 */
    public Map<String, Object> publicView(Long eventId) {
        Event e = must(eventId);
        PublicReport r = reports.findByEventId(eventId)
                .orElseThrow(() -> new ApiException(404, "该活动尚未发布公示"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", com.cm.dto.Views.event(e));
        m.put("report", com.cm.dto.Views.report(r));

        List<Map<String, Object>> vendors = new ArrayList<>();
        Map<Long, Stall> stallMap = new HashMap<>();
        stalls.findByEventIdOrderByCode(eventId).forEach(s -> stallMap.put(s.getId(), s));
        for (StallAssignment a : assignments.findByEventId(eventId)) {
            VendorApplication ap = applications.findById(a.getApplicationId()).orElse(null);
            if (ap == null) continue;
            DonationRecord d = donations.findByAssignmentId(a.getId()).orElse(null);
            Stall st = stallMap.get(a.getStallId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("stallName", ap.getStallName());
            row.put("category", ap.getCategory().getLabel());
            row.put("stallCode", st == null ? null : st.getCode());
            row.put("vendorName", users.findById(ap.getVendorId()).map(User::getName).orElse("?"));
            row.put("actualDonation", d == null ? BigDecimal.ZERO : d.getActualAmount());
            boolean good = Boolean.TRUE.equals(a.getCheckedIn()) && !Boolean.TRUE.equals(a.getEarlyLeave())
                    && issues.findByAssignmentId(a.getId()).isEmpty();
            row.put("goodFaith", good ? "履约良好" : "存在异常已处理");
            vendors.add(row);
        }
        vendors.sort((x, y) -> ((BigDecimal) y.get("actualDonation")).compareTo((BigDecimal) x.get("actualDonation")));
        m.put("vendors", vendors);

        List<Map<String, Object>> issueRows = new ArrayList<>();
        for (EventIssue i : issues.findByEventIdOrderByCreatedAtDesc(eventId)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type", i.getType().getLabel());
            row.put("status", i.getStatus().getLabel());
            row.put("resolution", i.getResolution());
            issueRows.add(row);
        }
        m.put("issues", issueRows);
        return m;
    }

    /** 已公示活动列表（居民页入口） */
    public List<Map<String, Object>> publicEvents() {
        return events.findAllByOrderByStartTimeDesc().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .map(e -> {
                    Map<String, Object> m = com.cm.dto.Views.event(e);
                    reports.findByEventId(e.getId()).ifPresent(r -> m.put("totalDonation", r.getTotalDonation()));
                    return m;
                }).toList();
    }

    private Event must(Long id) {
        return events.findById(id).orElseThrow(() -> new ApiException(404, "活动不存在"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object o) {
        return (Map<String, Object>) o;
    }

    /** 小适配器，避免构造器参数顺序出错 */
    private static class TrafficRepositoryAdapter {
        private final TrafficRecordRepository repo;
        TrafficRepositoryAdapter(TrafficRecordRepository repo) { this.repo = repo; }
        List<TrafficRecord> list(Long eventId) { return repo.findByEventIdOrderByCreatedAtAsc(eventId); }
    }
}
