import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import { useAuth } from '../../auth/useAuth.js';
import { homePathFor } from '../../router/RootRedirect.jsx';
import { paths } from '../../router/paths.js';
import { parseApiError } from '../../utils/apiError.js';

/** 로그인 (FR-U-10·11, US-02) */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const user = await login({ loginId: loginId.trim(), password });
      navigate(location.state?.from ?? homePathFor(user.role), { replace: true });
    } catch (e) {
      setError(e.response ? parseApiError(e) : { message: e.message });
    } finally {
      setSubmitting(false);
    }
  };

  // AC2: 이메일 인증 전(PENDING) 계정이면 인증 안내와 인증 화면 링크
  const needsVerification = error?.code === 'EMAIL_NOT_VERIFIED';

  return (
    <>
      <PageHeader title="로그인" stories={['US-02']} />
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          학번 (관리자는 관리자 ID)
          <input value={loginId} onChange={(e) => setLoginId(e.target.value)} autoComplete="username" required />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? '로그인 중…' : '로그인'}
        </button>
        <ErrorMessage error={error} />
        {needsVerification && <Link to={paths.verifyEmail}>이메일 인증하러 가기</Link>}
      </form>
      <nav className="auth-links">
        <Link to={paths.signup}>회원가입</Link>
        <Link to={paths.passwordReset}>비밀번호 재설정</Link>
      </nav>
    </>
  );
}
