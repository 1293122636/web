# Library Full-Stack System — AGENTS.md

> AI agent instruction file. This file tells an AI coding agent what it needs to know to work on this project: how to build, test, navigate the code, and what rules to follow. For human-readable introduction see README.md.

## 1. Project Overview

图书馆全栈管理系统。四层架构：前端(Vue3+NaiveUI) → 路由(Fastify) → 服务(pure functions) → 数据(Prisma5+MySQL)。三层业务深度：书目→复本→规则引擎。TypeScript 全栈，monorepo（frontend/ + backend/）。

**GitHub**: https://github.com/Mrappleking/library-full-stack
**Status**: 14 audit rounds, 95 fixes, 106/106 tests pass, 45 API endpoints, vite build ✅

## 2. Commands

```bash
# Backend
cd backend
npm run dev                       # tsx src/index.ts → localhost:3000
npx vitest run                    # all tests
npx vitest run <file>             # single
npx vitest run --coverage         # coverage
npx prisma db push                # push schema to DB (dev)
npx prisma generate               # regen client after schema change
npx prisma db seed                # seed demo data (upsert, idempotent)
npx tsx prisma/seed.ts            # or directly
npx tsc --noEmit                  # type check
npm run lint                      # eslint
npm run format                    # prettier

# Frontend
cd frontend
npm run dev                       # vite → localhost:5173
npx vite build                    # production build
npx vitest run                    # component tests
```

## 3. Architecture

```
frontend/                         # Vue 3 + Vite + Naive UI
└── src/
    ├── api/           books.ts, index.ts       — typed HTTP client (fetch, no axios)
    ├── components/    13 reusables             — BookCard, HoldingsTable, SearchBar, etc.
    ├── views/
    │   ├── admin/     Dashboard,Books,Borrows  — role: admin
    │   ├── reader/    MyBorrows,Profile        — role: reader
    │   └── public/    Search,BookDetail        — no auth required
    ├── stores/        auth.ts, books.ts        — Pinia state
    ├── types/         api.ts                   — mirror of backend DTOs
    └── router/        index.ts                 — vue-router guards (role-based)

backend/                          # Fastify + Prisma 5 + MySQL
└── src/
    ├── routes/        10 files, thin (≤30 LoC) — HTTP only, delegates to services
    ├── services/      11 files, pure functions — first param always PrismaClient
    ├── types/         api.types.ts             — 45 DTO interfaces
    ├── middleware/    requireAdmin.ts           — role guard
    └── index.ts                                — Fastify setup, JWT, CORS, Helmet, error handler

prisma/
├── schema.prisma      12 models, 6 enums, 5 indexes
└── seed.ts            idempotent upsert — 20 books, 8 readers, 23 borrows, 2 fines, 3 holds
```

**Four-layer rule**: Routes parse request → call service → return. Routes NEVER call `prisma.findMany` directly. Services hold all business logic, are pure functions taking `(prisma, params)`.

## 4. API Route Table (45 endpoints)

### Auth (`/api/auth`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /register | public | Reader register |
| POST | /login | public | Returns JWT token |
| GET | /me | auth | Current user profile |
| GET | /users | admin | All users list |
| POST | /admin/create | admin | Create admin |

### Books (`/api/books`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | public | List + search + facet filters + sortBy |
| GET | /facets | public | Facet counts (campus/category/language/year) |
| GET | /:id | public | Detail + holdings + cover |
| GET | /:id/items | public | Copy list with type info |
| GET | /book-items/:barcode | public | Barcode lookup (circulation desk scan) |
| POST | / | admin | Create |
| PUT | /:id | admin | Update (total change guarded) |
| DELETE | /:id | admin | Delete (guarded: no copies or active borrows) |
| POST | /:id/reconcile | admin | Fix available count drift |

### Categories (`/api/categories`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | public | List with book count |
| POST | / | admin | Create |
| PUT | /:id | admin | Update |
| DELETE | /:id | admin | Delete (refused if has books) |

### Borrows (`/api/borrows`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /my | reader | My active borrows |
| GET | / | admin | All borrows |
| GET | /history | reader | My history (returned) |
| POST | /borrow | reader | Borrow book (interactive $transaction) |
| POST | /return/:id | reader+admin | Return (triggers hold promotion if any) |
| POST | /renew/:id | reader | Renew (once per borrow) |

### Holds (`/api/holds`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | / | reader | Create hold (only when available=0) |
| GET | /count | public | Pending count for a book |
| GET | /my | reader | My holds |
| GET | / | admin | All holds (filterable by status/bookId) |
| DELETE | /:id | reader | Cancel hold (releases item if ready) |
| POST | /:id/fulfill | admin | Fulfill ready hold → borrow |

### Readers (`/api/readers`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Reader list |
| GET | /:id | admin | Detail + borrow history |
| PUT | /:id | admin | Edit by admin |
| PUT | /profile | reader | Self-edit |

### Fines (`/api/fines`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | All fines (filter by type/paid) |
| GET | /my | reader | My fines |
| POST | /:id/pay | admin | Mark paid |

### Circulation Rules (`/api/rules`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | public | Rule matrix (patronType × itemType) |
| GET | /patron-categories | public | Patron types list |
| GET | /item-types | public | Material types list |
| PUT | / | admin | Upsert rule |

