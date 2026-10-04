package com.cm.domain;

public enum DonationStatus {
    PENDING_PAY("待缴纳"),
    CONFIRMED("已核实"),
    MISMATCH("金额不符"),
    PUBLICIZED("已公示");
    private final String label;
    DonationStatus(String l) { this.label = l; }
    public String getLabel() { return label; }
}
