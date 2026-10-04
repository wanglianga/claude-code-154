package com.community.market.service;

import com.community.market.common.BizException;
import com.community.market.domain.entity.Application;
import com.community.market.domain.entity.DonationRecord;
import com.community.market.domain.entity.PaymentRecord;
import com.community.market.domain.enums.ApplicationStatus;
import com.community.market.domain.enums.DonationStatus;
import com.community.market.domain.enums.IncidentType;
import com.community.market.dto.FinanceDtos;
import com.community.market.repository.ApplicationRepository;
import com.community.market.repository.DonationRecordRepository;
import com.community.market.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收款统计与捐赠核对：以收款流水合计 × 承诺比例计算应捐，
 * 实际捐赠与应捐不一致时进入"流水不符"异常档案。
 */
@Service
@RequiredArgsConstructor
public class FinanceService {

    private final PaymentRecordRepository paymentRepository;
    private final DonationRecordRepository donationRepository;
    private final ApplicationRepository applicationRepository;
    private final IncidentService incidentService;
    private final CreditService creditService;

    @Transactional
    public PaymentRecord addPayment(Long recorderId, FinanceDtos.PaymentRequest req) {
        Application a = applicationRepository.findById(req.applicationId())
                .orElseThrow(() -> new BizException("报名不存在"));
        if (!a.getEventId().equals(req.eventId())) {
            throw new BizException("流水活动与报名活动不一致");
        }
        if (req.amount().signum() < 0) {
            throw new BizException("收款金额不能为负");
        }

        PaymentRecord p = new PaymentRecord();
        p.setEventId(req.eventId());
        p.setApplicationId(req.applicationId());
        p.setPaymentMethod(req.paymentMethod() == null ? a.getPaymentMethod() : req.paymentMethod());
        p.setAmount(req.amount());
        p.setReceiptNo(req.receiptNo());
        p.setRecorderId(recorderId);
        p.setRemark(req.remark());
        paymentRepository.save(p);

        // 维护捐赠记录快照并按最新流水重算应捐
        DonationRecord d = ensureDonation(a);
        recalcExpected(d);

        // 已核对过的捐赠若因补录流水导致应捐变化，重新置为存疑
        if (d.getStatus() == DonationStatus.CONFIRMED
                && d.getActualAmount().compareTo(d.getExpectedAmount()) != 0) {
            d.setStatus(DonationStatus.DISPUTED);
            donationRepository.save(d);
            incidentService.raise(a.getEventId(), a.getId(), IncidentType.DONATION_MISMATCH,
                    "补录收款流水后应捐变为 " + d.getExpectedAmount() + " 元，与已确认捐赠 "
                            + d.getActualAmount() + " 元不一致", 3, recorderId);
        } else {
            donationRepository.save(d);
        }
        return p;
    }

    @Transactional
    public DonationRecord confirmDonation(Long operatorId, FinanceDtos.DonationConfirmRequest req) {
        Application a = applicationRepository.findById(req.applicationId())
                .orElseThrow(() -> new BizException("报名不存在"));
        DonationRecord d = ensureDonation(a);
        recalcExpected(d);
        d.setActualAmount(req.actualAmount());
        d.setVoucherNo(req.voucherNo());
        d.setConfirmedBy(operatorId);
        d.setConfirmedAt(LocalDateTime.now());

        if (req.actualAmount().compareTo(d.getExpectedAmount()) == 0) {
            d.setStatus(DonationStatus.CONFIRMED);
            donationRepository.save(d);
            // 捐赠守信：每满 100 元 +2 信用（封顶 15）
            int bonus = Math.min(15, req.actualAmount().divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue() * 2);
            if (bonus > 0) {
                creditService.adjust(a.getVendorId(), a.getEventId(), a.getId(), bonus,
                        "按承诺比例足额捐赠 " + req.actualAmount() + " 元");
            }
        } else {
            d.setStatus(DonationStatus.DISPUTED);
            donationRepository.save(d);
            incidentService.raise(a.getEventId(), a.getId(), IncidentType.DONATION_MISMATCH,
                    "捐赠金额与流水不一致：应捐 " + d.getExpectedAmount() + " 元（流水 "
                            + d.getSalesAmount() + " × " + d.getRate() + "%），实际上缴 "
                            + req.actualAmount() + " 元", 3, operatorId);
            creditService.adjust(a.getVendorId(), a.getEventId(), a.getId(), -12, "捐赠金额与收款流水不符");
        }
        return d;
    }

    private DonationRecord ensureDonation(Application a) {
        return donationRepository.findByApplicationId(a.getId()).orElseGet(() -> {
            DonationRecord d = new DonationRecord();
            d.setEventId(a.getEventId());
            d.setApplicationId(a.getId());
            d.setRate(a.getDonationRate());
            return donationRepository.save(d);
        });
    }

    private void recalcExpected(DonationRecord d) {
        List<PaymentRecord> payments = paymentRepository.findByApplicationIdOrderByRecordedAtDesc(d.getApplicationId());
        BigDecimal sales = payments.stream().map(PaymentRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = sales.multiply(d.getRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        d.setSalesAmount(sales);
        d.setExpectedAmount(expected);
    }
}
