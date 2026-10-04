package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.DonationRecord;
import com.community.market.domain.entity.PaymentRecord;
import com.community.market.domain.enums.UserRole;
import com.community.market.dto.FinanceDtos;
import com.community.market.repository.DonationRecordRepository;
import com.community.market.repository.PaymentRecordRepository;
import com.community.market.security.CurrentUser;
import com.community.market.security.TokenStore;
import com.community.market.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final PaymentRecordRepository paymentRepository;
    private final DonationRecordRepository donationRepository;
    private final TokenStore tokenStore;

    /** 现场登记收款流水（工作人员），摊主也可上报自己的流水 */
    @PostMapping("/payments")
    public ApiResult<PaymentRecord> addPayment(@Valid @RequestBody FinanceDtos.PaymentRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF, UserRole.VENDOR);
        return ApiResult.ok(financeService.addPayment(CurrentUser.userId(), req));
    }

    @GetMapping("/events/{eventId}/payments")
    public ApiResult<List<PaymentRecord>> payments(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(paymentRepository.findByEventIdOrderByIdDesc(eventId));
    }

    @GetMapping("/events/{eventId}/donations")
    public ApiResult<List<DonationRecord>> donations(@PathVariable Long eventId) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(donationRepository.findByEventIdOrderByIdAsc(eventId));
    }

    /** 核对确认捐赠：金额=流水×比例 则确认，否则进入流水不符异常 */
    @PostMapping("/donations/confirm")
    public ApiResult<DonationRecord> confirm(@Valid @RequestBody FinanceDtos.DonationConfirmRequest req) {
        tokenStore.requireRole(UserRole.ADMIN, UserRole.STAFF);
        return ApiResult.ok(financeService.confirmDonation(CurrentUser.userId(), req));
    }
}
