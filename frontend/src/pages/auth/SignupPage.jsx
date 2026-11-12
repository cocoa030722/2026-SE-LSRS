import { Link } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import { paths } from '../../router/paths.js';

/** 회원가입 (FR-U-01·05·06, US-01) */
export default function SignupPage() {
  // TODO(US-01 AC1): authApi.signup → 성공 시 이메일 인증 화면으로 이동
  return (
    <>
      <PageHeader title="회원가입" stories={['US-01']} />
      <Placeholder
        name="SignupForm"
        story="US-01"
        todos={[
          '학번·이메일·비밀번호 입력 (AC1)',
          '비밀번호 규칙(8자 이상, 영문·숫자·특수문자) 미충족 항목 안내 (AC3)',
          '학번·이메일 중복 안내 (AC4)',
        ]}
      />
      <nav className="auth-links">
        <Link to={paths.login}>로그인으로</Link>
      </nav>
    </>
  );
}
