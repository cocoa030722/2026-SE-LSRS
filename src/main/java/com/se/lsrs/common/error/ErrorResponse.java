package com.se.lsrs.common.error;

/** 오류 응답 본문 (설계서 4장: code·message 형식으로 통일) */
public record ErrorResponse(String code, String message) {

}
