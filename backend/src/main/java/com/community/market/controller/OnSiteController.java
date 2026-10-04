package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.Application;
import com.community.market.domain.entity.Complaint;
import com.community.market.domain.entity.Event;
import com.community.market.domain.entity.Inspection;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.OnSiteDtos;
import com.community.market.repository.ApplicationRepository;
import com.community.market.repository.ComplaintRepository;
import com.community.market.repository.IncidentRepository;
import com.community.market.repository.InspectionRepository;
import com.community.market.security.CurrentUser;
import com.community.market.security.TokenStore;
import com.community.market.service.IncidentService;
import com.community.market.service.OnSiteService;
import com.community.market.service.ViewSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onsite")
@RequiredArgsConstructor
public class OnSiteController {

    private final OnSiteService onSiteService;
    private final IncidentService incidentService;
    private final ComplaintRepository complaintRepository;
    private final IncidentRepository incidentRepository;
    private final InspectionRepository inspectionRepository;
    private final ApplicationRepository applicationRepository;
    private final ViewSupport viewSupport;
    private final TokenStore tokenStore;

    /** 扫二维码核销入场（返回摊主信息与物资领取清单） */
    @PostMapping("/checkin")
    public ApiResult<Map<String, Object>> checkin(@Valid @RequestBody OnSiteDtos.CheckinRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.checkin(CurrentUser.userId(), req.code()));
    }

    /** 根据报名查看物资清单与核销状态 */
    @GetMapping("/applications/{id}/materials")
    public ApiResult<Object> materials(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        Application a = applicationRepository.findById(id).orElseThrow();
        return ApiResult.ok(Map.of(
                "application", viewSupport.applicationView(a, false),
                "materials", viewSupport.materialList(a)));
    }

    @PostMapping("/applications/{id}/materials/collect")
    public ApiResult<Application> collect(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.collectMaterials(id));
    }

    /** 现场巡检：实际售卖内容、临时换品、用电实测 */
    @PostMapping("/inspections")
    public ApiResult<Inspection> inspect(@Valid @RequestBody OnSiteDtos.InspectionRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.inspect(CurrentUser.userId(), req));
    }

    @GetMapping("/events/{eventId}/inspections")
    public ApiResult<List<Inspection>> inspections(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(inspectionRepository.findByEventIdOrderByIdDesc(eventId));
    }

    /** 摊主提前离场 */
    @PostMapping("/applications/{id}/leave-early")
    public ApiResult<Application> leaveEarly(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.leaveEarly(CurrentUser.userId(), id));
    }

    // ---------- 投诉 ----------

    @PostMapping("/complaints")
    public ApiResult<Complaint> createComplaint(@Valid @RequestBody OnSiteDtos.ComplaintRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.createComplaint(req));
    }

    @GetMapping("/events/{eventId}/complaints")
    public ApiResult<List<Complaint>> complaints(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(complaintRepository.findByEventIdOrderByIdDesc(eventId));
    }

    @PostMapping("/complaints/{id}/handle")
    public ApiResult<Complaint> handleComplaint(@PathVariable Long id,
                                                @Valid @RequestBody OnSiteDtos.ComplaintHandleRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.handleComplaint(CurrentUser.userId(), id, req.status(), req.handleResult()));
    }

    // ---------- 异常 ----------

    @PostMapping("/incidents")
    public ApiResult<Object> raiseIncident(@Valid @RequestBody OnSiteDtos.IncidentRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(incidentService.raise(req.eventId(), req.applicationId(), req.type(),
                req.description(), req.severity(), CurrentUser.userId()));
    }

    @GetMapping("/events/{eventId}/incidents")
    public ApiResult<Object> incidents(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(incidentRepository.findByEventIdOrderByIdDesc(eventId));
    }

    @PostMapping("/incidents/{id}/resolve")
    public ApiResult<Object> resolveIncident(@PathVariable Long id,
                                             @Valid @RequestBody OnSiteDtos.IncidentResolveRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(incidentService.resolve(id, req.resolution(), CurrentUser.userId()));
    }

    // ---------- 客流 ----------

    @PutMapping("/events/{eventId}/visitors")
    public ApiResult<Event> visitors(@PathVariable Long eventId, @Valid @RequestBody OnSiteDtos.VisitorRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(onSiteService.updateVisitorCount(eventId, req.visitorCount()));
    }
}
