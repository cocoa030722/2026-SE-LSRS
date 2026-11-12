/**
 * 토큰 저장소. 저장 위치를 이 모듈 뒤로 숨겨 다른 코드가 저장 방식에 의존하지 않게 한다.
 *
 * TODO(US-02): 저장 위치(localStorage / 메모리 등)와 Refresh 토큰 보관 방식을 정해 구현한다.
 */
export const tokenStorage = {
  /** @returns {string | null} */
  getAccessToken: () => null,
  /** @returns {string | null} */
  getRefreshToken: () => null,
  /** @param {{ accessToken?: string, refreshToken?: string }} _tokens */
  save(_tokens) {},
  clear() {},
};
