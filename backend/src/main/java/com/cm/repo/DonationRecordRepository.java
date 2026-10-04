package com.cm.repo;

import com.cm.domain.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
    List<DonationRecord> findByEventId(Long eventId);
    Optional<DonationRecord> findByAssignmentId(Long assignmentId);
}
