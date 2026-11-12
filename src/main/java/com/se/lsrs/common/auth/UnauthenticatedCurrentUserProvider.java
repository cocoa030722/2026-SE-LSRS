package com.se.lsrs.common.auth;

import org.springframework.stereotype.Component;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;

/**
 * 인증(US-02)이 구현되기 전의 자리 표시 구현. 항상 미인증(401)으로 응답한다.
 * TODO(US-02): JWT 인증 결과에서 userId를 꺼내는 구현으로 교체한다.
 */
@Component
public class UnauthenticatedCurrentUserProvider implements CurrentUserProvider {

	@Override
	public Long currentUserId() {
		throw new BusinessException(ErrorCode.UNAUTHENTICATED);
	}

}
