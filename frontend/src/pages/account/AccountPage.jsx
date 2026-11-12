import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import { useAuth } from '../../auth/useAuth.js';
import { Role } from '../../constants/roles.js';

/** 내 계정: 비밀번호 변경(US-03, 공통) · 회원 탈퇴(US-04, 학생) */
export default function AccountPage() {
  const { user } = useAuth();

  return (
    <>
      <PageHeader title="내 계정" stories={['US-03', 'US-04']} />
      <Placeholder
        name="ChangePasswordForm"
        story="US-03"
        todos={[
          '기존·새 비밀번호 입력 → 변경 후 즉시 로그아웃 (AC1)',
          '기존 비밀번호 오류·규칙 미충족 사유 안내 (AC2)',
        ]}
      />
      {user?.role === Role.STUDENT && (
        <Placeholder
          name="WithdrawSection"
          story="US-04"
          todos={[
            '탈퇴 확인 후 userApi.withdraw → 로그아웃 (AC1)',
            '진행 중 예약·이용 제한 시 거부 안내 (AC2)',
          ]}
        />
      )}
    </>
  );
}
