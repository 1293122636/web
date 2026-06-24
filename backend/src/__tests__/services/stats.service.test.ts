import { describe, it, expect, vi } from 'vitest'
import * as statsService from '../../services/stats.service.js'

function mockPrisma() {
  return {
    book: { count: vi.fn(), findMany: vi.fn() },
    user: { count: vi.fn() },
    borrowRecord: { count: vi.fn(), findMany: vi.fn() },
    category: { count: vi.fn() },
  } as any
}

describe('getOverview', () => {
  it('返回五项聚合统计', async () => {
    const prisma = mockPrisma()
    prisma.book.count.mockResolvedValue(7)
    prisma.user.count.mockResolvedValue(5)
    prisma.borrowRecord.count.mockResolvedValueOnce(3).mockResolvedValueOnce(1)
    prisma.category.count.mockResolvedValue(5)

    const result = await statsService.getOverview(prisma)
    expect(result).toEqual({ totalBooks: 7, totalReaders: 5, activeBorrows: 3, totalCategories: 5, overdueCount: 1 })
  })
})

describe('getPopularBooks', () => {
  it('返回热门 TOP 20', async () => {
    const prisma = mockPrisma()
    prisma.book.findMany.mockResolvedValue([{ id: 1, title: 'Popular' }])
    const result = await statsService.getPopularBooks(prisma)
    expect(result).toHaveLength(1)
  })
})

describe('getMonthlyStats', () => {
  it('返回月度统计', async () => {
    const prisma = mockPrisma()
    const now = new Date()
    prisma.borrowRecord.findMany.mockResolvedValue([{ borrowDate: now }])
    const result = await statsService.getMonthlyStats(prisma)
    expect(result).toHaveLength(1)
    expect(result[0].count).toBe(1)
  })
})
