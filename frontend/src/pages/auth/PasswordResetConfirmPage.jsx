import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';

/** 메일 링크로 진입하는 새 비밀번호 설정 (US-03, Sprint 2) */
export default function PasswordResetConfirmPage() {
  // TODO(US-03): 링크의 토큰 파라미터 이름은 백엔드 메일 템플릿과 맞춘다.
  return (
    <>
      <PageHeader title="새 비밀번호 설정" stories={['US-03']} />
      <Placeholder
        name="PasswordResetConfirmForm"
        story="US-03"
        todos={['새 비밀번호 입력 (규칙 검증 안내)']}
      />
    </>
  );
}
