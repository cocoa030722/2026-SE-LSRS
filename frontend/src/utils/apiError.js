/**
 * 서버 오류 응답 { code, message } (설계서 4장)를 화면 표시용으로 꺼낸다.
 * @returns {{ status: number | null, code: string | null, message: string }}
 */
export function parseApiError(error) {
  const response = error?.response;
  if (!response) {
    return { status: null, code: null, message: '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.' };
  }
  return {
    status: response.status,
    code: response.data?.code ?? null,
    message: response.data?.message ?? '요청을 처리하지 못했습니다.',
  };
}
