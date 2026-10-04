package com.cm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;

import java.security.SecureRandom;
import java.util.*;

/**
 * 摊位自动分配。
 * 综合评分（影响优先级）：摊主信用分、历史累计捐赠、本次承诺捐赠比例、公益属性加分、食品风险扣分。
 * 硬约束：用电（单摊功率 + 同回路总功率容量）、食品/噪音入专区、摊位面积、摊位未占用。
 * 软约束：高人流优先给高评分与公益摊位、亲子摊位入亲子区、同类品类不相邻（无法满足时给出冲突提示）。
 */
@Service
public class StallAssignmentService {

    private final StallAssignmentRepository assignments;
    private final StallRepository stalls;
    private final VendorApplicationRepository applications;
    private final UserRepository users;
    private final EventRepository events;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.power-circuit-capacity:2000}")
    private int circuitCapacity;

    public StallAssignmentService(StallAssignmentRepository assignments, StallRepository stalls,
                                  VendorApplicationRepository applications, UserRepository users,
                                  EventRepository events) {
        this.assignments = assignments;
        this.stalls = stalls;
        this.applications = applications;
        this.users = users;
        this.events = events;
    }

    /** 单条报名的综合评分 */
    public int scoreOf(User vendor, VendorApplication a) {
        int score = 0;
        score += vendor.getCreditScore() != null ? vendor.getCreditScore() : 80;        // 信用 0~100
        score += Math.min(40, vendor.getTotalDonationCents() / 5000);                   // 历史捐赠每 50 元 +1，封顶 40
        score += a.getDonationPercent().intValue() / 2;                                 // 承诺比例每 2% +1
        if (a.getCategory() == Category.CHARITY) score += 20;                           // 公益属性加权
        if (Boolean.TRUE.equals(vendor.getFoodRiskFlag())) score -= 30;                 // 食品风险降权
        return score;
    }

    @Transactional
    public Map<String, Object> autoAssign(Long eventId) {
        events.findById(eventId).orElseThrow(() -> new ApiException(404, "活动不存在"));

        List<VendorApplication> approved = applications.findByEventIdAndStatus(eventId, ApplicationStatus.APPROVED);
        List<StallAssignment> existing = assignments.findByEventId(eventId);
        Set<Long> assignedAppIds = new HashSet<>(existing.stream().map(StallAssignment::getApplicationId).toList());
        Set<Long> occupiedStalls = new HashSet<>(existing.stream().map(StallAssignment::getStallId).toList());

        // 已通过但尚未分配的报名，按综合评分从高到低安排
        List<VendorApplication> pending = approved.stream()
                .filter(a -> !assignedAppIds.contains(a.getId()))
                .sorted(Comparator.comparingInt((VendorApplication a) ->
                        scoreOf(users.findById(a.getVendorId()).orElseThrow(), a)).reversed())
                .toList();

        List<Stall> allStalls = stalls.findByEventIdOrderByCode(eventId);
        Map<String, Integer> circuitUsed = buildCircuitLoad(existing, allStalls);
        Map<Category, List<Point>> categoryPoints = new EnumMap<>(Category.class);
        existing.forEach(as -> {
            Stall st = stalls.findById(as.getStallId()).orElse(null);
            VendorApplication ap = applications.findById(as.getApplicationId()).orElse(null);
            if (st != null && ap != null) {
                categoryPoints.computeIfAbsent(ap.getCategory(), k -> new ArrayList<>())
                        .add(new Point(st.getPosX(), st.getPosY()));
            }
        });

        List<Map<String, Object>> assigned = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        for (VendorApplication a : pending) {
            User vendor = users.findById(a.getVendorId()).orElseThrow();
            List<Stall> free = allStalls.stream().filter(s -> !occupiedStalls.contains(s.getId())).toList();

            Stall best = null;
            int bestFit = Integer.MIN_VALUE;
            List<String> blockers = new ArrayList<>();
            for (Stall s : free) {
                int hard = hardConstraint(s, a, circuitUsed);
                if (hard < 0) {
                    blockers.add(s.getCode() + ":" + hardReason(hard));
                    continue;
                }
                int fit = fitScore(s, a, scoreOf(vendor, a), categoryPoints);
                if (fit > bestFit) {
                    bestFit = fit;
                    best = s;
                }
            }

            if (best == null) {
                Map<String, Object> f = new LinkedHashMap<>();
                f.put("applicationId", a.getId());
                f.put("stallName", a.getStallName());
                f.put("vendorName", vendor.getName());
                f.put("reason", "无满足硬约束的空闲摊位（" + String.join("；", dedup(blockers)) + "），请人工调整布局或扩容供电");
                failed.add(f);
                continue;
            }

            boolean conflict = adjacentConflict(best, a.getCategory(), categoryPoints);
            StallAssignment as = new StallAssignment();
            as.setEventId(eventId);
            as.setApplicationId(a.getId());
            as.setStallId(best.getId());
            as.setScore(scoreOf(vendor, a));
            as.setAutoAssigned(true);
            as.setCheckinCode(newCode());
            as.setReason(buildReason(best, a, vendor, conflict));
            assignments.save(as);

            occupiedStalls.add(best.getId());
            if (a.getPowerNeedWatt() != null && a.getPowerNeedWatt() > 0) {
                circuitUsed.merge(best.getPowerCircuit(), a.getPowerNeedWatt(), Integer::sum);
            }
            categoryPoints.computeIfAbsent(a.getCategory(), k -> new ArrayList<>())
                    .add(new Point(best.getPosX(), best.getPosY()));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("applicationId", a.getId());
            row.put("stallName", a.getStallName());
            row.put("vendorName", vendor.getName());
            row.put("stallCode", best.getCode());
            row.put("score", as.getScore());
            row.put("conflictWarning", conflict);
            row.put("reason", as.getReason());
            assigned.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assigned", assigned);
        result.put("failed", failed);
        result.put("circuitLoad", circuitUsed);
        return result;
    }

    /** 人工调整摊位（管理员拖拽换摊） */
    @Transactional
    public StallAssignment reassign(Long assignmentId, Long targetStallId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        Stall target = stalls.findById(targetStallId)
                .orElseThrow(() -> new ApiException(404, "目标摊位不存在"));
        if (!target.getEventId().equals(as.getEventId())) {
            throw new ApiException("不能跨活动调换摊位");
        }
        assignments.findByStallId(targetStallId).ifPresent(other -> {
            if (!other.getId().equals(as.getId())) {
                throw new ApiException("目标摊位已被占用");
            }
        });
        VendorApplication a = applications.findById(as.getApplicationId())
                .orElseThrow(() -> new ApiException(404, "报名不存在"));
        List<StallAssignment> others = assignments.findByEventId(as.getEventId()).stream()
                .filter(x -> !x.getId().equals(as.getId())).toList();
        Map<String, Integer> circuitUsed = buildCircuitLoad(others, stalls.findByEventIdOrderByCode(as.getEventId()));
        int hard = hardConstraint(target, a, circuitUsed);
        if (hard < 0) {
            throw new ApiException("调换不满足约束：" + hardReason(hard));
        }
        as.setStallId(targetStallId);
        as.setAutoAssigned(false);
        User vendor = users.findById(a.getVendorId()).orElseThrow();
        Map<Category, List<Point>> points = new EnumMap<>(Category.class);
        others.forEach(o -> {
            Stall st = stalls.findById(o.getStallId()).orElse(null);
            VendorApplication ap = applications.findById(o.getApplicationId()).orElse(null);
            if (st != null && ap != null) {
                points.computeIfAbsent(ap.getCategory(), k -> new ArrayList<>()).add(new Point(st.getPosX(), st.getPosY()));
            }
        });
        boolean conflict = adjacentConflict(target, a.getCategory(), points);
        as.setReason(buildReason(target, a, vendor, conflict) + "（人工调整）");
        return assignments.save(as);
    }

    // ---------------- 约束与打分 ----------------

    /** 返回负数表示违反硬约束（错误码），>=0 表示通过 */
    private int hardConstraint(Stall s, VendorApplication a, Map<String, Integer> circuitUsed) {
        int need = a.getPowerNeedWatt() == null ? 0 : a.getPowerNeedWatt();
        if (need > 0) {
            if (!Boolean.TRUE.equals(s.getPowerAvailable())) return -1;
            if (need > s.getMaxPowerWatt()) return -2;
            int used = circuitUsed.getOrDefault(s.getPowerCircuit(), 0);
            if (used + need > circuitCapacity) return -3;
        }
        if (a.getCategory() == Category.FOOD && !"F".equals(s.getZone())) return -4;
        if (Boolean.TRUE.equals(a.getNoisy()) && !Boolean.TRUE.equals(s.getNoiseSuitable())) return -5;
        // 桌子特别多的需要大面积摊位（6㎡ 可容纳 2 张桌）
        if (a.getTableNeed() != null && a.getTableNeed() >= 3 && (s.getSizeSqm() == null || s.getSizeSqm() < 8)) return -6;
        return 0;
    }

    private String hardReason(int code) {
        return switch (code) {
            case -1 -> "无供电";
            case -2 -> "单摊功率不足";
            case -3 -> "回路用电超载";
            case -4 -> "食品须入食品专区 F";
            case -5 -> "噪音/油烟须入耐噪专区";
            case -6 -> "摊位面积不足";
            default -> "其他";
        };
    }

    private int fitScore(Stall s, VendorApplication a, int score, Map<Category, List<Point>> categoryPoints) {
        int fit = 0;
        // 人流匹配：评分 >=120 高人流，>=90 中人流，其余低人流；匹配 +25
        TrafficLevel prefer = score >= 120 ? TrafficLevel.HIGH : score >= 90 ? TrafficLevel.MEDIUM : TrafficLevel.LOW;
        if (s.getTrafficLevel() == prefer) fit += 25;
        else if (s.getTrafficLevel() == TrafficLevel.MEDIUM) fit += 10;
        // 公益义卖优先高人流
        if (a.getCategory() == Category.CHARITY && s.getTrafficLevel() == TrafficLevel.HIGH) fit += 20;
        // 食品专区只优先承接食品摊位，非食品进入扣适配分（高人流的食品专区不挤占公益/手作位）
        if ("F".equals(s.getZone())) fit += a.getCategory() == Category.FOOD ? 15 : -25;
        // 亲子摊优先亲子区
        if (Boolean.TRUE.equals(a.getFamilyStall()) && Boolean.TRUE.equals(s.getFamilySpot())) fit += 30;
        // 商业摊位不占高人流公益位
        if (a.getCategory() == Category.COMMERCIAL && s.getTrafficLevel() == TrafficLevel.HIGH) fit -= 15;
        // 同品类越远越好（距离 100 坐标制），最近距离作为软分
        Point self = new Point(s.getPosX(), s.getPosY());
        double nearest = nearestDistance(self, categoryPoints.get(a.getCategory()));
        fit += (int) Math.min(20, nearest / 2);
        // 面积与需求贴近
        if (a.getTableNeed() != null && a.getTableNeed() >= 2 && s.getSizeSqm() != null && s.getSizeSqm() >= 8) fit += 8;
        return fit;
    }

    private boolean adjacentConflict(Stall s, Category category, Map<Category, List<Point>> points) {
        List<Point> others = points.get(category);
        if (others == null) return false;
        Point self = new Point(s.getPosX(), s.getPosY());
        return others.stream().anyMatch(p -> self.distance(p) < 18);
    }

    private double nearestDistance(Point self, List<Point> others) {
        if (others == null || others.isEmpty()) return 100;
        return others.stream().mapToDouble(self::distance).min().orElse(100);
    }

    private String buildReason(Stall s, VendorApplication a, User v, boolean conflict) {
        List<String> parts = new ArrayList<>();
        parts.add(s.getCode() + "（" + zoneName(s.getZone()) + "·" + s.getTrafficLevel().getLabel() + "）");
        parts.add("信用" + v.getCreditScore());
        parts.add("历史捐赠" + v.getTotalDonationCents() / 100 + "元");
        parts.add("承诺捐赠" + a.getDonationPercent().stripTrailingZeros().toPlainString() + "%");
        if (a.getCategory() == Category.CHARITY) parts.add("公益摊位优先");
        if (Boolean.TRUE.equals(v.getFoodRiskFlag())) parts.add("存在食品风险记录，已降低人流等级");
        if (a.getPowerNeedWatt() != null && a.getPowerNeedWatt() > 0)
            parts.add("用电" + a.getPowerNeedWatt() + "W→回路" + s.getPowerCircuit());
        if (conflict) parts.add("⚠同类摊位相邻，现场请注意错峰叫卖");
        return String.join("，", parts);
    }

    private String zoneName(String zone) {
        return switch (zone) {
            case "A" -> "主通道高人流区";
            case "B" -> "中人流区";
            case "C" -> "低人流区";
            case "F" -> "食品专区";
            case "K" -> "亲子专区";
            default -> zone;
        };
    }

    private Map<String, Integer> buildCircuitLoad(List<StallAssignment> existing, List<Stall> allStalls) {
        Map<String, Integer> used = new HashMap<>();
        Map<Long, Stall> stallMap = new HashMap<>();
        allStalls.forEach(s -> stallMap.put(s.getId(), s));
        for (StallAssignment as : existing) {
            Stall s = stallMap.get(as.getStallId());
            VendorApplication a = applications.findById(as.getApplicationId()).orElse(null);
            if (s != null && a != null && a.getPowerNeedWatt() != null && a.getPowerNeedWatt() > 0) {
                used.merge(s.getPowerCircuit(), a.getPowerNeedWatt(), Integer::sum);
            }
        }
        return used;
    }

    private String newCode() {
        for (int i = 0; i < 50; i++) {
            String code = String.format("%06d", random.nextInt(1_000_000));
            if (assignments.findAll().stream().noneMatch(a -> code.equals(a.getCheckinCode()))) {
                return code;
            }
        }
        throw new IllegalStateException("核销码生成失败");
    }

    private List<String> dedup(List<String> in) {
        return in.stream().distinct().limit(6).toList();
    }

    private record Point(Double x, Double y) {
        double distance(Point o) {
            if (x == null || y == null || o.x == null || o.y == null) return 100;
            return Math.hypot(x - o.x, y - o.y);
        }
    }
}
