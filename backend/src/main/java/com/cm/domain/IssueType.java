package com.cm.domain;

/** 活动档案中的异常类型，覆盖题目列举的全部现场风险 */
public enum IssueType {
    FOOD_QUALIFICATION("食品资质不完整"),
    MISMATCH("现场售卖与报名不符"),
    CATEGORY_CONFLICT("同类摊位冲突"),
    POWER_OVERLOAD("用电超载"),
    EARLY_LEAVE("摊主提前离场"),
    DONATION_MISMATCH("捐赠金额与流水不一致"),
    OTHER("其他异常");

    private final String label;
    IssueType(String l) { this.label = l; }
    public String getLabel() { return label; }
}
