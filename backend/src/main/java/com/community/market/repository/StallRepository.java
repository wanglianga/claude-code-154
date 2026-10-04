package com.community.market.repository;

import com.community.market.domain.entity.Stall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StallRepository extends JpaRepository<Stall, Long> {
    List<Stall> findByEventIdOrderByCodeAsc(Long eventId);
    List<Stall> findByEventIdAndStatus(Long eventId, String status);
}
