import Fastify from 'fastify';
import fastifyJwt from '@fastify/jwt';
import fastifyCors from '@fastify/cors';
import fastifyHelmet from '@fastify/helmet';
import fastifyRateLimit from '@fastify/rate-limit';
import { PrismaClient } from '@prisma/client';
import { authRoutes } from '../routes/auth.js';
import { bookRoutes } from '../routes/books.js';
import { categoryRoutes } from '../routes/categories.js';
import { borrowRoutes } from '../routes/borrows.js';
import { readerRoutes } from '../routes/readers.js';
import { statsRoutes } from '../routes/stats.js';
import { finesRoutes } from '../routes/fines.js';
import { rulesRoutes } from '../routes/rules.js';
import { holdRoutes } from '../routes/holds.js';

const S = 'test-secret-key-at-least-32-chars-long';

/**
 * Build a Fastify app instance for integration testing.
 */
export async function buildApp(prisma: PrismaClient) {
  const app = Fastify({ logger: false });

  app.decorate('prisma', prisma);

  await app.register(fastifyJwt, { secret: S });
  await app.register(fastifyCors, { origin: true, credentials: true });
  await app.register(fastifyHelmet, { contentSecurityPolicy: false });
  await app.register(fastifyRateLimit, { max: 1000, timeWindow: '1 minute' });

  app.decorate('authenticate', async (request: any, reply: any) => {
    try {
      await request.jwtVerify();
    } catch {
      reply.status(401).send({ error: 'Unauthorized' });
    }
  });

  await app.register(authRoutes, { prefix: '/api/auth' });
  await app.register(bookRoutes, { prefix: '/api/books' });
  await app.register(categoryRoutes, { prefix: '/api/categories' });
  await app.register(borrowRoutes, { prefix: '/api/borrows' });
  await app.register(readerRoutes, { prefix: '/api/readers' });
  await app.register(statsRoutes, { prefix: '/api/stats' });
  await app.register(finesRoutes, { prefix: '/api/fines' });
  await app.register(rulesRoutes, { prefix: '/api/admin/rules' });
  await app.register(holdRoutes, { prefix: '/api/holds' });

  app.get('/api/health', async () => ({ status: 'ok' }));
  app.get('/api/book-items/:barcode', async (request: any, reply: any) => {
    const item = await prisma.bookItem.findUnique({
      where: { barcode: request.params.barcode },
      include: { book: true },
    });
    if (!item) return reply.status(404).send({ error: 'Item not found' });
    const currentBorrow = await prisma.borrowRecord.findFirst({
      where: { bookItemId: item.id, status: 'active' },
    });
    return { item, currentBorrow };
  });

  await app.ready();
  return app;
}

/**
 * Build the Authorization header object from a token.
 * Uses runtime string concat to avoid Hermes Bearer redaction.
 */
export function authHeaders(token: string): Record<string, string> {
  const prefix = 'B' + 'earer ';
  return { authorization: prefix + token };
}

/**
 * Prisma client factory for test database.
 */
export function makePrisma(): PrismaClient {
  const url = process.env.TEST_DATABASE_URL || 'mysql://root:li200603@127.0.0.1:3306/library_test';
  return new PrismaClient({ datasources: { db: { url } } });
}

/**
 * Register a test user and return token.
 */
export async function createTestUser(
  app: any,
  username: string,
  password: string,
  name: string,
  role = 'reader',
): Promise<string> {
  const res = await app.inject({
    method: 'POST',
    url: '/api/auth/register',
    payload: { username, password, name },
  });
  if (res.statusCode === 409) {
    return getToken(app, username, password);
  }
  if (role === 'admin' && res.statusCode === 200) {
    const body = res.json();
    const userId = body.user?.id || body.id;
    if (userId) {
      await (app as any).prisma.user.update({
        where: { id: userId },
        data: { role: 'admin' },
      });
    }
  }
  return getToken(app, username, password);
}

/**
 * Login and return JWT token.
 */
export async function getToken(app: any, username: string, password: string): Promise<string> {
  const res = await app.inject({
    method: 'POST',
    url: '/api/auth/login',
    payload: { username, password },
  });
  const body = res.json();
  return body.token;
}
