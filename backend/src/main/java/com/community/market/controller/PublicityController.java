package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.Event;
import com.community.market.domain.entity.Publicity;
import com.community.market.domain.enums.EventStatus;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.PublicityDtos;
import com.community.market.repository.EventRepository;
import com.community.market.repository.PublicityRepository;
import com.community.market.security.CurrentUser;
import com.community.market.security.TokenStore;
import com.community.market.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class PublicityController {

    private final EventService eventService;
    private final PublicityRepository publicityRepository;
    private final EventRepository eventRepository;
    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 社区编辑/发布活动公示（总捐赠、用途、履约、异常处理、逐摊明细） */
    @PostMapping("/api/admin/publicity")
    public ApiResult<Publicity> save(@Valid @RequestBody PublicityDtos.PublicityRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(eventService.savePublicity(CurrentUser.userId(), req));
    }

    @GetMapping("/api/admin/events/{eventId}/publicity")
    public ApiResult<Publicity> adminGet(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN);
        return ApiResult.ok(publicityRepository.findByEventId(eventId).orElse(null));
    }

    // ==================== 居民公开端（无需登录） ====================

    /** 已结束/已公示的活动列表 */
    @GetMapping("/api/public/events")
    public ApiResult<List<Event>> publicEvents() {
        return ApiResult.ok(eventRepository.findAllByOrderByEventDateDescIdDesc().stream()
                .filter(e -> e.getStatus() == EventStatus.FINISHED || e.getStatus() == EventStatus.PUBLISHED)
                .toList());
    }

    /** 居民查看公示：总捐赠、用途、履约、异常处理、捐赠明细 */
    @GetMapping("/api/public/events/{eventId}/publicity")
    public ApiResult<Map<String, Object>> publicView(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        Publicity p = publicityRepository.findByEventId(eventId).orElse(null);
        if (p == null || !p.isPublished()) {
            return ApiResult.ok(Map.of("event", event, "published", false));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("published", true);
        view.put("event", event);
        view.put("title", p.getTitle());
        view.put("totalSales", p.getTotalSales());
        view.put("totalDonation", p.getTotalDonation());
        view.put("fundUsage", p.getFundUsage());
        view.put("fulfillmentSummary", p.getFulfillmentSummary());
        view.put("incidentSummary", p.getIncidentSummary());
        view.put("publishedAt", p.getPublishedAt());
        Object detail = null;
        if (p.getDonationDetailJson() != null) {
            try {
                detail = objectMapper.readValue(p.getDonationDetailJson(), Object.class);
            } catch (Exception ignored) {
            }
        }
        view.put("donationDetails", detail);
        return ApiResult.ok(view);
    }
}
