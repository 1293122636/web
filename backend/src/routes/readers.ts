import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import * as userService from '../services/user.service.js';

export async function readerRoutes(app: FastifyInstance) {
  app.get('/', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    if (request.user.role !== 'admin') return reply.status(403).send({ error: 'Admin only' });
    return userService.listReaders(app.prisma);
  });

  app.get('/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    if (request.user.role !== 'admin') return reply.status(403).send({ error: 'Admin only' });
    const reader = await userService.getReaderDetail(app.prisma, parseInt(request.params.id));
    if (!reader) return reply.status(404).send({ error: 'Reader not found' });
    return reader;
  });

  app.put('/:id', { onRequest: [app.authenticate] }, async (request: any, reply: any) => {
    if (request.user.role !== 'admin') return reply.status(403).send({ error: 'Admin only' });
    try {
      return await userService.updateReader(app.prisma, parseInt(request.params.id), request.body);
    } catch (e: any) {
      return reply.status(e.statusCode || 500).send({ error: e.message });
    }
  });

  app.put('/profile', { onRequest: [app.authenticate] }, async (request: any) => {
    return userService.updateProfile(app.prisma, request.user.id, request.body);
  });
}
