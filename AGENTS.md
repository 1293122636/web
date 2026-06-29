# Library Full-Stack System — AGENTS.md

> AI agent instruction file. Matches original TypeScript version (Fastify + Prisma) feature-by-feature.
> For human-readable introduction see README.md.

## 1. Project Overview

图书馆全栈管理系统（Spring Boot 版）。前端与后端全部对齐原版 TypeScript 系统。
四层架构：前端(Vue3+NaiveUI) → Controller → Service → Mapper(MyBatis+MySQL)。Java 全栈，Maven monorepo（frontend/ + src/）。

**Origin**: https://github.com/Mrappleking/library-full-stack
**Status**: 45 API endpoints, vite build ✅, all 13 original components ported

## 2. Commands

```bash
# Backend (Maven)
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn compile           # compile
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test              # run tests
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run   # start → :8080
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean package     # build JAR

# Seed database
mysql -h127.0.0.1 -uroot -p library < seed.sql                     # populate demo data

# Frontend
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev                    # → :5175 (proxies /api → :8080)
npm run build                  # production build

# Stop
kill -9 $(lsof -ti:8080)      # kill backend
kill -9 $(lsof -ti:5175)      # kill frontend
```

## 3. Architecture

```
src/                             # Spring Boot + MyBatis + MySQL
└── main/java/com/library/
    ├── LibraryApplication.java  # @SpringBootApplication entry
    ├── config/       3 files   — JwtAuthFilter, WebConfig (CORS), JwtAuthInterceptor
    ├── controller/   11 files  — 45 REST endpoints, @RestController
    ├── service/      9 files   — @Transactional business logic
    ├── mapper/       11 files  — MyBatis @Mapper + annotated SQL
    ├── entity/       11 files  — POJOs matching MySQL tables
    ├── dto/request/  6 files   — @Valid request bodies
    ├── dto/response/ 16 files  — Response DTOs
    ├── exception/    2 files   — AppException + @RestControllerAdvice
    └── util/         1 file    — JwtUtil

frontend/                        # Vue 3 + Vite + Naive UI
└── src/
    ├── api/          index.ts, books.ts   — Axios instance + typed API
    ├── stores/       auth.ts, books.ts   — Pinia state
    ├── router/       index.ts            — vue-router (matches original routing)
    ├── types/        api.ts              — TypeScript interfaces
    ├── composables/  index.ts            — usePagination, useDebounce
    ├── components/   13 files            — All original components ported
    │   ├── BookCard.vue / BookGrid.vue / BookDetailSection.vue
    │   ├── FacetPanel.vue / SearchBar.vue / HoldingsTable.vue
    │   ├── StatusBadge.vue / EmptyState.vue / SkeletonCard.vue
    │   ├── BarcodeInput.vue / BarcodeLabel.vue
    │   ├── LoginBg.vue / AnimatedBackground.vue
    ├── views/        17 files
    │   ├── admin/    9 pages — Dashboard,Books,Borrows,Categories,Circulation,Fines,Readers,Settings,Stats
    │   ├── reader/   4 pages — Layout,Books,MyBorrows,Profile
    │   ├── public/   2 pages — Search,BookDetail
    │   └── Login.vue
    └── App.vue
```

**REST convention**: Controller → Service → Mapper. Controllers never call Mapper directly.

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
| GET | / | public | List + search + filters + sort |
| GET | /facets | public | Facet counts |
| GET | /:id | public | Detail + holdings |
| GET | /:id/items | public | Copy list |
| POST | / | admin | Create book |
| PUT | /:id | admin | Update |
| DELETE | /:id | admin | Delete (guarded: no borrows) |
| POST | /:id/reconcile | admin | Fix available count drift |

### BookItems (`/api/book-items`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /:barcode | auth | Barcode lookup + current borrow |

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
| POST | /borrow | reader | Borrow book (@Transactional) |
| POST | /return/:id | reader+admin | Return (triggers hold promotion) |
| POST | /renew/:id | reader | Renew (once per borrow) |

### Holds (`/api/holds`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | / | reader | Create hold (when available=0) |
| GET | /count | public | Pending count for a book |
| GET | /my | reader | My holds |
| GET | / | admin | All holds (filterable) |
| DELETE | /:id | reader | Cancel hold |
| POST | /:id/fulfill | admin | Fulfill ready hold |

### Readers (`/api/readers`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Reader list |
| GET | /:id | admin | Detail + borrow records |
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
| GET | / | public | Rule matrix |
| GET | /patron-categories | public | Patron types list |
| GET | /item-types | public | Material types list |
| PUT | / | admin | Upsert rule |

### Stats (`/api/stats`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Overview (totals) |
| GET | /popular | admin | Top 20 most borrowed |
| GET | /monthly | admin | Monthly borrows (12 months) |

### System
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/health | public | `{ status: 'ok' }` |

## 5. Routing (Frontend)

