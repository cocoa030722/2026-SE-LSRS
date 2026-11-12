/**
 * 서버 오류 응답 { code, message } (설계서 4장)를 화면 표시용으로 꺼내는 함수 자리.
 *
 * TODO: error.response.data 의 code·message 추출, 응답 본문이 없는 네트워크 오류 처리
 *
 * @returns {{ status: number | null, code: string | null, message: string }}
 */
export function parseApiError(_error) {
  return { status: null, code: null, message: '' };
}
