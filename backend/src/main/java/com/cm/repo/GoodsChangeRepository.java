package com.cm.repo;

import com.cm.domain.GoodsChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoodsChangeRepository extends JpaRepository<GoodsChange, Long> {
    List<GoodsChange> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<GoodsChange> findByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
}
