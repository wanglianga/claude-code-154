package com.cm.web;

import com.cm.domain.InspectionResult;
import com.cm.dto.Views;
import com.cm.service.OnSiteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OnSiteController {

    private final OnSiteService onSite;

    public OnSiteController(OnSiteService onSite) {
        this.onSite = onSite;
    }

    /** 扫码查询摊主信息（工作人员核销前核对） */
    @GetMapping("/checkin/{code}")
    public Map<String, Object> lookup(@PathVariable String code) {
        Roles.requireStaff();
        return onSite.lookupByCode(code);
    }

    /** 核销入场 */
    @PostMapping("/checkin/{code}")
    public Map<String, Object> checkin(@PathVariable String code) {
        Roles.requireStaff();
        return onSite.checkin(code, Roles.user().getId());
    }

    /** 物资领取清单标记已领取 */
    @PostMapping("/assignments/{id}/supplies")
    public Map<String, Object> supplies(@PathVariable Long id) {
        Roles.requireStaff();
        return Views.assignment(onSite.claimSupplies(id));
    }

    /** 现场检查 */
    @PostMapping("/assignments/{id}/inspections")
    public Map<String, Object> inspect(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        InspectionResult result = InspectionResult.valueOf(body.get("result").toString());
        return Views.inspection(onSite.inspect(id, result,
                (String) body.get("actualGoods"), (String) body.get("note"), Roles.user().getId()));
    }

    /** 临时换品登记/审批 */
    @PostMapping("/assignments/{id}/goods-changes")
    public Map<String, Object> changeGoods(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        boolean approved = Boolean.parseBoolean(String.valueOf(body.getOrDefault("approved", false)));
        return Views.goodsChange(onSite.changeGoods(id, (String) body.get("changedGoods"),
                (String) body.get("reason"), approved, Roles.user().getId()));
    }

    /** 摊主提前离场 */
    @PostMapping("/assignments/{id}/early-leave")
    public Map<String, Object> earlyLeave(@PathVariable Long id) {
        Roles.requireStaff();
        return Views.assignment(onSite.markEarlyLeave(id, Roles.user().getId()));
    }
}
