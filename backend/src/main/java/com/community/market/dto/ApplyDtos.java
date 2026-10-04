package com.community.market.dto;

import com.community.market.domain.enums.FoodQualStatus;
import com.community.market.domain.enums.StallType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ApplyDtos {
    public record ApplyRequest(
            @NotNull Long eventId,
            String shopName,
            @NotNull StallType category,
            String goodsDescription,
            @NotNull FoodQualStatus foodQualification,
            String foodLicenseNo,
            @NotNull BigDecimal donationRate,
            boolean powerNeeded,
            Integer powerWanted,
            boolean tableNeeded,
            Integer tableCount,
            boolean chairNeeded,
            Integer chairCount,
            boolean familyFriendly,
            boolean noiseMaking,
            String paymentMethod,
            String paymentAccount) {}

    public record ReviewRequest(boolean approved, String comment) {}

    public record ReassignRequest(@NotNull Long applicationId, @NotNull Long stallId) {}

    public record AutoAllocateRequest(@NotNull Long eventId) {}
}
