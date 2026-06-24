import { PrismaClient } from '@prisma/client';
import type {
  BookListParams,
  BookSummary,
  BookDetail,
  BookCreateParams,
  BookUpdateParams,
  BookListResponse,
  BookItemsResponse,
  BookItemSummary,
  FacetsResponse,
  FacetValue,
} from '../types/api.types.js';

export async function list(
  prisma: PrismaClient,
  params: BookListParams,
): Promise<BookListResponse> {
  const page = Math.max(1, params.page || 1);
  const limit = Math.min(50, Math.max(1, params.limit || 20));
  const where: Record<string, unknown> = {};
  if (params.search) {
    where.OR = [
      { title: { contains: params.search } },
      { author: { contains: params.search } },
      { isbn: { contains: params.search } },
    ];
  }
  if (params.categoryId) where.categoryId = params.categoryId;
  if (params.campus) where.items = { some: { campus: params.campus } };
  if (params.yearMin || params.yearMax) {
    where.year = {};
    if (params.yearMin) where.year.gte = params.yearMin;
    if (params.yearMax) where.year.lte = params.yearMax;
  }
  if (params.language) where.language = params.language;

  const [books, total] = await Promise.all([
    prisma.book.findMany({
      where,
      skip: (page - 1) * limit,
      take: limit,
      include: { category: true, _count: { select: { items: true } } },
      orderBy: { createdAt: 'desc' },
    }),
    prisma.book.count({ where }),
  ]);
  return {
    books: books as unknown as BookSummary[],
    total,
    page,
    limit,
    pages: Math.ceil(total / limit),
  };
}

export async function getById(prisma: PrismaClient, id: number): Promise<BookDetail | null> {
  return prisma.book.findUnique({
    where: { id },
    include: {
      category: true,
      _count: { select: { items: true } },
      items: {
        include: { itemType: true, borrowRecords: { where: { status: 'active' }, take: 1 } },
        orderBy: { barcode: 'asc' },
      },
    },
  }) as unknown as BookDetail | null;
}

export async function getItems(
  prisma: PrismaClient,
  bookId: number,
): Promise<BookItemsResponse | null> {
  const book = await prisma.book.findUnique({
    where: { id: bookId },
    select: { id: true, title: true, isbn: true },
  });
  if (!book) return null;
  const items = await prisma.bookItem.findMany({
    where: { bookId },
    include: { itemType: true, borrowRecords: { where: { status: 'active' }, take: 1 } },
    orderBy: { barcode: 'asc' },
  });
  return { book, items: items as unknown as BookItemSummary[] };
}

export async function create(prisma: PrismaClient, data: BookCreateParams): Promise<BookSummary> {
  const { total, ...rest } = data;
  return prisma.book.create({
    data: { ...rest, total, available: total, status: 'available' },
    include: { category: true },
  }) as unknown as BookSummary;
}

export async function update(
  prisma: PrismaClient,
  id: number,
  data: BookUpdateParams,
): Promise<BookSummary> {
  const updateData: Record<string, unknown> = { ...data };
  if (data.total !== undefined && data.total !== null) {
    const current = await prisma.book.findUnique({ where: { id } });
    if (current) {
      const borrowed = current.total - current.available;
      if (data.total < borrowed) {
        throw Object.assign(new Error(`Cannot reduce total below ${borrowed}`), {
          statusCode: 400,
        });
      }
      const diff = data.total - current.total;
      updateData.available = current.available + diff;
    }
  }
  return prisma.book.update({
    where: { id },
    data: updateData,
    include: { category: true },
  }) as unknown as BookSummary;
}

export async function remove(prisma: PrismaClient, id: number): Promise<void> {
  await prisma.book.delete({ where: { id } });
}

