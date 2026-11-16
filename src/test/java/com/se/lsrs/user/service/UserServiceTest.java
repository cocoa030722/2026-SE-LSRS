package com.se.lsrs.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.reservation.repository.ReservationRepository;
import com.se.lsrs.user.domain.Role;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.domain.UserStatus;
import com.se.lsrs.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final ZoneId ZONE = ZoneId.systemDefault();

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 10, 0);

	@Mock
	private UserRepository userRepository;

	@Mock
	private ReservationRepository reservationRepository;

	private UserService service() {
		return new UserService(userRepository, reservationRepository,
				Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
	}

	private User studentWithId() {
		User user = new User("20260004", "s4@univ.ac.kr", "hash", Role.STUDENT, UserStatus.ACTIVE);
		ReflectionTestUtils.setField(user, "userId", 4L);
		given(userRepository.findByLoginId("20260004")).willReturn(Optional.of(user));
		given(userRepository.findByIdForUpdate(4L)).willReturn(Optional.of(user));
		return user;
	}

	@Test
	void 진행_중_예약과_패널티가_없으면_탈퇴하고_개인정보가_비식별화된다_AC1() {
		User user = studentWithId();
		given(reservationRepository.existsByUserUserIdAndStatusIn(eq(4L), any())).willReturn(false);

		service().withdraw("20260004");

		assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
		assertThat(user.getLoginId()).isEqualTo("WITHDRAWN_4");
		assertThat(user.getEmail()).isEqualTo("WITHDRAWN_4@deleted.com");
		assertThat(user.getTokenVersion()).isEqualTo(2);
	}

	@Test
	void 진행_중_예약이_있으면_탈퇴할_수_없고_계정은_유지된다_AC2() {
		User user = studentWithId();
		given(reservationRepository.existsByUserUserIdAndStatusIn(eq(4L), any())).willReturn(true);

		assertThatThrownBy(() -> service().withdraw("20260004")).isInstanceOf(BusinessException.class)
			.hasMessage("진행 중인 예약 또는 패널티가 존재하여 탈퇴할 수 없습니다.")
			.satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.WITHDRAW_NOT_ALLOWED));
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(user.getLoginId()).isEqualTo("20260004");
	}

	@Test
	void 이용_제한_중이면_탈퇴할_수_없다_AC2() {
		User user = studentWithId();
		ReflectionTestUtils.setField(user, "restrictedUntil", NOW.plusDays(3));

		assertThatThrownBy(() -> service().withdraw("20260004")).isInstanceOf(BusinessException.class)
			.hasMessageContaining("패널티");
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}

}
