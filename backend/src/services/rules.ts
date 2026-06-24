import { PrismaClient } from '@prisma/client';

/**
 * Borrow engine rules — used by borrow.service.ts for limit checks and loanDays.
 * (checkBorrowLimit retired; logic inlined into borrow.service.ts)
 */
const DEFAULTS = {
  maxBorrows: 5,
  loanDays: 30,
  renewals: 1,
  renewalDays: 15,
  finePerDay: 0.1,
};

export interface BorrowRule {
  maxBorrows: number;
  loanDays: number;
  renewals: number;
  renewalDays: number;
  finePerDay: number;
}

/**
 * 查询借阅规则：读者类型 × 资料类型
 * 无匹配时返回默认规则（向后兼容旧数据）
 */
export async function getRule(
  prisma: PrismaClient,
  patronCategoryId: number | null,
  itemTypeId: number | null,
): Promise<BorrowRule> {
  if (!patronCategoryId || !itemTypeId) {
    return DEFAULTS;
  }

  const rule = await prisma.circulationRule.findUnique({
    where: {
      patronCategoryId_itemTypeId: { patronCategoryId, itemTypeId },
    },
  });

  if (!rule) return DEFAULTS;

  return {
    maxBorrows: rule.maxBorrows,
    loanDays: rule.loanDays,
    renewals: rule.renewals,
    renewalDays: rule.renewalDays,
    finePerDay: Number(rule.finePerDay),
  };
}

// checkBorrowLimit retired — logic inlined into borrow.service.ts for single getRule() call
