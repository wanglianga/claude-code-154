package com.cm.dto;

import com.cm.domain.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 实体转前端视图 Map，并附带枚举中文标签，避免大量样板 DTO */
public final class Views {

    private Views() {}

    public static Map<String, Object> user(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("name", u.getName());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole());
        m.put("creditScore", u.getCreditScore());
        m.put("totalDonationYuan", centsToYuan(u.getTotalDonationCents()));
        m.put("foodRiskFlag", u.getFoodRiskFlag());
        return m;
    }

    public static Map<String, Object> event(Event e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName());
        m.put("description", e.getDescription());
        m.put("location", e.getLocation());
        m.put("startTime", e.getStartTime());
        m.put("endTime", e.getEndTime());
        m.put("status", e.getStatus());
        m.put("statusLabel", e.getStatus().getLabel());
        m.put("donationGoal", e.getDonationGoal());
        m.put("donationPurpose", e.getDonationPurpose());
        m.put("totalPowerWatt", e.getTotalPowerWatt());
        return m;
    }

    public static Map<String, Object> stall(Stall s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("eventId", s.getEventId());
        m.put("code", s.getCode());
        m.put("zone", s.getZone());
        m.put("sizeSqm", s.getSizeSqm());
        m.put("trafficLevel", s.getTrafficLevel());
        m.put("trafficLabel", s.getTrafficLevel().getLabel());
        m.put("powerAvailable", s.getPowerAvailable());
        m.put("maxPowerWatt", s.getMaxPowerWatt());
        m.put("powerCircuit", s.getPowerCircuit());
        m.put("noiseSuitable", s.getNoiseSuitable());
        m.put("familySpot", s.getFamilySpot());
        m.put("posX", s.getPosX());
        m.put("posY", s.getPosY());
        return m;
    }

    public static Map<String, Object> application(VendorApplication a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("eventId", a.getEventId());
        m.put("vendorId", a.getVendorId());
        m.put("stallName", a.getStallName());
        m.put("category", a.getCategory());
        m.put("categoryLabel", a.getCategory().getLabel());
        m.put("goodsDescription", a.getGoodsDescription());
        m.put("foodQualified", a.getFoodQualified());
        m.put("foodCertNo", a.getFoodCertNo());
        m.put("foodCertAttachment", a.getFoodCertAttachment());
        m.put("donationPercent", a.getDonationPercent());
        m.put("powerNeedWatt", a.getPowerNeedWatt());
        m.put("tableNeed", a.getTableNeed());
        m.put("chairNeed", a.getChairNeed());
        m.put("familyStall", a.getFamilyStall());
        m.put("noisy", a.getNoisy());
        m.put("paymentMethod", a.getPaymentMethod());
        m.put("paymentAccount", a.getPaymentAccount());
        m.put("status", a.getStatus());
        m.put("statusLabel", a.getStatus().getLabel());
        m.put("reviewComment", a.getReviewComment());
        m.put("reviewedBy", a.getReviewedBy());
        m.put("reviewedAt", a.getReviewedAt());
        m.put("ruleHints", a.getRuleHints());
        m.put("createdAt", a.getCreatedAt());
        return m;
    }

    public static Map<String, Object> assignment(StallAssignment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("eventId", a.getEventId());
        m.put("applicationId", a.getApplicationId());
        m.put("stallId", a.getStallId());
        m.put("checkinCode", a.getCheckinCode());
        m.put("score", a.getScore());
        m.put("reason", a.getReason());
        m.put("autoAssigned", a.getAutoAssigned());
        m.put("checkedIn", a.getCheckedIn());
        m.put("checkInAt", a.getCheckInAt());
        m.put("earlyLeave", a.getEarlyLeave());
        m.put("leaveAt", a.getLeaveAt());
        m.put("suppliesClaimed", a.getSuppliesClaimed());
        m.put("suppliesClaimedAt", a.getSuppliesClaimedAt());
        m.put("createdAt", a.getCreatedAt());
        return m;
    }

    public static Map<String, Object> inspection(Inspection i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("eventId", i.getEventId());
        m.put("assignmentId", i.getAssignmentId());
        m.put("staffId", i.getStaffId());
        m.put("result", i.getResult());
        m.put("resultLabel", i.getResult().getLabel());
        m.put("actualGoods", i.getActualGoods());
        m.put("note", i.getNote());
        m.put("createdAt", i.getCreatedAt());
        return m;
    }

    public static Map<String, Object> goodsChange(GoodsChange g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("eventId", g.getEventId());
        m.put("assignmentId", g.getAssignmentId());
        m.put("originalGoods", g.getOriginalGoods());
        m.put("changedGoods", g.getChangedGoods());
        m.put("reason", g.getReason());
        m.put("staffId", g.getStaffId());
        m.put("approved", g.getApproved());
        m.put("createdAt", g.getCreatedAt());
        return m;
    }

    public static Map<String, Object> sales(SalesRecord s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("eventId", s.getEventId());
        m.put("assignmentId", s.getAssignmentId());
        m.put("amount", s.getAmount());
        m.put("paymentMethod", s.getPaymentMethod());
        m.put("receiptNo", s.getReceiptNo());
        m.put("staffId", s.getStaffId());
        m.put("note", s.getNote());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    public static Map<String, Object> donation(DonationRecord d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("eventId", d.getEventId());
        m.put("assignmentId", d.getAssignmentId());
        m.put("salesTotal", d.getSalesTotal());
        m.put("percent", d.getPercent());
        m.put("expectedAmount", d.getExpectedAmount());
        m.put("actualAmount", d.getActualAmount());
        m.put("status", d.getStatus());
        m.put("statusLabel", d.getStatus().getLabel());
        m.put("receiptNo", d.getReceiptNo());
        m.put("note", d.getNote());
        m.put("confirmedAt", d.getConfirmedAt());
        return m;
    }

    public static Map<String, Object> complaint(Complaint c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("eventId", c.getEventId());
        m.put("assignmentId", c.getAssignmentId());
        m.put("complainant", c.getComplainant());
        m.put("contact", c.getContact());
        m.put("content", c.getContent());
        m.put("status", c.getStatus());
        m.put("statusLabel", c.getStatus().getLabel());
        m.put("handleResult", c.getHandleResult());
        m.put("handledAt", c.getHandledAt());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    public static Map<String, Object> issue(EventIssue i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("eventId", i.getEventId());
        m.put("assignmentId", i.getAssignmentId());
        m.put("type", i.getType());
        m.put("typeLabel", i.getType().getLabel());
        m.put("description", i.getDescription());
        m.put("status", i.getStatus());
        m.put("statusLabel", i.getStatus().getLabel());
        m.put("resolution", i.getResolution());
        m.put("creditPenalty", i.getCreditPenalty());
        m.put("createdAt", i.getCreatedAt());
        m.put("resolvedAt", i.getResolvedAt());
        return m;
    }

    public static Map<String, Object> traffic(TrafficRecord t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("eventId", t.getEventId());
        m.put("period", t.getPeriod());
        m.put("visitorCount", t.getVisitorCount());
        m.put("note", t.getNote());
        m.put("createdAt", t.getCreatedAt());
        return m;
    }

    public static Map<String, Object> report(PublicReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("eventId", r.getEventId());
        m.put("totalDonation", r.getTotalDonation());
        m.put("totalVisitors", r.getTotalVisitors());
        m.put("goodPerformers", r.getGoodPerformers());
        m.put("issueVendorCount", r.getIssueVendorCount());
        m.put("donationPurpose", r.getDonationPurpose());
        m.put("issueSummary", r.getIssueSummary());
        m.put("nextPlan", r.getNextPlan());
        m.put("publishedAt", r.getPublishedAt());
        return m;
    }

    public static double centsToYuan(Long cents) {
        return cents == null ? 0 : cents / 100.0;
    }
}
