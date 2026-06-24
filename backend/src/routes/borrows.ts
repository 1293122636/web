import { FastifyInstance } from 'fastify';
import * as borrowService from '../services/borrow.service.js';
import { requireAdmin } from '../middleware/requireAdmin.js';

export async function borrowRoutes(app: FastifyInstance) {
  app.get('/my', { onRequest: [app.authenticate] }, async (request: any) =>
    borrowService.getMyBorrows(app.prisma, request.user.id),
  );

  app.get('/', { onRequest: [app.authenticate, requireAdmin] }, async (request: any) => {
    return borrowService.getAllBorrows(app.prisma);
  });

  app.post('/borrow', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    try {
      return await borrowService.borrow(app.prisma, request.user.id, request.body);
    } catch (e: any) {
      return reply.status(e.statusCode || 400).send({ error: e.message });
    }
  });

  app.post('/return/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    try {
      return await borrowService.returnBook(
        app.prisma,
        parseInt(request.params.id),
        request.user.id,
        request.user.role === 'admin',
      );
    } catch (e: any) {
      return reply.status(e.statusCode || 404).send({ error: e.message });
    }
  });

  app.post('/renew/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    try {
      return await borrowService.renew(app.prisma, parseInt(request.params.id), request.user.id);
    } catch (e: any) {
      return reply.status(e.statusCode || 400).send({ error: e.message });
    }
  });

  app.get('/history', { onRequest: [app.authenticate] }, async (request: any) =>
    borrowService.getHistory(app.prisma, request.user.id),
  );
}
