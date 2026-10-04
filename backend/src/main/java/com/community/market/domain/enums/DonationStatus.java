package com.community.market.domain.enums;

/** 捐赠确认状态：与收款流水核对一致后确认 */
public enum DonationStatus {
    PENDING("待核对"),
    CONFIRMED("已核对"),
    DISPUTED("流水不符");

    private final String label;

    DonationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
