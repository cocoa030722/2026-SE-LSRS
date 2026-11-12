import { Link } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import { paths } from '../../router/paths.js';

/** 로그인 (FR-U-10·11, US-02) */
export default function LoginPage() {
  // TODO(US-02 AC1): useAuth().login → 성공 시 location.state.from 또는 역할별 첫 화면으로 이동
  return (
    <>
      <PageHeader title="로그인" stories={['US-02']} />
      <Placeholder
        name="LoginForm"
        story="US-02"
        todos={[
          '학번(관리자 ID)·비밀번호 입력 (AC1)',
          'ID·비밀번호 오류는 계정 존재 여부와 무관한 동일 메시지 (AC2)',
          'PENDING 계정이면 이메일 인증 필요 안내 + 인증 화면 링크 (AC2)',
        ]}
      />
      <nav className="auth-links">
        <Link to={paths.signup}>회원가입</Link>
        <Link to={paths.passwordReset}>비밀번호 재설정</Link>
      </nav>
    </>
  );
}
