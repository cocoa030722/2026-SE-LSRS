package com.se.lsrs.support;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.ResultHandler;

import com.se.lsrs.common.error.BusinessException;

/**
 * 데모(예외 케이스 E1~E3)용 테스트 출력. 검증이 모두 통과한 뒤에 호출해, IntelliJ 결과 창에서 테스트를 누르면
 * 어떤 상황에서 실제로 어떤 응답이 나왔는지 보이게 한다. 검증에는 관여하지 않는다.
 */
public final class DemoLog {

	private static final int MAX_BODY = 300;

	private DemoLog() {
	}

	/** MockMvc 체인 맨 끝에 {@code .andDo(DemoLog.api("E1 ..."))}: 앞의 andExpect 가 모두 통과해야 출력된다. */
	public static ResultHandler api(String scenario) {
		return result -> {
			MockHttpServletRequest request = result.getRequest();
			String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
			byte[] content = request.getContentAsByteArray();
			String requestBody = content == null ? "" : " " + new String(content, StandardCharsets.UTF_8);
			String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
			print(scenario, "요청: " + request.getMethod() + " " + request.getRequestURI() + query + requestBody,
					"응답: " + result.getResponse().getStatus() + " " + shorten(responseBody));
		};
	}

	/** 거부 검증 체인 맨 끝에 {@code .satisfies(e -> DemoLog.rejected("E2 ...", e))}: 실제 오류 코드와 메시지를 출력한다. */
	public static void rejected(String scenario, Throwable e) {
		String code = e instanceof BusinessException be ? be.getErrorCode().name() : e.getClass().getSimpleName();
		print(scenario, "결과: 거부 " + code + " \"" + e.getMessage() + "\"");
	}

	/** 경계 안쪽 통과 검증 뒤에 호출한다. */
	public static void allowed(String scenario) {
		print(scenario, "결과: 통과 (예외 없음)");
	}

	/** 동시 요청 결과(성공 "OK" 또는 오류 코드별 건수)를 출력한다. */
	public static void concurrent(String scenario, Map<String, Long> outcomes, String dbCheck) {
		String counts = new TreeMap<>(outcomes).entrySet()
			.stream()
			.map(entry -> entry.getKey() + " " + entry.getValue() + "건")
			.collect(Collectors.joining(", "));
		print(scenario, "결과: " + counts, "확인: " + dbCheck);
	}

	/** 직전 출력에 덧붙이는 추가 확인 사항 (예: 저장 호출 0회, DB 건수). */
	public static void check(String detail) {
		System.out.println("    확인: " + detail);
	}

	private static void print(String scenario, String... lines) {
		System.out.println("[통과] " + scenario);
		for (String line : lines) {
			System.out.println("    " + line);
		}
	}

	private static String shorten(String body) {
		return body.length() <= MAX_BODY ? body : body.substring(0, MAX_BODY) + "…";
	}

}
