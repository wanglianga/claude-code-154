package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {

    private final VendorApplicationRepository applications;
    private final EventRepository events;
    private final UserRepository users;
    private final StallAssignmentRepository assignments;

    public ApplicationService(VendorApplicationRepository applications, EventRepository events,
                              UserRepository users, StallAssignmentRepository assignments) {
        this.applications = applications;
        this.events = events;
        this.users = users;
        this.assignments = assignments;
    }

    public List<VendorApplication> listByEvent(Long eventId) {
        return applications.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    public List<VendorApplication> listByVendor(Long vendorId) {
        return applications.findByVendorIdOrderByCreatedAtDesc(vendorId);
    }

    public VendorApplication get(Long id) {
        return applications.findById(id).orElseThrow(() -> new ApiException(404, "报名记录不存在"));
    }

    /** 摊主提交/修改报名；审核通过后不允许修改 */
    @Transactional
    public VendorApplication submit(Long eventId, Long vendorId, Map<String, Object> body) {
        Event event = events.findById(eventId).orElseThrow(() -> new ApiException(404, "活动不存在"));
        User vendor = users.findById(vendorId).orElseThrow(() -> new ApiException(404, "摊主不存在"));
        if (vendor.getRole() != UserRole.VENDOR) {
            throw new ApiException("只有摊主账号可以报名");
        }
        VendorApplication a = applications.findByEventIdAndVendorId(eventId, vendorId).orElseGet(() -> {
            VendorApplication n = new VendorApplication();
            n.setEventId(eventId);
            n.setVendorId(vendorId);
            return n;
        });
        if (a.getStatus() == ApplicationStatus.APPROVED) {
            throw new ApiException("报名已审核通过，不能修改；如需调整请联系社区管理员");
        }

        a.setStallName((String) body.getOrDefault("stallName", a.getStallName()));
        if (body.get("category") != null) a.setCategory(Category.valueOf(body.get("category").toString()));
        a.setGoodsDescription((String) body.get("goodsDescription"));
        a.setFoodQualified(Boolean.valueOf(String.valueOf(body.getOrDefault("foodQualified", false))));
        a.setFoodCertNo((String) body.get("foodCertNo"));
        a.setFoodCertAttachment((String) body.get("foodCertAttachment"));
        if (body.get("donationPercent") != null) {
            BigDecimal pct = new BigDecimal(body.get("donationPercent").toString());
            if (pct.signum() < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ApiException("捐赠比例须在 0~100 之间");
            }
            a.setDonationPercent(pct);
        }
        a.setPowerNeedWatt(Integer.valueOf(String.valueOf(body.getOrDefault("powerNeedWatt", 0))));
        a.setTableNeed(Integer.valueOf(String.valueOf(body.getOrDefault("tableNeed", 1))));
        a.setChairNeed(Integer.valueOf(String.valueOf(body.getOrDefault("chairNeed", 2))));
        a.setFamilyStall(Boolean.valueOf(String.valueOf(body.getOrDefault("familyStall", false))));
        a.setNoisy(Boolean.valueOf(String.valueOf(body.getOrDefault("noisy", false))));
        a.setPaymentMethod((String) body.getOrDefault("paymentMethod", "微信"));
        a.setPaymentAccount((String) body.get("paymentAccount"));
        a.setStatus(ApplicationStatus.PENDING);
        a.setRuleHints(String.join("；", checkRules(a)));
        return applications.save(a);
    }

    /**
     * 活动规则校验，返回命中的风险提示：
     * 1) 食品类必须具备食品资质；2) 公益义卖捐赠比例≥50%；
     * 3) 商业摊位捐赠比例≥5%；4) 有用电/噪音需求须服从专区安排。
     */
    public List<String> checkRules(VendorApplication a) {
        List<String> hints = new ArrayList<>();
        if (a.getCategory() == null) {
            hints.add("请选择售卖品类");
            return hints;
        }
        if (a.getCategory() == Category.FOOD) {
            if (!Boolean.TRUE.equals(a.getFoodQualified()) || isBlank(a.getFoodCertNo())) {
                hints.add("食品类摊位缺少食品经营许可/健康证资质，不符合食品准入规则");
            }
        }
        if (a.getCategory() == Category.CHARITY
                && a.getDonationPercent().compareTo(BigDecimal.valueOf(50)) < 0) {
            hints.add("公益义卖摊位捐赠比例不应低于 50%");
        }
        if (a.getCategory() == Category.COMMERCIAL
                && a.getDonationPercent().compareTo(BigDecimal.valueOf(5)) < 0) {
            hints.add("商业摊位公益捐赠比例不应低于 5%");
        }
        if (a.getPowerNeedWatt() != null && a.getPowerNeedWatt() > 1500) {
            hints.add("用电功率 " + a.getPowerNeedWatt() + "W 超过单回路常规容量，须由社区统筹用电");
        }
        if (Boolean.TRUE.equals(a.getNoisy()) && a.getCategory() != Category.FOOD) {
            hints.add("存在噪音/油烟，须安排到远离居民区的耐噪专区");
        }
        return hints;
    }

    @Transactional
    public VendorApplication review(Long applicationId, boolean approved, String comment, Long reviewerId) {
        VendorApplication a = get(applicationId);
        if (a.getStatus() != ApplicationStatus.PENDING) {
            throw new ApiException("该报名已审核，不能重复审核");
        }
        List<String> hints = checkRules(a);
        a.setRuleHints(String.join("；", hints));
        if (approved && !hints.isEmpty()) {
            throw new ApiException("存在不符合活动规则的事项，请先驳回让摊主补正：" + String.join("；", hints));
        }
        a.setStatus(approved ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        a.setReviewComment(comment);
        a.setReviewedBy(reviewerId);
        a.setReviewedAt(LocalDateTime.now());
        return applications.save(a);
    }

    /** 管理员视角：摊主 + 报名 + 分配 合并信息 */
    public Map<String, Object> richView(VendorApplication a) {
        Map<String, Object> m = new LinkedHashMap<>();
        User v = users.findById(a.getVendorId()).orElse(null);
        m.put("application", com.cm.dto.Views.application(a));
        if (v != null) {
            Map<String, Object> vu = com.cm.dto.Views.user(v);
            m.put("vendor", vu);
        }
        assignments.findByApplicationId(a.getId()).ifPresent(x -> m.put("assignment", com.cm.dto.Views.assignment(x)));
        return m;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
