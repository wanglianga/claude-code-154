package com.community.market.repository;

import com.community.market.domain.entity.Application;
import com.community.market.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByEventIdOrderByIdAsc(Long eventId);
    Optional<Application> findByEventIdAndVendorId(Long eventId, Long vendorId);
    Optional<Application> findByCheckinCode(String checkinCode);
    List<Application> findByEventIdAndStatus(Long eventId, ApplicationStatus status);
    List<Application> findByVendorIdOrderByIdDesc(Long vendorId);
    List<Application> findByStallId(Long stallId);
}
