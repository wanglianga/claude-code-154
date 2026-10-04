package com.cm.repo;

import com.cm.domain.StallAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StallAssignmentRepository extends JpaRepository<StallAssignment, Long> {
    List<StallAssignment> findByEventId(Long eventId);
    Optional<StallAssignment> findByApplicationId(Long applicationId);
    Optional<StallAssignment> findByStallId(Long stallId);
    Optional<StallAssignment> findByCheckinCode(String checkinCode);
    long countByEventId(Long eventId);
    long countByEventIdAndCheckedInTrue(Long eventId);
    long countByEventIdAndEarlyLeaveTrue(Long eventId);
}
