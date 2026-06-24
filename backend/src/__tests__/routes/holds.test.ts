import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import { buildApp, makePrisma, authHeaders } from '../helpers.js'
import type { FastifyInstance } from 'fastify'
import type { PrismaClient } from '@prisma/client'

let app: FastifyInstance
let prisma: PrismaClient
let readerToken: string
let adminToken: string
let bookId: number
let catId: number

beforeAll(async () => {
  prisma = makePrisma()
  app = await buildApp(prisma)

  // Create reader
  const r = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_hld_rd', password: 'reader123', name: 'Hold Reader' } })
  readerToken = r.json().token

  // Create admin
  const a = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_hld_adm', password: 'admin123', name: 'Hold Admin' } })
  const aId = a.json().user.id
  await prisma.user.update({ where: { id: aId }, data: { role: 'admin' } })
  const login = await app.inject({ method: 'POST', url: '/api/auth/login', payload: { username: 'it_hld_adm', password: 'admin123' } })
  adminToken = login.json().token

  // Create category + book with available=0
  const cat = await prisma.category.create({ data: { name: 'Hold RT Cat' } })
  const book = await prisma.book.create({
    data: { isbn: '978-HR-000001', title: 'Hold Route Book', author: 'H', total: 1, available: 0, categoryId: cat.id },
  })
  bookId = book.id; catId = cat.id
  // Seed ItemType for borrow rules
  await prisma.itemType.upsert({ where: { id: 1 }, create: { id: 1, name: 'default', loanDays: 30, fineRate: 0.1 }, update: {} })
  await prisma.patronCategory.upsert({ where: { id: 1 }, create: { id: 1, name: 'default' }, update: {} })
  await prisma.circulationRule.upsert({ where: { id: 1 }, create: { id: 1, patronCategoryId: 1, itemTypeId: 1, maxBorrows: 5, loanDays: 30, renewals: 1, renewalDays: 15, finePerDay: 0.1 }, update: {} })
}, 15000)

afterAll(async () => {
  await prisma.hold.deleteMany({ where: { book: { isbn: { startsWith: '978-HR' } } } })
  await prisma.book.deleteMany({ where: { isbn: { startsWith: '978-HR' } } })
  await prisma.category.deleteMany({ where: { name: 'Hold RT Cat' } })
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_hld' } } })
  await prisma.$disconnect()
})

describe('Holds Integration', () => {
  it('POST /api/holds — reader can place hold on unavailable book', async () => {
    const res = await app.inject({
      method: 'POST', url: '/api/holds',
      headers: authHeaders(readerToken),
      payload: { bookId },
    })
    expect(res.statusCode).toBe(200)
    expect(res.json().status).toBe('pending')
    expect(res.json().book.title).toBe('Hold Route Book')
  })

  it('POST /api/holds — rejects book with available copies', async () => {
    const availBook = await prisma.book.create({
      data: { isbn: '978-HR-AVAIL', title: 'Avail', author: 'A', total: 1, available: 1, categoryId: catId },
    })
    const res = await app.inject({
      method: 'POST', url: '/api/holds',
      headers: authHeaders(readerToken),
      payload: { bookId: availBook.id },
    })
    expect(res.statusCode).toBe(400)
    await prisma.book.delete({ where: { id: availBook.id } })
  })

  it('GET /api/holds/my — reader sees own holds', async () => {
    const res = await app.inject({
      method: 'GET', url: '/api/holds/my',
      headers: authHeaders(readerToken),
    })
    expect(res.statusCode).toBe(200)
    expect(res.json().length).toBeGreaterThanOrEqual(1)
  })

  it('DELETE /api/holds/:id — reader cancels own hold', async () => {
    const my = await app.inject({ method: 'GET', url: '/api/holds/my', headers: authHeaders(readerToken) })
    const holdId = my.json()[0].id
    const res = await app.inject({
      method: 'DELETE', url: '/api/holds/' + holdId,
      headers: authHeaders(readerToken),
    })
    expect(res.statusCode).toBe(200)

    const after = await app.inject({ method: 'GET', url: '/api/holds/my', headers: authHeaders(readerToken) })
    const h = after.json().find((x: any) => x.id === holdId)
    expect(h?.status).toBe('cancelled')
  })

  it('GET /api/holds — admin can list all', async () => {
    const res = await app.inject({
      method: 'GET', url: '/api/holds',
      headers: authHeaders(adminToken),
    })
    expect(res.statusCode).toBe(200)
    expect(Array.isArray(res.json())).toBe(true)
  })
})
