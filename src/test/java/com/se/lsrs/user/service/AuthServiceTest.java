package com.se.lsrs.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.user.domain.Role;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.domain.UserStatus;
import com.se.lsrs.user.repository.UserRepository;
import com.se.lsrs.user.repository.VerificationTokenRepository;
import com.se.lsrs.user.security.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final String GOOD_PASSWORD = "Passw0rd!";

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Mock
	private UserRepository userRepository;

	@Mock
	private VerificationTokenRepository verificationTokenRepository;

	@Mock
	private JwtProvider jwtProvider;

	private AuthService authService() {
		return new AuthService(userRepository, verificationTokenRepository, encoder, jwtProvider);
	}

	private User user(UserStatus status) {
		return new User("20260001", "s1@univ.ac.kr", encoder.encode(GOOD_PASSWORD), Role.STUDENT, status);
	}

	@Test
	void 규칙을_충족한_비밀번호는_통과한다_US01_AC3() {
		assertThatCode(() -> AuthService.validatePassword("Passw0rd!")).doesNotThrowAnyException();
	}

	@Test
	void 비밀번호가_규칙을_어기면_보완할_항목을_안내한다_US01_AC3() {
		assertThatThrownBy(() -> AuthService.validatePassword("1234"))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("8자 이상")
			.hasMessageContaining("영문")
			.hasMessageContaining("특수문자")
			.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
		assertThatThrownBy(() -> AuthService.validatePassword("password1"))
			.hasMessageContaining("보완할 항목: 특수문자");
		assertThatThrownBy(() -> AuthService.validatePassword("Pass!word"))
			.hasMessageContaining("보완할 항목: 숫자");
		assertThatThrownBy(() -> AuthService.validatePassword("12345678!"))
			.hasMessageContaining("보완할 항목: 영문");
		assertThatThrownBy(() -> AuthService.validatePassword("Pw0!"))
			.hasMessageContaining("보완할 항목: 8자 이상");
	}

	@Test
	void 규칙을_어긴_비밀번호는_가입되지_않는다_US01_AC3() {
		assertThatThrownBy(() -> authService().signup("20260009", "s9@univ.ac.kr", "1234"))
			.isInstanceOf(BusinessException.class);
		verify(userRepository, never()).save(any());
	}

	@Test
	void 가입하면_PENDING으로_저장되고_비밀번호는_해시된다_US01_AC1() {
		authService().signup("20260009", "s9@univ.ac.kr", GOOD_PASSWORD);

		org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.PENDING);
		assertThat(captor.getValue().getPasswordHash()).isNotEqualTo(GOOD_PASSWORD).startsWith("$2");
	}

	@Test
	void 비밀번호가_틀리면_계정_상태와_무관하게_같은_메시지다_US02_AC2() {
		given(userRepository.findByLoginId("20260001")).willReturn(Optional.of(user(UserStatus.PENDING)));
		given(userRepository.findByLoginId("nobody")).willReturn(Optional.empty());

		BusinessException pendingWrongPw = catchBusiness(() -> authService().login("20260001", "wrong"));
		BusinessException noSuchUser = catchBusiness(() -> authService().login("nobody", "wrong"));

		assertThat(pendingWrongPw.getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED);
		assertThat(pendingWrongPw.getMessage()).isEqualTo(noSuchUser.getMessage());
		assertThat(noSuchUser.getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED);
	}

	@Test
	void 올바른_비밀번호의_PENDING_계정은_이메일_인증_안내를_받는다_US02_AC2() {
		given(userRepository.findByLoginId("20260001")).willReturn(Optional.of(user(UserStatus.PENDING)));

		BusinessException e = catchBusiness(() -> authService().login("20260001", GOOD_PASSWORD));

		assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
		assertThat(e.getMessage()).contains("이메일 인증");
	}

	@Test
	void ACTIVE_계정은_토큰을_발급받는다_US02_AC1() {
		User active = user(UserStatus.ACTIVE);
		given(userRepository.findByLoginId("20260001")).willReturn(Optional.of(active));
		given(jwtProvider.generateAccessToken(active)).willReturn("token");

		assertThat(authService().login("20260001", GOOD_PASSWORD)).isEqualTo("token");
	}

	private static BusinessException catchBusiness(Runnable runnable) {
		return (BusinessException) org.assertj.core.api.Assertions.catchThrowable(runnable::run);
	}

}
