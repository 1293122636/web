import { describe, it, expect, vi } from 'vitest'
import * as ruleService from '../../services/rule.service.js'

function mockPrisma() {
  return {
    circulationRule: { findMany: vi.fn(), upsert: vi.fn() },
    patronCategory: { findMany: vi.fn() },
    itemType: { findMany: vi.fn() },
  } as any
}

describe('listRules', () => {
  it('返回全部规则', async () => {
    const prisma = mockPrisma()
    prisma.circulationRule.findMany.mockResolvedValue([{ id: 1, maxBorrows: 5 }])
    const result = await ruleService.listRules(prisma)
    expect(result).toHaveLength(1)
  })
})

describe('upsertRule', () => {
  it('创建或更新规则', async () => {
    const prisma = mockPrisma()
    prisma.circulationRule.upsert.mockResolvedValue({ id: 1, maxBorrows: 10, loanDays: 60 })
    const result = await ruleService.upsertRule(prisma, {
      patronCategoryId: 2, itemTypeId: 1, maxBorrows: 10, loanDays: 60, renewals: 2, renewalDays: 30, finePerDay: 0.2
    })
    expect(result.maxBorrows).toBe(10)
  })
})

describe('listPatronCategories', () => {
  it('返回读者类型列表', async () => {
    const prisma = mockPrisma()
    prisma.patronCategory.findMany.mockResolvedValue([{ id: 1, name: '本科生' }])
    const result = await ruleService.listPatronCategories(prisma)
    expect(result).toHaveLength(1)
  })
})
