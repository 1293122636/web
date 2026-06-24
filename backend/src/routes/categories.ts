import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import * as categoryService from '../services/category.service.js';
import { requireAdmin } from '../middleware/requireAdmin.js';

const createSchema = z.object({
  name: z.string().min(1).max(100),
  desc: z.string().optional(),
});

export async function categoryRoutes(app: FastifyInstance) {
  app.get('/', async () => categoryService.list(app.prisma));

  app.post('/', { onRequest: [app.authenticate, requireAdmin] }, async (request: any, reply: any) => {
    const parsed = createSchema.safeParse(request.body);
    if (!parsed.success) return reply.status(400).send({ error: 'Validation failed' });
    try {
      return await categoryService.create(app.prisma, parsed.data);
    } catch (e: any) {
      return reply.status(e.statusCode || 500).send({ error: e.message });
    }
  });

  app.put('/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    if (request.user.role !== 'admin') return reply.status(403).send({ error: 'Admin only' });
    const parsed = createSchema.partial().safeParse(request.body);
    if (!parsed.success) return reply.status(400).send({ error: 'Validation failed' });
    try {
      return await categoryService.update(app.prisma, parseInt(request.params.id), parsed.data);
    } catch (e: any) {
      return reply.status(e.statusCode || 500).send({ error: e.message });
    }
  });

  app.delete('/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    if (request.user.role !== 'admin') return reply.status(403).send({ error: 'Admin only' });
    try {
      await categoryService.remove(app.prisma, parseInt(request.params.id));
      return { success: true };
    } catch (e: any) {
      if (e.statusCode === 400) return reply.status(400).send({ error: e.message });
      return reply.status(404).send({ error: 'Category not found' });
    }
  });
}
