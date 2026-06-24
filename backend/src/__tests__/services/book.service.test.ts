import { describe, it, expect, vi } from 'vitest';
import * as bookService from '../../services/book.service.js';

function mockPrisma(overrides: Record<string, any> = {}) {
  return {
    book: {
      findMany: vi.fn(),
      findUnique: vi.fn(),
      count: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
      groupBy: vi.fn(),
    },
    bookItem: { findMany: vi.fn(), groupBy: vi.fn(), count: vi.fn() },
    borrowRecord: { count: vi.fn() },
    category: { findMany: vi.fn() },
    ...overrides,
  } as any;
}

describe('list', () => {
  it('1. 空搜索 — 返回分页结果', async () => {
    const prisma = mockPrisma();
    prisma.book.findMany.mockResolvedValue([{ id: 1, title: 'Test' }]);
    prisma.book.count.mockResolvedValue(1);

    const result = await bookService.list(prisma, { page: 1, limit: 20 });
    expect(result.total).toBe(1);
    expect(result.books).toHaveLength(1);
  });

  it('2. 关键词搜索 — title/author/isbn 过滤', async () => {
    const prisma = mockPrisma();
    prisma.book.findMany.mockResolvedValue([]);
    prisma.book.count.mockResolvedValue(0);

    await bookService.list(prisma, { search: '算法' });
    const where = prisma.book.findMany.mock.calls[0][0].where;
    expect(where.OR).toBeDefined();
    expect(where.OR.length).toBe(3);
  });

  it('3. 分面过滤 — campus + year 组合', async () => {
    const prisma = mockPrisma();
    prisma.book.findMany.mockResolvedValue([]);
    prisma.book.count.mockResolvedValue(0);

    await bookService.list(prisma, { campus: '青岛', yearMin: 2020, yearMax: 2025 });
    const where = prisma.book.findMany.mock.calls[0][0].where;
    expect(where.items).toEqual({ some: { campus: '青岛' } });
    expect(where.year.gte).toBe(2020);
    expect(where.year.lte).toBe(2025);
  });
});

describe('create', () => {
  it('4. total > 0 — 自动设 available = total', async () => {
    const prisma = mockPrisma();
    prisma.book.create.mockResolvedValue({ id: 1, title: 'New', total: 3, available: 3 });

    const result = await bookService.create(prisma, {
      isbn: 'X',
      title: 'New',
      author: 'A',
      total: 3,
      categoryId: 1,
    });
    expect(result.available).toBe(3);
    const createArgs = prisma.book.create.mock.calls[0][0];
    expect(createArgs.data.available).toBe(3);
  });
});

describe('update', () => {
  it('5. total 缩小 — 检查已借出数量保护', async () => {
    const prisma = mockPrisma();
    prisma.book.findUnique.mockResolvedValue({ id: 1, total: 5, available: 2 });
    prisma.book.update.mockResolvedValue({ id: 1, total: 3, available: 0 });

    const result = await bookService.update(prisma, 1, { total: 3 });
    expect(result.total).toBe(3);
  });

  it('6. total 缩小到借出量以下 — 拒绝', async () => {
    const prisma = mockPrisma();
    prisma.book.findUnique.mockResolvedValue({ id: 1, total: 5, available: 2 }); // 3 borrowed

    await expect(bookService.update(prisma, 1, { total: 2 })).rejects.toThrow(
      'Cannot reduce total',
    );
  });
});

describe('delete', () => {
  it('7. 删除 — 调用 prisma.book.delete', async () => {
    const prisma = mockPrisma();
    prisma.bookItem.count.mockResolvedValue(0); // no copies
    prisma.borrowRecord.count.mockResolvedValue(0); // no active borrows
    prisma.book.delete.mockResolvedValue({});

    await bookService.remove(prisma, 1);
    expect(prisma.book.delete).toHaveBeenCalledWith({ where: { id: 1 } });
  });
});
