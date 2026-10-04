package com.community.market.repository;

import com.community.market.domain.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<Inspection> findByEventIdOrderByIdDesc(Long eventId);
}
