/**
 * 화면 제목과 관련 User Story를 표시한다.
 * @param {{ title: string, stories?: string[], children?: React.ReactNode }} props
 */
export default function PageHeader({ title, stories = [], children }) {
  return (
    <div className="page-header">
      <h2>{title}</h2>
      <div className="story-tags">
        {stories.map((id) => (
          <span key={id} className="story-tag">
            {id}
          </span>
        ))}
      </div>
      {children}
    </div>
  );
}
