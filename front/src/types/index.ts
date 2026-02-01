// API 응답 래퍼
export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
}

// 카테고리 응답 (백엔드 CategoryResponse와 일치)
export interface Category {
  id: number
  parentId: number | null
  name: string
  description: string | null
  sortOrder: number
  isActive: boolean
  isParent: boolean
  createdAt: string
  updatedAt: string
}

// 트리 구조용 카테고리 - 무한 depth 지원 (재귀 구조)
export interface CategoryWithChildren {
  id: number
  name: string
  description: string | null
  sortOrder: number
  isActive: boolean
  children: CategoryWithChildren[]  // 재귀: 자기 자신 타입
  createdAt: string
  updatedAt: string
}

export interface CategoryCreateRequest {
  parentId?: number | null
  name: string
  description?: string
  sortOrder?: number
}

export interface CategoryUpdateRequest {
  name: string
  description?: string
  sortOrder?: number
  isActive?: boolean
}

export interface CategoryTreeResponse {
  categories: CategoryWithChildren[]
}

// 게시글 상세 응답 (백엔드 PostResponse와 일치)
export interface Post {
  id: number
  title: string
  content: string
  category: CategoryInfo
  author: string
  viewCount: number
  status: 'DRAFT' | 'PUBLISHED' | 'DELETED'
  createdAt: string
  updatedAt: string
}

// 게시글 목록 응답 (백엔드 PostListResponse와 일치)
export interface PostListItem {
  id: number
  title: string
  category: CategoryInfo
  author: string
  viewCount: number
  createdAt: string
}

export interface CategoryInfo {
  id: number
  name: string
}

export interface PostCreateRequest {
  author: string
  categoryId: number
  title: string
  content: string
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface PostUpdateRequest {
  title: string
  content: string
}

// 페이징 응답 (PagedApiResponse)
export interface PagedApiResponse<T> {
  success: boolean
  data: T[]
  pagination: Pagination
  message: string | null
}

export interface Pagination {
  page: number
  size: number
  totalElements: number
  totalPages: number
}
