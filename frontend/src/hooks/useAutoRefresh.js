/**
 * 주기적으로 콜백을 실행하는 훅 자리. 대시보드 30초 자동 갱신(설계서 2장, US-12 AC3)과
 * 좌석 현황 갱신(US-05 AC3)에 쓴다.
 *
 * TODO(US-05, US-12): setInterval 등록·해제 구현
 *
 * @param {() => void} _callback
 * @param {number | null} _intervalMs null이면 비활성
 */
export function useAutoRefresh(_callback, _intervalMs) {}