export async function getFacets(
  prisma: PrismaClient,
  params: BookListParams,
): Promise<FacetsResponse> {
  const where: Record<string, unknown> = {};
  if (params.search) {
    where.OR = [
      { title: { contains: params.search } },
      { author: { contains: params.search } },
      { isbn: { contains: params.search } },
    ];
  }
  if (params.categoryId) where.categoryId = params.categoryId;

  // Prisma 5 groupBy doesn't support relation filters (book: { where })
  // Workaround: get matching bookIds first
  const matchingBooks = await prisma.book.findMany({ where, select: { id: true } });
  const bookIds = matchingBooks.map((b) => b.id);
  const itemWhere = bookIds.length > 0 ? { bookId: { in: bookIds } } : { bookId: -1 }; // -1 = no match

  const [campus, locations, languages, subjects] = await Promise.all([
    prisma.bookItem.groupBy({
      by: ['campus'],
      where: { ...itemWhere, campus: { not: null } },
      _count: true,
      orderBy: { campus: 'desc' },
    }) as unknown as { campus: string | null; _count: number }[],
    prisma.bookItem.groupBy({
      by: ['location'],
      where: { ...itemWhere, location: { not: null } },
      _count: true,
      orderBy: { location: 'desc' },
    }) as unknown as { location: string | null; _count: number }[],
    prisma.book.groupBy({
      by: ['language'],
      where: { ...where, language: { not: null } },
      _count: true,
      orderBy: { language: 'desc' },
    }) as unknown as { language: string; _count: number }[],
    prisma.book.groupBy({ by: ['categoryId'], where, _count: true }) as unknown as { categoryId: number; _count: number }[],
  ]);

  const catIds = subjects.map((s) => s.categoryId);
  const categories = await prisma.category.findMany({
    where: { id: { in: catIds } },
    select: { id: true, name: true },
  });
  const catMap = new Map(categories.map((c) => [c.id, c.name]));

  const yearBooks = await prisma.book.groupBy({
    by: ['year'],
    where: { ...where, year: { not: null } },
    _count: true,
  });
  const yearRanges: FacetValue[] = [];
  const decades: Record<string, number> = {};
  for (const y of yearBooks) {
    if (!y.year) continue;
    const decade = `${Math.floor(y.year / 10) * 10}s`;
    decades[decade] = (decades[decade] || 0) + y._count;
  }
  for (const [d, c] of Object.entries(decades).sort()) {
    yearRanges.push({ value: d, count: c });
  }

  return {
    facets: {
      campus: campus.map((c) => ({ value: c.campus!, count: c._count })),
      location: locations.map((l) => ({ value: l.location!, count: l._count })),
      language: languages.map((l) => ({ value: l.language!, count: l._count })),
      subject: subjects.map((s) => ({
        value: catMap.get(s.categoryId) || `#${s.categoryId}`,
        count: s._count,
      })),
      yearRange: yearRanges,
    },
  };
}

// ===== BookItem status machine =====

const STATUS_TRANSITIONS: Record<string, string[]> = {
  available: ['borrowed', 'on_hold', 'repairing', 'lost', 'withdrawn'],
  borrowed: ['available', 'lost', 'on_hold'],  // on_hold: returnBook→hold promotion
  on_hold: ['available', 'borrowed', 'expired'],
  repairing: ['available', 'lost', 'withdrawn'],
  lost: ['available', 'withdrawn'],
  withdrawn: [],
};

export function validateItemStatus(current: string, next: string): void {
  const allowed = STATUS_TRANSITIONS[current];
  if (!allowed || !allowed.includes(next)) {
    throw Object.assign(
      new Error(`Invalid status transition: ${current} → ${next}`),
      { statusCode: 400 },
    );
  }
}

export async function reconcileBookAvailable(
  prisma: PrismaClient,
  bookId: number,
): Promise<{ available: number; was: number }> {
  const book = await prisma.book.findUnique({ where: { id: bookId } });
  if (!book) throw Object.assign(new Error('Book not found'), { statusCode: 404 });
  const actual = await prisma.bookItem.count({
    where: { bookId, status: 'available' },
  });
  if (actual !== book.available) {
    await prisma.book.update({
      where: { id: bookId },
      data: { available: actual },
    });
  }
  return { available: actual, was: book.available };
}
