package com.community.market.repository;

import com.community.market.domain.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByApplicationIdOrderByRecordedAtDesc(Long applicationId);
    List<PaymentRecord> findByEventIdOrderByIdDesc(Long eventId);
}
