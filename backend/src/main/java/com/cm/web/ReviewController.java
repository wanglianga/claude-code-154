package com.cm.web;

import com.cm.dto.Views;
import com.cm.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 活动档案、复盘仪表盘、居民公示（工作人员/管理员） */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService review;

    public ReviewController(ReviewService review) {
        this.review = review;
    }

    /** 统一活动档案：报名/审核/摊位/收款/捐赠/投诉/异常/公示一体 */
    @GetMapping("/events/{id}/archive")
    public Map<String, Object> archive(@PathVariable Long id) {
        Roles.requireStaff();
        return review.archive(id);
    }

    /** 复盘仪表盘 */
    @GetMapping("/events/{id}/dashboard")
    public Map<String, Object> dashboard(@PathVariable Long id) {
        Roles.requireStaff();
        return review.dashboard(id);
    }

    /** 发布居民公示 */
    @PostMapping("/events/{id}/publish")
    public Map<String, Object> publish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        return Views.report(review.publish(id, (String) body.get("donationPurpose"),
                (String) body.get("issueSummary"), (String) body.get("nextPlan"), Roles.user().getId()));
    }

    /** 已公示活动（供公开页选择）—— 需登录的后台入口也可使用 */
    @GetMapping("/published")
    public List<Map<String, Object>> published() {
        Roles.user();
        return review.publicEvents();
    }
}
