package com.community.market.dto;

import com.community.market.domain.enums.IncidentType;
import jakarta.validation.constraints.NotNull;

public class OnSiteDtos {
    public record CheckinRequest(@NotNull String code) {}

    public record InspectionRequest(
            @NotNull Long applicationId,
            String actualGoods,
            boolean matched,
            boolean goodsChanged,
            String changeDescription,
            Integer powerUsedWatt,
            String remark) {}

    public record ComplaintRequest(
            @NotNull Long eventId,
            Long applicationId,
            String reporterName,
            String reporterPhone,
            String complaintType,
            @NotNull String content) {}

    public record ComplaintHandleRequest(String status, String handleResult) {}

    public record IncidentRequest(
            @NotNull Long eventId,
            Long applicationId,
            @NotNull IncidentType type,
            @NotNull String description,
            Integer severity) {}

    public record IncidentResolveRequest(@NotNull String resolution) {}

    public record VisitorRequest(@NotNull Integer visitorCount) {}
}
