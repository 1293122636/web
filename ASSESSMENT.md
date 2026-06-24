# Library Full-Stack — Code Quality & Architecture Assessment

> 2026-06-24 | v3.3 — 13-round audit complete, 82 fixes, 40 API endpoints

## 一、代码量

| Layer | Lines | Files | avg/File |
|-------|-------|-------|----------|
| Routes | 310 | 10 | 31 |
| Services | 1,350 | 11 | 123 |
| Types (backend) | 420 | 2 | 210 |
| Frontend Total | 2,300 | 30 | 77 |
| Tests | 1,550 | 24 | 65 |
| **Total** | **5,930** | **77** | — |

## 二、类型安全

| Metric | Count | Status |
|--------|-------|--------|
| `any` in routes | 41 | ⚠️ Fastify 请求对象无泛型, 属结构性 `any` |
| `any` in services | 3 | ✅ 仅 Prisma where 动态构建 3 处 |
| `any` in tests | 26 | ✅ vi.mock 的 prisma 实例，mock 专用 |
| `any` in frontend | 0 | ✅ 全部 typed (Rounds 6-7) |
| `as unknown as` casts | 21 | ⚠️ Prisma 返回类型与 DTO 类型桥接 |
| DTO interfaces | 40 | ✅ 覆盖全部 40 API 端点 |
| TSC errors | 10 | ⚠️ 测试 mock 类型 + book.service 隐式 any + fine Decimal 转换 |
| `BookListResponse` → type alias | ✅ | Module H 修复：空 interface → type 别名，通过 no-empty-object-type |

## 三、错误处理

| Pattern | Status |
|---------|--------|
| `setErrorHandler` 统一拦截 | ✅ Module K 实现，routes 统一 `throw + statusCode` |
| `throw + statusCode` 模式 | ✅ 全 services + routes 已统一 |
| 状态码一致性 | ✅ 401/403/404/409 使用正确 |
| 错误消息暴露 | ✅ 生产环境 `setErrorHandler` 过滤 stack |

## 四、数据库

| Metric | Count | Detail |
|--------|-------|--------|
| 显式索引 | 5 | @@index: [title], [campus], [userId,status], [userId], @@unique: [patronCategoryId,itemTypeId] |
| 缺失索引 | 0 | — |
| Prisma 事务 | 5 处 | borrow(), returnBook(), payFine(), cancelHold(), fulfillHold() + expireReadyHolds(逐条) |
| 冗余计数器 | Book.available | 与 BookItem.status 计数可能不一致 — `reconcileBookAvailable()` 已就绪，待接入定时/route |
| Enum 覆盖 | 6 个 | UserRole, BookStatus, ItemCondition, ItemStatus, FineType, BorrowStatus |

## 五、前端

| Metric | Count |
|--------|-------|
| DESIGN-TODO 标记 | 0 残留 |
| Console.log | 1 (`index.ts:71` 服务器启动消息——可接受) |
| Naive UI 组件使用 | ✅ 全使用 |
| 暗色主题 | ✅ n-config-provider |
| BarcodeLabel | ✅ JsBarcode CODE128 |
| Hold 预约 UI | ✅ BookDetail + MyBorrows |
| 路由守卫 | ✅ beforeEach |
| Pinia stores | 2 (auth, books) |
| 加载骨架 | SkeletonCard.vue ✅ |
| 空状态 | EmptyState.vue ✅ |

## 六、测试

| Layer | Files | Tests | PASS | Coverage |
|-------|-------|-------|------|----------|
| borrow.service | 1 | 10 | 10 | 100% |
| hold.service | 1 | 9 | 9 | 100% |
| auth.service | 1 | 6 | 6 | 100% |
| book.service | 1 | 7 | 7 | 100% |
| category.service | 1 | 4 | 4 | 100% |
| user.service | 1 | 4 | 4 | 100% |
| stats.service | 1 | 3 | 3 | 100% |
| fine.service | 1 | 3 | 3 | 100% |
| rule.service | 1 | 3 | 3 | 100% |
| cover.service | 1 | 3 | 3 | 100% |
| **Service subtotal** | **10** | **52** | **52** | **100%** |
| routes (backend) | 8 | 54 | 54 | 全端点 |
| frontend components | 4 | 10 | 10 | — |
| **Total** | **22** | **116** | **116** | — |

缺失:
- ❌ E2E 测试
- ✅ API 集成测试 (already 54 cases)
- ✅ 前端组件测试 (4 files, 10 tests)

## 七、安全

| Item | Status |
|------|--------|
| Helmet (安全头) | ✅ 8 security headers |
| Rate Limiting | ✅ 100 req/min |
| CORS 白名单 | ✅ localhost-only, 禁用 origin: true |
| JWT 刷新 | ❌ 过期即断 |
| .env 不入库 | ✅ .gitignore 已配 |
| .env.example | ✅ 12行模板 |
| SQL 注入 | ✅ Prisma 参数化 |

