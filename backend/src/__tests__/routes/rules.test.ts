import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma } from '../helpers.js';
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
});
