import type { FastifyRequest, FastifyReply } from 'fastify';

/**
 * Middleware: requires admin role.
 * Usage: { onRequest: [app.authenticate, requireAdmin] }
 */
export async function requireAdmin(request: FastifyRequest, reply: FastifyReply) {
  const user = (request as FastifyRequest & { user?: { role: string } }).user;
  if (!user || user.role !== 'admin') {
    throw Object.assign(new Error('Admin only'), { statusCode: 403 });
  }
}
