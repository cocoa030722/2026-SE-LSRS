package com.se.lsrs.reservation.dto;

import com.se.lsrs.reservation.domain.CancelReason;

import jakarta.validation.constraints.NotNull;

/** 직접 취소 요청 (FR-X-02, US-08 AC3) */
public record CancelReservationRequest(@NotNull(message = "취소 사유를 선택해 주세요.") CancelReason reason) {

}
