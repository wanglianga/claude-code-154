package com.cm.domain;

public enum ApplicationStatus { PENDING("待审核"), APPROVED("审核通过"), REJECTED("已驳回");
    private final String label;
    ApplicationStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
}
