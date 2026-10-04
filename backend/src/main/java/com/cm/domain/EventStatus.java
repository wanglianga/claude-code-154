package com.cm.domain;

public enum EventStatus { PLANNED("筹备中"), ONGOING("进行中"), FINISHED("已结束"), PUBLISHED("已公示");
    private final String label;
    EventStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
}
