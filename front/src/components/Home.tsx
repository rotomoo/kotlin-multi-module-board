export default function Home() {
  return (
    <div className="home-container">
      <div className="welcome-box">
        <h1>게시판 시스템</h1>
        <p>왼쪽 사이드바에서 카테고리를 선택하여 게시글을 확인하세요.</p>
        <ul className="guide-list">
          <li>📁 <strong>상위 카테고리</strong>를 클릭하면 하위 카테고리가 펼쳐집니다.</li>
          <li>📄 <strong>하위 카테고리</strong>를 클릭하면 해당 카테고리의 게시글 목록을 볼 수 있습니다.</li>
          <li><strong>+</strong> 버튼으로 새 카테고리를 추가할 수 있습니다.</li>
        </ul>
      </div>

      <div className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">📂</div>
          <h3>계층형 카테고리</h3>
          <p>무한 depth 카테고리로 체계적인 분류</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">📝</div>
          <h3>게시글 작성</h3>
          <p>제목, 내용으로 간편하게 작성</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">🔍</div>
          <h3>검색 기능</h3>
          <p>제목, 작성자로 검색 가능</p>
        </div>
      </div>
    </div>
  )
}
