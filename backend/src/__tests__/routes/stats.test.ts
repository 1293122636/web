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
  const r = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_statadm', password: 'admin123', name: 'Stat Admin' } });
  await prisma.user.update({ where: { id: r.json().user.id }, data: { role: 'admin' } });
  const l = await app.inject({ method: 'POST', url: '/api/auth/login', payload: { username: 'it_statadm', password: 'admin123' } });
  adminToken = l.json().token;
}, 15000);

afterAll(async () => {
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_stat' } } });
  await prisma.$disconnect();
});

describe('Stats Integration', () => {
  it('GET /api/stats — returns overview', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/stats', headers: authHeaders(adminToken) });
    expect(res.statusCode).toBe(200);
    expect(res.json().totalBooks).toBeDefined();
  });

  it('GET /api/stats — rejects without auth', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/stats' });
    expect(res.statusCode).toBe(401);
  });
});
