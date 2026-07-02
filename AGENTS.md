# Library Full-Stack — AGENTS.md

> AI agent instruction file for the Java (Spring Boot) version.
> Must match the original TypeScript version (Fastify + Prisma + NaiveUI) feature-by-feature.
> For human-readable introduction see README.md.

## 1. Project Overview

图书馆全栈管理系统（Spring Boot 3 版）。四层架构：Vue3+NaiveUI（`frontend/`）→ Controller → Service → Mapper(MyBatis+XML) → MySQL。

Origin: https://github.com/Mrappleking/library-full-stack
Status: 45 API endpoints | 71 Java files | 31 Vue files | 9 XML mappers | 96 tests

## 2. Commands

```bash
# 关键：必须先设置 JAVA_HOME（WSL Java 25 不兼容此项目）
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Backend
./mvnw compile                                          # compile
./mvnw test                                             # 96 tests, 0 failures required
./mvnw clean package -DskipTests                        # build JAR (skip tests)
./start.sh                                              # build + run -> :8080 (dev)
./start.sh prod                                         # build + run -> :8080 (prod)

# Seed database
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS library"
mysql -uroot -p library < seed.sql

# Frontend
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev                           # -> :5175
npm run build                         # production build

# Kill
kill -9 $(lsof -ti:8080) $(lsof -ti:5175)
```

**`./start.sh` 内部流程：** `./mvnw clean package -DskipTests -q && java -jar target/*.jar --spring.profiles.active=dev`

**数据库密码传入：** `DB_PASSWORD=yourpwd ./start.sh`

**前端导入路径注意：** `views/admin/Books.vue` 引用 `components/` → `../../components/XXX`。迁移 Vue 文件后必须 `npm run build` 全量验证。

## 3. Architecture

```
src/                             # Spring Boot + MyBatis + MySQL
main/java/com/library/
  LibraryApplication.java        # @SpringBootApplication
  config/    3 files             JwtAuthFilter, WebConfig (CORS), JwtAuthInterceptor
  controller/ 11 files           45 REST endpoints
  service/   9 files             @Transactional business logic
  mapper/    11 files            MyBatis @Mapper interfaces (SQL in XML)
  entity/    11 files            POJO matching MySQL tables
  dto/request/  6 files          @Valid request bodies
  dto/response/ 16 files         Response DTOs
  exception/  2 files            AppException + @RestControllerAdvice
  util/      1 file              JwtUtil

frontend/                        # Vue 3 + Vite + Naive UI
  src/
    api/      index.ts, books.ts  Axios + typed API
    stores/   auth.ts, books.ts   Pinia
    router/   index.ts            vue-router (matches original)
    types/    api.ts              TypeScript interfaces
    composables/  index.ts        usePagination, useDebounce
    components/  13 files         All original components ported
    views/       17 files
    App.vue
```

### 3.1 认证流程

```
JwtAuthFilter (OncePerRequestFilter)
  ├── isPublicPath()? → 放行
  └── 需认证 → 解析 Bearer token → request attributes (userId, userRole)
       └── Controller @RequestAttribute 获取 → Service 层用 Interceptor 校验角色
```

**Public paths**（新增公共端点时同步更新 `JwtAuthFilter.isPublicPath()`）:
`POST /api/auth/login`, `POST /api/auth/register`, `GET /api/health`,
`GET /api/books/**`, `GET /api/categories/**`, `GET /api/rules/**`,
`GET /api/holds/count`

### 3.2 CORS

`WebConfig.java` 允许来源 5173+5175（allowCredentials=true 禁通配符）。新增端口同步添加。

## 4. API Route Table (45 endpoints)

### Auth (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /register | public | Reader register |
| POST | /login | public | Returns JWT |
| GET | /me | auth | Current user profile |
| GET | /users | admin | All users |
| POST | /admin/create | admin | Create admin |

### Books (`/api/books`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | public | List + search + filters + sort + pagination |
| GET | /facets | public | Facet counts |
| GET | /:id | public | Detail + holdings |
| GET | /:id/items | public | Copy list |
| POST | / | admin | Create book |
| PUT | /:id | admin | Update |
| DELETE | /:id | admin | Delete (guarded) |
| POST | /:id/reconcile | admin | Fix available count |

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
| GET | /my | reader | My borrows |
| GET | / | admin | All borrows |
| GET | /history | reader | My history |
| POST | /borrow | reader | Borrow (@Transactional) |
| POST | /return/:id | reader+admin | Return (hold promotion) |
| POST | /renew/:id | reader | Renew (once per borrow) |

