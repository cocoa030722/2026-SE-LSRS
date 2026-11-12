package com.se.lsrs.policy.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * 정책 파라미터 키 (설계서 3장 PolicySetting). 값은 DB(policy_setting)에서 읽는다 (NFR 4.1).
 * 기본값은 data.sql 에 둔다. 다른 기능의 키(NOSHOW_THRESHOLD 등)는 각 담당이 추가한다.
 */
public enum PolicyKey {

	/** 평상시 예약 가능 기간: 오늘 ~ +N일 (FR-A-20) */
	NORMAL_DAYS(0),
	/** 평상시 최대 이용 시간(시간) (FR-A-20) */
	NORMAL_MAX_HOURS(1),
	/** 시험 기간 예약 가능 기간: 오늘 ~ +N일 (FR-A-20) */
	EXAM_DAYS(0),
	/** 시험 기간 최대 이용 시간(시간) (FR-A-20) */
	EXAM_MAX_HOURS(1),
	/** 체크인 가능 시간(분). 이 시간 안의 RESERVED('체크인 대기')까지 학생이 직접 취소할 수 있다 (FR-C-02, FR-X-01) */
	CHECKIN_WINDOW_MIN(1);

	private final int minValue;

	PolicyKey(int minValue) {
		this.minValue = minValue;
	}

	public int minValue() {
		return minValue;
	}

	public static Optional<PolicyKey> find(String name) {
		return Arrays.stream(values()).filter(key -> key.name().equals(name)).findFirst();
	}

}
