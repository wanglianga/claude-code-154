package com.community.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StallDtos {
    public record StallRequest(
            @NotNull Long eventId,
            @NotBlank String code,
            String zone,
            Integer rowNo,
            Integer colNo,
            Integer sizeLevel,
            Integer trafficLevel,
            boolean powerAvailable,
            Integer powerCapacity,
            boolean noiseSensitive) {}
}