### Holds (`/api/holds`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | / | reader | Create hold |
| GET | /count | public | Pending count |
| GET | /my | reader | My holds |
| GET | / | admin | All holds |
| DELETE | /:id | reader | Cancel hold |
| POST | /:id/fulfill | admin | Fulfill ready hold |

### Readers (`/api/readers`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Reader list |
| GET | /:id | admin | Detail + borrows |
| PUT | /:id | admin | Edit by admin |
| PUT | /profile | reader | Self-edit |

### Fines (`/api/fines`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | All (filterable by type/paid) |
| GET | /my | reader | My fines |
| POST | /:id/pay | admin | Mark paid |

### Rules (`/api/rules`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | public | Rule matrix |
| GET | /patron-categories | public | Patron types |
| GET | /item-types | public | Material types |
| PUT | / | admin | Upsert rule |

### Stats (`/api/stats`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | / | admin | Overview (totalBooks, totalReaders, totalCategories, activeBorrows, overdueCount) |
| GET | /popular | admin | Top 20 (JOIN books + category) |
| GET | /monthly | admin | Monthly (12 months) |

### System

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/health | public | `{ status: 'ok' }` |

## 5. Environment

| 配置 | 位置 | 说明 |
|------|------|------|
| application.yml | src/main/resources/ | 通用：server.port=8080, MyBatis map-underscore-to-camel-case=true |
| application-dev.yml | src/main/resources/ | Dev：Druid 数据源、DB 密码 `${DB_PASSWORD:li200603}`、JWT 密钥 `${JWT_SECRET:LibraryFullStack2024JWTSecretKeyForSpringBoot}` |
| application-prod.yml | src/main/resources/ | Prod：DB 密码 `${DB_PASSWORD}`、JWT 密钥 `${JWT_SECRET}` |

**安全警告：** dev.yml 用 `${VAR:default}` 占位符。禁止在注释中写密码、禁止硬编码密码字符串、禁止提交含密码的 dev.yml 到 GitHub。

**Java 版本：** pom.xml target=17，但本地环境是 Java 21。**Java 25 不兼容** — `./mvnw test` 前必须设 `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`。

**Druid 数据源警告：** 日志 `testWhileIdle is true, validationQuery not set` 是良性警告。去除可加 `spring.datasource.druid.validation-query: SELECT 1`。

## 6. Error Handling

| Condition | HTTP | ErrorResponse |
|-----------|------|---------------|
| `@Valid` fails | 400 | `{ error: "请求参数校验失败", detail: "字段1: 错误消息" }` |
| `AppException.notFound()` | 404 | `{ error: "用户不存在", detail: null }` |
| `AppException.conflict()` | 409 | `{ error: "用户名已存在", detail: null }` |
| JWT auth fail | 401 | `{ error: "Unauthorized" }` |
| Role mismatch | 403 | `{ error: "Forbidden" }` |
| Other Exception | 500 | `{ error: "Internal Server Error" }` |

用户可见错误消息全部用中文。所有 `throw AppException.*()` 处检查是否已中文化。

## 7. Coding Conventions

### Java

- **Services:** `@Service`, constructor injection, `@Transactional` for multi-DAO writes
- **Controllers:** `@RestController`, thin dispatch, no mapper calls. Get userId/role via `@RequestAttribute`
- **Mappers:** Interface only. **所有 SQL 在 XML** — 不在 Java 注解写 `@Select`/`@Insert` 等（注解+XML 重复会报 `Mapped Statements collection already contains key`）
- **DTOs:** POST/PUT 全部用 `@Valid` + `jakarta.validation` 注解
- **Errors:** `AppException` 带中文描述
- **@Transactional 方法列表（15 个）：** `HoldService.create/cancel/fulfill`, `FineService.create/pay`, `UserService.updateByAdmin`, `BookService.create/update/delete/reconcile`, `BorrowService.borrow/returnBook/renew`, `CategoryService.create/update/delete`

### SQL (XML mappers only)

- **9 个 Mapper XML：** `BookMapper.xml`, `BookItemMapper.xml`, `BorrowRecordMapper.xml`, `CategoryMapper.xml`, `CirculationRuleMapper.xml`, `FineMapper.xml`, `HoldMapper.xml`, `UserMapper.xml`
- **DOCTYPE 必须用官方格式：** `-//mybatis.org//DTD Mapper 3.0//EN`
- **Mapper 接口 `@Param` 名必须与 XML `#{xxx}` 完全一致** — 如 `updateToReady` 有 3 个参数但没 `status`，XML 不能写 `#{status}`
- **动态 SQL：** `<if>`, `<choose>`, `<where>` 标签
- `<resultMap>` 替代 `@Results` 注解

