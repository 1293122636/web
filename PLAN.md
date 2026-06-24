# Library Full-Stack — 架构与实施计划

> 2026-06-24 | v2.0 模块化版

## 架构原则

```
┌── FRONTEND ────────────────────────────┐
│ views/ → composables/ → stores/ → api/ │
├── BACKEND: Routes ────────────────────┤
│ 路由层: 解析请求 → 调 service → 返回    │
├── BACKEND: Services ──────────────────┤
│ 业务层: 纯函数(prisma, params) → 数据   │
├── BACKEND: Data ──────────────────────┤
│ Prisma Client + DTO 类型定义            │
├── DATABASE ───────────────────────────┤
│ MySQL + Schema + Migrations + Indexes  │
└───────────────────────────────────────┘
```

## 外部依赖约束

| 禁用 | 原因 | 替代 |
|------|------|------|
| 超星 cover API | 需授权 | OpenLibrary Covers + CSS 占位 |
| findsdust.libsp.cn | 封闭系统 | MySQL GROUP BY 自建分面 |
| 豆瓣 API | 需 Referer | 仅 fallback，主用 OpenLibrary |

> OPAC 采集数据 (`resources/opac-data-collection.md`) 仅供设计参考。

---

## 测试体系

### 五层测试架构

```
┌── 5. Manual E2E  ── 关键用户流程手动验证 (Module G)
├── 4. Component    ── Vue 组件渲染 + Pinia stores (vitest + @vue/test-utils)
├── 3. API 集成     ── Fastify inject() + 测试数据库 (vitest)
├── 2. Service 单元 ── 纯函数测试，mock Prisma (vitest + vi.mock)
└── 1. 回归基线     ── curl 对比 Step 0 基线 (每模块后自动跑)
```

### 测试目录结构

```
backend/src/
├── __tests__/
│   ├── setup.ts              ← 测试数据库初始化
│   ├── helpers.ts             ← buildApp(), createTestUser()
│   ├── services/
│   │   ├── auth.service.test.ts
│   │   ├── book.service.test.ts
│   │   ├── borrow.service.test.ts
│   │   ├── user.service.test.ts
│   │   ├── category.service.test.ts
│   │   ├── stats.service.test.ts
│   │   ├── fine.service.test.ts
│   │   └── rule.service.test.ts
│   └── routes/
│       ├── auth.test.ts
│       ├── books.test.ts
│       ├── borrows.test.ts
│       ├── readers.test.ts
│       ├── categories.test.ts
│       ├── stats.test.ts
│       ├── fines.test.ts
│       └── rules.test.ts

frontend/src/
├── __tests__/
│   ├── setup.ts
│   ├── components/
│   │   ├── BookCard.test.ts
│   │   ├── FacetPanel.test.ts
│   │   ├── HoldingsTable.test.ts
│   │   └── StatusBadge.test.ts
│   ├── stores/
│   │   ├── auth.test.ts
│   │   └── books.test.ts
│   └── composables/
│       ├── usePagination.test.ts
│       └── useDebounce.test.ts
```

### 测试工具

| 层级 | 工具 | 原因 |
|------|------|------|
| Unit (services) | vitest + vi.mock | 快，原生 ESM |
| API 集成 | vitest + Fastify inject() | 不需要启动真实 HTTP |
| 组件 | vitest + @vue/test-utils | Vue 官方推荐 |
| Store | vitest + @pinia/testing | Pinia 官方 |
| 回归 | curl + diff + jq | 零依赖 |

> 不做 Playwright/Cypress E2E — 学生项目太重。用 Manual E2E Checklist 替代。

### 覆盖率目标

| 层级 | 目标 | 关键路径 |
|------|------|---------|
| Services | 80%+ | **borrow.service 必须 90%+**（涉及金钱和库存） |
| Routes | 60%+ | 重点：认证、借书、还书 |
| Components | 50%+ | 核心组件（BookCard, HoldingsTable, FacetPanel） |
| Stores | 80%+ | Pinia actions 逻辑 |

### 每模块测试要求

