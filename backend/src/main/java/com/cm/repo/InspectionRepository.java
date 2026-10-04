package com.cm.repo;

import com.cm.domain.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<Inspection> findByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
}
