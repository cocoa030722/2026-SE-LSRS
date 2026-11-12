/**
 * 아직 구현되지 않은 컴포넌트 자리를 표시하는 스켈레톤 박스.
 * 기능을 구현하면 이 컴포넌트를 실제 UI로 교체한다.
 *
 * @param {{ name: string, story?: string, todos?: string[] }} props
 */
export default function Placeholder({ name, story, todos = [] }) {
  return (
    <section className="placeholder">
      <div className="placeholder-title">
        {name}
        {story && <span className="story-tag">{story}</span>}
      </div>
      {todos.length > 0 && (
        <ul className="placeholder-todos">
          {todos.map((todo) => (
            <li key={todo}>{todo}</li>
          ))}
        </ul>
      )}
    </section>
  );
}
