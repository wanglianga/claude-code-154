package com.community.market.repository;

import com.community.market.domain.entity.User;
import com.community.market.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRoleOrderByIdAsc(UserRole role);
}
