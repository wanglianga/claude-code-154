package com.community.market.repository;

import com.community.market.domain.entity.Publicity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicityRepository extends JpaRepository<Publicity, Long> {
    Optional<Publicity> findByEventId(Long eventId);
}
