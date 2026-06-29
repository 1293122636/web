import api from './index'
import type { BookDetail, BookItemsResponse, FacetsResponse, BookListResponse, BookListParams } from '../types/api'

export const bookApi = {
  list: (params: BookListParams = {}) =>
    api.get<BookListResponse>('/books', { params }),

  getById: (id: number) =>
    api.get<BookDetail>(`/books/${id}`),

  getItems: (bookId: number) =>
    api.get<BookItemsResponse>(`/books/${bookId}/items`),

  getFacets: (params: BookListParams = {}) =>
    api.get<FacetsResponse>('/books/facets', { params }),
}
