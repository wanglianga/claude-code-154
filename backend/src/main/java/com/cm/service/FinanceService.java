package com.cm.service;

import com.cm.domain.*;
import com.cm.repo.*;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 收款与捐赠：现场逐笔登记收款流水 → 汇总营业额 → 按承诺比例计算应捐 →
 * 摊主实缴 → 系统比对流水，标记已核实/金额不符（不一致自动登记异常）。
 */
@Service
public class FinanceService {

    private final SalesRecordRepository sales;
    private final DonationRecordRepository donations;
    private final StallAssignmentRepository assignments;
    private final VendorApplicationRepository applications;
    private final UserRepository users;
    private final IssueService issues;

    public FinanceService(SalesRecordRepository sales, DonationRecordRepository donations,
                          StallAssignmentRepository assignments, VendorApplicationRepository applications,
                          UserRepository users, IssueService issues) {
        this.sales = sales;
        this.donations = donations;
        this.assignments = assignments;
        this.applications = applications;
        this.users = users;
        this.issues = issues;
    }

    // ---------------- 收款流水 ----------------

    public List<SalesRecord> listSales(Long eventId) {
        return sales.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    @Transactional
    public SalesRecord addSale(Long eventId, Long assignmentId, BigDecimal amount, String method,
                               String receiptNo, String note, Long staffId) {
        if (amount == null || amount.signum() <= 0) {
            throw new ApiException("收款金额必须大于 0");
        }
        SalesRecord r = new SalesRecord();
        r.setEventId(eventId);
        r.setAssignmentId(assignmentId);
        r.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        r.setPaymentMethod(method);
        r.setReceiptNo(receiptNo);
        r.setNote(note);
        r.setStaffId(staffId);
        sales.save(r);
        // 流水变化后同步应捐金额
        syncDonation(assignmentId);
        return r;
    }

    // ---------------- 捐赠 ----------------

    public List<DonationRecord> listDonations(Long eventId) {
        return donations.findByEventId(eventId);
    }

    public DonationRecord donationOf(Long assignmentId) {
        return donations.findByAssignmentId(assignmentId).orElse(null);
    }

    /** 依据流水与报名承诺比例（重新）计算捐赠记录 */
    @Transactional
    public DonationRecord syncDonation(Long assignmentId) {
        StallAssignment as = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(404, "分配记录不存在"));
        VendorApplication a = applications.findById(as.getApplicationId())
                .orElseThrow(() -> new ApiException(404, "报名不存在"));

        BigDecimal total = sales.findByAssignmentIdOrderByCreatedAtDesc(assignmentId).stream()
                .map(SalesRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percent = a.getDonationPercent() == null ? BigDecimal.ZERO : a.getDonationPercent();
        BigDecimal expected = total.multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        DonationRecord d = donations.findByAssignmentId(assignmentId).orElseGet(() -> {
            DonationRecord n = new DonationRecord();
            n.setEventId(as.getEventId());
            n.setAssignmentId(assignmentId);
            return n;
        });
        d.setSalesTotal(total);
        d.setPercent(percent);
        d.setExpectedAmount(expected);
        if (d.getStatus() != DonationStatus.CONFIRMED && d.getStatus() != DonationStatus.MISMATCH) {
            d.setStatus(expected.signum() > 0 ? DonationStatus.PENDING_PAY : DonationStatus.PENDING_PAY);
        }
        return donations.save(d);
    }

    /** 摊主缴纳捐赠，工作人员核实；与流水不一致自动登记异常 */
    @Transactional
    public DonationRecord confirmDonation(Long assignmentId, BigDecimal actual, String receiptNo,
                                          String note, Long staffId) {
        DonationRecord d = syncDonation(assignmentId);
        if (actual == null || actual.signum() < 0) {
            throw new ApiException("实缴金额不合法");
        }
        d.setActualAmount(actual.setScale(2, RoundingMode.HALF_UP));
        d.setReceiptNo(receiptNo);
        d.setNote(note);
        d.setStaffId(staffId);
        d.setConfirmedAt(LocalDateTime.now());

        boolean match = actual.compareTo(d.getExpectedAmount()) == 0;
        d.setStatus(match ? DonationStatus.CONFIRMED : DonationStatus.MISMATCH);
        donations.save(d);

        StallAssignment as = assignments.findById(assignmentId).orElseThrow();
        if (!match) {
            issues.report(d.getEventId(), assignmentId, IssueType.DONATION_MISMATCH,
                    String.format("捐赠与流水不一致：营业额 %s 元 × %s%% 应捐 %s 元，实缴 %s 元",
                            d.getSalesTotal().toPlainString(), d.getPercent().stripTrailingZeros().toPlainString(),
                            d.getExpectedAmount().toPlainString(), actual.toPlainString()),
                    staffId, 10);
        } else {
            // 实缴一致：累计到摊主历史捐赠，影响其以后摊位位置
            VendorApplication a = applications.findById(as.getApplicationId()).orElseThrow();
            User v = users.findById(a.getVendorId()).orElseThrow();
            v.setTotalDonationCents(v.getTotalDonationCents() + actual.multiply(BigDecimal.valueOf(100)).longValue());
            users.save(v);
        }
        return d;
    }
}