### 数据库列名陷阱（CRITICAL）

数据库列名混用 `categoryId`（camelCase）和 `created_at`（snake_case）。**永远不要猜测列名，必须 `SHOW CREATE TABLE` 确认。**

**关键规则：** `map-underscore-to-camel-case: true` 只帮 MyBatis 映射查询结果（`total_fines` → Java `totalFines`），
**但 INSERT/UPDATE SQL 中的列名必须是实际 DB 列名**。写错 `totalFines` 报 `Unknown column`。

全表列名对照：

| 表 | camelCase | snake_case |
|----|-----------|------------|
| users | `patronCategoryId` | `total_fines`, `created_at`, `updated_at` |
| books | `categoryId`, `clcNumber`, `physicalDesc` | `created_at`, `updated_at` |
| book_items | `bookId`, `itemTypeId`, `callNumber` | `acquired_at`, `created_at`, `updated_at` |
| borrow_records | `userId`, `bookId`, `bookItemId` | `borrow_date`, `due_date`, `return_date`, `created_at`, `updated_at` |
| circulation_rules | `patronCategoryId`, `itemTypeId`, `maxBorrows`, `loanDays`, `renewals`, `renewalDays`, `finePerDay` | `created_at` |
| fines | `borrowRecordId`, `userId` | `paid_at`, `created_at` |
| holds | `userId`, `bookId`, `bookItemId` | `request_date`, `expiry_date`, `fulfilled_at`, `created_at` |
| item_types | `loanDays`, `fineRate` | `created_at` |
| categories/patron_categories | — | `created_at`, `updated_at` |

### 前端

- **UI：** Naive UI（`n-data-table`, `n-button`, `n-modal` 等）
- **Icons：** `@vicons/ionicons5` — 不用 emoji，用 SVG 线条图标
- **API：** Axios via `api/index.ts` — interceptor 自动加 Bearer token
- **Auth：** localStorage (token + user JSON), Pinia store
- **Routing：** 必须匹配原版精确。守卫逻辑：

```
/login, /books, /books/:id — public，已登录从 /login 跳角色首页
其他路径 — 未登录跳 /login
meta.role 校验 — 角色不匹配跳角色首页
/:pathMatch(.*)* → 404 redirect to /books
```

- **Axios vs fetch 注意：** 原版 fetch 直出 body（`res.data` = 业务数据），Axios 封装（`res.data` = HTTP body 如 `{ book, items }`）。迁移时检查每个 `.data` 层级是否匹配。

**前端路由表：**

```
/                  → /books (public)
/books             → BookGrid + FacetPanel + SearchBar
/books/:id         → BookDetail + HoldingsTable + borrow/hold
/login             → LoginBg + glassmorphism + register modal
/admin/*           → admin layout (role:admin)
  /admin/dashboard, /admin/books, /admin/borrows, /admin/categories
  /admin/circulation, /admin/fines, /admin/readers, /admin/settings, /admin/stats
/reader/*          → reader layout (role:reader)
  /reader/books, /reader/my-borrows, /reader/profile
/:pathMatch(.*)*   → /books
```

### 数据完整性

- 并发 borrow：必须在 `@Transactional` **事务内**重查可用性，不在外部检查
- `returnBook` 有 pending hold 时：item `borrowed → on_hold`，book.available 不变
- Cancel hold(ready)：item `on_hold → available`，增加 book.available
- Book delete：检查 copies+borrows，有则拒绝

**Hold/Borrow 状态机：**
```
borrow() → available → borrowed

returnBook() WITH pending hold → borrowed → on_hold (→ fulfill → borrowed)
returnBook() WITHOUT hold     → borrowed → available
cancelHold(ready)             → on_hold → available
cancelHold(pending)           → 仅取消预约，item 不变
```

**预约提升流程：** `BorrowService.returnBook()` 中 `findNextPendingByBookId` → `updateToReady(bookItemId, now+3d)` → `updateStatus('on_hold')`。`updateToReady` 接口没有 `status` 参数，XML 必须硬编码 `status='ready'`，不能用 `#{status}`。

### 测试

- Service：`@ExtendWith(MockitoExtension.class)`，mock 所有 mapper
- Controller：`@SpringBootTest + @AutoConfigureMockMvc + @ActiveProfiles("test")`，`@MockBean` mock service
- 未来集成测试：`src/test/resources/application-test.yml` 配 H2（`MODE=MYSQL`）
- **提交前必须 `./mvnw test`，55 tests 0 failure**

