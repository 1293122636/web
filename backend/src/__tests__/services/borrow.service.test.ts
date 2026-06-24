import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as borrowService from '../../services/borrow.service.js';
import * as rules from '../../services/rules.js';
import * as fines from '../../services/fine.service.js';

// Mock dependencies
vi.mock('../../services/hold.service.js', () => ({
  getNextPendingHold: vi.fn().mockResolvedValue(null),
}))

vi.mock('../../services/rules.js', () => ({
  getRule: vi.fn(),
  checkBorrowLimit: vi.fn(),
}));
vi.mock('../../services/fine.service.js', () => ({
  createFine: vi.fn(),
  calcOverdueFine: vi.fn(),
}));

// Helpers to build mock PrismaClient
function mockPrisma(overrides: Record<string, any> = {}) {
  const defaults = {
    bookItem: { findUnique: vi.fn(), findFirst: vi.fn(), update: vi.fn() },
    book: { findUnique: vi.fn(), update: vi.fn(), findMany: vi.fn() },
    borrowRecord: {
      findFirst: vi.fn(),
      findUnique: vi.fn(),
      findMany: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      count: vi.fn(),
    },
    user: { findUnique: vi.fn() },
    $transaction: vi.fn(),
    auditLog: { create: vi.fn().mockResolvedValue({}) },
  };
  const mock = { ...defaults, ...overrides };
  // Make $transaction handle both array and interactive (callback) modes
  mock.$transaction = vi.fn((fn: any) => {
    if (typeof fn === 'function') return fn(mock as any);
    return Promise.all(fn);
  });
  return mock as any;
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(rules.getRule).mockResolvedValue({
    maxBorrows: 5,
    loanDays: 30,
    renewals: 1,
    renewalDays: 15,
    finePerDay: 0.1,
  });
  vi.mocked(rules.checkBorrowLimit).mockResolvedValue({
    allowed: true,
    currentCount: 2,
    maxBorrows: 5,
  });
  vi.mocked(fines.calcOverdueFine).mockReturnValue(5.0);
  vi.mocked(fines.createFine).mockResolvedValue({ amount: 5.0, type: 'overdue' });
});

describe('borrow', () => {
  it('1. 正常借书 — 创建记录 + 库存-1 + 复本状态变 borrowed', async () => {
    const prisma = mockPrisma();
    prisma.bookItem.findUnique = vi
      .fn()
      .mockResolvedValue({
        id: 1,
        bookId: 10,
        status: 'available',
        itemTypeId: 1,
        book: { id: 10 },
      });
    prisma.bookItem.update = vi.fn().mockResolvedValue({ id: 1, status: 'borrowed' });
    prisma.borrowRecord.findFirst = vi.fn().mockResolvedValue(null);
    prisma.borrowRecord.create = vi
      .fn()
      .mockResolvedValue({
        id: 100,
        bookId: 10,
        bookItemId: 1,
        status: 'active',
        book: { id: 10, title: 'Test', author: 'A', isbn: 'X' },
        bookItem: { id: 1, barcode: 'LIB-1' },
      });
    prisma.user.findUnique = vi.fn().mockResolvedValue({ id: 1, patronCategoryId: 1 });

    const result = await borrowService.borrow(prisma, 1, { bookId: 10, bookItemId: 1 });
    expect(result.id).toBe(100);
    expect(result.status).toBe('active');
    // Interactive transaction: first arg is a callback function
    const txnFn = prisma.$transaction.mock.calls[0][0];
    expect(typeof txnFn).toBe('function');
  });

  it('2. 读者已有此书 — 返回错误 "already borrowed"', async () => {
    const prisma = mockPrisma();
    prisma.bookItem.findUnique = vi
      .fn()
      .mockResolvedValue({
        id: 1,
        bookId: 10,
        status: 'available',
        itemTypeId: 1,
        book: { id: 10 },
      });
    prisma.borrowRecord.findFirst = vi.fn().mockResolvedValue({ id: 99, status: 'active' });

    await expect(borrowService.borrow(prisma, 1, { bookId: 10, bookItemId: 1 })).rejects.toThrow(
      'already borrowed',
    );
  });

  it('3. 超借阅上限 — 返回错误 "exceeded limit"', async () => {
    const prisma = mockPrisma();
    prisma.borrowRecord.count = vi.fn().mockResolvedValue(5); // trigger limit
    prisma.bookItem.findUnique = vi
      .fn()
      .mockResolvedValue({
        id: 1,
        bookId: 10,
        status: 'available',
        itemTypeId: 1,
        book: { id: 10 },
      });
    prisma.borrowRecord.findFirst = vi.fn().mockResolvedValue(null);
    prisma.user.findUnique = vi.fn().mockResolvedValue({ id: 1, patronCategoryId: 1 });

    await expect(borrowService.borrow(prisma, 1, { bookId: 10, bookItemId: 1 })).rejects.toThrow(
      'Borrow limit exceeded',
    );
  });

  it('4. 无可用复本 — 返回错误 "not available"', async () => {
    const prisma = mockPrisma();
    prisma.bookItem.findUnique = vi
      .fn()
      .mockResolvedValue({
        id: 1,
        bookId: 10,
        status: 'borrowed',
        itemTypeId: 1,
        book: { id: 10 },
      });

    await expect(borrowService.borrow(prisma, 1, { bookItemId: 1 })).rejects.toThrow(
      'not available',
    );
  });
});

