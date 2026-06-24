import { FastifyInstance } from 'fastify'
import * as holdService from '../services/hold.service.js'
import { requireAdmin } from '../middleware/requireAdmin.js'

export async function holdRoutes(app: FastifyInstance) {
  // Create hold
  app.post('/', { onRequest: [app.authenticate] }, async (request: any) =>
    holdService.createHold(app.prisma, request.user.id, request.body.bookId))

  // Cancel own hold
  app.delete('/:id', { onRequest: [app.authenticate] }, async (request: any) =>
    holdService.cancelHold(app.prisma, parseInt(request.params.id), request.user.id))

  // My holds
  app.get('/my', { onRequest: [app.authenticate] }, async (request: any) =>
    holdService.getMyHolds(app.prisma, request.user.id))

  // Admin: list all holds
  app.get('/', { onRequest: [app.authenticate, requireAdmin] }, async (request: any) =>
    holdService.listHolds(app.prisma, request.query))

  // Admin: fulfill hold
  app.post('/:id/fulfill', { onRequest: [app.authenticate, requireAdmin] }, async (request: any) =>
    holdService.fulfillHold(app.prisma, parseInt(request.params.id)))
}
