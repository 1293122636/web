import { describe, it, expect, vi } from 'vitest'
import * as categoryService from '../../services/category.service.js'

function mockPrisma() {
  return {
    category: { findMany: vi.fn(), count: vi.fn(), create: vi.fn(), update: vi.fn(), delete: vi.fn() },
    book: { count: vi.fn() },
  } as any
}

describe('list', () => {
  it('返回分类列表', async () => {
    const prisma = mockPrisma()
    prisma.category.findMany.mockResolvedValue([{ id: 1, name: 'CS' }])
    const result = await categoryService.list(prisma)
    expect(result).toHaveLength(1)
  })
})

describe('create', () => {
  it('创建分类', async () => {
    const prisma = mockPrisma()
    prisma.category.create.mockResolvedValue({ id: 1, name: 'New' })
    const result = await categoryService.create(prisma, { name: 'New' })
    expect(result.name).toBe('New')
  })
})

describe('remove', () => {
  it('有图书时拒绝删除', async () => {
    const prisma = mockPrisma()
    prisma.book.count.mockResolvedValue(3)
    await expect(categoryService.remove(prisma, 1)).rejects.toThrow('has 3 books')
  })

  it('无图书时成功删除', async () => {
    const prisma = mockPrisma()
    prisma.book.count.mockResolvedValue(0)
    prisma.category.delete.mockResolvedValue({})
    await categoryService.remove(prisma, 1)
    expect(prisma.category.delete).toHaveBeenCalled()
  })
})
