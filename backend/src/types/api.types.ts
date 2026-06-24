/**
 * Library Full-Stack — API Type Definitions
 *
 * 覆盖全部 35 个端点。命名: XxxParams(请求) / XxxResponse(响应)。
 * Module A 产物。零行为变更 — 纯类型定义。
 */

// ══════════════════════════════════════════════════════════════
// 通用
// ══════════════════════════════════════════════════════════════

export interface PaginationParams {
  page?: number;
  limit?: number;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  pages: number;
}

// ══════════════════════════════════════════════════════════════
// Auth (5 端点)
// ══════════════════════════════════════════════════════════════

export interface RegisterParams {
  username: string;
  password: string;
  name: string;
  phone?: string;
  email?: string;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface UserProfile {
  id: number;
  username: string;
  name: string;
  role: 'admin' | 'reader';
  phone?: string | null;
  email?: string | null;
  patronCategoryId?: number | null;
  totalFines?: number;
  createdAt: string;
}

export interface LoginResponse {
  user: UserProfile;
  token: string;
}

export interface AuthError {
  error: string;
  details?: unknown;
}

// ══════════════════════════════════════════════════════════════
// Book (6 端点 + 2 新增)  → 模块 C 新增 facets / barcode
// ══════════════════════════════════════════════════════════════

export interface BookListParams extends PaginationParams {
  search?: string;
  categoryId?: number;
  // 模块 C 新增分面过滤
  campus?: string;
  location?: string;
  yearMin?: number;
  yearMax?: number;
  language?: string;
  clcNumber?: string;
  sortBy?: 'relevance' | 'year' | 'title';
}

export interface CategoryRef {
  id: number;
  name: string;
  desc?: string | null;
}

export interface BookSummary {
  id: number;
  isbn: string;
  title: string;
  author: string;
  publisher?: string | null;
  year?: number | null;
  total: number;
  available: number;
  status: 'available' | 'borrowed' | 'removed';
  desc?: string | null;
  location?: string | null;
  // 新增
  clcNumber?: string | null;
  physicalDesc?: string | null;
  cover?: string | null;
  language?: string | null;
  country?: string | null;
  categoryId: number;
  category: CategoryRef;
  _count?: { items: number };
  createdAt: string;
  updatedAt: string;
}

export interface BookDetail extends BookSummary {
  items?: BookItemSummary[];
  electronicResources?: string[];
}

export type BookListResponse = PaginatedResponse<BookSummary>;

export interface BookCreateParams {
  isbn: string;
  title: string;
  author: string;
  publisher?: string;
  year?: number;
  total: number;
  location?: string;
  desc?: string;
  categoryId: number;
  clcNumber?: string;
  physicalDesc?: string;
  cover?: string;
  language?: string;
  country?: string;
}

export type BookUpdateParams = Partial<BookCreateParams>;

export type BookStatus = 'available' | 'borrowed' | 'removed';

// ══════════════════════════════════════════════════════════════
// BookItem (2 端点 + Module F 新增 barcode 查询)
// ══════════════════════════════════════════════════════════════

export type ItemCondition = 'normal' | 'damaged' | 'repairing' | 'lost' | 'withdrawn';
export type ItemStatus = 'available' | 'borrowed' | 'repairing' | 'lost' | 'withdrawn' | 'on_hold';

export interface BookItemSummary {
  id: number;
  barcode: string;
  callNumber?: string | null;
  location?: string | null;
  campus?: string | null;
  status: ItemStatus;
  condition: ItemCondition;
  price?: number | null;
  acquiredAt?: string | null;
  requests: number;
  bookId: number;
  itemTypeId?: number | null;
  itemType?: ItemTypeRef | null;
}

export interface BookItemsResponse {
  book: { id: number; title: string; isbn: string };
  items: BookItemSummary[];
}

// Module F: GET /api/book-items/:barcode
export interface BookItemBarcodeResponse {
  item: BookItemSummary & { book: BookSummary };
  currentBorrow: BorrowRecordResponse | null;
}

// ══════════════════════════════════════════════════════════════
// Category (4 端点)
// ══════════════════════════════════════════════════════════════

export interface CategoryCreateParams {
  name: string;
  desc?: string;
}

export type CategoryUpdateParams = Partial<CategoryCreateParams>;

export interface CategoryResponse {
  id: number;
  name: string;
  desc?: string | null;
  _count?: { books: number };
}

// ══════════════════════════════════════════════════════════════
// Borrow (6 端点)
// ══════════════════════════════════════════════════════════════

export interface BorrowParams {
  bookId?: number;
  bookItemId?: number;
}

export type BorrowStatus = 'active' | 'returned' | 'overdue';

export interface BorrowRecordResponse {
  id: number;
  userId: number;
  bookId: number;
  bookItemId?: number | null;
  borrowDate: string;
  dueDate: string;
  returnDate?: string | null;
  status: BorrowStatus;
  renewed: boolean;
  book: { id: number; title: string; author: string; isbn: string };
  bookItem?: { id: number; barcode: string; callNumber?: string | null } | null;
  user?: { id: number; name: string; username: string } | null;
  fines?: FineSummary[] | null;
}

export interface ReturnResult {
  id: number
  status: string
  returnDate: string
  fine: { amount: number; type: string } | null
  holdPromoted?: { holdId: number; userId: number } | null
}

export interface RenewResult {
  id: number;
  dueDate: string;
  renewed: boolean;
  renewedDays: number;
}

// ══════════════════════════════════════════════════════════════
// Reader (4 端点)
// ══════════════════════════════════════════════════════════════

export interface ReaderSummary {
  id: number;
  username: string;
  name: string;
  phone?: string | null;
  email?: string | null;
  createdAt: string;
  _count?: { borrowRecords: number };
}

export interface ReaderDetail extends ReaderSummary {
  patronCategoryId?: number | null;
  totalFines?: number;
  borrowRecords: BorrowRecordResponse[];
}

export interface ReaderUpdateParams {
  name?: string;
  phone?: string;
  email?: string;
}

// ══════════════════════════════════════════════════════════════
// Stats (3 端点)
// ══════════════════════════════════════════════════════════════

export interface StatsOverviewResponse {
  totalBooks: number;
  totalReaders: number;
  activeBorrows: number;
  totalCategories: number;
  overdueCount: number;
}

export interface PopularBook {
  id: number;
  title: string;
  author: string;
  isbn: string;
  category: CategoryRef;
  _count: { borrowRecords: number };
}

export interface MonthlyStat {
  month: string;
  count: number;
}

// ══════════════════════════════════════════════════════════════
// Fine (3 端点)
// ══════════════════════════════════════════════════════════════

export type FineType = 'overdue' | 'lost' | 'damage';

export interface FineListParams {
  type?: FineType;
  paid?: boolean;
}

export interface FineSummary {
  id: number;
  amount: number;
  type: FineType;
  paid: boolean;
  paidAt?: string | null;
  createdAt: string;
}

export interface FineResponse extends FineSummary {
  user: { id: number; username: string; name: string };
  borrowRecord?: { id: number; book: { title: string } } | null;
}

export interface FinePayResult {
  id: number;
  amount: number;
  type: FineType;
  paid: boolean;
  paidAt: string;
}

// ══════════════════════════════════════════════════════════════
// Rule (4 端点)
// ══════════════════════════════════════════════════════════════

export interface PatronCategoryResponse {
  id: number;
  name: string;
}

export interface ItemTypeResponse {
  id: number;
  name: string;
  loanDays: number;
  fineRate: number;
}

export interface CirculationRuleResponse {
  id: number;
  patronCategoryId: number;
  itemTypeId: number;
  maxBorrows: number;
  loanDays: number;
  renewals: number;
  renewalDays: number;
  finePerDay: number;
  patronCategory: PatronCategoryResponse;
  itemType: ItemTypeResponse;
}

export interface RuleUpsertParams {
  patronCategoryId: number;
  itemTypeId: number;
  maxBorrows: number;
  loanDays: number;
  renewals: number;
  renewalDays: number;
  finePerDay: number;
}

// ══════════════════════════════════════════════════════════════
// Facets (Module C 新增)
// ══════════════════════════════════════════════════════════════

export interface FacetValue {
  value: string;
  count: number;
}

export interface FacetsResponse {
  facets: {
    type?: FacetValue[];
    campus?: FacetValue[];
    location?: FacetValue[];
    yearRange?: FacetValue[];
    language?: FacetValue[];
    subject?: FacetValue[];
    publisher?: FacetValue[];
  };
}

// ══════════════════════════════════════════════════════════════
// Hold (Module G 新增)
// ══════════════════════════════════════════════════════════════

export type HoldStatus = 'pending' | 'waiting' | 'ready' | 'cancelled' | 'expired';

export interface HoldResponse {
  id: number;
  userId: number;
  bookItemId: number;
  placedAt: string;
  expiresAt?: string | null;
  pickupLocation?: string | null;
  status: HoldStatus;
  queuePosition: number;
  notifiedAt?: string | null;
  bookItem: BookItemSummary;
  user: { id: number; name: string; username: string };
}

// ══════════════════════════════════════════════════════════════
// ItemType ref (shared)
// ══════════════════════════════════════════════════════════════

export interface ItemTypeRef {
  id: number;
  name: string;
}
