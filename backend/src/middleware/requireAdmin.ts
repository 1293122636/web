import type { FastifyRequest, FastifyReply } from 'fastify';

/**
 * Middleware: requires admin role.
 * Usage: { onRequest: [app.authenticate, requireAdmin] }
 */
export async function requireAdmin(request: FastifyRequest, reply: FastifyReply) {
  const user = (request as any).user;
  if (!user || user.role !== 'admin') {
    return reply.status(403).send({ error: 'Admin only' });
  }
}
