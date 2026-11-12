import { Link } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import { paths } from '../../router/paths.js';

/** 비밀번호 재설정 메일 요청 (US-03, Sprint 2) */
export default function PasswordResetRequestPage() {
  return (
    <>
      <PageHeader title="비밀번호 재설정" stories={['US-03']} />
      <Placeholder
        name="PasswordResetRequestForm"
        story="US-03"
        todos={[
          '가입 이메일 입력 → 재설정 링크 발송 (AC3)',
          '가입 여부와 무관하게 동일 안내 메시지 (AC4)',
        ]}
      />
      <nav className="auth-links">
        <Link to={paths.login}>로그인으로</Link>
      </nav>
    </>
  );
}
