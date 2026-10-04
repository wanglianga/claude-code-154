package com.cm.repo;

import com.cm.domain.Stall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StallRepository extends JpaRepository<Stall, Long> {
    List<Stall> findByEventIdOrderByCode(Long eventId);
    long countByEventId(Long eventId);
}
