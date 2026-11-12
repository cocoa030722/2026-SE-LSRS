package com.se.lsrs.common.error;

/** Service가 던지는 업무 예외. GlobalExceptionHandler가 { code, message }로 변환한다. */
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.defaultMessage());
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

}
