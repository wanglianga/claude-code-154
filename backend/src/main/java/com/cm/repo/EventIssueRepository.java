package com.cm.repo;

import com.cm.domain.EventIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventIssueRepository extends JpaRepository<EventIssue, Long> {
    List<EventIssue> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<EventIssue> findByAssignmentId(Long assignmentId);
}