## 8. Error Zone（Pitfalls）

| # | 错误的操作 | 后果 | 正确做法 |
|---|-----------|------|---------|
| 1 | `mvn spring-boot:run` 直接运行 | OOM killer (exit 137) | `./start.sh` 或 `java -jar target/*.jar` |
| 2 | 数据库列名写成 camelCase | MyBatis `Unknown column` | `SHOW CREATE TABLE` 确认，INSERT/UPDATE 用实际列名 |
| 3 | SQL 嵌在 Java 注解字符串中 | 动态 SQL 不可读不可调 | 抽到 `mappers/*.xml` |
| 4 | `@Transactional` 缺失 | 并发导致 available 负数 | 多 DAO 写入全加 `@Transactional` |
| 5 | 注解+XML 同一定义同一 statement | `Mapped Statements collection already contains key` | 移除 Java 注解中的 `@Select`/`@Insert` 等 |
| 6 | 事务外部查可用性 | 竞态双借同一本 | `@Transactional` 内重查 |
| 7 | Mapper 参数与 XML #{xxx} 不匹配 | MyBatis binding 异常，500 | `updateToReady` 无 `status` 参数，XML 用 `status='ready'` |
| 8 | XML DOCTYPE 格式错 | 解析器不兼容 | `-//mybatis.org//DTD Mapper 3.0//EN` |
| 9 | dev.yml 密码泄漏 | Git 提交暴露敏感信息 | `${DB_PASSWORD:default}` 占位符 |
| 10 | 新 API 未加 public path | 未登录用户 401 | 同步更新 `JwtAuthFilter.isPublicPath()` |
| 11 | seed.sql 前没建库 | `No database selected` | 先 `CREATE DATABASE IF NOT EXISTS library` |
| 12 | Java 25 编译 | `release version 17 not supported` | `export JAVA_HOME=.../java-21-openjdk-amd64` |
| 13 | 错误消息未中文化 | 用户看不懂提示 | 扫 `throw AppException.*()` 全翻译 |
| 14 | 连续 mvn 命令 | exit 137 OOM | 一次 `package` 替代多次 `compile` |

## 9. Architecture Decisions

只追加不删旧记录。

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-06-29 | MyBatis over JPA | Course teaches MyBatis |
| 2026-06-29 | Custom JwtAuthFilter over Spring Security | Keep dependencies minimal |
| 2026-06-29 | @Transactional over Prisma $transaction | Declarative transactions cleaner |
| 2026-06-29 | Naive UI over Element Plus | Align with original version |
| 2026-06-29 | Axios over fetch() | Course teaches Axios |
| 2026-06-30 | CORS for 5175 + 5173 | Migration coexistence |
| 2026-06-30 | seed.sql over Java CommandLineRunner | Reproducible, no recompile |
| 2026-06-30 | SQL from annotations to XML mappers | Readability + maintainability |
| 2026-06-30 | Maven Wrapper (./mvnw) | Consistent builds |
| 2026-06-30 | java -jar over mvn spring-boot:run | Avoids OOM |
| 2026-06-30 | application-dev/prod profiles | Password isolation |
| 2026-06-30 | H2 for tests | No MySQL in CI |
| 2026-06-30 | 后端错误消息使用中文 | 目标用户中文 |
| 2026-06-30 | UserMapper.xml 列名 totalFines → total_fines | 对齐实际列名 |

## 10. Database & Seed

11 张表：`patron_categories`, `item_types`, `categories`, `circulation_rules`, `users`, `books`, `book_items`, `borrow_records`, `fines`, `holds`, `audit_logs`。

Seed data（`seed.sql`）：3 patron types, 3 item types, 9 rules, 9 users, 20 books, 63 items, 23 borrows, 2 fines, 3 holds。

**schema.sql：** 简化版 DDL（省略 FK/索引/ENUM/部分 NOT NULL）。完整 DDL 用 `mysqldump --no-data library` 导出。

## 11. File Count Verification

每次变更后验证：

```bash
# API endpoints
grep -rn '@\(GetMapping\|PostMapping\|PutMapping\|DeleteMapping\)' src/main/java/com/library/controller/ | wc -l    # 45
# Tests
grep -rn '@Test' src/test/ --include='*.java' | wc -l      # 55
# Java files
find src/main -name '*.java' | wc -l                       # 71
# Vue files
find frontend/src -name '*.vue' | wc -l                    # 31
# XML mappers
find src -name '*.xml' -path '*/mappers/*' | wc -l         # 9
```
