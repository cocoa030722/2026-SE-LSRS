import Placeholder from '../common/Placeholder.jsx';

/**
 * 학생 검색 (FR-A-10, US-13 AC1). 학번 또는 좌석 번호로 검색한다.
 * @param {{ onSearch: (params: { studentNo?: string, seatNo?: string }) => void }} props
 */
export default function StudentSearchForm() {
  return (
    <Placeholder
      name="StudentSearchForm"
      story="US-13"
      todos={['학번 / 좌석 번호 입력 → onSearch 호출']}
    />
  );
}
