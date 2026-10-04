package com.community.market.domain.enums;

public enum ApplicationStatus {
    SUBMITTED("待审核"),
    APPROVED("审核通过"),
    REJECTED("审核驳回");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