### Stats (`/api/stats`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Overview (totals: books, readers, active, overdue) |
| GET | /popular | admin | Top 20 most borrowed |
| GET | /monthly | admin | Monthly borrows (12 months, zero-filled) |

### System
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/health | public | `{ status: 'ok' }` |

## 5. Environment Variables

Required in `backend/.env`:
- `DATABASE_URL` = mysql://user:pass@127.0.0.1:3306/library
- `JWT_SECRET` = random string ≥ 32 chars

Optional: `PORT`(3000), `NODE_ENV`, `LOG_LEVEL`(info). Template at `backend/.env.example`.

## 6. Error Handling

Unified format: `{ error: string, details?: any }`. `setErrorHandler` intercepts all. Routes only `throw`, never `reply.status().send()`.

| Condition | Status |
|-----------|--------|
| Zod validation | 400 |
| Prisma P2025 (not found) | 404 |
| Prisma P2002 (unique) | 409 |
| JWT auth fail | 401 |
| Role mismatch | 403 |
| Other | 500 |

## 7. Coding Conventions

### TypeScript
- ESM everywhere: imports end with `.js` (`import './foo.js'`)
- Services: pure functions, `prisma: PrismaClient` always first param
- Routes: ≤30 LoC per handler, no Prisma calls, only Zod parse → service call → return
- All POST/PUT use Zod schema for validation

### Data Integrity (critical)
- `borrow()` / `returnBook()` / `payFine()` / `cancelHold()` / `fulfillHold()` MUST use `prisma.$transaction`
- `returnBook` when hold exists: item goes `borrowed → on_hold`, **available unchanged** (no increment→decrement net-zero)
- `expireReadyHolds`: re-check status inside transaction to prevent double-expiry
- `book.update()` total change: recalculate available **inside $transaction**
- `book.remove()`: check copies+borrows **inside $transaction**

### Database
- Schema change → run `prisma generate` + `prisma db push` + `seed`
- Select only needed fields, never `select *`
- All list endpoints paginated (default 20, max 50)

### Frontend
- All UI components from Naive UI (`n-data-table`, `n-button`, etc.)
- API calls via `frontend/src/api/` typed client (fetch, no axios)
- Store tokens in localStorage, use `Authorization: Bearer <token>` header
- No inline styles — use `<style scoped>` or Naive UI props
- No emoji icons — use `@vicons/ionicons5` SVG icons
- Component before creating: output TEMPLATE / SYSTEM / LAYOUT design block

### Git
- Branch: `<type>/<YYYYMMDD>-<description>` (type: feature/fix/refactor/docs/chore)
- Commit: `<type>: <verb> <noun>` (e.g. `feat: add book search`)
- Feature branch → PR → main. Never push directly to main.
- Commit checklist: no console.log, no hardcoded secrets, .env not in diff

### Test
- Every service needs a unit test; every route needs integration test
- Test DB: `library_test`, setup via `__tests__/setup.ts`
- Mock Prisma with `vi.fn()`, mock `$transaction` for interactive mode
- Route tests: `Fastify.inject()` — no HTTP server needed
- After every code change: verify with `curl` (backend) or `vite build` (frontend)

## 8. Pitfalls (learned the hard way)

| Situation | Consequence | Correct |
|-----------|-------------|---------|
| No `.env.example` | AI invents wrong env vars | Always maintain `.env.example` |
| Multiple error formats | Frontend parsing chaos | `setErrorHandler` + `throw` only |
| `Promise.all` for borrow/return | Inventory↔record inconsistency | Always `prisma.$transaction` |
| Inline styles in Vue | Contradicts AGENTS.md rules | Use Naive UI components |
| Schema change without `prisma generate` | TypeScript errors, build fails | Run generate immediately |
| No `curl` after backend change | Assume OK, actually 500 | Always verify with curl |
| Direct `prisma.*` in routes | Architecture violation | Delegate to service |
| `returnBook` increment+decrement available | Net-zero when hold promoted | Hold path: no available change |
| Status validation outside transaction | TOCTOU race | Validate inside `$transaction` |

## 9. Architecture Decisions

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-23 | Monorepo (frontend/ + backend/) | Small project, simple management |
| 2026-06-23 | Prisma over TypeORM | Better type generation |
| 2026-06-23 | Naive UI over Element Plus | Native TypeScript support |
| 2026-06-24 | Borrow/return must use Prisma $transaction | Prevent inventory drift |
| 2026-06-24 | db push for dev, migrate deploy for prod | Protect production data |
| 2026-06-24 | Frontend proxy /api → backend:3000 | Avoid CORS in dev |
| 2026-06-24 | Prisma 5 not 7 | Prisma 7 lacks MySQL adapter |
| 2026-06-24 | Four-layer architecture | Routes only dispatch, services hold logic |
| 2026-06-24 | Services as pure functions | Testable, consistent with existing rules.ts |
| 2026-06-24 | Zero external API (student project) | OpenLibrary covers, CSS fallback |
| 2026-06-24 | Phase 2: ESLint/Helmet/CI/Indexes/Types | Engineering excellence |
| 2026-06-24 | Hold reservation system (5 endpoints) | OPAC feature parity |
| 2026-06-24 | Light theme + dark sidebar | User request, replaces dark theme |
| 2026-06-29 | R14 audit: 13 fixes | race conditions, architecture violations |
