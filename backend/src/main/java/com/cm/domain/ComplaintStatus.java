package com.cm.domain;

public enum ComplaintStatus { OPEN("待处理"), PROCESSING("处理中"), RESOLVED("已办结");
    private final String label;
    ComplaintStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
}