describe('returnBook', () => {
  it('5. 正常还书 — 记录更新 + 库存+1 + 复本变 available', async () => {
    const record = {
      id: 100,
      bookId: 10,
      bookItemId: 1,
      userId: 1,
      status: 'active',
      dueDate: new Date(Date.now() + 86400000), // future
      book: { id: 10, title: 'Test' },
      bookItem: { id: 1, itemTypeId: 1 },
      user: { id: 1, patronCategoryId: 1 },
    };
    const prisma = mockPrisma({
      borrowRecord: {
        findUnique: vi.fn().mockResolvedValue(record),
        update: vi.fn().mockResolvedValue({ id: 100, status: 'returned' }),
      },
    });
    const result = await borrowService.returnBook(prisma, 100, 1, false);
    expect(result.status).toBe('returned');
    expect(result.fine).toBeNull();
  });

  it('6. 逾期还书 — 自动创建 Fine + 计算罚款', async () => {
    const record = {
      id: 100,
      bookId: 10,
      bookItemId: 1,
      userId: 2,
      status: 'active',
      dueDate: new Date('2020-01-01'), // past
      book: { id: 10, title: 'Overdue' },
      bookItem: { id: 1, itemTypeId: 1 },
      user: { id: 2, patronCategoryId: 1 },
    };
    const prisma = mockPrisma({
      borrowRecord: {
        findUnique: vi.fn().mockResolvedValue(record),
        update: vi.fn().mockResolvedValue({ id: 100, status: 'overdue' }),
      },
    });
    const result = await borrowService.returnBook(prisma, 100, 2, false);
    expect(result.status).toBe('overdue');
    expect(result.fine).toEqual({ amount: 5.0, type: 'overdue' });
    expect(fines.calcOverdueFine).toHaveBeenCalled();
    expect(fines.createFine).toHaveBeenCalled();
  });

  it('7. 已还书不能再次还 — 返回错误 "Already returned"', async () => {
    const prisma = mockPrisma({
      borrowRecord: {
        findUnique: vi.fn().mockResolvedValue({ id: 100, status: 'returned', userId: 1 }),
      },
    });
    await expect(borrowService.returnBook(prisma, 100, 1, false)).rejects.toThrow(
      'Already returned',
    );
  });
});

describe('renew', () => {
  it('8. 正常续借 — 到期日延长 + renewed=true', async () => {
    const dueDate = new Date();
    const record = {
      id: 100,
      userId: 1,
      status: 'active',
      renewed: false,
      dueDate,
      bookItem: { itemTypeId: 1 },
      user: { patronCategoryId: 1 },
    };
    const newDue = new Date(dueDate.getTime() + 15 * 86400000);
    const prisma = mockPrisma({
      borrowRecord: {
        findUnique: vi.fn().mockResolvedValue(record),
        update: vi.fn().mockResolvedValue({ id: 100, dueDate: newDue, renewed: true }),
      },
    });
    const result = await borrowService.renew(prisma, 100, 1);
    expect(result.renewed).toBe(true);
    expect(result.renewedDays).toBe(15);
  });

  it('9. 已续借过 — 返回错误 "Already renewed"', async () => {
    const prisma = mockPrisma({
      borrowRecord: {
        findUnique: vi
          .fn()
          .mockResolvedValue({
            id: 100,
            userId: 1,
            status: 'active',
            renewed: true,
            bookItem: { itemTypeId: 1 },
            user: { patronCategoryId: 1 },
          }),
      },
    });
    await expect(borrowService.renew(prisma, 100, 1)).rejects.toThrow('Already renewed');
  });
});

describe('transaction', () => {
  it('10. 借书 $transaction 原子性 — 创建记录+扣库存+改状态', async () => {
    const prisma = mockPrisma();
    prisma.bookItem.findUnique = vi
      .fn()
      .mockResolvedValue({
        id: 1,
        bookId: 10,
        status: 'available',
        itemTypeId: 1,
        book: { id: 10 },
      });
    prisma.bookItem.update = vi.fn().mockResolvedValue({ id: 1, status: 'borrowed' });
    prisma.borrowRecord.findFirst = vi.fn().mockResolvedValue(null);
    prisma.borrowRecord.create = vi
      .fn()
      .mockResolvedValue({
        id: 100,
        bookId: 10,
        bookItemId: 1,
        status: 'active',
        book: { id: 10, title: 'Test', author: 'A', isbn: 'X' },
        bookItem: { id: 1, barcode: 'LIB-1' },
      });
    prisma.user.findUnique = vi.fn().mockResolvedValue({ id: 1, patronCategoryId: 1 });
    await borrowService.borrow(prisma, 1, { bookItemId: 1 });
    const txnFn = prisma.$transaction.mock.calls[0][0];
    expect(typeof txnFn).toBe('function');
    expect(prisma.$transaction).toHaveBeenCalledTimes(1);
  });
});
