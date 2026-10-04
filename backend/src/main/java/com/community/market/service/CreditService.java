package com.community.market.service;

import com.community.market.domain.entity.CreditLog;
import com.community.market.domain.entity.User;
import com.community.market.repository.CreditLogRepository;
import com.community.market.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditService {

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 120;

    private final UserRepository userRepository;
    private final CreditLogRepository creditLogRepository;

    /** 调整摊主信用分并记录流水（信用会影响后续活动摊位位置） */
    @Transactional
    public int adjust(Long vendorId, Long eventId, Long applicationId, int delta, String reason) {
        User vendor = userRepository.findById(vendorId).orElse(null);
        if (vendor == null) {
            return 0;
        }
        int after = Math.max(MIN_SCORE, Math.min(MAX_SCORE, (vendor.getCreditScore() == null ? 100 : vendor.getCreditScore()) + delta));
        vendor.setCreditScore(after);
        userRepository.save(vendor);

        CreditLog log = new CreditLog();
        log.setVendorId(vendorId);
        log.setEventId(eventId);
        log.setApplicationId(applicationId);
        log.setDelta(delta);
        log.setScoreAfter(after);
        log.setReason(reason);
        creditLogRepository.save(log);
        return after;
    }

    public List<CreditLog> logs(Long vendorId) {
        return creditLogRepository.findByVendorIdOrderByIdDesc(vendorId);
    }
}
