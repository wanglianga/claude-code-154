package com.cm.web;

import com.cm.domain.*;
import com.cm.dto.Views;
import com.cm.repo.*;
import com.cm.service.OnSiteService;
import com.cm.service.QrCodeService;
import com.cm.service.StallAssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final StallAssignmentService assignmentService;
    private final StallAssignmentRepository assignments;
    private final VendorApplicationRepository applications;
    private final StallRepository stalls;
    private final UserRepository users;
    private final QrCodeService qr;
    private final OnSiteService onSite;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public AssignmentController(StallAssignmentService assignmentService,
                                StallAssignmentRepository assignments,
                                VendorApplicationRepository applications,
                                StallRepository stalls, UserRepository users,
                                QrCodeService qr, OnSiteService onSite) {
        this.assignmentService = assignmentService;
        this.assignments = assignments;
        this.applications = applications;
        this.stalls = stalls;
        this.users = users;
        this.qr = qr;
        this.onSite = onSite;
    }

    /** 活动下摊位分配总览：摊位 + 分配 + 摊主/报名概要 */
    @GetMapping("/events/{eventId}/assignments")
    public Map<String, Object> list(@PathVariable Long eventId) {
        Map<Long, Map<String, Object>> stallMap = new LinkedHashMap<>();
        stalls.findByEventIdOrderByCode(eventId).forEach(s -> {
            Map<String, Object> m = Views.stall(s);
            m.put("assigned", false);
            stallMap.put(s.getId(), m);
        });
        List<Map<String, Object>> rows = new ArrayList<>();
        for (StallAssignment a : assignments.findByEventId(eventId)) {
            Map<String, Object> row = Views.assignment(a);
            applications.findById(a.getApplicationId()).ifPresent(ap -> {
                row.put("application", Views.application(ap));
                users.findById(ap.getVendorId()).ifPresent(v -> row.put("vendor", Views.user(v)));
                row.put("supplies", onSite.supplyList(ap));
            });
            stalls.findById(a.getStallId()).ifPresent(st -> {
                row.put("stall", Views.stall(st));
                Map<String, Object> sm = stallMap.get(st.getId());
                if (sm != null) {
                    sm.put("assigned", true);
                    sm.put("assignmentId", a.getId());
                }
            });
            rows.add(row);
        }
        return Map.of("stalls", stallMap.values(), "assignments", rows);
    }

    /** 一键自动分配 */
    @PostMapping("/events/{eventId}/auto-assign")
    public Map<String, Object> autoAssign(@PathVariable Long eventId) {
        Roles.requireAdmin();
        return assignmentService.autoAssign(eventId);
    }

    /** 人工调换摊位 */
    @PostMapping("/assignments/{id}/reassign")
    public Map<String, Object> reassign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireAdmin();
        Long stallId = Long.valueOf(body.get("stallId").toString());
        return Views.assignment(assignmentService.reassign(id, stallId));
    }

    /** 摊主查看自己某场活动的入场凭证 */
    @GetMapping("/my/event/{eventId}/pass")
    public Map<String, Object> myPass(@PathVariable Long eventId) {
        Roles.requireVendor();
        User u = Roles.user();
        VendorApplication a = applications.findByEventIdAndVendorId(eventId, u.getId())
                .orElseThrow(() -> new ApiException(404, "未找到报名记录"));
        StallAssignment as = assignments.findByApplicationId(a.getId())
                .orElseThrow(() -> new ApiException(400, "报名通过并完成摊位分配后生成入场凭证"));
        Stall st = stalls.findById(as.getStallId()).orElseThrow();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignment", Views.assignment(as));
        m.put("application", Views.application(a));
        m.put("stall", Views.stall(st));
        m.put("supplies", onSite.supplyList(a));
        m.put("qrContent", qrContent(as.getCheckinCode()));
        return m;
    }

    /** 入场二维码 PNG（登录后访问，供摊主截图保存） */
    @GetMapping(value = "/assignments/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcode(@PathVariable Long id) {
        Roles.user();
        StallAssignment as = assignments.findById(id)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        return ResponseEntity.ok(qr.generatePng(qrContent(as.getCheckinCode()), 280));
    }

    /** 公开二维码短链：扫码后展示核销码信息页（无需登录，方便工作人员用手机扫） */
    @GetMapping(value = "/qr/{code}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> publicQr(@PathVariable String code) {
        assignments.findByCheckinCode(code).orElseThrow(() -> new ApiException(404, "核销码无效"));
        return ResponseEntity.ok(qr.generatePng(publicBaseUrl + "/checkin?code=" + code, 280));
    }

    private String qrContent(String code) {
        return publicBaseUrl + "/checkin?code=" + code;
    }
}
