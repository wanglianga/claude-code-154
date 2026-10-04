package com.community.market.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EventDtos {
    public record EventRequest(
            @NotBlank String name,
            LocalDate eventDate,
            String location,
            String publicGoal,
            BigDecimal goalAmount,
            String status,
            Integer visitorCount) {}
}
