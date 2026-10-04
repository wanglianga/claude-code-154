package com.cm.repo;

import com.cm.domain.TrafficRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrafficRecordRepository extends JpaRepository<TrafficRecord, Long> {
    List<TrafficRecord> findByEventIdOrderByCreatedAtAsc(Long eventId);
}