| 模块 | 必须写的测试 |
|------|------------|
| **A** 后端地基 | service 单元测试 ×8 + route 集成测试 ×8 |
| **B** 数据层 | seed 验证测试 (npx prisma db seed && 检查数据) |
| **C** 后端功能 | book.service 新增 list/facets/detail 测试 + cover.service 测试 |
| **D** 前端地基 | 组件测试(核心4) + store 测试(2) + composable 测试(2) |
| **E** 前端功能 | 无新测试（组件已在 D 测过） |
| **F** 流通台 | Circulation 页面手动 checklist |
| **G** 集成 | 端到端手动 checklist + 回归基线全量对比 |

### 关键测试用例

#### borrow.service.test.ts（最重要 — 涉及金钱+库存）

```
□ borrow() 正常借书 — 创建记录 + 库存-1 + 复本状态变 borrowed
□ borrow() 读者已有此书 — 返回错误 "already borrowed"
□ borrow() 超借阅上限 — 返回错误 "exceeded limit"
□ borrow() 无可用复本 — 返回错误 "no available copy"
□ returnBook() 正常还书 — 记录更新 + 库存+1 + 复本变 available
□ returnBook() 逾期还书 — 自动创建 Fine + totalFines 累加
□ returnBook() 工具书 — 无罚款（finePerDay=0）
□ renew() 正常续借 — 到期日延长 + renewed=true
□ renew() 已续借过 — 返回错误 "already renewed"
□ borrow/return $transaction 原子性 — 一半失败则全回滚
```

#### book.service.test.ts

```
□ list() 空搜索 — 返回分页结果
□ list() 关键词 — title/author/isbn 过滤
□ list() 分面过滤 — campus + year 组合
□ getFacets() — 返回各维度计数
□ create() + total > 0 — 自动设 available
□ update() total 缩减小 — 检查已借出数量保护
□ delete() — 有活跃借阅时拒绝
```

#### auth.service.test.ts

```
□ register() 正常 — 返回 user+token
□ register() 用户名重复 — 409
□ login() 正确密码 — 200
□ login() 错误密码 — 401
□ me() 有效 token — 返回用户信息
□ me() 无效 token — 401
```

### 测试数据库

```typescript
// __tests__/setup.ts
// 每个 test suite 前:
// 1. 连接测试专用 MySQL 数据库 (library_test)
// 2. db push schema
// 3. 运行 seed
// 4. 每个测试用例用事务包裹，完成后回滚
```

### 测试命令

```bash
# 后端全部测试
cd backend && npx vitest run

# 后端单个 service
cd backend && npx vitest run src/__tests__/services/borrow.service.test.ts

# 前端全部测试
cd frontend && npx vitest run

# 覆盖率
cd backend && npx vitest run --coverage
```

### 评估矩阵（每模块完成后打分）

| 维度 | 权重 | 测量方式 | 通过标准 |
|------|------|---------|---------|
| 功能正确 | 40% | 测试通过率 | 100% pass |
| 类型安全 | 20% | `tsc --noEmit` | 零错误 |
| API 一致 | 20% | 基线 diff | 零差异 |
| 代码质量 | 10% | `wc -l routes` + `grep app.prisma` | 路由≤60行 & 零裸调 |
| 覆盖率 | 10% | vitest --coverage | 达标层级的 80%+ |

**评估结论**: 五项全过 = ✅PASS。任一项不过 = ❌不进下一步。

```
Module A: 后端地基 (4.5h)
  ├── types/api.types.ts + 8 services + 路由瘦身
  └── 不变规则: 零 API 变化，curl 基线对照

Module B: 数据层 (1h)
  └── Schema +7字段 + Seed (SDUST 一期联动)

Module C: 后端功能 (2h)
  └── 分面搜索 + 封面服务

Module D: 前端地基 (4h)
  ├── typed API + Pinia stores + composables
  └── 11个可复用组件

Module E: 前端功能 (3h)
  └── 搜索页 + 详情页 (对齐 OPAC)

Module F: 流通台 (2h)
  └── 收银模式 (偷懒实现，5行后端)

Module G: 集成 (2h)
  ├── 端到端测试 + 文档
  └── 还书→预约联动
```

