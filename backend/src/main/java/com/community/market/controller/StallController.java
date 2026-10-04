package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.Stall;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.StallDtos;
import com.community.market.repository.StallRepository;
import com.community.market.security.TokenStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stalls")
@RequiredArgsConstructor
public class StallController {

    private final StallRepository stallRepository;
    private final TokenStore tokenStore;

    @GetMapping
    public ApiResult<List<Stall>> list(@RequestParam Long eventId) {
        return ApiResult.ok(stallRepository.findByEventIdOrderByCodeAsc(eventId));
    }

    @PostMapping
    public ApiResult<Stall> create(@Valid @RequestBody StallDtos.StallRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        Stall s = new Stall();
        apply(s, req);
        return ApiResult.ok(stallRepository.save(s));
    }

    @PutMapping("/{id}")
    public ApiResult<Stall> update(@PathVariable Long id, @Valid @RequestBody StallDtos.StallRequest req) {
        tokenStore.requireRole(UserRole.ADMIN);
        Stall s = stallRepository.findById(id).orElseThrow();
        apply(s, req);
        return ApiResult.ok(stallRepository.save(s));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        tokenStore.requireRole(UserRole.ADMIN);
        stallRepository.deleteById(id);
        return ApiResult.ok();
    }

    private void apply(Stall s, StallDtos.StallRequest req) {
        s.setEventId(req.eventId());
        s.setCode(req.code());
        s.setZone(req.zone());
        s.setRowNo(req.rowNo());
        s.setColNo(req.colNo());
        s.setSizeLevel(req.sizeLevel() == null ? 2 : req.sizeLevel());
        s.setTrafficLevel(req.trafficLevel() == null ? 2 : req.trafficLevel());
        s.setPowerAvailable(req.powerAvailable());
        s.setPowerCapacity(req.powerCapacity() == null ? 0 : req.powerCapacity());
        s.setNoiseSensitive(req.noiseSensitive());
    }
}
