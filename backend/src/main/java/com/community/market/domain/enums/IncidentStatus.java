package com.community.market.domain.enums;

public enum IncidentStatus {
    OPEN("待处理"),
    RESOLVED("已处理");

    private final String label;

    IncidentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