### 依赖图

```
Module A ──┬──→ B ──→ C ──┬──→ E ──→ G
           │                │
           └──→ D ──────────┼──→ F ──→ G
```

- A→B→C: 后端线性依赖
- D: 可和 B+C 并行（类型已在 A 就绪）
- E: 等 C+D
- F: 等 D 即可

### 验收体系（每模块通用）

| 层级 | 命令 | 标准 |
|------|------|------|
| TypeScript | `npx tsc --noEmit` | 零错误 |
| API | `diff <(curl) 基线` | 一致 |
| 构建 | `npm run build` | 零错误 |
| 路由纯度 | `grep "app.prisma" routes/` | 零结果 |

---

## Module A: 后端地基

**状态**: 待开始
**预计**: 4.5h
**前置**: 项目可运行

### 文件变更

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | `backend/src/types/api.types.ts` | 35 端点 × 2 (Params/Response) = 70 类型 |
| 新建 | `backend/src/services/auth.service.ts` | register, login, getMe, listUsers, createAdmin |
| 新建 | `backend/src/services/book.service.ts` | list, getById, getItems, create, update, delete |
| 新建 | `backend/src/services/borrow.service.ts` | myBorrows, allBorrows, borrow, returnBook, renew, history |
| 新建 | `backend/src/services/user.service.ts` | listReaders, getDetail, updateReader, updateProfile |
| 新建 | `backend/src/services/category.service.ts` | list, create, update, delete |
| 新建 | `backend/src/services/stats.service.ts` | overview, popular, monthly |
| 新建 | `backend/src/services/fine.service.ts` | listFines, myFines, payFine |
| 新建 | `backend/src/services/rule.service.ts` | listRules, patronCategories, itemTypes, upsert |
| 保留 | `backend/src/services/rules.ts` | getRule, checkBorrowLimit (已有，不动) |
| 保留 | `backend/src/services/fines.ts` | createFine, calcOverdueFine (已有，不动) |
| 重写 | `backend/src/routes/*.ts` (8个) | 每个 handler ≤30行，只调 service |

### 服务函数签名

```typescript
// 统一模式: 纯函数，prisma 第一参数
export async function list(prisma: PrismaClient, params: ListParams): Promise<ListResponse>

// Route 调用:
app.get('/', async (request) => {
  return bookService.list(app.prisma, parseQuery(request.query))
})
```

### 执行顺序（8 子步）

| # | 文件 | 复杂度 | 验证 |
|---|------|--------|------|
| 1 | `category.service.ts` | 低 | curl /api/categories |
| 2 | `user.service.ts` | 低 | curl /api/readers |
| 3 | `stats.service.ts` | 低 | curl /api/stats 三端点 |
| 4 | `rule.service.ts` | 低 | curl /api/admin/rules |
| 5 | `fine.service.ts` | 中 | curl /api/fines |
| 6 | `auth.service.ts` | 中 | curl /api/auth/login + /me |
| 7 | `book.service.ts` | 中 | curl /api/books 六端点 |
| 8 | `borrow.service.ts` | 高 | curl /api/borrows 六端点 |

### 验收

```bash
npx tsc --noEmit                          # 零错误
diff <(curl /api/books?page=1) 基线       # 全端点 PASS
wc -l backend/src/routes/*.ts             # 每个 ≤60
grep -r "app.prisma" backend/src/routes/  # 零结果
```

**OUTPUT**: 四层架构成形。所有 API 不变。后续只改 service 不改 route。

---

## Module B: 数据层

**状态**: 待开始
**预计**: 1h
**前置**: Module A

### Schema 变更

```prisma
model Book {
  // ... 现有字段
  clcNumber    String?   // 中图法分类号 (TP36)
  physicalDesc String?   // 载体形态 ("169页")
  cover        String?   // 封面 URL
  language     String?   // 语种 (chi/eng/jpn)
  country      String?   // 国别 (CN/US/JP)
}

model BookItem {
  // ... 现有字段
  campus       String?   // 校区 (青岛/泰安/济南)
  requests     Int @default(0)  // 当前请求数
}
```

