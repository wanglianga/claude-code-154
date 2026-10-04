package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.Event;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.EventDtos;
import com.community.market.repository.EventRepository;
import com.community.market.security.TokenStore;
import com.community.market.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final TokenStore tokenStore;

    /** 已登录用户可见活动列表 */
    @GetMapping
    public ApiResult<List<Event>> list() {
        return ApiResult.ok(eventRepository.findAllByOrderByEventDateDescIdDesc());
    }

    @GetMapping("/{id}")
    public ApiResult<Event> get(@PathVariable Long id) {
        return ApiResult.ok(eventRepository.findById(id).orElseThrow());
    }

    @PostMapping
    public ApiResult<Event> create(@Valid @RequestBody EventDtos.EventRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(eventService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResult<Event> update(@PathVariable Long id, @Valid @RequestBody EventDtos.EventRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(eventService.update(id, req));
    }

    @PutMapping("/{id}/status")
    public ApiResult<Event> status(@PathVariable Long id, @RequestParam String status) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(eventService.changeStatus(id, status));
    }

    /** 活动档案：报名、审核、摊位、收款、捐赠、投诉、异常、公示同一档案 */
    @GetMapping("/{id}/archive")
    public ApiResult<Object> archive(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(eventService.archive(id));
    }

    /** 复盘：公益金额、客流、投诉、履约、下次招商建议 */
    @GetMapping("/{id}/review")
    public ApiResult<Object> review(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(eventService.review(id));
    }
}
