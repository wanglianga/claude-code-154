package com.community.market.repository;

import com.community.market.domain.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByEventIdOrderByIdDesc(Long eventId);
    List<Incident> findByApplicationIdOrderByIdDesc(Long applicationId);
}