### Seed — SDUST 一期联动（零校方配合）

**读者类型:**

| 类型 | 上限 | 天数 | 续借 | 日罚金 |
|------|------|------|------|--------|
| 本科生 | 5册 | 30天 | 1次/15天 | ¥0.10 |
| 研究生 | 10册 | 60天 | 2次/30天 | ¥0.20 |
| 教师 | 20册 | 180天 | 3次/60天 | ¥0.50 |

**资料类型:** 普通图书 / 新书速递(7天) / 工具书(不外借)

**规则矩阵:** 3×3=9 条 CirculationRule

**馆藏地:** 青岛馆A区3楼 / 青岛馆B区2楼 / 青岛8楼阅览区 / 泰安馆自科借阅区 / 济南馆自科借阅区

**读者:** `2023110101` 本科生张三 / `2022110201` 研究生李四 / `T2023001` 教师王五

### 验收

```bash
npx prisma generate               # 零错误
npx prisma db seed                # 零错误
curl /api/books/1 | jq '.clcNumber'  # null (not undefined)
npx tsc --noEmit                  # 零错误
```

**OUTPUT**: Schema 对齐 OPAC。Seed 对标山科大真实规则。

---

## Module C: 后端功能

**状态**: 待开始
**预计**: 2h
**前置**: Module B

### 4.1 分面搜索

```typescript
// BookListParams 扩展
campus?: string | string[]     // 校区
location?: string              // 馆藏地
yearMin?: number               // 出版年范围
yearMax?: number
language?: string              // 语种
clcNumber?: string             // 分类号前缀
sortBy?: 'relevance' | 'year' | 'title'

// 新接口
GET /api/books/facets?search=计算机
→ { facets: {
    type: [{value:"图书",count:35833}, ...],
    campus: [{value:"青岛校区",count:22373}, ...],
    location: [{value:"青岛8楼阅览区",count:9414}, ...],
    yearRange: [{value:"2020-2025",count:...}, ...],
    language: [...], publisher: [...], subject: [...]
  }}
```

实现: MySQL `GROUP BY` 动态查询，4 维度起步（类型/校区/年份/分类）。

### 4.2 详情增强

```typescript
GET /api/books/:id
→ { ...book, clcNumber, physicalDesc, cover, language, country,
    holdings: BookItem[]  // 按 campus→location 分组
  }
```

### 4.3 封面服务

```typescript
// services/cover.service.ts
async resolveCover(isbn: string): Promise<string | null> {
  // 1. OpenLibrary (免费无认证)
  //    https://covers.openlibrary.org/b/isbn/<isbn>-M.jpg
  // 2. 豆瓣 (免费，需 Referer，可能限流)
  //    https://api.douban.com/v2/book/isbn/<isbn>
  // 3. null → CSS 渐变色 + 书名首字占位
}
```

### 验收

```bash
curl "/api/books?campus=青岛&yearMin=2020" | jq '.total'
curl "/api/books/facets?search=计算机" | jq '.facets'
curl /api/books/1 | jq '{clcNumber,cover,holdings}'
time curl "/api/books/facets?search=计算机" > /dev/null  # <200ms
npx tsc --noEmit
```

**OUTPUT**: 分面搜索 + 详情页 API 就绪。

---

## Module D: 前端地基

**状态**: 待开始
**预计**: 4h
**前置**: Module A（类型已就绪，不依赖 B/C 数据）

### D1. Typed API (7 文件)

```
frontend/src/api/
├── index.ts      ← HTTP 壳 (已有)
├── books.ts      ← bookApi.list(), .getById(), .getFacets()
├── auth.ts       ← authApi.login(), .register(), .getMe()
├── borrows.ts    ← borrowApi.borrow(), .return(), .renew()
├── fines.ts
├── readers.ts
├── categories.ts
└── stats.ts
```

### D2. Pinia Stores (3)

