package com.se.lsrs.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.ExamPeriod;
import com.se.lsrs.policy.repository.ExamPeriodRepository;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ExamPeriodServiceTest {

	private static final LocalDate START = LocalDate.of(2026, 12, 14);

	private static final LocalDate END = LocalDate.of(2026, 12, 20);

	@Mock
	private ExamPeriodRepository examPeriodRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CurrentUserProvider currentUserProvider;

	@InjectMocks
	private ExamPeriodService examPeriodService;

	@Test
	void 시험_기간을_등록한_관리자와_함께_저장한다() {
		User admin = mock(User.class);
		given(currentUserProvider.currentUserId()).willReturn(1L);
		given(userRepository.getReferenceById(1L)).willReturn(admin);
		given(examPeriodRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		ExamPeriod saved = examPeriodService.create(START, END);

		assertThat(saved.getStartDate()).isEqualTo(START);
		assertThat(saved.getEndDate()).isEqualTo(END);
	}

	@Test
	void 하루짜리_기간도_허용한다() {
		given(currentUserProvider.currentUserId()).willReturn(1L);
		given(examPeriodRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		assertThat(examPeriodService.create(START, START).getEndDate()).isEqualTo(START);
	}

	@Test
	void 시작일이_종료일보다_늦으면_거부_AC4() {
		assertThatThrownBy(() -> examPeriodService.create(END, START))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_EXAM_PERIOD);
		verify(examPeriodRepository, never()).save(any());
	}

	@Test
	void 기존_기간과_겹치면_거부() {
		given(examPeriodRepository.existsOverlapping(START, END)).willReturn(true);

		assertThatThrownBy(() -> examPeriodService.create(START, END))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXAM_PERIOD_OVERLAP);
		verify(examPeriodRepository, never()).save(any());
	}

	@Test
	void 없는_기간_삭제는_404() {
		given(examPeriodRepository.findById(9L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> examPeriodService.delete(9L))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXAM_PERIOD_NOT_FOUND);
	}

	@Test
	void 기간을_삭제한다() {
		ExamPeriod examPeriod = new ExamPeriod(START, END, null);
		given(examPeriodRepository.findById(1L)).willReturn(Optional.of(examPeriod));

		examPeriodService.delete(1L);

		verify(examPeriodRepository).delete(examPeriod);
	}

}
