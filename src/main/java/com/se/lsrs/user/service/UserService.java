package com.se.lsrs.user.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.repository.ReservationRepository;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.repository.UserRepository;

@Service
public class UserService {

    private static final List<ReservationStatus> IN_PROGRESS =
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_IN);

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public UserService(UserRepository userRepository,
                       ReservationRepository reservationRepository,
                       Clock clock) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /**
     * 회원 탈퇴 (US-04). 진행 중 예약(RESERVED·CHECKED_IN)이 있거나 이용 제한(패널티) 중이면 거부한다 (AC2).
     * 같은 학생의 동시 예약과 겹치지 않도록 학생 행을 잠근 뒤 검사한다 (예약 생성과 같은 잠금 대상).
     */
    @Transactional
    public void withdraw(String loginId) {
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED))
                .getUserId();
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));

        if (user.isRestricted(LocalDateTime.now(clock))
                || reservationRepository.existsByUserUserIdAndStatusIn(userId, IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.WITHDRAW_NOT_ALLOWED);
        }

        user.withdraw();
    }
}
