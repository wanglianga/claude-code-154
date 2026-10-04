package com.community.market.domain.enums;

public enum CheckInStatus {
    NOT_CHECKED("未入场"),
    CHECKED_IN("已入场"),
    LEFT_EARLY("提前离场");

    private final String label;

    CheckInStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
