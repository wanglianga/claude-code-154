package com.community.market.domain.enums;

public enum EventStatus {
    RECRUITING("招募中"),
    REVIEWING("审核分配中"),
    ONGOING("进行中"),
    FINISHED("已结束"),
    PUBLISHED("已公示");

    private final String label;

    EventStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
