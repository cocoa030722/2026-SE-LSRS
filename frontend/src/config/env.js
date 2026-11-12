/** 빌드 시점 환경 변수를 한곳에서 읽는다. (.env.example 참고) */
export const env = {
  apiBaseUrl: '/api',
  deviceKey: import.meta.env.VITE_DEVICE_KEY ?? '',
  devMockRole: import.meta.env.DEV ? import.meta.env.VITE_DEV_MOCK_ROLE || null : null,
};
