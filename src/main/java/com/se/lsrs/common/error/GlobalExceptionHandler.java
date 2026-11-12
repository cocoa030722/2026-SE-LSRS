package com.se.lsrs.common.error;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 업무 예외와 입력 검증 오류를 { code, message } 응답으로 변환한다 (설계서 4장). */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.status()).body(new ErrorResponse(code.name(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(FieldError::getDefaultMessage)
			.orElse(ErrorCode.INVALID_INPUT.defaultMessage());
		return invalidInput(message);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> handleUnreadable(Exception e) {
		return invalidInput(ErrorCode.INVALID_INPUT.defaultMessage());
	}

	private ResponseEntity<ErrorResponse> invalidInput(String message) {
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.status())
			.body(new ErrorResponse(ErrorCode.INVALID_INPUT.name(), message));
	}

}
