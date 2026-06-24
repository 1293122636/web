# Library Full-Stack — Code Quality & Architecture Assessment

> 2026-06-24 | v2.0 — Module H complete, reassessed

## 一、代码量

| Layer | Lines | Files | avg/File |
|-------|-------|-------|----------|
| Routes | 336 | 8 | 42 |
| Services | 889 | 10 | 89 |
| Types (backend) | 415 | 2 | 207 |
| Frontend Total | 2,156 | 28 | 77 |
| Tests | 751 | 9 | 83 |
| **Total** | **4,547** | **57** | — |

## 二、类型安全

| Metric | Count | Status |
|--------|-------|--------|
| `any` in routes | 41 | ⚠️ Fastify 请求对象无泛型, 属结构性 `any` |
| `any` in services | 28 | ⚠️ Prisma where 动态构建, 可逐步替换为强类型 |
| `any` in tests | 26 | ✅ vi.mock 的 prisma 实例，mock 专用 |
| `any` in frontend | 15+ | ⚠️ Vue views 中 store/ref 类型不完整 |
| `as unknown as` casts | 21 | ⚠️ Prisma 返回类型与 DTO 类型桥接 |
| DTO interfaces | 40 | ✅ 覆盖全部 35 API 端点 |
| TSC errors | 12 | ⚠️ 测试 mock 类型 + book.service 隐式 any + fine Decimal 转换 |
| `BookListResponse` → type alias | ✅ | Module H 修复：空 interface → type 别名，通过 no-empty-object-type |

## 三、错误处理

| Pattern | Status |
|---------|--------|
| `setErrorHandler` 统一拦截 | ❌ 不存在。routes 用 `reply.status(x).send()` 直接返回 |
| `throw + statusCode` 模式 | ⚠️ services 中部分使用, routes 未统一 |
| 状态码一致性 | ✅ 401/403/404/409 使用正确 |
| 错误消息暴露 | ⚠️ 生产环境未过滤 stack trace |

## 四、数据库

| Metric | Count | Detail |
|--------|-------|--------|
| 显式索引 | 1 | 仅 `@@unique([patronCategoryId, itemTypeId])` |
| 缺失索引 | 4 | Book.title, BookItem.campus, BorrowRecord(userId,status), Fine.userId |
| Prisma 事务 | 2 处 | borrow() + returnBook() 均使用 `$transaction` |
| 冗余计数器 | Book.available | 与 BookItem.status 计数可能不一致 (无校验) |
| Enum 覆盖 | 6 个 | UserRole, BookStatus, ItemCondition, ItemStatus, FineType, BorrowStatus |

## 五、前端

| Metric | Count |
|--------|-------|
| DESIGN-TODO 标记 | 8 处未解决 |
| Console.log | 1 (`index.ts:71` 服务器启动消息——可接受) |
| Naive UI 组件使用 | ✅ 全使用 |
| 暗色主题 | ✅ n-config-provider |
| 路由守卫 | ✅ beforeEach |
| Pinia stores | 2 (auth, books) |
| 响应式图片 | ❌ 未做 |
| 加载骨架 | SkeletonCard.vue ✅ |
| 空状态 | EmptyState.vue ✅ |

## 六、测试

| Layer | Files | Tests | PASS | Coverage |
|-------|-------|-------|------|----------|
| borrow.service | 1 | 10 | 10 | 100% |
| auth.service | 1 | 6 | 6 | 100% |
| book.service | 1 | 7 | 7 | 100% |
| category.service | 1 | 4 | 4 | 100% |
| user.service | 1 | 4 | 4 | 100% |
| stats.service | 1 | 3 | 3 | 100% |
| fine.service | 1 | 3 | 3 | 100% |
| rule.service | 1 | 3 | 3 | 100% |
| cover.service | 1 | 3 | 3 | 100% |
| **Total** | **9** | **43** | **43** | **service: 100%** |

缺失:
- ❌ API 集成测试 (8 route files, ~24 cases)
- ❌ 前端组件测试 (4 components)
- ❌ E2E 测试

## 七、安全

| Item | Status |
|------|--------|
| Helmet (安全头) | ❌ |
| Rate Limiting | ❌ |
| CORS 白名单 | ❌ `origin: true` 全放 |
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
| Conventional Commits | ✅ 全部 10 次提交遵循 `<type>: <verb>` 格式 |
| CI/CD | ❌ 无 (Module J) |
| .prettierignore | ✅ dist/node_modules/migrations |
| npm scripts | ✅ lint/lint:fix/format/prepare |

## 九、Git

| Metric | Value |
|--------|-------|
| Total commits | 10 on main |
| 分支策略 | feature → main merged |
| Commit 规范 | ✅ Conventional Commits |
| pre-commit hook | ✅ ESLint + Prettier 自动拦截 |
| 代码审查 | ❌ solo, 无 PR |

## 十、路由纯净度

| Metric | Count |
|--------|-------|
| 路由直接调 Prisma | 0 ✅ (全部通过 service 层) |
| 路由行数 | 336 行 / 8 文件 = 42 行/文件 (≤60 目标) |
| Services 行数 | 889 行 / 10 文件 = 89 行/文件 |

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
| 错误处理 | 5/10 | 缺 setErrorHandler 统一拦截 | — |
| 数据库设计 | 7/10 | Schema 对齐 OPAC, 缺索引 | — |
| 前端质量 | 6/10 | 组件库完整, 8 个 DESIGN-TODO 未决策 | — |
| 测试覆盖 | 7/10 | Service 100%, 集成/E2E 零 | — |
| 安全性 | 3/10 | 仅 SQL 注入防护 | — |
| 工程规范 | **9/10** | ESLint+Prettier+Husky+lint-staged ✅ | **+4** |
| **加权总分** | **6.5/10** | 骨架完整, 工程化达 9/10 | **+0.5** |

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

## 十四、Phase 2 剩余模块进度

| Module | 内容 | 状态 | 预计提分 |
|--------|------|------|---------|
| **H** | ESLint + Prettier + Husky | ✅ | 工程规范 +4 |
| **I** | Helmet + Rate Limit + CORS | ⏳ | 安全性 +4 |
| **J** | GitHub Actions + 集成测试 | ⏳ | 测试 +2, 工程 +1 |
| **K** | 索引 + setErrorHandler | ⏳ | 数据库 +2, 错误处理 +3 |
| **L** | DESIGN-TODO + 收尾 | ⏳ | 前端 +2 |
| **目标总分** | — | — | **8.1/10** |
