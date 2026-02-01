import type { ApiResponse, Category, CategoryWithChildren, CategoryCreateRequest, CategoryUpdateRequest } from '../types'

const BASE_URL = '/api/v1/categories'

async function handleResponse<T>(res: Response): Promise<T> {
  const json: ApiResponse<T> = await res.json()
  if (!json.success) {
    throw new Error(json.message || 'API Error')
  }
  return json.data
}

export const categoryApi = {
  // 기획서: GET /api/v1/categories - 트리 조회
  async getTree(): Promise<CategoryWithChildren[]> {
    const res = await fetch(BASE_URL)
    return handleResponse<CategoryWithChildren[]>(res)
  },

  // 관리자용: 플랫 리스트
  async getAll(): Promise<Category[]> {
    const res = await fetch(`${BASE_URL}/all`)
    return handleResponse<Category[]>(res)
  },

  async getById(id: number): Promise<Category> {
    const res = await fetch(`${BASE_URL}/${id}`)
    return handleResponse<Category>(res)
  },

  async create(request: CategoryCreateRequest): Promise<Category> {
    const res = await fetch(BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
    return handleResponse<Category>(res)
  },

  async update(id: number, request: CategoryUpdateRequest): Promise<Category> {
    const res = await fetch(`${BASE_URL}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
    return handleResponse<Category>(res)
  },

  async delete(id: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' })
    if (!res.ok) {
      const json = await res.json()
      throw new Error(json.message || '삭제 실패')
    }
  }
}
