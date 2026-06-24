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
  const r1 = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_rdadm', password: 'admin123', name: 'Reader Admin' } });
  await prisma.user.update({ where: { id: r1.json().user.id }, data: { role: 'admin' } });
  const l1 = await app.inject({ method: 'POST', url: '/api/auth/login', payload: { username: 'it_rdadm', password: 'admin123' } });
  adminToken = l1.json().token;
  const r2 = await app.inject({ method: 'POST', url: '/api/auth/register', payload: { username: 'it_rduser', password: 'reader123', name: 'Reader User' } });
  readerToken = r2.json().token;
}, 15000);

afterAll(async () => {
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_rd' } } });
  await prisma.$disconnect();
});

describe('Readers Integration', () => {
  it('GET /api/readers — admin can list readers', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/readers', headers: authHeaders(adminToken) });
    expect(res.statusCode).toBe(200);
  });

  it('GET /api/readers — rejects without auth', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/readers' });
    expect(res.statusCode).toBe(401);
  });

  it('PUT /api/readers/profile — reader can edit own profile', async () => {
    const res = await app.inject({ method: 'PUT', url: '/api/readers/profile', headers: authHeaders(readerToken), payload: { name: 'Updated Name' } });
    expect(res.statusCode).toBe(200);
    expect(res.json().name).toBe('Updated Name');
  });
});