```
frontend/src/stores/
├── auth.ts       ← user, token, login(), logout()
├── books.ts      ← searchResults, facets, currentBook
└── categories.ts ← categoryList (全局缓存)
```

### D3. Composables (2)

```
frontend/src/composables/
├── usePagination.ts   ← page, pageSize, total, goTo()
└── useDebounce.ts     ← 搜索防抖 300ms
```

### D4. 组件库 (11)

| 组件 | 用途 |
|------|------|
| `BookCard.vue` | 封面 + 标题 + 作者 + 状态徽章 |
| `BookGrid.vue` | 网格布局（替代表格） |
| `FacetPanel.vue` | 左侧分面面板，带计数 |
| `SearchBar.vue` | 搜索栏：类型切换 + 输入 + 按钮 |
| `HoldingsTable.vue` | 馆藏表格：序号|索书号|校区|馆藏地|可借|请求数 |
| `StatusBadge.vue` | NTAG 四色：在架/借出/预约/逾期 |
| `BarcodeInput.vue` | 扫码输入框（自动回车，焦点常驻） |
| `BarcodeLabel.vue` | JsBarcode 条码标签 |
| `EmptyState.vue` | 空状态插画 + 文字 |
| `SkeletonCard.vue` | 骨架屏 |
| `BookDetailSection.vue` | 详情区 tab 切换 |

### 验收

```bash
npm run build  # 零错误 + 体积不退化
```

**OUTPUT**: 前端不再裸调 `api.get()`。11 组件可复用。3 stores 管理状态。

---

## Module E: 前端功能

**状态**: 待开始
**预计**: 3h
**前置**: Module C + Module D

### 页面

| 文件 | 内容 |
|------|------|
| `views/public/Search.vue` | SearchBar + BookGrid + FacetPanel |
| `views/public/BookDetail.vue` | 封面 + 信息 + HoldingsTable + 借阅按钮 |
| `reader/Books.vue` (改造) | 嵌入 Search |
| `admin/Books.vue` (改造) | HoldingsTable 替换纯文字 |

### 路由

| 路径 | 组件 | 权限 |
|------|------|------|
| `/books` | Search.vue | public |
| `/books/:id` | BookDetail.vue | public |
| `/admin/books` | Books.vue (增强) | admin |
| `/reader/books` | Books.vue (增强) | reader |

### DESIGN-TODO 解决

15 个 DESIGN-TODO 全部决策并实现，不再挂黄标。

### 验收

```bash
npm run build
# 页面清单: 搜索→分面→详情→借阅 完整走通
# 暗色主题全覆盖
# 响应式 ≥1366px
```

**OUTPUT**: 搜索+详情体验对齐 OPAC 标准。

---

## Module F: 流通台

**状态**: 待开始
**预计**: 2h
**前置**: Module D（不依赖 E 的搜索功能）

### 后端前置 (5行)

```typescript
// routes/books.ts
GET /api/book-items/:barcode
→ { item: BookItem & { book: Book }, currentBorrow: BorrowRecord | null }
```

### 前端: 偷懒实现

- 复用现有 `POST /api/borrows/borrow` + `return`
- `<input>` 模拟扫码枪 (自动回车)
- 扫读者条码 → 显示信息 → 扫图书条码 → 自动判断借/还 → 加入队列 → 确认全部

### 交互

```
1. 扫读者条码 → 左侧显示 (已借/欠费/上限)
2. 扫图书条码 → 在架=借书队列 / 借出=还书队列 (显示逾期)
3. 点击"确认全部" → 批量提交
4. Toast + Web Audio 反馈 → 焦点回输入框
```

### 验收

- [ ] 输入条码 → 自动查询
- [ ] 借/还流程完整
- [ ] 逾期自动计算
- [ ] 键盘焦点不丢失

**OUTPUT**: 流通台就绪。

---

## Module G: 集成

**状态**: 待开始
**预计**: 2h
**前置**: Module E + Module F

### G1. 端到端测试

```
读者登录 → 搜索"计算机" → 分面"青岛校区" → 详情页 → 借阅
管理员登录 → 流通台扫码还书 → 查看罚款
```

