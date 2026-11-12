import { apiClient } from './client.js';

/** 정책 조회 API (설계서 4장, US-05·07) */
export const policyApi = {
  /**
   * 적용 정책·시험 기간 여부.
   * @param {string} [date] 예약 대상일 yyyy-MM-dd (생략 시 오늘)
   * @returns {Promise<{ data: { targetDate, examPeriod, reservableFrom, reservableUntil, maxHours } }>}
   */
  getCurrent: (date) => apiClient.get('/policies/current', { params: date ? { date } : undefined }),
};
