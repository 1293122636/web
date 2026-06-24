import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);
}, 10000);

afterAll(async () => { await prisma.$disconnect(); });

describe('Rules Integration', () => {
  it('GET /api/admin/rules — returns rules list (public)', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/rules' });
    expect(res.statusCode).toBe(200);
  });

  it('GET /api/admin/rules/patron-categories — returns types', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/rules/patron-categories' });
    expect(res.statusCode).toBe(200);
  });

  it('GET /api/admin/rules/item-types — returns types', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/rules/item-types' });
    expect(res.statusCode).toBe(200);
  });

  it('PUT /api/admin/rules — admin can upsert rule', async () => {
    // Need an admin token
    const r = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_ruleadm', password: 'admin123', name: 'Rule Admin' } });
    await prisma.user.update({ where: { id: r.json().user.id }, data: { role: 'admin' } });
    const login = await app.inject({ method: 'POST', url: '/api/auth/login', payload: { username: 'it_ruleadm', password: 'admin123' } });
    const token = login.json().token;

    const res = await app.inject({
      method: 'PUT', url: '/api/admin/rules',
      headers: authHeaders(token),
      payload: { patronCategoryId: 1, itemTypeId: 1, maxBorrows: 10, loanDays: 30, renewals: 2, renewalDays: 15, finePerDay: 0.2 },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().maxBorrows).toBe(10);
  });
});
