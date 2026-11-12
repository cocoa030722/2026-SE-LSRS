import Placeholder from '../common/Placeholder.jsx';

/**
 * 시험 기간 등록·조회·삭제 (FR-A-20, US-07 AC1·4).
 *
 * @param {{
 *   examPeriods: Array<{ examPeriodId: number, startDate: string, endDate: string }>,
 *   onCreate: (body: { startDate: string, endDate: string }) => void,
 *   onDelete: (examPeriodId: number) => void,
 * }} props
 */
export default function ExamPeriodManager({ examPeriods }) {
  return (
    <Placeholder
      name="ExamPeriodManager"
      story="US-07"
      todos={[
        `등록된 시험 기간 ${examPeriods?.length ?? 0}건 목록·삭제`,
        '시작일·종료일 입력 후 저장 (시작일 > 종료일이면 거부 안내, AC4)',
      ]}
    />
  );
}
