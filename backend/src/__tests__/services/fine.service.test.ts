import { describe, it, expect, vi } from 'vitest';
import * as fineService from '../../services/fine.service.js';

function mockPrisma() {
  return {
    fine: { findMany: vi.fn(), findUnique: vi.fn(), update: vi.fn(), count: vi.fn() },
    user: { update: vi.fn() },
    $transaction: vi.fn((ops: Promise<any>[]) => Promise.all(ops)),
    auditLog: { create: vi.fn().mockResolvedValue({}) },
  } as any;
}

describe('listFines', () => {
  it('按类型筛选', async () => {
    const prisma = mockPrisma();
    prisma.fine.findMany.mockResolvedValue([{ id: 1, type: 'overdue', amount: 5 }]);
    prisma.fine.count.mockResolvedValue(1);
    const result = await fineService.listFines(prisma, { type: 'overdue' });
    expect(result.fines).toHaveLength(1);
  });
});

describe('getMyFines', () => {
  it('返回我的罚款', async () => {
    const prisma = mockPrisma();
    prisma.fine.findMany.mockResolvedValue([]);
    const result = await fineService.getMyFines(prisma, 1);
    expect(result).toEqual([]);
  });
});

describe('payFine', () => {
  it('标记已缴并递减 totalFines', async () => {
    const prisma = mockPrisma();
    prisma.fine.findUnique.mockResolvedValue({ id: 1, amount: 10, paid: false, userId: 1 });
    prisma.fine.update.mockResolvedValue({ id: 1, paid: true });

    const result = await fineService.payFine(prisma, 1);
    expect(result.paid).toBe(true);
    expect(prisma.user.update).toHaveBeenCalledWith(
      expect.objectContaining({ data: { totalFines: { decrement: 10 } } }),
    );
  });
});