### G2. 还书→预约联动

```typescript
// services/borrow.service.ts — returnBook() 末尾追加
// 还书成功 → 查 pending holds → 队首变 ready → bookItem 变 on_hold → 站内通知
```

### G3. 文档

- 新建 `ARCHITECTURE.md` — 四层架构文档
- `AGENTS.md` — 架构决策追加
- `TODO.md` — 划掉完成项

### 验收

- [ ] 端到端完整流程走通
- [ ] API 一致性 (diff 基线)
- [ ] 预约联动正确
- [ ] 文档更新

**OUTPUT**: v0.4.0 + v0.4.1 + v0.4.2 全部交付。

---

## 总时间

| Module | 内容 | 时间 | 含测试 |
|--------|------|------|--------|
| A | 后端地基 + 全部后端测试 (22个文件) | 8h | 测试4h |
| B | 数据层 + seed 验证 | 1.5h | 测试0.5h |
| C | 后端功能 | 3h | 测试1h |
| D | 前端地基 + 全部前端测试 (8个文件) | 6h | 测试2h |
| E | 前端功能 | 3h | — |
| F | 流通台 | 2h | 手动checklist |
| G | 集成 | 2h | E2E checklist |
| **合计** | | **25.5h** | 测试7.5h |

## 不变规则

1. 每模块完成 → 验收全过 → 才进下一步
2. API 响应变化 = 回退、找原因
3. 构建失败 = 不进下一步
4. 发现问题先修当前模块，不"记下来以后改"

---

## Phase 2: 工程卓越（六维度补全）

> Phase 1 (Module A-G) = 骨架完成。Phase 2 = 血肉补全。

### 现状评估（六维度）

| 维度 | 得分 | 关键缺口 |
|------|------|---------|
| 架构分层 | ✅ 做到 | — |
| 工程规范 | ⚠️ 一半 | ESLint/Prettier/Husky 未配 |
| 数据状态 | ⚠️ 一半 | 无请求缓存 (SWR等效) |
| 质量防线 | ⚠️ 刚起步 | CI/CD 未建, 集成/E2E 测试为零 |
| 安全性能 | ❌ 大部分缺 | helmet/rate-limit/CORS 白名单/索引 |
| 可观测性 | ❌ 零 | requestId 未加 |

### 不做的事（学生项目过重）

| 不做的 | 原因 |
|--------|------|
| Redis 缓存 | 单机 MySQL 已满足, P95 < 50ms |
| Sentry/Prometheus/Grafana | 无运维环境 |
| Playwright E2E | 太重, 已用手动 checklist |
| JWT refresh token | 单页面应用, 过期重登即可 |
| GraphQL | RESTful 够用 |

### Module H: 工程化工具链 (1h)

**目标**: 统一代码风格, 提交前自动格式化。

```
backend/ + frontend/
├── .eslintrc.js           ← ESLint 配置 (TypeScript + Vue)
├── .prettierrc             ← Prettier 配置 (2 space, single quote)
├── .editorconfig           ← 编辑器统一
├── .husky/
│   └── pre-commit          ← lint-staged → eslint --fix + prettier --write
└── commitlint.config.js    ← Conventional Commits 校验
```

**验证**: 故意写一个 `console.log` → `git commit` → Husky 拦截 → 自动修复。

- [ ] `npm run lint` 通过
- [ ] `git commit` 自动格式化
- [ ] commit message 不规范时拦截

### Module I: 安全加固 (1h)

**目标**: 生产环境必备的 4 项安全措施。

| 文件 | 内容 |
|------|------|
| `backend/src/index.ts` | `@fastify/helmet` — XSS/CSRF/clickjacking 头 |
| `backend/src/index.ts` | `@fastify/rate-limit` — 100 req/min per IP |
| `backend/src/index.ts` | CORS: `origin: ['http://localhost:5173']` (禁止 `true` 全放) |
| `backend/.env.example` | 已有, 验证必需变量完整性 |

**验证**: `curl -I` 检查 security headers。

