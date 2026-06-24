import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import { PrismaClient } from '@prisma/client'
import * as holdService from '../../services/hold.service.js'

let prisma: PrismaClient
let bookId: number
let userId: number

beforeAll(async () => {
  prisma = new PrismaClient()
  const cat = await prisma.category.create({ data: { name: 'Hold Test Cat' } })
  const book = await prisma.book.create({
    data: { isbn: '978-HD-000001', title: 'Hold Test Book', author: 'H', total: 1, available: 0, categoryId: cat.id },
  })
  bookId = book.id
  const user = await prisma.user.create({
    data: { username: 'it_holduser', password: 'hash', name: 'Hold User', role: 'reader' },
  })
  userId = user.id
}, 15000)

afterAll(async () => {
  await prisma.hold.deleteMany({ where: { bookId } })
  await prisma.book.deleteMany({ where: { isbn: '978-HD-000001' } })
  await prisma.category.deleteMany({ where: { name: 'Hold Test Cat' } })
  await prisma.user.deleteMany({ where: { username: 'it_holduser' } })
  await prisma.$disconnect()
})

describe('hold.service', () => {
  it('createHold — places a hold on unavailable book', async () => {
    const hold = await holdService.createHold(prisma, userId, bookId)
    expect(hold.status).toBe('pending')
    expect(hold.bookId).toBe(bookId)
    expect(hold.book?.title).toBe('Hold Test Book')
  })

  it('createHold — rejects duplicate hold', async () => {
    try {
      await holdService.createHold(prisma, userId, bookId)
      expect(true).toBe(false)
    } catch (e: any) {
      expect(e.statusCode).toBe(409)
    }
  })

  it('createHold — rejects book with available copies', async () => {
    const cat = await prisma.category.findFirst({ where: { name: 'Hold Test Cat' } })
    const availBook = await prisma.book.create({
      data: { isbn: '978-HD-AVAIL-1', title: 'Available Book', author: 'A', total: 1, available: 1, categoryId: cat!.id },
    })
    try {
      await holdService.createHold(prisma, userId, availBook.id)
      expect(true).toBe(false)
    } catch (e: any) {
      expect(e.statusCode).toBe(400)
    }
    await prisma.book.delete({ where: { id: availBook.id } })
  })

  it('getMyHolds — returns user holds', async () => {
    const holds = await holdService.getMyHolds(prisma, userId)
    expect(holds.length).toBeGreaterThanOrEqual(1)
    expect(holds[0].status).toBe('pending')
  })

  it('cancelHold — cancels own hold', async () => {
    const holds = await holdService.getMyHolds(prisma, userId)
    const pendingHold = holds.find((h: any) => h.status === 'pending')
    expect(pendingHold).toBeDefined()
    await holdService.cancelHold(prisma, pendingHold!.id, userId)
    const after = await holdService.getMyHolds(prisma, userId)
    const cancelled = after.find((h: any) => h.id === pendingHold!.id)
    expect(cancelled?.status).toBe('cancelled')
  })

  it('cancelHold — rejects other user', async () => {
    const user2 = await prisma.user.create({
      data: { username: 'it_hold2', password: 'hash', name: 'Hold User 2', role: 'reader' },
    })
    const hold = await holdService.createHold(prisma, user2.id, bookId)
    try {
      await holdService.cancelHold(prisma, hold.id, userId)
      expect(true).toBe(false)
    } catch (e: any) {
      expect(e.statusCode).toBe(403)
    }
    await prisma.hold.delete({ where: { id: hold.id } })
    await prisma.user.delete({ where: { id: user2.id } })
  })
})
