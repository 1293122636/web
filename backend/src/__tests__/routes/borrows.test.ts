import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;
let readerToken: string;
let bookId: number;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);

  // Create reader
  const r = await app.inject({
    method: 'POST',
    url: '/api/auth/register',
    payload: { username: 'it_borrower', password: 'reader123', name: 'Borrower' },
  });
  readerToken = r.json().token;

  // Seed category + book with items
  const cat = await prisma.category.create({ data: { name: 'Borrow Test' } });
  const book = await prisma.book.create({
    data: { isbn: '978-BB-BBBBBB-1', title: 'Borrow Test Book', author: 'B', total: 3, available: 3, categoryId: cat.id },
  });
  bookId = book.id;
  // Seed ItemType and PatronCategory if they don't exist
  const itemType = await prisma.itemType.upsert({
    where: { id: 1 },
    create: { id: 1, name: '普通图书', loanDays: 30, fineRate: 0.1 },
    update: {},
  });
  const patronCat = await prisma.patronCategory.upsert({
    where: { id: 1 },
    create: { id: 1, name: '本科生' },
    update: {},
  });
  // Seed circulation rule
  await prisma.circulationRule.upsert({
    where: { id: 1 },
    create: { id: 1, patronCategoryId: 1, itemTypeId: 1, maxBorrows: 5, loanDays: 30, renewals: 1, renewalDays: 15, finePerDay: 0.1 },
    update: {},
  });

  // Create 3 physical items
  for (let i = 1; i <= 3; i++) {
    await prisma.bookItem.create({
      data: { barcode: `LIB-BRB-00${i}`, callNumber: 'B123', location: 'Test Shelf', bookId: book.id, itemTypeId: 1 },
    });
  }
}, 15000);

afterAll(async () => {
  await prisma.borrowRecord.deleteMany({ where: { book: { isbn: { startsWith: '978-BB' } } } });
  await prisma.bookItem.deleteMany({ where: { barcode: { startsWith: 'LIB-BRB' } } });
  await prisma.book.deleteMany({ where: { isbn: { startsWith: '978-BB' } } });
  await prisma.category.deleteMany({ where: { name: 'Borrow Test' } });
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_borrow' } } });
  // Don't clean up ItemType/PatronCategory — they're shared
  await prisma.$disconnect();
});

describe('Borrows Integration', () => {
  it('GET /api/borrows/my — returns empty list for new reader', async () => {
    const res = await app.inject({
      method: 'GET',
      url: '/api/borrows/my',
      headers: authHeaders(readerToken),
    });
    expect(res.statusCode).toBe(200);
    expect(Array.isArray(res.json())).toBe(true);
  });

  it('POST /api/borrows/borrow — reader can borrow a book', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/api/borrows/borrow',
      headers: authHeaders(readerToken),
      payload: { bookId },
    });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.dueDate).toBeTruthy();
    expect(body.bookItem).toBeDefined();
  });

  it('POST /api/borrows/borrow — rejects without auth', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/api/borrows/borrow',
      payload: { bookId },
    });
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/borrows/my — shows active borrow after borrowing', async () => {
    const res = await app.inject({
      method: 'GET',
      url: '/api/borrows/my',
      headers: authHeaders(readerToken),
    });
    expect(res.statusCode).toBe(200);
    const data = res.json();
    expect(data.length).toBe(1);
    expect(data[0].status).toBe('active');
  });

  it('POST /api/borrows/return/:id — can return a borrowed book', async () => {
    // Find the active borrow
    const myBorrows = await app.inject({
      method: 'GET',
      url: '/api/borrows/my',
      headers: authHeaders(readerToken),
    });
    const borrowId = myBorrows.json()[0].id;

    const res = await app.inject({
      method: 'POST',
      url: `/api/borrows/return/${borrowId}`,
      headers: authHeaders(readerToken),
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().status).toBe('returned');
  });
});
