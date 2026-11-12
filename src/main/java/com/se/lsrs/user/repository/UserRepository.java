package com.se.lsrs.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se.lsrs.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
