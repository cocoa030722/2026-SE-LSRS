import { Link } from 'react-router';
import { paths } from './paths.js';

/**
 * "/" 진입 화면.
 * TODO(US-02): 미인증 → 로그인, ADMIN → 대시보드, STUDENT → 좌석 예약 으로 이동한다.
 */
export default function RootRedirect() {
  return (
    <div className="error-page">
      <h2>도서관 좌석 예약 시스템</h2>
      <Link to={paths.login}>로그인</Link>
    </div>
  );
}
