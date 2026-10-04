package com.cm.domain;

public enum IssueStatus { OPEN("待处理"), RESOLVED("已处理"), PENALIZED("已扣分处理");
    private final String label;
    IssueStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
}
