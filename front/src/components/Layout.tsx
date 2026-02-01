import { useState, useEffect } from 'react'
import { Outlet, useNavigate, useParams } from 'react-router-dom'
import { categoryApi } from '../api/categoryApi'
import type { CategoryWithChildren } from '../types'
import Sidebar from './Sidebar'

export default function Layout() {
  const [categories, setCategories] = useState<CategoryWithChildren[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()
  const { categoryId } = useParams()

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.getTree()
      setCategories(data)
    } catch (error) {
      console.error('카테고리 로딩 실패:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCategorySelect = (category: CategoryWithChildren) => {
    // leaf 카테고리(children이 없는 것)만 게시글 목록으로 이동
    if (!category.children || category.children.length === 0) {
      navigate(`/categories/${category.id}/posts`)
    }
  }

  const handleCategoryCreate = async (parentId: number | null, name: string) => {
    try {
      await categoryApi.create({ parentId, name, sortOrder: 0 })
      await loadCategories()
    } catch (error) {
      alert(error instanceof Error ? error.message : '카테고리 생성 실패')
    }
  }

  const handleCategoryDelete = async (id: number) => {
    if (!confirm('카테고리를 삭제하시겠습니까?')) return
    try {
      await categoryApi.delete(id)
      await loadCategories()
    } catch (error) {
      alert(error instanceof Error ? error.message : '카테고리 삭제 실패')
    }
  }

  return (
    <div className="layout">
      <Sidebar
        categories={categories}
        loading={loading}
        selectedId={categoryId ? Number(categoryId) : undefined}
        onSelect={handleCategorySelect}
        onCreate={handleCategoryCreate}
        onDelete={handleCategoryDelete}
      />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
