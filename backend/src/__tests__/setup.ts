import { PrismaClient } from '@prisma/client';
import { config } from 'dotenv';
import { beforeAll, afterAll } from 'vitest';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

// Load test env from backend/.env.test
const __dirname = dirname(fileURLToPath(import.meta.url));
config({ path: ***.test' });

const DB = process.env.DATABASE_URL;

let prisma: PrismaClient;

beforeAll(async () => {
  prisma = new PrismaClient({ datasources: { db: { url: DB } } });
});

afterAll(async () => {
  await prisma?.\();
});

export { prisma, DB };
