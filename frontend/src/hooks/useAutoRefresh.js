import { useEffect, useRef } from 'react';

/**
 * 주기적으로 콜백을 실행한다. 대시보드 30초 자동 갱신(설계서 2장, US-12 AC3)과
 * 좌석 현황 갱신(US-05 AC3)에 쓴다. 콜백이 바뀌어도 타이머를 다시 만들지 않는다.
 *
 * @param {() => void} callback
 * @param {number | null} intervalMs null이면 비활성
 */
export function useAutoRefresh(callback, intervalMs) {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    if (intervalMs == null) return undefined;
    const id = setInterval(() => callbackRef.current(), intervalMs);
    return () => clearInterval(id);
  }, [intervalMs]);
}
