import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { postApi } from '../api/postApi'
import { categoryApi } from '../api/categoryApi'
import type { PostListItem, Category } from '../types'

export default function PostList() {
  const { categoryId } = useParams<{ categoryId: string }>()
  const navigate = useNavigate()
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [category, setCategory] = useState<Category | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchKeyword, setSearchKeyword] = useState('')

  useEffect(() => {
    if (categoryId) {
      loadData()
    }
  }, [categoryId])

  const loadData = async () => {
    setLoading(true)
    try {
      const [categoryData, postsData] = await Promise.all([
        categoryApi.getById(Number(categoryId)),
        postApi.getByCategoryId(Number(categoryId))
      ])
      setCategory(categoryData)
      setPosts(postsData)
    } catch (error) {
      console.error('데이터 로딩 실패:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()

    if (isToday) {
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    }
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }

  const filteredPosts = posts.filter(post =>
    post.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
    post.author.toLowerCase().includes(searchKeyword.toLowerCase())
  )

  if (loading) {
    return <div className="loading-container">로딩중...</div>
  }

  return (
    <div className="post-list-container">
      <div className="post-list-header">
        <div className="category-info">
          <h1>{category?.name}</h1>
          {category?.description && <p>{category.description}</p>}
        </div>
        <button
          className="btn btn-primary"
          onClick={() => navigate(`/categories/${categoryId}/posts/new`)}
        >
          글쓰기
        </button>
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="제목 또는 작성자 검색..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
      </div>

      <table className="post-table">
        <thead>
          <tr>
            <th className="col-id">번호</th>
            <th className="col-title">제목</th>
            <th className="col-author">작성자</th>
            <th className="col-date">작성일</th>
            <th className="col-views">조회</th>
          </tr>
        </thead>
        <tbody>
          {filteredPosts.length === 0 ? (
            <tr>
              <td colSpan={5} className="empty-row">
                {searchKeyword ? '검색 결과가 없습니다.' : '게시글이 없습니다.'}
              </td>
            </tr>
          ) : (
            filteredPosts.map(post => (
              <tr
                key={post.id}
                onClick={() => navigate(`/posts/${post.id}`)}
                className="clickable"
              >
                <td className="col-id">{post.id}</td>
                <td className="col-title">{post.title}</td>
                <td className="col-author">{post.author}</td>
                <td className="col-date">{formatDate(post.createdAt)}</td>
                <td className="col-views">{post.viewCount || 0}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      <div className="post-count">
        총 {filteredPosts.length}개의 게시글
      </div>
    </div>
  )
}
