package com.community.market.domain.enums;

/** 食品资质状态 */
public enum FoodQualStatus {
    NONE("无需食品资质"),
    COMPLETE("资质齐全"),
    INCOMPLETE("资质不完整");

    private final String label;

    FoodQualStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
