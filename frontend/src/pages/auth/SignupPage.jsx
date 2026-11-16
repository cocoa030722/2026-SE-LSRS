import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import { authApi } from '../../api/authApi.js';
import { paths } from '../../router/paths.js';
import { parseApiError } from '../../utils/apiError.js';

/** 회원가입 (FR-U-01·05·06, US-01). 비밀번호 규칙·중복 검사는 서버가 하고 사유를 그대로 보여 준다 (AC3·4). */
export default function SignupPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ loginId: '', email: '', password: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const change = (field) => (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const response = await authApi.signup({
        loginId: form.loginId.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      navigate(paths.verifyEmail, { state: { notice: response.data } });
    } catch (e) {
      setError(parseApiError(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="회원가입" stories={['US-01']} />
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          학번
          <input value={form.loginId} onChange={change('loginId')} autoComplete="username" required />
        </label>
        <label>
          학교 이메일
          <input type="email" value={form.email} onChange={change('email')} autoComplete="email" required />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={form.password}
            onChange={change('password')}
            autoComplete="new-password"
            required
          />
        </label>
        <p className="hint">8자 이상, 영문·숫자·특수문자를 모두 포함해야 합니다.</p>
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? '가입 중…' : '가입하기'}
        </button>
        <ErrorMessage error={error} />
      </form>
      <nav className="auth-links">
        <Link to={paths.login}>로그인으로</Link>
      </nav>
    </>
  );
}
