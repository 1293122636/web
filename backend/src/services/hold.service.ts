import { PrismaClient } from '@prisma/client'

export interface HoldResponse {
  id: number
  userId: number
  bookId: number
  bookItemId: number | null
  status: string
  requestDate: string
  expiryDate: string | null
  fulfilledAt: string | null
  book?: { id: number; title: string; author: string; isbn: string }
  user?: { id: number; username: string; name: string }
}

const MAX_HOLDS = 3

/**
 * Create a hold request. Only allowed when no copies are available.
 */
export async function createHold(
  prisma: PrismaClient,
  userId: number,
  bookId: number
): Promise<HoldResponse> {
  // 1. Book must exist and have zero available copies
  const book = await prisma.book.findUnique({ where: { id: bookId } })
  if (!book) throw Object.assign(new Error('Book not found'), { statusCode: 404 })
  if (book.available > 0) {
    throw Object.assign(new Error('Book has available copies — borrow directly'),
      { statusCode: 400 })
  }

  // 2. No duplicate holds for same book by same user
  const existing = await prisma.hold.findFirst({
    where: { userId, bookId, status: { in: ['pending', 'ready'] } }
  })
  if (existing) throw Object.assign(new Error('You already have a hold on this book'),
    { statusCode: 409 })

  // 3. Check max holds limit
  const activeCount = await prisma.hold.count({
    where: { userId, status: { in: ['pending', 'ready'] } }
  })
  if (activeCount >= MAX_HOLDS) {
    throw Object.assign(new Error(`Max ${MAX_HOLDS} holds allowed`),
      { statusCode: 400 })
  }

  const hold = await prisma.hold.create({
    data: { userId, bookId },
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
    },
  })

  return {
    id: hold.id,
    userId: hold.userId,
    bookId: hold.bookId,
    bookItemId: hold.bookItemId,
    status: hold.status,
    requestDate: hold.requestDate.toISOString(),
    expiryDate: hold.expiryDate?.toISOString() ?? null,
    fulfilledAt: hold.fulfilledAt?.toISOString() ?? null,
    book: hold.book,
  }
}

/**
 * Cancel own hold.
 */
export async function cancelHold(
  prisma: PrismaClient,
  holdId: number,
  userId: number
): Promise<void> {
  const hold = await prisma.hold.findUnique({ where: { id: holdId } })
  if (!hold) throw Object.assign(new Error('Hold not found'), { statusCode: 404 })
  if (hold.userId !== userId) throw Object.assign(new Error('Unauthorized'), { statusCode: 403 })
  if (hold.status === 'fulfilled' || hold.status === 'cancelled') {
    throw Object.assign(new Error('Hold already resolved'), { statusCode: 400 })
  }

  await prisma.hold.update({
    where: { id: holdId },
    data: { status: 'cancelled' },
  })
}

/**
 * Get current user's holds.
 */
export async function getMyHolds(
  prisma: PrismaClient,
  userId: number
): Promise<HoldResponse[]> {
  const holds = await prisma.hold.findMany({
    where: { userId },
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
    },
    orderBy: { requestDate: 'desc' },
  })

  return holds.map(h => ({
    id: h.id,
    userId: h.userId,
    bookId: h.bookId,
    bookItemId: h.bookItemId,
    status: h.status,
    requestDate: h.requestDate.toISOString(),
    expiryDate: h.expiryDate?.toISOString() ?? null,
    fulfilledAt: h.fulfilledAt?.toISOString() ?? null,
    book: h.book,
  }))
}

/**
 * Admin: list all holds with optional filters.
 */
export async function listHolds(
  prisma: PrismaClient,
  filters?: { status?: string; bookId?: number }
): Promise<HoldResponse[]> {
  const where: any = {}
  if (filters?.status) where.status = filters.status
  if (filters?.bookId) where.bookId = filters.bookId

  const holds = await prisma.hold.findMany({
    where,
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
      user: { select: { id: true, username: true, name: true } },
    },
    orderBy: { requestDate: 'desc' },
  })

  return holds.map(h => ({
    id: h.id,
    userId: h.userId,
    bookId: h.bookId,
    bookItemId: h.bookItemId,
    status: h.status,
    requestDate: h.requestDate.toISOString(),
    expiryDate: h.expiryDate?.toISOString() ?? null,
    fulfilledAt: h.fulfilledAt?.toISOString() ?? null,
    book: h.book,
    user: h.user,
  }))
}

/**
 * Admin: mark hold as fulfilled.
 */
export async function fulfillHold(
  prisma: PrismaClient,
  holdId: number
): Promise<HoldResponse> {
  const hold = await prisma.hold.findUnique({ where: { id: holdId } })
  if (!hold) throw Object.assign(new Error('Hold not found'), { statusCode: 404 })
  if (hold.status !== 'ready') {
    throw Object.assign(new Error('Hold must be in ready status'),
      { statusCode: 400 })
  }

  const updated = await prisma.hold.update({
    where: { id: holdId },
    data: {
      status: 'fulfilled',
      fulfilledAt: new Date(),
    },
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
    },
  })

  return {
    id: updated.id,
    userId: updated.userId,
    bookId: updated.bookId,
    bookItemId: updated.bookItemId,
    status: updated.status,
    requestDate: updated.requestDate.toISOString(),
    expiryDate: updated.expiryDate?.toISOString() ?? null,
    fulfilledAt: updated.fulfilledAt?.toISOString() ?? null,
    book: updated.book,
  }
}

/**
 * Return the first pending hold for a book (used by returnBook).
 */
export async function getNextPendingHold(
  prisma: PrismaClient,
  bookId: number
): Promise<HoldResponse | null> {
  const hold = await prisma.hold.findFirst({
    where: { bookId, status: 'pending' },
    orderBy: { requestDate: 'asc' },
  })

  if (!hold) return null

  return {
    id: hold.id,
    userId: hold.userId,
    bookId: hold.bookId,
    bookItemId: hold.bookItemId,
    status: hold.status,
    requestDate: hold.requestDate.toISOString(),
    expiryDate: hold.expiryDate?.toISOString() ?? null,
    fulfilledAt: hold.fulfilledAt?.toISOString() ?? null,
  }
}
