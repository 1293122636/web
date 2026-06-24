import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;
let adminToken: string;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);
  const r = await app.inject({
    method: 'POST', url: '/api/auth/register',
    payload: { username: 'it_catadm', password: 'admin123', name: 'Cat Admin' },
  });
  await prisma.user.update({ where: { id: r.json().user.id }, data: { role: 'admin' } });
  const login = await app.inject({
    method: 'POST', url: '/api/auth/login',
    payload: { username: 'it_catadm', password: 'admin123' },
  });
  adminToken = login.json().token;
}, 15000);

afterAll(async () => {
  await prisma.category.deleteMany({ where: { name: { startsWith: 'IT Cat' } } });
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_cat' } } });
  await prisma.$disconnect();
});

describe('Categories Integration', () => {
  it('GET /api/categories — returns list', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/categories' });
    expect(res.statusCode).toBe(200);
    expect(Array.isArray(res.json())).toBe(true);
  });

  it('POST /api/categories — admin can create', async () => {
    const res = await app.inject({
      method: 'POST', url: '/api/categories',
      headers: authHeaders(adminToken),
      payload: { name: 'IT Cat Alpha' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().name).toBe('IT Cat Alpha');
  });

  it('POST /api/categories — rejects without auth', async () => {
    const res = await app.inject({
      method: 'POST', url: '/api/categories',
      payload: { name: 'No Auth' },
    });
    expect(res.statusCode).toBe(401);
  });

  it('PUT /api/categories/:id — admin can update', async () => {
    const cat = await prisma.category.create({ data: { name: 'IT Cat Beta' } });
    const res = await app.inject({
      method: 'PUT', url: `/api/categories/${cat.id}`,
      headers: authHeaders(adminToken),
      payload: { name: 'IT Cat Beta Updated' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().name).toBe('IT Cat Beta Updated');
  });

  it('DELETE /api/categories/:id — admin can delete empty category', async () => {
    const cat = await prisma.category.create({ data: { name: 'IT Cat Gamma' } });
    const res = await app.inject({
      method: 'DELETE', url: `/api/categories/${cat.id}`,
      headers: authHeaders(adminToken),
    });
    expect(res.statusCode).toBe(200);
  });
});
