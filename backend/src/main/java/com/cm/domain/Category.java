package com.cm.domain;

/** 售卖品类：手作 / 二手物品 / 食品 / 公益义卖 / 商业摊位 */
public enum Category {
    HANDMADE("手作"),
    SECONDHAND("二手物品"),
    FOOD("食品"),
    CHARITY("公益义卖"),
    COMMERCIAL("商业摊位");

    private final String label;
    Category(String label) { this.label = label; }
    public String getLabel() { return label; }
}
