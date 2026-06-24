import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;
let adminToken: string;
let categoryId: number;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);

  // Create admin via register + promote
  const r = await app.inject({
    method: 'POST',
    url: '/api/auth/register',
    payload: { username: 'it_bkadmin', password: 'admin123', name: 'Book Admin' },
  });
  await prisma.user.update({
    where: { id: r.json().user.id },
    data: { role: 'admin' },
  });
  const login = await app.inject({
    method: 'POST',
    url: '/api/auth/login',
    payload: { username: 'it_bkadmin', password: 'admin123' },
  });
  adminToken = login.json().token;

  // Create a test category
  const cat = await prisma.category.create({ data: { name: 'IT Test Category' } });
  categoryId = cat.id;

  // Seed a test book
  await prisma.book.create({
    data: {
      isbn: '978-0-00-000000-1',
      title: 'IT Test Book',
      author: 'Test Author',
      total: 1,
      available: 1,
      categoryId,
    },
  });
}, 15000);

afterAll(async () => {
  await prisma.book.deleteMany({ where: { isbn: { startsWith: '978-0-00' } } });
  await prisma.category.deleteMany({ where: { name: 'IT Test Category' } });
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_bk' } } });
  await prisma.$disconnect();
});

describe('Books Integration', () => {
  it('GET /api/books — returns paginated list', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/books?page=1&limit=5' });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.total).toBeGreaterThanOrEqual(1);
    expect(body.books.length).toBeGreaterThanOrEqual(1);
  });

  it('GET /api/books/:id — returns book detail', async () => {
    // Find the seeded book
    const book = await prisma.book.findFirst({ where: { isbn: '978-0-00-000000-1' } });
    const res = await app.inject({ method: 'GET', url: `/api/books/${book!.id}` });
    expect(res.statusCode).toBe(200);
    expect(res.json().title).toBe('IT Test Book');
    expect(res.json().isbn).toBe('978-0-00-000000-1');
  });

  it('GET /api/books/:id — 404 for non-existent', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/books/99999' });
    expect(res.statusCode).toBe(404);
  });

  it('GET /api/books/facets — returns facet groups', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/books/facets' });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.facets).toBeDefined();
    expect(Object.keys(body.facets).length).toBeGreaterThanOrEqual(1);
  });

  it('POST /api/books — admin can create book', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/api/books',
      headers: authHeaders(adminToken),
      payload: {
        isbn: '978-0-00-000000-2',
        title: 'New Test Book',
        author: 'New Author',
        year: 2024,
        categoryId,
      },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().title).toBe('New Test Book');
  });

  it('POST /api/books — rejects without auth', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/api/books',
      payload: { isbn: 'x', title: 'x', author: 'x', categoryId },
    });
    expect(res.statusCode).toBe(401);
  });

  it('DELETE /api/books/:id — admin can delete', async () => {
    // Create a disposable book
    const book = await prisma.book.create({
      data: { isbn: '978-0-00-000000-3', title: 'To Delete', author: 'X', total: 1, available: 1, categoryId },
    });
    const res = await app.inject({
      method: 'DELETE',
      url: `/api/books/${book.id}`,
      headers: authHeaders(adminToken),
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().success).toBe(true);
  });
});
