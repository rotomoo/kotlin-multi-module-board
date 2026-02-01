import { useEffect, useState, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { postApi } from '../api/postApi'
import type { Post } from '../types'

export default function PostDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [post, setPost] = useState<Post | null>(null)
  const [loading, setLoading] = useState(true)
  const fetchedIdRef = useRef<string | null>(null)

  useEffect(() => {
    if (id && fetchedIdRef.current !== id) {
      fetchedIdRef.current = id
      loadPost()
    }
  }, [id])

  const loadPost = async () => {
    try {
      const data = await postApi.getById(Number(id))
      setPost(data)
    } catch (error) {
      console.error('게시글 로딩 실패:', error)
      alert('게시글을 찾을 수 없습니다.')
      navigate(-1)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!confirm('게시글을 삭제하시겠습니까?')) return
    try {
      await postApi.delete(Number(id))
      navigate(`/categories/${post?.category.id}/posts`)
    } catch (error) {
      alert(error instanceof Error ? error.message : '삭제 실패')
    }
  }

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  if (loading) {
    return <div className="loading-container">로딩중...</div>
  }

  if (!post) {
    return <div className="error-container">게시글을 찾을 수 없습니다.</div>
  }

  return (
    <div className="post-detail-container">
      <article className="post-article">
        <header className="post-header">
          <h1 className="post-title">{post.title}</h1>
          <div className="post-meta">
            <span className="category">{post.category.name}</span>
            <span className="divider">|</span>
            <span className="author">{post.author}</span>
            <span className="divider">|</span>
            <span className="date">{formatDate(post.createdAt)}</span>
            <span className="divider">|</span>
            <span className="views">조회 {post.viewCount || 0}</span>
            {post.createdAt !== post.updatedAt && (
              <>
                <span className="divider">|</span>
                <span className="updated">수정됨 {formatDate(post.updatedAt)}</span>
              </>
            )}
          </div>
        </header>

        <div className="post-content">
          {post.content.split('\n').map((line, i) => (
            <p key={i}>{line || '\u00A0'}</p>
          ))}
        </div>
      </article>

      <div className="post-actions">
        <button
          className="btn"
          onClick={() => navigate(`/categories/${post.category.id}/posts`)}
        >
          목록
        </button>
        <div className="right-actions">
          <button
            className="btn"
            onClick={() => navigate(`/posts/${post.id}/edit`)}
          >
            수정
          </button>
          <button
            className="btn btn-danger"
            onClick={handleDelete}
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  )
}
