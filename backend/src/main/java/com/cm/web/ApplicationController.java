package com.cm.web;

import com.cm.domain.User;
import com.cm.dto.Views;
import com.cm.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applications;

    public ApplicationController(ApplicationService applications) {
        this.applications = applications;
    }

    /** 活动下全部报名（管理员/工作人员） */
    @GetMapping("/events/{eventId}/applications")
    public List<Map<String, Object>> listByEvent(@PathVariable Long eventId) {
        User u = Roles.user();
        return applications.listByEvent(eventId).stream()
                .map(applications::richView).toList();
    }

    /** 当前登录摊主的报名列表 */
    @GetMapping("/my/applications")
    public List<Map<String, Object>> mine() {
        Roles.requireVendor();
        User u = Roles.user();
        return applications.listByVendor(u.getId()).stream().map(Views::application).toList();
    }

    @GetMapping("/applications/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return applications.richView(applications.get(id));
    }

    /** 摊主提交/修改报名 */
    @PostMapping("/events/{eventId}/applications")
    public Map<String, Object> submit(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        Roles.requireVendor();
        User u = Roles.user();
        return Views.application(applications.submit(eventId, u.getId(), body));
    }

    /** 管理员审核：{approved, comment} */
    @PostMapping("/applications/{id}/review")
    public Map<String, Object> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        boolean approved = Boolean.parseBoolean(String.valueOf(body.get("approved")));
        String comment = (String) body.get("comment");
        return Views.application(applications.review(id, approved, comment, Roles.user().getId()));
    }
}
