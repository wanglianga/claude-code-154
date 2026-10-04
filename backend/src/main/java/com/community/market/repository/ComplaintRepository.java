package com.community.market.repository;

import com.community.market.domain.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByEventIdOrderByIdDesc(Long eventId);
    List<Complaint> findByApplicationIdOrderByIdDesc(Long applicationId);
}
