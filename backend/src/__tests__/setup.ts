import { PrismaClient } from '@prisma/client'
import { beforeEach, afterAll } from 'vitest'

// Test database connection
const DB = process.env.TEST_DATABASE_URL || 'mysql://root:li200603@127.0.0.1:3306/library_test'

let prisma: PrismaClient

beforeEach(async () => {
  prisma = new PrismaClient({ datasources: { db: { url: DB } } })
  // Ensure schema is up to date
  // Note: in CI, schema is pushed once. For local dev, run `npx prisma db push` before tests.
})

afterAll(async () => {
  await prisma?.$disconnect()
})

// Re-export for test use
export { prisma }
