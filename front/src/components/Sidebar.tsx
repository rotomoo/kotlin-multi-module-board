import { useState } from 'react'
import type { CategoryWithChildren } from '../types'

interface SidebarProps {
  categories: CategoryWithChildren[]
  loading: boolean
  selectedId?: number
  onSelect: (category: CategoryWithChildren) => void
  onCreate: (parentId: number | null, name: string) => void
  onDelete: (id: number) => void
}

export default function Sidebar({
  categories,
  loading,
  selectedId,
  onSelect,
  onCreate,
  onDelete
}: SidebarProps) {
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set())
  const [showAddForm, setShowAddForm] = useState<number | null | 'root'>(null)
  const [newCategoryName, setNewCategoryName] = useState('')

  const toggleExpand = (id: number) => {
    setExpandedIds(prev => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  const handleAddSubmit = (parentId: number | null) => {
    if (newCategoryName.trim()) {
      onCreate(parentId, newCategoryName.trim())
      setNewCategoryName('')
      setShowAddForm(null)
    }
  }

  // 무한 depth 재귀 렌더링
  const renderCategory = (category: CategoryWithChildren, depth: number = 0) => {
    const hasChildren = category.children && category.children.length > 0
    const isExpanded = expandedIds.has(category.id)
    const isSelected = selectedId === category.id
    const paddingLeft = depth * 20 + 12

    return (
      <div key={category.id} className="category-item">
        <div
          className={`category-row ${isSelected ? 'selected' : ''} ${!hasChildren ? 'leaf' : ''}`}
          style={{ paddingLeft: `${paddingLeft}px` }}
        >
          {hasChildren ? (
            <button
              className="expand-btn"
              onClick={() => toggleExpand(category.id)}
            >
              {isExpanded ? '▼' : '▶'}
            </button>
          ) : (
            <span className="expand-placeholder" />
          )}

          <span
            className="category-name"
            onClick={() => onSelect(category)}
            title={category.description || undefined}
          >
            {hasChildren ? '📁' : '📄'} {category.name}
          </span>

          <div className="category-actions">
            <button
              className="action-btn"
              onClick={(e) => {
                e.stopPropagation()
                setShowAddForm(category.id)
              }}
              title="하위 카테고리 추가"
            >
              +
            </button>
            <button
              className="action-btn delete"
              onClick={(e) => {
                e.stopPropagation()
                onDelete(category.id)
              }}
              title="삭제"
            >
              ×
            </button>
          </div>
        </div>

        {showAddForm === category.id && (
          <div className="add-form" style={{ paddingLeft: `${paddingLeft + 20}px` }}>
            <input
              type="text"
              value={newCategoryName}
              onChange={(e) => setNewCategoryName(e.target.value)}
              placeholder="하위 카테고리명"
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleAddSubmit(category.id)
                if (e.key === 'Escape') setShowAddForm(null)
              }}
            />
            <button onClick={() => handleAddSubmit(category.id)}>추가</button>
            <button onClick={() => setShowAddForm(null)}>취소</button>
          </div>
        )}

        {hasChildren && isExpanded && (
          <div className="category-children">
            {category.children.map(child => renderCategory(child, depth + 1))}
          </div>
        )}
      </div>
    )
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h2>카테고리</h2>
        <button
          className="add-root-btn"
          onClick={() => setShowAddForm('root')}
          title="상위 카테고리 추가"
        >
          +
        </button>
      </div>

      {showAddForm === 'root' && (
        <div className="add-form root-form">
          <input
            type="text"
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            placeholder="상위 카테고리명"
            autoFocus
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleAddSubmit(null)
              if (e.key === 'Escape') setShowAddForm(null)
            }}
          />
          <button onClick={() => handleAddSubmit(null)}>추가</button>
          <button onClick={() => setShowAddForm(null)}>취소</button>
        </div>
      )}

      <div className="sidebar-content">
        {loading ? (
          <div className="loading">로딩중...</div>
        ) : categories.length === 0 ? (
          <div className="empty">카테고리가 없습니다</div>
        ) : (
          categories.map(category => renderCategory(category, 0))
        )}
      </div>
    </aside>
  )
}
