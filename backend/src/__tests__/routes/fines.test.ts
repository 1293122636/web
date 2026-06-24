import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;
let adminToken: string;
let readerToken: string;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);
  const r1 = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_fineadm', password: 'admin123', name: 'Fine Admin' } });
  await prisma.user.update({ where: { id: r1.json().user.id }, data: { role: 'admin' } });
  const l1 = await app.inject({ method: 'POST', url: '/api/auth/login', payload: { username: 'it_fineadm', password: 'admin123' } });
  adminToken = l1.json().token;
  const r2 = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_finedr', password: 'reader123', name: 'Fine Reader' } });
  readerToken = r2.json().token;
}, 15000);

afterAll(async () => {
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_fine' } } });
  await prisma.$disconnect();
});

describe('Fines Integration', () => {
  it('GET /api/fines — admin can list all fines', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/fines', headers: authHeaders(adminToken) });
    expect(res.statusCode).toBe(200);
    expect(Array.isArray(res.json())).toBe(true);
  });

  it('GET /api/fines/my — reader can see own fines', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/fines/my', headers: authHeaders(readerToken) });
    expect(res.statusCode).toBe(200);
  });

  it('GET /api/fines — rejects without auth', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/fines' });
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/fines/:id/pay — admin can pay fine', async () => {
    // Create a borrower + book + borrow record first
    const reader = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_finepay', password: 'reader123', name: 'Fine Pay' } });
    const userId = reader.json().user.id;
    // Need a real borrow record — create via borrow endpoint
    const cat = await prisma.category.create({ data: { name: 'Fine Test Cat' } });
    const book = await prisma.book.create({ data: { isbn: '978-FN-000001', title: 'Fine Test Book', author: 'X', total: 1, available: 1, categoryId: cat.id } });
    await prisma.bookItem.create({ data: { barcode: 'LIB-FINE-001', callNumber: 'F001', location: 'Shelf', bookId: book.id, itemTypeId: 1 } });
    const borrow = await app.inject({ method: 'POST', url: '/api/borrows/borrow', headers: authHeaders(readerToken), payload: { bookId: book.id } });
    const borrowId = borrow.json().id;

    const fine = await prisma.fine.create({
      data: { userId, amount: 5.0, type: 'overdue', borrowRecordId: borrowId },
    });
    const res = await app.inject({
      method: 'POST', url: `/api/fines/${fine.id}/pay`,
      headers: authHeaders(adminToken),
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().paid).toBe(true);

    // Cleanup test data
    await prisma.fine.deleteMany({ where: { userId } });
    await prisma.borrowRecord.deleteMany({ where: { id: borrowId } });
    await prisma.bookItem.deleteMany({ where: { barcode: 'LIB-FINE-001' } });
    await prisma.book.deleteMany({ where: { isbn: '978-FN-000001' } });
    await prisma.category.deleteMany({ where: { name: 'Fine Test Cat' } });
  });
});
