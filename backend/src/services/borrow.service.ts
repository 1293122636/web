import { PrismaClient } from '@prisma/client'
import type {
  BorrowParams,
  BorrowRecordResponse,
  ReturnResult,
  RenewResult,
} from '../types/api.types.js'

/**
 * Borrow Orchestrator — coordinates across rules, fines, and holds domains.
 *
 * Domain dependencies:
 *   rules.js    → getRule() for loanDays / borrow limit
 *   fine.service.ts → createFine() / calcOverdueFine() for overdue charges
 *   hold.service.ts → getNextPendingHold() for reservation promotion
 *
 * All three domains are consumed via pure function calls with PrismaClient
 * passed as first argument, keeping this module testable via mock injection.
 */
import { getRule } from './rules.js'
import { createFine, calcOverdueFine } from './fine.service.js'
import { getNextPendingHold } from './hold.service.js'

async function audit(
  prisma: PrismaClient,
  userId: number,
  action: string,
  target: string,
  detail?: string,
) {
  await prisma.auditLog.create({
    data: { userId, action, target, detail },
  }).catch(() => {}); // fire-and-forget, don't block on audit failure
}

export async function getMyBorrows(
  prisma: PrismaClient,
  userId: number,
): Promise<BorrowRecordResponse[]> {
  return prisma.borrowRecord.findMany({
    where: { userId },
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
      bookItem: { select: { id: true, barcode: true, callNumber: true } },
      fines: { select: { id: true, amount: true, type: true, paid: true } },
    },
    orderBy: { borrowDate: 'desc' },
  }) as unknown as BorrowRecordResponse[];
}

export async function listBorrows(
  prisma: PrismaClient,
  page = 1,
  limit = 20,
): Promise<{ borrows: BorrowRecordResponse[]; total: number }> {
  const [borrows, total] = await Promise.all([
    prisma.borrowRecord.findMany({
      include: {
        user: { select: { id: true, name: true, username: true } },
        book: { select: { id: true, title: true, author: true, isbn: true } },
        bookItem: { select: { id: true, barcode: true, callNumber: true } },
        fines: { select: { id: true, amount: true, type: true, paid: true } },
      },
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { borrowDate: 'desc' },
    }),
    prisma.borrowRecord.count(),
  ]);
  return { borrows: borrows as unknown as BorrowRecordResponse[], total };
}

export async function borrow(
  prisma: PrismaClient,
  userId: number,
  params: BorrowParams,
): Promise<BorrowRecordResponse> {
  const { bookId, bookItemId } = params;
  if (!bookId && !bookItemId)
    throw Object.assign(new Error('bookId or bookItemId required'), { statusCode: 400 });

  let targetBookId = bookId;
  let targetItemId = bookItemId;

  if (bookItemId) {
    const item = await prisma.bookItem.findUnique({
      where: { id: bookItemId },
      include: { book: true },
    });
    if (!item) throw Object.assign(new Error('Book item not found'), { statusCode: 404 });
    if (item.status !== 'available')
      throw Object.assign(new Error('This copy is not available'), { statusCode: 400 });
    targetBookId = item.bookId;
  } else {
    const book = await prisma.book.findUnique({ where: { id: bookId! } });
    if (!book) throw Object.assign(new Error('Book not found'), { statusCode: 404 });
    const firstItem = await prisma.bookItem.findFirst({
      where: { bookId: bookId!, status: 'available' },
    });
    if (firstItem) targetItemId = firstItem.id;
  }

  const existing = await prisma.borrowRecord.findFirst({
    where: { userId, bookId: targetBookId!, status: 'active' },
  });
  if (existing)
    throw Object.assign(new Error('You already borrowed this book'), { statusCode: 400 });

  const user = await prisma.user.findUnique({ where: { id: userId } });
  const item = targetItemId
    ? await prisma.bookItem.findUnique({ where: { id: targetItemId }, include: { itemType: true } })
    : null;

  // Fetch rule once for both borrow limit check and loanDays
  const rule = await getRule(prisma, user?.patronCategoryId ?? null, item?.itemTypeId ?? null);

  // Borrow limit check (inline, no second getRule call)
  const currentCount = await prisma.borrowRecord.count({
    where: { userId, status: 'active' },
  });
  if (currentCount >= rule.maxBorrows) {
    throw Object.assign(
      new Error(`已达到借阅上限 ${rule.maxBorrows} 册`),
      { statusCode: 400 },
    );
  }
  const dueDate = new Date();
  dueDate.setDate(dueDate.getDate() + rule.loanDays);

  const [record] = await prisma.$transaction([
    prisma.borrowRecord.create({
      data: {
        userId,
        bookId: targetBookId!,
        bookItemId: targetItemId ?? null,
        dueDate,
        status: 'active',
      },
      include: {
        book: { select: { id: true, title: true, author: true, isbn: true } },
        bookItem: { select: { id: true, barcode: true } },
      },
    }),
    prisma.book.update({ where: { id: targetBookId! }, data: { available: { decrement: 1 } } }),
    ...(targetItemId
      ? [prisma.bookItem.update({ where: { id: targetItemId }, data: { status: 'borrowed' } })]
      : []),
  ]);
  audit(prisma, userId, 'borrow', `book:${targetBookId}`, item ? `item:${targetItemId}` : void 0);
  return record as unknown as BorrowRecordResponse;
}

