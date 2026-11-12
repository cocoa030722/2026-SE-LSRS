import axios from 'axios';
import { env } from '../config/env.js';

/**
 * 체크인 단말기 전용 Axios 인스턴스 (US-09, 설계서 DeviceKeyFilter).
 * 사용자 JWT가 아닌 단말기 키(env.deviceKey)로 인증한다.
 *
 * TODO(US-09): 백엔드 DeviceKeyFilter 와 헤더 이름을 맞춘 뒤 단말기 키 헤더를 붙인다.
 */
export const deviceClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});
