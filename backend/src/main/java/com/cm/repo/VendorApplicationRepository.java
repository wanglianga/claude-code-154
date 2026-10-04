package com.cm.repo;

import com.cm.domain.VendorApplication;
import com.cm.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VendorApplicationRepository extends JpaRepository<VendorApplication, Long> {
    Optional<VendorApplication> findByEventIdAndVendorId(Long eventId, Long vendorId);
    List<VendorApplication> findByEventIdOrderByCreatedAtDesc(Long eventId);
    List<VendorApplication> findByEventIdAndStatus(Long eventId, ApplicationStatus status);
    List<VendorApplication> findByVendorIdOrderByCreatedAtDesc(Long vendorId);
}
