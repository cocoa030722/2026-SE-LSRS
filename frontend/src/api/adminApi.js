import { apiClient } from './client.js';

/** 관리자 API (설계서 4장, /api/admin/** 는 모두 Admin 권한) */
export const adminApi = {
  // US-12 대시보드
  getDashboard: () => apiClient.get('/admin/dashboard'),

  // US-13 학생·이용 제한 관리
  /** params: { studentNo, seatNo } */
  searchStudents: (params) => apiClient.get('/admin/students', { params }),
  /** body: { until } */
  setRestriction: (studentId, body) => apiClient.put(`/admin/students/${studentId}/restriction`, body),
  releaseRestriction: (studentId) => apiClient.delete(`/admin/students/${studentId}/restriction`),
  resetNoShowCount: (studentId) => apiClient.delete(`/admin/students/${studentId}/no-show-count`),

  // US-14 강제 취소
  /** body: { reason, applyPenalty } */
  forceCancelReservation: (reservationId, body) =>
    apiClient.put(`/admin/reservations/${reservationId}/cancel`, body),

  // US-07 시험 기간·정책 파라미터
  getExamPeriods: () => apiClient.get('/admin/exam-periods'),
  /** body: { startDate, endDate } (yyyy-MM-dd) */
  createExamPeriod: (body) => apiClient.post('/admin/exam-periods', body),
  deleteExamPeriod: (examPeriodId) => apiClient.delete(`/admin/exam-periods/${examPeriodId}`),
  getPolicies: () => apiClient.get('/admin/policies'),
  /** body: { policyValue } */
  updatePolicy: (policyKey, body) => apiClient.put(`/admin/policies/${policyKey}`, body),

  // US-15 사용 통계
  /** params: { from, to } */
  getStatistics: (params) => apiClient.get('/admin/statistics', { params }),

  // US-16 권한 관리
  /** body: { role } */
  updateUserRole: (userId, body) => apiClient.put(`/admin/users/${userId}/role`, body),
};
