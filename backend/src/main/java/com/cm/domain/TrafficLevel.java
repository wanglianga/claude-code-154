package com.cm.domain;

public enum TrafficLevel { HIGH("高人流"), MEDIUM("中人流"), LOW("低人流");
    private final String label;
    TrafficLevel(String l) { this.label = l; }
    public String getLabel() { return label; }
}
