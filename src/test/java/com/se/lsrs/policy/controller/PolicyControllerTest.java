package com.se.lsrs.policy.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.ExamPeriod;
import com.se.lsrs.policy.domain.PolicySetting;
import com.se.lsrs.policy.service.AppliedPolicy;
import com.se.lsrs.policy.service.ExamPeriodService;
import com.se.lsrs.policy.service.PolicyService;


@WebMvcTest(controllers = { PolicyController.class, AdminExamPeriodController.class, AdminPolicyController.class })
class PolicyControllerTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PolicyService policyService;

	@MockitoBean
	private ExamPeriodService examPeriodService;

	@Test
	void 현재_정책을_조회한다_AC3() throws Exception {
		given(policyService.currentPolicy())
			.willReturn(new AppliedPolicy(TODAY, true, TODAY, TODAY.plusDays(1), 3));

		mockMvc.perform(get("/api/policies/current"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.examPeriod").value(true))
			.andExpect(jsonPath("$.maxHours").value(3))
			.andExpect(jsonPath("$.reservableFrom").value("2026-12-01"))
			.andExpect(jsonPath("$.reservableUntil").value("2026-12-02"));
	}

	@Test
	void 대상일을_지정해_정책을_조회한다() throws Exception {
		LocalDate target = TODAY.plusDays(3);
		given(policyService.policyFor(target)).willReturn(new AppliedPolicy(target, false, TODAY, TODAY.plusDays(7), 4));

		mockMvc.perform(get("/api/policies/current").param("date", "2026-12-04"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.targetDate").value("2026-12-04"))
			.andExpect(jsonPath("$.maxHours").value(4));
	}

	@Test
	void 시험_기간을_등록한다() throws Exception {
		given(examPeriodService.create(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 20)))
			.willReturn(new ExamPeriod(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 20), null));

		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startDate\":\"2026-12-14\",\"endDate\":\"2026-12-20\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.startDate").value("2026-12-14"))
			.andExpect(jsonPath("$.endDate").value("2026-12-20"));
	}

	@Test
	void 잘못된_기간은_code_message_로_거부된다_AC4() throws Exception {
		given(examPeriodService.create(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 14)))
			.willThrow(new BusinessException(ErrorCode.INVALID_EXAM_PERIOD));

		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startDate\":\"2026-12-20\",\"endDate\":\"2026-12-14\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_EXAM_PERIOD"))
			.andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EXAM_PERIOD.defaultMessage()));
	}

	@Test
	void 날짜가_빠지면_400() throws Exception {
		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startDate\":\"2026-12-14\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
			.andExpect(jsonPath("$.message").value("종료일을 입력해 주세요."));
	}

	@Test
	void 날짜_형식이_틀리면_400() throws Exception {
		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startDate\":\"12/14\",\"endDate\":\"2026-12-20\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 시험_기간_목록과_삭제() throws Exception {
		given(examPeriodService.findAll())
			.willReturn(List.of(new ExamPeriod(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 20), null)));

		mockMvc.perform(get("/api/admin/exam-periods"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].startDate").value("2026-12-14"));

		mockMvc.perform(delete("/api/admin/exam-periods/1")).andExpect(status().isNoContent());

		willThrow(new BusinessException(ErrorCode.EXAM_PERIOD_NOT_FOUND)).given(examPeriodService).delete(9L);
		mockMvc.perform(delete("/api/admin/exam-periods/9"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("EXAM_PERIOD_NOT_FOUND"));
	}

	@Test
	void 정책_파라미터를_조회하고_변경한다_AC5() throws Exception {
		LocalDateTime updatedAt = LocalDateTime.of(2026, 12, 1, 10, 0);
		given(policyService.getSettings()).willReturn(List.of(new PolicySetting("EXAM_MAX_HOURS", "3", updatedAt)));
		given(policyService.updateSetting("EXAM_MAX_HOURS", "2"))
			.willReturn(new PolicySetting("EXAM_MAX_HOURS", "2", updatedAt));

		mockMvc.perform(get("/api/admin/policies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].policyKey").value("EXAM_MAX_HOURS"))
			.andExpect(jsonPath("$[0].policyValue").value("3"));

		mockMvc.perform(put("/api/admin/policies/EXAM_MAX_HOURS").contentType(MediaType.APPLICATION_JSON)
			.content("{\"policyValue\":\"2\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.policyValue").value("2"));
	}

	@Test
	void 빈_정책_값은_400() throws Exception {
		mockMvc.perform(put("/api/admin/policies/EXAM_MAX_HOURS").contentType(MediaType.APPLICATION_JSON)
			.content("{\"policyValue\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

}
