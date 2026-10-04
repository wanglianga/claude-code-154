package com.cm.repo;

import com.cm.domain.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {
    List<SalesRecord> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<SalesRecord> findByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
}
