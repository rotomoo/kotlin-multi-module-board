import type { ApiResponse, PagedApiResponse, Post, PostListItem, PostCreateRequest, PostUpdateRequest } from '../types'

async function handleResponse<T>(res: Response): Promise<T> {
  const json: ApiResponse<T> = await res.json()
  if (!json.success) {
    throw new Error(json.message || 'API Error')
  }
  return json.data
}

async function handlePagedResponse<T>(res: Response): Promise<{ data: T[], totalElements: number, totalPages: number }> {
  const json: PagedApiResponse<T> = await res.json()
  if (!json.success) {
    throw new Error(json.message || 'API Error')
  }
  return {
    data: json.data,
    totalElements: json.pagination.totalElements,
    totalPages: json.pagination.totalPages
  }
}

export const postApi = {
  // 카테고리별 게시글 목록 (페이징)
  async getByCategoryId(categoryId: number, page = 0, size = 20): Promise<PostListItem[]> {
    const res = await fetch(`/api/v1/categories/${categoryId}/posts?page=${page}&size=${size}`)
    const result = await handlePagedResponse<PostListItem>(res)
    return result.data
  },

  // 게시글 상세
  async getById(id: number): Promise<Post> {
    const res = await fetch(`/api/v1/posts/${id}`)
    return handleResponse<Post>(res)
  },

  // 게시글 생성 (POST /api/v1/posts)
  async create(request: PostCreateRequest): Promise<Post> {
    const res = await fetch('/api/v1/posts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
    return handleResponse<Post>(res)
  },

  // 게시글 수정
  async update(id: number, request: PostUpdateRequest): Promise<Post> {
    const res = await fetch(`/api/v1/posts/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
    return handleResponse<Post>(res)
  },

  // 게시글 삭제
  async delete(id: number): Promise<void> {
    const res = await fetch(`/api/v1/posts/${id}`, { method: 'DELETE' })
    if (!res.ok) {
      const json = await res.json()
      throw new Error(json.message || '삭제 실패')
    }
  }
}
