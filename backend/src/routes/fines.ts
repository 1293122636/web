import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import * as fineService from '../services/fine.service.js';
import { requireAdmin } from '../middleware/requireAdmin.js';

const fineQuerySchema = z.object({
  type: z.string().optional(),
  paid: z.enum(['true', 'false']).optional(),
});

export async function finesRoutes(app: FastifyInstance) {
  app.get('/', { onRequest: [app.authenticate, requireAdmin] }, async (request: any) => {
    const q = fineQuerySchema.safeParse(request.query);
    const filters = q.success ? q.data : {};
    return fineService.listFines(app.prisma, {
      type: filters.type,
      paid: filters.paid !== undefined ? filters.paid === 'true' : undefined,
      page: parseInt(request.query.page as string) || 1,
      limit: parseInt(request.query.limit as string) || 20,
    });
  });

  app.get('/my', { onRequest: [app.authenticate] }, async (request: any) =>
    fineService.getMyFines(app.prisma, request.user.id));

  app.post('/:id/pay', { onRequest: [app.authenticate, requireAdmin] }, async (request: any) =>
    fineService.payFine(app.prisma, parseInt(request.params.id)));
}
