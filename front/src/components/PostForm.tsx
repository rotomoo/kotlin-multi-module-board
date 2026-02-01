import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { postApi } from '../api/postApi'
import { categoryApi } from '../api/categoryApi'
import type { Category } from '../types'

// 임시: 로그인 기능 구현 전까지 기본 작성자 사용
const DEFAULT_AUTHOR = '익명'

export default function PostForm() {
  const { categoryId, id } = useParams<{ categoryId?: string; id?: string }>()
  const navigate = useNavigate()
  const isEdit = !!id

  const [category, setCategory] = useState<Category | null>(null)
  const [author, setAuthor] = useState(DEFAULT_AUTHOR)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [postCategoryId, setPostCategoryId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    loadData()
  }, [categoryId, id])

  const loadData = async () => {
    try {
      if (isEdit && id) {
        // 수정 모드: 게시글 데이터 로드
        const post = await postApi.getById(Number(id))
        setAuthor(post.author)
        setTitle(post.title)
        setContent(post.content)
        setPostCategoryId(post.category.id)
        // 카테고리 정보도 로드
        const cat = await categoryApi.getById(post.category.id)
        setCategory(cat)
      } else if (categoryId) {
        // 새 글 작성 모드: 카테고리 정보 로드
        const cat = await categoryApi.getById(Number(categoryId))
        setCategory(cat)
        setPostCategoryId(Number(categoryId))
      }
    } catch (error) {
      console.error('데이터 로딩 실패:', error)
      alert('데이터를 불러올 수 없습니다.')
      navigate(-1)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!title.trim() || !content.trim()) {
      alert('제목과 내용을 입력해주세요.')
      return
    }

    setSubmitting(true)
    try {
      if (isEdit) {
        await postApi.update(Number(id), { title, content })
        navigate(`/posts/${id}`)
      } else {
        const newPost = await postApi.create({
          author: author.trim() || DEFAULT_AUTHOR,
          categoryId: postCategoryId!,
          title,
          content,
          status: 'PUBLISHED'
        })
        navigate(`/posts/${newPost.id}`)
      }
    } catch (error) {
      alert(error instanceof Error ? error.message : '저장 실패')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCancel = () => {
    if (isEdit) {
      navigate(`/posts/${id}`)
    } else {
      navigate(`/categories/${categoryId}/posts`)
    }
  }

  if (loading) {
    return <div className="loading-container">로딩중...</div>
  }

  return (
    <div className="post-form-container">
      <h1>{isEdit ? '게시글 수정' : '새 게시글 작성'}</h1>
      {category && (
        <div className="category-badge">
          📁 {category.name}
        </div>
      )}

      <form onSubmit={handleSubmit} className="post-form">
        {!isEdit && (
          <div className="form-group">
            <label htmlFor="author">작성자</label>
            <input
              id="author"
              type="text"
              value={author}
              onChange={(e) => setAuthor(e.target.value)}
              placeholder="작성자명을 입력하세요"
              maxLength={50}
            />
          </div>
        )}

        <div className="form-group">
          <label htmlFor="title">제목</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="제목을 입력하세요"
            maxLength={200}
            required
          />
          <span className="char-count">{title.length}/200</span>
        </div>

        <div className="form-group">
          <label htmlFor="content">내용</label>
          <textarea
            id="content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="내용을 입력하세요"
            rows={15}
            required
          />
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn"
            onClick={handleCancel}
            disabled={submitting}
          >
            취소
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? '저장중...' : (isEdit ? '수정' : '등록')}
          </button>
        </div>
      </form>
    </div>
  )
}
