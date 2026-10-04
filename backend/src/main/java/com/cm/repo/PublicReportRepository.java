package com.cm.repo;

import com.cm.domain.PublicReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PublicReportRepository extends JpaRepository<PublicReport, Long> {
    Optional<PublicReport> findByEventId(Long eventId);
}
