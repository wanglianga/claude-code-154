package com.community.market.repository;

import com.community.market.domain.entity.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
    Optional<DonationRecord> findByApplicationId(Long applicationId);
    List<DonationRecord> findByEventIdOrderByIdAsc(Long eventId);
}