- [ ] `X-Content-Type-Options: nosniff`
- [ ] `X-Frame-Options: DENY`
- [ ] rate-limit 超限返回 429
- [ ] CORS 限制生效

### Module J: 质量防线 (2h)

**目标**: CI 自动化 + 集成测试。

**J1. GitHub Actions**

```
.github/workflows/ci.yml
  on: [push, pull_request]
  jobs:
    lint:    eslint + prettier --check
    test:    vitest run (backend) + vitest run (frontend)
    build:   tsc --noEmit (backend) + vite build (frontend)
```

**J2. API 集成测试 (Fastify inject)**

```typescript
// __tests__/routes/auth.test.ts
// 无需启动真实 HTTP — Fastify inject() 直接测
it('POST /api/auth/login — 正确密码返回 token', async () => {
  const app = await buildApp()
  const res = await app.inject({ method: 'POST', url: '/api/auth/login', payload: {...} })
  expect(res.statusCode).toBe(200)
  expect(res.json().token).toBeDefined()
})
```

**覆盖 8 个路由文件, ~24 个用例:**
- auth (login/register/me), books (list/detail/items), borrows (borrow/return/renew)
- categories (crud), readers (list/detail), stats (overview/popular), fines (list/pay), rules (list/upsert)

**验证**:
- [ ] GitHub Actions 绿灯
- [ ] `npm test` 包含集成测试
- [ ] PR 时自动跑 CI

### Module K: 性能优化 (1h)

**目标**: MySQL 索引 + 查询优化。

| 操作 | 文件 | SQL |
|------|------|-----|
| campus 过滤 | schema.prisma | `@@index([campus])` on BookItem |
| barcode 查询 | 已有 | UNIQUE 已有 |
| borrowRecord 查询 | schema.prisma | `@@index([userId, status])` |
| book 搜索 | schema.prisma | `@@index([title])` on Book |
| fine 查询 | schema.prisma | `@@index([userId])` |

**验证**:
```bash
EXPLAIN SELECT * FROM book_items WHERE campus = '青岛';  -- key: campus_idx
time curl /api/books/facets  # P95 < 150ms
```

- [ ] 4 个关键索引创建
- [ ] facets API P95 < 150ms
- [ ] books list P95 < 50ms

### Module L: 收尾 (1.5h)

**L1. 15 DESIGN-TODOs 全部决策**

| # | 位置 | 决策 |
|---|------|------|
| 9 | 封面占位图 | CSS 渐变色 + 书名首字 ✅ 已实现 |
| 10 | 复本状态颜色 | StatusBadge 四色方案 ✅ 已实现 |
| 12-15 | 流通台 | 已实现 |

剩余 11 项逐项决策并写入代码, 去掉所有 NTag 黄标。

**L2. Step 10: 还书→预约联动**

```typescript
// borrow.service.ts — returnBook() 末尾 +15行
// 还书成功 → 查 pending holds → 队首变 ready → 站内通知
```

**L3. BarcodeLabel.vue**

`npm install jsbarcode` → 条码 SVG 生成组件。

---

### 总时间 (Phase 2)

| Module | 内容 | 时间 |
|--------|------|------|
| H | ESLint/Prettier/Husky | 1h |
| I | helmet/rate-limit/CORS | 1h |
| J | CI + 集成测试 (24用例) | 2h |
| K | MySQL 索引 + 查询优化 | 1h |
| L | DESIGN-TODOs + 预约联动 + BarcodeLabel | 1.5h |
| **合计** | | **6.5h** |

### 完整交付标准 (Phase 1 + Phase 2 全过)

```
✅ 四层架构 (routes/services/Prisma/MySQL)
✅ 43 单元测试 + 24 集成测试 = 67 测试
✅ CI/CD 绿灯 (lint → test → build)
✅ 安全头 + 速率限制
✅ MySQL 索引
✅ 15 DESIGN-TODOs 全部解决
✅ 预约联动
✅ 流通台
✅ 零裸调 Prisma
✅ 零硬编码
✅ ESLint/Prettier 格式化通过
```
