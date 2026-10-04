package com.community.market.domain.enums;

/** 摊位 / 报卖品类 */
public enum StallType {
    HANDMADE("手作"),
    SECONDHAND("二手物品"),
    FOOD("食品"),
    CHARITY("公益义卖"),
    COMMERCIAL("商业");

    private final String label;

    StallType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
