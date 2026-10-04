package com.community.market.dto;

import jakarta.validation.constraints.NotNull;

public class PublicityDtos {
    public record PublicityRequest(
            @NotNull Long eventId,
            String title,
            String fundUsage,
            String fulfillmentSummary,
            String incidentSummary,
            boolean includeDetail,
            boolean publish) {}
}
