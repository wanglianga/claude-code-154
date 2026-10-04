package com.community.market.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class FinanceDtos {
    public record PaymentRequest(
            @NotNull Long eventId,
            @NotNull Long applicationId,
            String paymentMethod,
            @NotNull BigDecimal amount,
            String receiptNo,
            String remark) {}

    public record DonationConfirmRequest(
            @NotNull Long applicationId,
            @NotNull BigDecimal actualAmount,
            String voucherNo) {}
}
