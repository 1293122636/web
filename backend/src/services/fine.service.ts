import { PrismaClient } from '@prisma/client';
import type { FineType } from '@prisma/client';
import type { FineListParams, FineResponse, FinePayResult } from '../types/api.types.js';

async function audit(
  prisma: PrismaClient,
  userId: number,
  action: string,
  target: string,
  detail?: string,
) {
  await prisma.auditLog.create({
    data: { userId, action, target, detail },
  }).catch(() => {});
}

export async function listFines(
  prisma: PrismaClient,
  filters: FineListParams,
): Promise<{ fines: FineResponse[]; total: number }> {
  const where: Record<string, unknown> = {};
  if (filters.type) where.type = filters.type;
  if (filters.paid !== undefined) where.paid = filters.paid;
  const page = Math.max(1, filters.page || 1);
  const limit = Math.min(100, Math.max(1, filters.limit || 20));
  const [fines, total] = await Promise.all([
    prisma.fine.findMany({
      where,
      include: {
        user: { select: { id: true, username: true, name: true } },
        borrowRecord: { select: { id: true, book: { select: { title: true } } } },
      },
      orderBy: { createdAt: 'desc' },
      skip: (page - 1) * limit,
      take: limit,
    }),
    prisma.fine.count({ where }),
  ]);
  return { fines: fines as unknown as FineResponse[], total };
}

export async function getMyFines(prisma: PrismaClient, userId: number): Promise<FineResponse[]> {
  return prisma.fine.findMany({
    where: { userId },
    include: {
      borrowRecord: { select: { id: true, book: { select: { title: true } } } },
    },
    orderBy: { createdAt: 'desc' },
  }) as unknown as FineResponse[];
}

export async function payFine(prisma: PrismaClient, fineId: number): Promise<FinePayResult> {
  const fine = await prisma.fine.findUnique({ where: { id: fineId } });
  if (!fine) throw Object.assign(new Error('Fine not found'), { statusCode: 404 });
  if (fine.paid) throw Object.assign(new Error('Already paid'), { statusCode: 400 });

  const [updated] = await prisma.$transaction([
    prisma.fine.update({
      where: { id: fineId },
      data: { paid: true, paidAt: new Date() },
    }),
    prisma.user.update({
      where: { id: fine.userId },
      data: { totalFines: { decrement: fine.amount } },
    }),
  ]);
  audit(prisma, fine.userId, 'fine_pay', `fine:${fineId}`, `¥${fine.amount}`);
  return updated as unknown as FinePayResult;
}

// ===== Borrow-time fine utilities (merged from fines.ts) =====

export async function createFine(
  prisma: PrismaClient,
  params: {
    borrowRecordId: number;
    userId: number;
    amount: number;
    type: FineType;
  },
) {
  if (params.amount <= 0) return null;

  const [fine] = await prisma.$transaction([
    prisma.fine.create({
      data: {
        borrowRecordId: params.borrowRecordId,
        userId: params.userId,
        amount: params.amount,
        type: params.type,
      },
    }),
    prisma.user.update({
      where: { id: params.userId },
      data: { totalFines: { increment: params.amount } },
    }),
  ]);

  return fine;
}

export function calcOverdueFine(dueDate: Date, returnDate: Date, finePerDay: number): number {
  const overdueMs = returnDate.getTime() - dueDate.getTime();
  const overdueDays = Math.max(0, Math.ceil(overdueMs / (1000 * 60 * 60 * 24)));
  return Math.round(overdueDays * finePerDay * 100) / 100;
}
