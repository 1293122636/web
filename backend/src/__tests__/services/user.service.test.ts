import { describe, it, expect, vi } from 'vitest'
import * as userService from '../../services/user.service.js'

function mockPrisma() {
  return {
    user: { findMany: vi.fn(), findFirst: vi.fn(), update: vi.fn() },
  } as any
}

describe('listReaders', () => {
  it('返回读者列表', async () => {
    const prisma = mockPrisma()
    prisma.user.findMany.mockResolvedValue([{ id: 1, username: '20230001', name: 'Alice' }])
    const result = await userService.listReaders(prisma)
    expect(result).toHaveLength(1)
  })
})

describe('getReaderDetail', () => {
  it('返回读者详情', async () => {
    const prisma = mockPrisma()
    prisma.user.findFirst.mockResolvedValue({ id: 1, username: '20230001', name: 'Alice', borrowRecords: [] })
    const result = await userService.getReaderDetail(prisma, 1)
    expect(result?.username).toBe('20230001')
  })
  it('不存在返回 null', async () => {
    const prisma = mockPrisma()
    prisma.user.findFirst.mockResolvedValue(null)
    const result = await userService.getReaderDetail(prisma, 999)
    expect(result).toBeNull()
  })
})

describe('updateProfile', () => {
  it('更新个人信息', async () => {
    const prisma = mockPrisma()
    prisma.user.update.mockResolvedValue({ id: 1, name: 'Updated' })
    const result = await userService.updateProfile(prisma, 1, { name: 'Updated' })
    expect(result.name).toBe('Updated')
  })
})