```
/                  → redirect to /books
/books             → public search (BookGrid + FacetPanel, no auth)
/books/:id         → public book detail (cover + holdings + borrow/hold)
/login             → dark glassmorphism login + register modal
/admin/*           → admin layout (requires admin role)
/admin/dashboard   → stat cards + quick actions + system info
/admin/books       → CRUD table + expandable items + add copy modal
/admin/borrows     → borrow table + return with overdue warning
/admin/categories  → CRUD table
/admin/circulation → barcode scan + borrow/return queue + beep
/admin/fines       → filterable table + pay action
/admin/readers     → expandable reader list + edit modal
/admin/settings    → rules / patron types / item types tables
/admin/stats       → top 20 popular + monthly stats
/reader/*          → reader layout (requires reader role)
/reader/books      → search + category filter + borrow action
/reader/my-borrows → fine alert + active/returned tabs + holds tab + renew
/reader/profile    → editable profile form
/:pathMatch(.*)*   → 404 redirect to /books
```

## 6. Environment

Configured in `src/main/resources/application.yml`:
- `spring.datasource.url` = `jdbc:mysql://127.0.0.1:3306/library`
- `spring.datasource.username` = root
- `spring.datasource.password` = li200603
- `app.jwt.secret` = `LibraryFullStack2024JWTSecretKeyForSpringBoot`
- `app.jwt.expiration-ms` = 86400000 (24h)

**CORS**: Allowed origins include `http://localhost:5175` (Java frontend) and `http://localhost:5173` (original frontend).

**Seed data**: Run `mysql -h127.0.0.1 -uroot -p library < seed.sql` to populate with 20 books, 9 users, 23 borrows, 2 fines, 3 holds.

## 7. Error Handling

Unified format: `{ error: string, details?: string }`. `@RestControllerAdvice GlobalExceptionHandler` intercepts all.

| Condition | Status |
|-----------|--------|
| `@Valid` validation fails | 400 |
| `AppException.notFound()` | 404 |
| `AppException.conflict()` | 409 |
| JWT auth fail (filter) | 401 |
| Role mismatch (filter) | 403 |
| Other `AppException` | see code |
| Any other Exception | 500 |

## 8. Coding Conventions

### Java
- Services: `@Service`, constructor injection, `@Transactional` for multi-DAO writes
- Controllers: `@RestController`, thin dispatch, no direct Mapper calls
- Mappers: `@Mapper` with annotated SQL (`@Select`, `@Insert`, etc.)
- All POST/PUT use `@Valid` DTOs
- Use `AppException` with descriptive messages

### Data Integrity (critical)
- `borrow()` / `returnBook()` / `payFine()` / `cancelHold()` / `fulfillHold()` MUST be `@Transactional`
- `returnBook` when hold exists: item goes `borrowed → on_hold`, **available unchanged**
- Cancel hold when `ready`: release item to `available`, increment book count
- Book total change: verify against borrowed count, recalculate available
- Book delete: check copies+borrows before allowing

### Database
- Table names match original: `borrow_records`, `book_items`, etc.
- **IMPORTANT**: Column names are mixed camelCase/snake_case (e.g. `categoryId`, `created_at`). MyBatis SQL must use the actual DB column name.
- Use `map-underscore-to-camel-case: true` in MyBatis config for auto-mapping where possible.

### Frontend
- UI components from Naive UI (`n-data-table`, `n-button`, etc.)
- Icons from `@vicons/ionicons5`
- API calls via Axios instance (`frontend/src/api/index.ts`)
- Auth via Pinia store + localStorage JWT
- Use `<style scoped>` for component styles
- No emoji — use SVG icons

## 9. Pitfalls

| Situation | Consequence | Correct |
|-----------|-------------|---------|
| DB column name mismatch | SQL errors | Column names are mixed camelCase (`categoryId`) / snake_case (`created_at`) |
| Java 25 default compiler | compile error: release version 17 not supported | Use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` |
| Vite foreground detection | CLI hangs | Use `background=true` or `npm run build` |
| npm mirror needed | Install fails behind GFW | Use `--registry=https://registry.npmmirror.com` |
| Prisma vs MyBatis @Results | N+1 query issue | Prefer JOIN queries over `@One` subqueries for list endpoints |
| `@Transactional` missing | Race condition on concurrent borrow | All multi-DAO writes need `@Transactional` |

## 10. Architecture Decisions

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-29 | MyBatis over JPA | Course teaches MyBatis, consistent with lab reports |
| 2026-06-29 | Custom JwtAuthFilter over Spring Security | Keep dependencies minimal |
| 2026-06-29 | `@Transactional` over Prisma `$transaction` | Spring declarative transactions are cleaner |
| 2026-06-29 | Naive UI over Element Plus | Align with original version's UI library |
| 2026-06-29 | Axios over fetch() | Course teaches Axios, interceptor pattern cleaner |
| 2026-06-29 | CORS for 5173 + 5175 | Both original and Java frontend must coexist |
| 2026-07-01 | seed.sql over Java CommandLineRunner | Reproducible, no recompile needed |
