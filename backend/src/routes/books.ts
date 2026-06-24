import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import * as bookService from '../services/book.service.js';
import { requireAdmin } from '../middleware/requireAdmin.js';

const createSchema = z.object({
  isbn: z.string().min(1).max(20),
  title: z.string().min(1).max(200),
  author: z.string().min(1).max(100),
  publisher: z.string().optional(),
  year: z.number().int().optional(),
  total: z.number().int().min(1).default(1),
  location: z.string().optional(),
  desc: z.string().optional(),
  categoryId: z.number().int(),
});

export async function bookRoutes(app: FastifyInstance) {
  app.get('/', async (request: any) => {
    const q = request.query;
    return bookService.list(app.prisma, {
      page: parseInt(q.page) || 1,
      limit: parseInt(q.limit) || 20,
      search: q.search,
      categoryId: q.categoryId ? parseInt(q.categoryId) : undefined,
    });
  });

  app.get('/:id/items', async (request: any, reply: any) => {
    const result = await bookService.getItems(app.prisma, parseInt(request.params.id));
    if (!result) return reply.status(404).send({ error: 'Book not found' });
    return result;
  });

  // Facets — Module C 新增
  app.get('/facets', async (request: any) => {
    const q = request.query;
    return bookService.getFacets(app.prisma, {
      search: q.search,
      categoryId: q.categoryId ? parseInt(q.categoryId) : undefined,
    });
  });

  app.get('/:id', async (request: any, reply: any) => {
    const book = await bookService.getById(app.prisma, parseInt(request.params.id));
    if (!book) return reply.status(404).send({ error: 'Book not found' });
    return book;
  });

  app.post('/', { onRequest: [app.authenticate, requireAdmin] }, async (request: any, reply: any) => {
    const parsed = createSchema.safeParse(request.body);
    if (!parsed.success)
      return reply
        .status(400)
        .send({ error: 'Validation failed', details: parsed.error.flatten() });
    return bookService.create(app.prisma, parsed.data);
  });

  app.put('/:id', { onRequest: [app.authenticate, requireAdmin] }, async (request: any, reply: any) => {
    const parsed = createSchema.partial().safeParse(request.body);
    if (!parsed.success) return reply.status(400).send({ error: 'Validation failed' });
    try {
      return await bookService.update(app.prisma, parseInt(request.params.id), parsed.data);
    } catch (e: any) {
      return reply.status(e.statusCode || 404).send({ error: e.message });
    }
  });

  app.delete('/:id', { onRequest: [app.authenticate, requireAdmin] }, async (request: any, reply: any) => {
    try {
      await bookService.remove(app.prisma, parseInt(request.params.id));
      return { success: true };
    } catch {
      return reply.status(404).send({ error: 'Book not found' });
    }
  });
}
