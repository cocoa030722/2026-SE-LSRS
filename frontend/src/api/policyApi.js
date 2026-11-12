import { apiClient } from './client.js';

/** 정책 조회 API (설계서 4장, US-05·07) */
export const policyApi = {
  /** 현재 적용 정책·시험 기간 여부 */
  getCurrent: () => apiClient.get('/policies/current'),
};
