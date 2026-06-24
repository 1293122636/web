// Frontend type mirrors of backend DTOs
// Keep these in sync with backend/src/types/api.types.ts

export interface PaginationParams { page?: number; limit?: number }
export interface PaginatedResponse<T> { data: T[]; total: number; page: number; limit: number; pages: number }

export interface UserProfile {
  id: number; username: string; name: string; role: 'admin' | 'reader'
  phone?: string | null; email?: string | null; createdAt: string
}
export interface LoginResponse { user: UserProfile; token: string }

export interface BookSummary {
  id: number; isbn: string; title: string; author: string
  publisher?: string | null; year?: number | null
  total: number; available: number; status: string
  clcNumber?: string | null; physicalDesc?: string | null; cover?: string | null
  language?: string | null; country?: string | null
  category: { id: number; name: string }; _count?: { items: number }
}
export interface BookDetail extends BookSummary { items?: BookItemSummary[] }
export interface BookListResponse { books: BookSummary[]; total: number; page: number; limit: number; pages: number }
export interface BookListParams extends PaginationParams {
  search?: string; categoryId?: number
  campus?: string; yearMin?: number; yearMax?: number; language?: string
}

export interface BookItemSummary {
  id: number; barcode: string; callNumber?: string | null
  location?: string | null; campus?: string | null; status: string
  requests: number; price?: number | null
}
export interface BookItemsResponse { book: { id: number; title: string; isbn: string }; items: BookItemSummary[] }

export interface CategoryResponse { id: number; name: string; desc?: string | null; _count?: { books: number } }

export interface BorrowRecordResponse {
  id: number; userId: number; bookId: number; borrowDate: string; dueDate: string
  returnDate?: string | null; status: string; renewed: boolean
  book: { id: number; title: string; author: string; isbn: string }
  bookItem?: { id: number; barcode: string; callNumber?: string | null } | null
  fines?: { id: number; amount: number; type: string; paid: boolean }[] | null
}

export interface StatsOverviewResponse { totalBooks: number; totalReaders: number; activeBorrows: number; totalCategories: number; overdueCount: number }
export interface PopularBook { id: number; title: string; author: string; isbn: string; category: { id: number; name: string }; _count: { borrowRecords: number } }
export interface MonthlyStat { month: string; count: number }

export interface FineResponse {
  id: number; amount: number; type: string; paid: boolean; createdAt: string
  user?: { id: number; username: string; name: string }
  borrowRecord?: { id: number; book: { title: string } } | null
}

export interface FacetValue { value: string; count: number }
export interface FacetsResponse { facets: Record<string, FacetValue[]> }

export interface CirculationRuleResponse {
  id: number; maxBorrows: number; loanDays: number; renewals: number; renewalDays: number; finePerDay: number
  patronCategory: { id: number; name: string }; itemType: { id: number; name: string }
}
