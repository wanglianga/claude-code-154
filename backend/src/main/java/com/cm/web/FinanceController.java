package com.cm.web;

import com.cm.dto.Views;
import com.cm.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FinanceController {

    private final FinanceService finance;

    public FinanceController(FinanceService finance) {
        this.finance = finance;
    }

    @GetMapping("/events/{eventId}/sales")
    public List<Map<String, Object>> sales(@PathVariable Long eventId) {
        Roles.requireStaff();
        return finance.listSales(eventId).stream().map(Views::sales).toList();
    }

    /** 登记收款流水 */
    @PostMapping("/events/{eventId}/sales")
    public Map<String, Object> addSale(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        Long assignmentId = Long.valueOf(body.get("assignmentId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return Views.sales(finance.addSale(eventId, assignmentId, amount,
                (String) body.get("paymentMethod"), (String) body.get("receiptNo"),
                (String) body.get("note"), Roles.user().getId()));
    }

    @GetMapping("/events/{eventId}/donations")
    public List<Map<String, Object>> donations(@PathVariable Long eventId) {
        Roles.requireStaff();
        return finance.listDonations(eventId).stream().map(Views::donation).toList();
    }

    /** 单个摊位的捐赠核算（应捐金额随流水实时计算） */
    @GetMapping("/assignments/{id}/donation")
    public Map<String, Object> donation(@PathVariable Long id) {
        Roles.user();
        var d = finance.donationOf(id);
        return d == null ? Map.of() : Views.donation(d);
    }

    /** 工作人员核实实缴捐赠，金额不符自动登记异常 */
    @PostMapping("/assignments/{id}/donation/confirm")
    public Map<String, Object> confirm(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        BigDecimal actual = new BigDecimal(body.get("actualAmount").toString());
        return Views.donation(finance.confirmDonation(id, actual,
                (String) body.get("receiptNo"), (String) body.get("note"), Roles.user().getId()));
    }
}
