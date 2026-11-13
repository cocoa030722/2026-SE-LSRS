package com.se.lsrs.common.error;

import org.springframework.http.HttpStatus;

/**
 * 업무 오류 코드. 응답은 { code, message } 형식으로 통일한다 (설계서 4장).
 * 400 입력 검증·예약 정책 위반, 401 미인증, 403 이용 제한, 404 대상 없음, 409 좌석·본인 중복·상태 충돌.
 */
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

	// US-07 예약 정책
	INVALID_EXAM_PERIOD(HttpStatus.BAD_REQUEST, "시험 기간의 시작일은 종료일보다 늦을 수 없습니다."),
	EXAM_PERIOD_OVERLAP(HttpStatus.CONFLICT, "기존 시험 기간과 겹칩니다."),
	EXAM_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "시험 기간을 찾을 수 없습니다."),
	POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "정책 항목을 찾을 수 없습니다."),
	INVALID_POLICY_VALUE(HttpStatus.BAD_REQUEST, "정책 값이 올바르지 않습니다."),
	RESERVATION_DATE_OUT_OF_POLICY(HttpStatus.BAD_REQUEST, "예약 가능 기간을 벗어난 날짜입니다."),
	RESERVATION_DURATION_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 이용 시간을 초과했습니다."),

	// US-05·06 좌석 조회·예약 생성
	INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "예약 시각이 올바르지 않습니다."),
	USER_RESTRICTED(HttpStatus.FORBIDDEN, "이용 제한 중이라 예약할 수 없습니다."),
	SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
	SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."),
	DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "같은 시간대에 이미 예약이 있습니다."),

	// US-08 내 예약 조회·취소
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
	RESERVATION_NOT_CANCELLABLE(HttpStatus.CONFLICT, "취소할 수 없는 예약입니다.");

	private final HttpStatus status;

	private final String defaultMessage;

	ErrorCode(HttpStatus status, String defaultMessage) {
		this.status = status;
		this.defaultMessage = defaultMessage;
	}

	public HttpStatus status() {
		return status;
	}

	public String defaultMessage() {
		return defaultMessage;
	}

}
