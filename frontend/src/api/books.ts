import { request } from './index'
import type { PaginatedResponse, BookSummary, BookDetail, BookListParams, BookItemsResponse, FacetsResponse } from '../types/api'

const BASE = '/api'

export const bookApi = {
  list: (params: BookListParams = {}) => {
    const q = new URLSearchParams()
    if (params.page) q.set('page', String(params.page))
    if (params.limit) q.set('limit', String(params.limit))
    if (params.search) q.set('search', params.search)
    if (params.categoryId) q.set('categoryId', String(params.categoryId))
    if (params.campus) q.set('campus', params.campus)
    if (params.yearMin) q.set('yearMin', String(params.yearMin))
    if (params.yearMax) q.set('yearMax', String(params.yearMax))
    if (params.language) q.set('language', params.language)
    const qs = q.toString()
    return request<BookListResponse>(`${BASE}/books${qs ? '?' + qs : ''}`)
  },
  getById: (id: number) => request<BookDetail>(`${BASE}/books/${id}`),
  getItems: (bookId: number) => request<BookItemsResponse>(`${BASE}/books/${bookId}/items`),
  getFacets: (search?: string) => request<FacetsResponse>(`${BASE}/books/facets${search ? '?search=' + encodeURIComponent(search) : ''}`),
}