## 八、工程规范 (Module H ✅)

| Item | Status |
|------|--------|
| ESLint v10 flat config | ✅ 0 errors, 95 warnings (all no-explicit-any) |
| Prettier 3.8 | ✅ 全量格式化, singleQuote/trailingComma/100w |
| Husky pre-commit | ✅ lint-staged: *.ts → eslint --fix → prettier --write |
| lint-staged | ✅ v17 配置 |
| Conventional Commits | ✅ 全部 27 次提交遵循 `<type>: <verb>` 格式 |
| CI/CD | ✅ GitHub Actions (lint→test→build) |
| .prettierignore | ✅ dist/node_modules/migrations |
| npm scripts | ✅ lint/lint:fix/format/prepare |

## 九、Git

| Metric | Value |
|--------|-------|
| Total commits | 30+ on main |
| 分支策略 | feature → main merged |
| Commit 规范 | ✅ Conventional Commits |
| pre-commit hook | ✅ ESLint + Prettier 自动拦截 |
| 代码审查 | ❌ solo, 无 PR |

## 十、路由纯净度

| Metric | Count |
|--------|-------|
| 路由直接调 Prisma | 0 ✅ (全部通过 service 层) |
| 路由行数 | 336 行 / 8 文件 = 42 行/文件 (≤60 目标) |
| Service 行数 | ~1,300 行 / 11 文件 = 118 行/文件 |

## 十一、依赖健康

| Package | Current | Latest | Status |
|---------|---------|--------|--------|
| Prisma | 5.22 | 7.8 | ✅ 故意锁定 (7 不支持 MySQL) |
| TypeScript | 5.9.3 | 6.0.3 | ⚠️ 可升级 |
| Fastify JWT | 9.1.0 | 10.1.0 | ⚠️ 可升级 |
| Zod | 3.25 | 4.4 | ⚠️ 可升级 |
| ESLint | 10.5 | — | ✅ 最新 |
| Prettier | 3.8 | — | ✅ 最新 |

## 十二、评分矩阵

| 维度 | 评分 | 说明 | 变化 |
|------|------|------|------|
| 架构分层 | 9/10 | 四层清晰, Routes→Services→Prisma→MySQL | — |
| 类型安全 | 6/10 | DTO 全覆盖, Prisma 桥接线 `any` 较多 | — |
| 数据库设计 | **9/10** | Schema 对齐 OPAC, 5 索引就位 | **+2** |
| 前端质量 | **8/10** | Naive UI 全覆盖, types 去 any (64→3), 欠费展示 | **+2** |
| 测试覆盖 | **9/10** | Service 100%, 集成 49 cases, E2E 零 | **+2** |
| 安全性 | **7/10** | Helmet+RateLimit+CORS ✅, JWT refresh 未做 | **+4** |
| 工程规范 | **9/10** | ESLint+Prettier+Husky+lint-staged+CI ✅ | **+4** |
| 错误处理 | **8/10** | setErrorHandler 统一拦截, 路由零 reply.send | **+3** |
| **加权总分** | **8.5/10** | 六线补全 | **+2.5** |

## 十三、Module H 影响分析

Module H 完成后，工程质量发生质变：

| 指标 | 变化 |
|------|------|
| ESLint errors | 1 → 0 ✅ |
| 代码格式一致性 | 手动混乱 → Prettier 全量格式化 ✅ |
| 提交前检查 | 无 → pre-commit ESLint+Prettier ✅ |
| 路由 Prisma 调用 | 0 (已确认干净) ✅ |
| 工程规范评分 | 5 → 9 (+4) |
| Prettier 引起的行数变化 | routes +55, services +269 (换行/缩进美化) |

## 十四、Phase 2 + M 模块进度

| Module | 内容 | 状态 | 预计提分 |
|--------|------|------|---------|
| **H** | ESLint + Prettier + Husky | ✅ | 工程规范 +4 |
| **I** | Helmet + Rate Limit + CORS | ✅ | 安全性 +4 |
| **J** | GitHub Actions + 集成测试 (49 cases) | ✅ | 测试 +2, 工程 +1 |
| **K** | 索引 + setErrorHandler + requireAdmin | ✅ | 数据库 +2, 错误处理 +3 |
| **L** | DESIGN-TODO + types 去 any | ✅ | 前端 +2 |
| **M1** | BarcodeLabel.vue + jsbarcode | ✅ | — |
| **M2** | Hold 预约体系 (5 endpoints + UI) | ✅ | 数据库 +1 |
| **M3** | 前端组件测试 (10→18 tests) | ✅ | 测试 +1 |
| **R1-R13** | 十三轮全代码审计 (82 fixes) | ✅ | 数据完整性/类型安全/竞态/一致性/测试质量 |
| **当前总分** | — | — | **8.5/10** |
