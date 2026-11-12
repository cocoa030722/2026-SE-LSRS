package com.se.lsrs.common.auth;

/**
 * 요청을 보낸 로그인 사용자의 식별자를 제공한다.
 * 실제 구현(JWT 인증 결과에서 꺼내기)은 US-02 담당이 제공한다.
 */
public interface CurrentUserProvider {

	/**
	 * @return 로그인 사용자의 userId
	 * @throws com.se.lsrs.common.error.BusinessException 인증되지 않은 요청이면 UNAUTHENTICATED
	 */
	Long currentUserId();

}
