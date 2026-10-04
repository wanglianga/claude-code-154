package com.community.market.repository;

import com.community.market.domain.entity.CreditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditLogRepository extends JpaRepository<CreditLog, Long> {
    List<CreditLog> findByVendorIdOrderByIdDesc(Long vendorId);
}
