package com.se.lsrs.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 회원 (설계서 3장 User). 테이블 매핑만 둔다.
 * 생성(US-01)·노쇼 반영 addNoShow()/해제 release()(US-11) 등 동작은 각 담당이 추가한다.
 */
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@Column(nullable = false)
	private int noShowCount;

	/** NULL = 제한 없음 */
	private LocalDateTime restrictedUntil;

	@Column(nullable = false)
	private int tokenVersion;

	protected User() {
	}

	public Long getUserId() {
		return userId;
	}

	public String getLoginId() {
		return loginId;
	}

	public Role getRole() {
		return role;
	}

	public UserStatus getStatus() {
		return status;
	}

}
