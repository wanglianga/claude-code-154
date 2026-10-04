package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/** 活动现场：核销入场、物资领取、现场检查、临时换品、提前离场 */
@Service
public class OnSiteService {

    private final StallAssignmentRepository assignments;
    private final VendorApplicationRepository applications;
    private final StallRepository stalls;
    private final UserRepository users;
    private final InspectionRepository inspections;
    private final GoodsChangeRepository changes;
    private final IssueService issues;

    public OnSiteService(StallAssignmentRepository assignments, VendorApplicationRepository applications,
                         StallRepository stalls, UserRepository users, InspectionRepository inspections,
                         GoodsChangeRepository changes, IssueService issues) {
        this.assignments = assignments;
        this.applications = applications;
        this.stalls = stalls;
        this.users = users;
        this.inspections = inspections;
        this.changes = changes;
        this.issues = issues;
    }

    /** 凭二维码核销码入场 */
    @Transactional
    public Map<String, Object> checkin(String code, Long staffId) {
        StallAssignment as = assignments.findByCheckinCode(code)
                .orElseThrow(() -> new ApiException(404, "核销码无效：" + code));
        if (Boolean.TRUE.equals(as.getCheckedIn())) {
            throw new ApiException("该摊主已核销入场，时间：" + as.getCheckInAt());
        }
        as.setCheckedIn(true);
        as.setCheckInAt(LocalDateTime.now());
        as.setCheckInBy(staffId);
        assignments.save(as);
        return checkinView(as);
    }

    public Map<String, Object> lookupByCode(String code) {
        StallAssignment as = assignments.findByCheckinCode(code)
                .orElseThrow(() -> new ApiException(404, "核销码无效：" + code));
        return checkinView(as);
    }

    private Map<String, Object> checkinView(StallAssignment as) {
        VendorApplication a = applications.findById(as.getApplicationId()).orElseThrow();
        Stall s = stalls.findById(as.getStallId()).orElseThrow();
        User v = users.findById(a.getVendorId()).orElseThrow();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignment", com.cm.dto.Views.assignment(as));
        m.put("application", com.cm.dto.Views.application(a));
        m.put("stall", com.cm.dto.Views.stall(s));
        m.put("vendor", com.cm.dto.Views.user(v));
        m.put("supplies", supplyList(a));
        return m;
    }

    /** 物资领取清单：按报名的桌椅/用电需求生成 */
    public List<Map<String, Object>> supplyList(VendorApplication a) {
        List<Map<String, Object>> list = new ArrayList<>();
        item(list, "工作桌", a.getTableNeed(), "张");
        item(list, "折叠椅", a.getChairNeed(), "把");
        if (a.getPowerNeedWatt() != null && a.getPowerNeedWatt() > 0) item(list, "电源插线板（已接 " + circuitOf(a) + "）", 1, "个");
        item(list, "摊主证 / 摊位号牌", 1, "套");
        item(list, "公益捐赠信封", 1, "个");
        if (a.getCategory() == Category.FOOD) item(list, "食品安全告知书 + 灭火器", 1, "套");
        if (Boolean.TRUE.equals(a.getFamilyStall())) item(list, "亲子地垫", 1, "块");
        return list;
    }

    private String circuitOf(VendorApplication a) {
        return assignments.findByApplicationId(a.getId())
                .flatMap(as -> stalls.findById(as.getStallId())).map(Stall::getPowerCircuit).orElse("-");
    }

    private void item(List<Map<String, Object>> list, String name, int qty, String unit) {
        if (qty <= 0) return;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("qty", qty);
        m.put("unit", unit);
        list.add(m);
    }

    @Transactional
    public StallAssignment claimSupplies(Long assignmentId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        as.setSuppliesClaimed(true);
        as.setSuppliesClaimedAt(LocalDateTime.now());
        return assignments.save(as);
    }

    /** 现场检查：记录实际售卖内容；不符时自动登记异常并扣分 */
    @Transactional
    public Inspection inspect(Long assignmentId, InspectionResult result, String actualGoods,
                              String note, Long staffId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        Inspection ins = new Inspection();
        ins.setEventId(as.getEventId());
        ins.setAssignmentId(assignmentId);
        ins.setStaffId(staffId);
        ins.setResult(result);
        ins.setActualGoods(actualGoods);
        ins.setNote(note);
        inspections.save(ins);

        if (result == InspectionResult.MISMATCH) {
            VendorApplication a = applications.findById(as.getApplicationId()).orElseThrow();
            issues.report(as.getEventId(), assignmentId, IssueType.MISMATCH,
                    "现场售卖（" + actualGoods + "）与报名内容（" + a.getGoodsDescription() + "）不符",
                    staffId, 15);
        }
        return ins;
    }

    /** 摊主登记临时换品，工作人员可批准；未经批准视为不符 */
    @Transactional
    public GoodsChange changeGoods(Long assignmentId, String changedGoods, String reason,
                                   boolean approved, Long staffId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        VendorApplication a = applications.findById(as.getApplicationId()).orElseThrow();
        GoodsChange g = new GoodsChange();
        g.setEventId(as.getEventId());
        g.setAssignmentId(assignmentId);
        g.setOriginalGoods(a.getGoodsDescription());
        g.setChangedGoods(changedGoods);
        g.setReason(reason);
        g.setApproved(approved);
        g.setStaffId(staffId);
        changes.save(g);
        if (!approved) {
            issues.report(as.getEventId(), assignmentId, IssueType.MISMATCH,
                    "临时换品未获批准：" + changedGoods, staffId, 10);
        }
        return g;
    }

    @Transactional
    public StallAssignment markEarlyLeave(Long assignmentId, Long staffId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        as.setEarlyLeave(true);
        as.setLeaveAt(LocalDateTime.now());
        assignments.save(as);
        issues.report(as.getEventId(), assignmentId, IssueType.EARLY_LEAVE,
                "摊主提前离场，离场时间 " + as.getLeaveAt(), staffId, 8);
        return as;
    }
}
