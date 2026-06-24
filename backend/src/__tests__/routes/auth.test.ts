import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { buildApp, makePrisma, authHeaders } from '../helpers.js';
import type { FastifyInstance } from 'fastify';
import type { PrismaClient } from '@prisma/client';

let app: FastifyInstance;
let prisma: PrismaClient;

beforeAll(async () => {
  prisma = makePrisma();
  app = await buildApp(prisma);
}, 15000);

afterAll(async () => {
  await prisma.user.deleteMany({ where: { username: { startsWith: 'it_' } } });
  await prisma.$disconnect();
});

describe('Auth Integration', () => {
  it('POST /api/auth/register — registers a new reader', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_reg', password: 'pass123', name: 'Reg Test' },
    });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.token).toBeTruthy();
    expect(body.user.role).toBe('reader');
  });

  it('POST /api/auth/register — rejects duplicate username', async () => {
    // First register
    const r1 = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_dup', password: 'pass123', name: 'Dup' },
    });
    expect(r1.statusCode).toBe(200);
    // Second register with same username
    const r2 = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_dup', password: 'pass456', name: 'Dup 2' },
    });
    expect(r2.statusCode).toBe(409);
  });

  it('POST /api/auth/login — returns token for valid credentials', async () => {
    // Seed user
    await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_login', password: 'pass123', name: 'Login' },
    });
    const res = await app.inject({
      method: 'POST',
      url: '/api/auth/login',
      payload: { username: 'it_login', password: 'pass123' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().token).toBeTruthy();
  });

  it('POST /api/auth/login — rejects bad password', async () => {
    // Seed user
    await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_badpwd', password: 'pass123', name: 'BadPwd' },
    });
    const res = await app.inject({
      method: 'POST',
      url: '/api/auth/login',
      payload: { username: 'it_badpwd', password: 'wrong' },
    });
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/auth/me — returns current user with valid token', async () => {
    const r = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_me', password: 'pass123', name: 'Me' },
    });
    const token = r.json().token;
    const res = await app.inject({
      method: 'GET',
      url: '/api/auth/me',
      headers: authHeaders(token),
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().username).toBe('it_me');
  });

  it('GET /api/auth/me — rejects without token', async () => {
    const res = await app.inject({
      method: 'GET',
      url: '/api/auth/me',
    });
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/auth/users — admin can list users', async () => {
    // Register then promote to admin via DB, then re-login for fresh token
    const r = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_adm1', password: 'pass123', name: 'Admin1' },
    });
    const userId = r.json().user.id;
    await prisma.user.update({ where: { id: userId }, data: { role: 'admin' } });
    // Re-login to get token with admin role
    const login = await app.inject({
      method: 'POST',
      url: '/api/auth/login',
      payload: { username: 'it_adm1', password: 'pass123' },
    });
    const token = login.json().token;
    const res = await app.inject({
      method: 'GET',
      url: '/api/auth/users',
      headers: authHeaders(token),
    });
    expect(res.statusCode).toBe(200);
  });

  it('GET /api/auth/users — reader forbidden', async () => {
    const r = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_rd1', password: 'pass123', name: 'Reader1' },
    });
    const token = r.json().token;
    const res = await app.inject({
      method: 'GET',
      url: '/api/auth/users',
      headers: authHeaders(token),
    });
    expect(res.statusCode).toBe(403);
  });

  it('POST /api/auth/admin/create — admin can create another admin', async () => {
    const r = await app.inject({
      method: 'POST',
      url: '/api/auth/register',
      payload: { username: 'it_master', password: 'pass123', name: 'Master' },
    });
    const userId = r.json().user.id;
    await prisma.user.update({ where: { id: userId }, data: { role: 'admin' } });
    const login = await app.inject({
      method: 'POST',
      url: '/api/auth/login',
      payload: { username: 'it_master', password: 'pass123' },
    });
    const adminToken2 = login.json().token;
    const res = await app.inject({
      method: 'POST',
      url: '/api/auth/admin/create',
      headers: authHeaders(adminToken2),
      payload: { username: 'it_staff', password: 'staff123', name: 'Staff' },
    });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.username || body.user?.username).toBe('it_staff');
    expect(body.role || body.user?.role).toBe('admin');
  });
});
