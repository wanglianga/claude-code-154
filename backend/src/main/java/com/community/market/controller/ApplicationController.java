package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.Application;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.ApplyDtos;
import com.community.market.repository.ApplicationRepository;
import com.community.market.security.CurrentUser;
import com.community.market.security.TokenStore;
import com.community.market.service.ApplicationService;
import com.community.market.service.ViewSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final ViewSupport viewSupport;
    private final TokenStore tokenStore;

    // ---------- 摊主端 ----------

    @PostMapping("/my/applications")
    public ApiResult<Application> apply(@Valid @RequestBody ApplyDtos.ApplyRequest req) {
        tokenStore.requireRole(UserRole.VENDOR);
        return ApiResult.ok(applicationService.apply(CurrentUser.userId(), req));
    }

    @GetMapping("/my/applications")
    public ApiResult<List<Map<String, Object>>> mine() {
        tokenStore.requireRole(UserRole.VENDOR);
        return ApiResult.ok(applicationRepository.findByVendorIdOrderByIdDesc(CurrentUser.userId())
                .stream().map(a -> viewSupport.applicationView(a, true)).toList());
    }

    /** 摊主查看自己某次报名的二维码与物资清单 */
    @GetMapping("/my/applications/{id}/entry")
    public ApiResult<Map<String, Object>> myEntry(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.VENDOR);
        Application a = applicationRepository.findById(id).orElseThrow();
        if (!a.getVendorId().equals(CurrentUser.userId())) {
            throw new IllegalArgumentException("无权查看他人报名");
        }
        return ApiResult.ok(Map.of(
                "application", viewSupport.applicationView(a, true),
                "materials", viewSupport.materialList(a)));
    }

    // ---------- 社区端 ----------

    @GetMapping("/events/{eventId}/applications")
    public ApiResult<List<Map<String, Object>>> listByEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        List<Application> apps = applicationRepository.findByEventIdOrderByIdAsc(eventId);
        return ApiResult.ok(apps.stream()
                .filter(a -> status == null || status.isBlank() || a.getStatus().name().equals(status))
                .map(a -> viewSupport.applicationView(a, true)).toList());
    }

    @GetMapping("/applications/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF, UserRole.VENDOR);
        Application a = applicationRepository.findById(id).orElseThrow();
        if (CurrentUser.get().role() == UserRole.VENDOR && !a.getVendorId().equals(CurrentUser.userId())) {
            throw new IllegalArgumentException("无权查看他人报名");
        }
        return ApiResult.ok(viewSupport.applicationView(a, true));
    }

    @PostMapping("/applications/{id}/review")
    public ApiResult<Application> review(@PathVariable Long id, @Valid @RequestBody ApplyDtos.ReviewRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(applicationService.review(CurrentUser.userId(), id, req.approved(), req.comment()));
    }

    /** 智能分配摊位 */
    @PostMapping("/allocations/auto")
    public ApiResult<List<Map<String, Object>>> autoAllocate(@Valid @RequestBody ApplyDtos.AutoAllocateRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(applicationService.autoAllocate(req.eventId()));
    }

    /** 手动调整摊位 */
    @PostMapping("/allocations/reassign")
    public ApiResult<Application> reassign(@Valid @RequestBody ApplyDtos.ReassignRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(applicationService.reassign(req.applicationId(), req.stallId()));
    }

    @PostMapping("/applications/{id}/release-stall")
    public ApiResult<Void> release(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN);
        applicationService.releaseStall(id);
        return ApiResult.ok();
    }
}