export async function returnBook(
  prisma: PrismaClient,
  recordId: number,
  userId: number,
  isAdmin: boolean,
): Promise<ReturnResult> {
  const record = await prisma.borrowRecord.findUnique({
    where: { id: recordId },
    include: { book: true, bookItem: { include: { itemType: true } }, user: true },
  });
  if (!record) throw Object.assign(new Error('Borrow record not found'), { statusCode: 404 });
  if (record.status !== 'active')
    throw Object.assign(new Error('Already returned'), { statusCode: 400 });
  if (record.userId !== userId && !isAdmin)
    throw Object.assign(new Error('Unauthorized'), { statusCode: 403 });

  const now = new Date();
  const isOverdue = now > record.dueDate;
  const rule = await getRule(
    prisma,
    record.user.patronCategoryId ?? null,
    record.bookItem?.itemTypeId ?? null,
  );

  let fineResult: Record<string, unknown> | null = null;
  if (isOverdue) {
    const fineAmount = calcOverdueFine(record.dueDate, now, rule.finePerDay);
    fineResult = await createFine(prisma, {
      borrowRecordId: record.id,
      userId: record.userId,
      amount: fineAmount,
      type: 'overdue',
    });
  }

  // Check for pending hold BEFORE transaction (read-only, safe)
  const nextHold = await getNextPendingHold(prisma, record.bookId);
  const holdOps = nextHold && record.bookItemId
    ? [
        prisma.hold.update({
          where: { id: nextHold.id },
          data: {
            status: 'ready',
            bookItemId: record.bookItemId,
            expiryDate: new Date(Date.now() + 3 * 86400000),
          },
        }),
        prisma.bookItem.update({
          where: { id: record.bookItemId },
          data: { status: 'on_hold' },
        }),
        prisma.book.update({
          where: { id: record.bookId },
          data: { available: { decrement: 1 } },
        }),
      ]
    : [];

  const [updated] = await prisma.$transaction([
    prisma.borrowRecord.update({
      where: { id: recordId },
      data: { returnDate: now, status: isOverdue ? 'overdue' : 'returned' },
    }),
    prisma.book.update({ where: { id: record.bookId }, data: { available: { increment: 1 } } }),
    ...(record.bookItemId
      ? [
          prisma.bookItem.update({
            where: { id: record.bookItemId },
            data: { status: 'available' },
          }),
        ]
      : []),
    ...holdOps,
  ]);

  return {
    id: updated.id,
    status: isOverdue ? 'overdue' : 'returned',
    returnDate: now.toISOString(),
    fine: fineResult ? { amount: fineResult.amount, type: fineResult.type } : null,
    holdPromoted: nextHold && record.bookItemId
      ? { holdId: nextHold.id, userId: nextHold.userId }
      : null,
  };
  audit(prisma, userId, 'return', `record:${recordId}`, isOverdue ? 'overdue' : void 0);
}

export async function renew(
  prisma: PrismaClient,
  recordId: number,
  userId: number,
): Promise<RenewResult> {
  const record = await prisma.borrowRecord.findUnique({
    where: { id: recordId },
    include: { bookItem: { include: { itemType: true } }, user: true },
  });
  if (!record) throw Object.assign(new Error('Borrow record not found'), { statusCode: 404 });
  if (record.status !== 'active')
    throw Object.assign(new Error('Cannot renew'), { statusCode: 400 });
  if (record.userId !== userId) throw Object.assign(new Error('Unauthorized'), { statusCode: 403 });

  const rule = await getRule(
    prisma,
    record.user.patronCategoryId ?? null,
    record.bookItem?.itemTypeId ?? null,
  );
  if (record.renewed)
    throw Object.assign(new Error(`Already renewed (limit: ${rule.renewals}x)`), {
      statusCode: 400,
    });

  const newDue = new Date(record.dueDate);
  newDue.setDate(newDue.getDate() + rule.renewalDays);

  const updated = await prisma.borrowRecord.update({
    where: { id: recordId },
    data: { dueDate: newDue, renewed: true },
  });
  return {
    id: updated.id,
    dueDate: newDue.toISOString(),
    renewed: true,
    renewedDays: rule.renewalDays,
  };
  audit(prisma, userId, 'renew', `record:${recordId}`, `${rule.renewalDays}d`);
}

export async function getHistory(
  prisma: PrismaClient,
  userId: number,
): Promise<BorrowRecordResponse[]> {
  return prisma.borrowRecord.findMany({
    where: { userId },
    include: {
      book: { select: { id: true, title: true, author: true, isbn: true } },
      fines: { select: { id: true, amount: true, type: true, paid: true } },
    },
    orderBy: { borrowDate: 'desc' },
  }) as unknown as BorrowRecordResponse[];
}
