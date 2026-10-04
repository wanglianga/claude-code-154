package com.community.market.domain.enums;

public enum ComplaintStatus {
    OPEN("待处理"),
    PROCESSING("处理中"),
    RESOLVED("已处理");

    private final String label;

    ComplaintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
