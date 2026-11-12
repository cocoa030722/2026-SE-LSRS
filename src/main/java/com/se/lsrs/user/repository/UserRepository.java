package com.se.lsrs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se.lsrs.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
