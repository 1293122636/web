import Fastify from 'fastify';
import fastifyJwt from '@fastify/jwt';
import fastifyCors from '@fastify/cors';
import fastifyHelmet from '@fastify/helmet';
import fastifyRateLimit from '@fastify/rate-limit';
import fastifySwagger from '@fastify/swagger';
import fastifySwaggerUi from '@fastify/swagger-ui';
import { PrismaClient } from '@prisma/client';
import { authRoutes } from './routes/auth.js';
import { bookRoutes } from './routes/books.js';
import { categoryRoutes } from './routes/categories.js';
import { borrowRoutes } from './routes/borrows.js';
import { readerRoutes } from './routes/readers.js';
import { statsRoutes } from './routes/stats.js';
import { finesRoutes } from './routes/fines.js';
import { rulesRoutes } from './routes/rules.js';
import { holdRoutes } from './routes/holds.js';
import { bookItemRoutes } from './routes/bookItems.js';

const prisma = new PrismaClient();
const app = Fastify({ logger: true });

// Decorate prisma
app.decorate('prisma', prisma);

// JWT — secret must be set in env
const jwtSecret = process.env.JWT_SECRET;
if (!jwtSecret || jwtSecret.length < 32) {
  console.error('FATAL: JWT_SECRET must be at least 32 characters. Set it in backend/.env');
  process.exit(1);
}
app.register(fastifyJwt, { secret: jwtSecret });

// CORS — whitelist local development origins
app.register(fastifyCors, {
  origin: (origin, cb) => {
    const allowed = [
      'http://localhost:5173',
      'http://127.0.0.1:5173',
      'http://localhost:3000',
      'http://127.0.0.1:3000',
    ];
    // Allow same-origin (null origin) and whitelisted origins
    if (!origin || allowed.includes(origin)) {
      cb(null, true);
    } else {
      cb(null, false);
    }
  },
  credentials: true,
});

// Helmet — security headers
app.register(fastifyHelmet, {
  contentSecurityPolicy: false, // Vite HMR needs inline scripts
});

// Rate limiting — 100 requests/min per IP
app.register(fastifyRateLimit, {
  max: 100,
  timeWindow: '1 minute',
});

// Swagger — API documentation
app.register(fastifySwagger, {
  openapi: {
    info: {
      title: 'Library Full-Stack API',
      version: '1.0.0',
    },
  },
});
app.register(fastifySwaggerUi, {
  routePrefix: '/docs',
});

// setErrorHandler — unified error interception
app.setErrorHandler((error: any, _request: any, reply: any) => {
  const statusCode = error.statusCode || 500;
  const message = error.message || 'Internal Server Error';

  // Zod validation errors → 400
  if (error.name === 'ZodError' || error.validation) {
    return reply.status(400).send({ error: 'Validation failed', details: error.issues || error.validation });
  }

  // Prisma known request errors
  if (error.code) {
    switch (error.code) {
      case 'P2025': // NotFoundError
        return reply.status(404).send({ error: message });
      case 'P2002': // Unique constraint
        return reply.status(409).send({ error: 'Resource already exists' });
      default:
        break;
    }
  }

  // JWT / Auth errors
  if (statusCode === 401 || error.status === 401) {
    return reply.status(401).send({ error: 'Unauthorized' });
  }

  // Don't expose stack traces in production
  if (process.env.NODE_ENV === 'production') {
    return reply.status(statusCode).send({ error: message });
  }

  return reply.status(statusCode).send({
    error: message,
    ...(process.env.NODE_ENV !== 'production' && { stack: error.stack }),
  });
});

// Auth decorator
app.decorate('authenticate', async (request: any, reply: any) => {
  try {
    await request.jwtVerify();
  } catch {
    throw Object.assign(new Error('Unauthorized'), { statusCode: 401 });
  }
});

// Routes
app.register(authRoutes, { prefix: '/api/auth' });
app.register(bookRoutes, { prefix: '/api/books' });
app.register(categoryRoutes, { prefix: '/api/categories' });
app.register(borrowRoutes, { prefix: '/api/borrows' });
app.register(readerRoutes, { prefix: '/api/readers' });
app.register(statsRoutes, { prefix: '/api/stats' });
app.register(finesRoutes, { prefix: '/api/fines' });
app.register(rulesRoutes, { prefix: '/api/rules' });
app.register(holdRoutes, { prefix: '/api/holds' });
app.register(bookItemRoutes, { prefix: '/api/book-items' });

// Health check
app.get('/api/health', async () => ({ status: 'ok' }));

// Root redirect to frontend
app.get('/', async (_request: any, reply: any) => {
  return reply.redirect('http://localhost:5173');
});

// Start
const start = async () => {
  try {
    // Validate required env vars
    if (!process.env.DATABASE_URL) {
      console.error('FATAL: DATABASE_URL is not set. Copy backend/.env.example to backend/.env and fill in values.');
      process.exit(1);
    }

    const port = parseInt(process.env.PORT || '3000');
    await app.listen({ port, host: '0.0.0.0' });
    console.log(`Server running on http://localhost:${port}`);
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();

export { app, prisma };
