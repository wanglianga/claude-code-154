package com.cm.domain;

/** 现场检查结论 */
public enum InspectionResult {
    OK("与报名相符"),
    MISMATCH("售卖与报名不符"),
    WARNING("警告并限期整改");
    private final String label;
    InspectionResult(String l) { this.label = l; }
    public String getLabel() { return label; }
}
