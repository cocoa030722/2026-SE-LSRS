package com.se.lsrs.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.se.lsrs.user.domain.User;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * 학생 행을 비관적 잠금(SELECT … FOR UPDATE)으로 조회한다. 같은 학생의 동시 예약 요청을 직렬화한다
	 * (설계서 Core Flow 1, FR-R-11). 잠금 순서는 학생 → 좌석으로 고정한다.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.userId = :userId")
	Optional<User> findByIdForUpdate(Long userId);

}
