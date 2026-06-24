# Library Full-Stack System — 未完成事项与优化清单

> 按 PLAN.md Phase 1 (A-G) + Phase 2 (H-L) 对齐。

## Phase 1 剩余 (feature 功能)

| # | 任务 | 归属 | 状态 |
|---|------|------|------|
| 1 | 还书→预约联动 (returnBook hook + Hold 表) | G/远期 | ✅ |
| 2 | BarcodeLabel.vue (JsBarcode) | D/远期 | ✅ |
| 3 | 前端组件测试 | 远期 | ✅ |

## Phase 2 待做 (engineering 工程)

| 模块 | 状态 |
|------|------|
| Module H: ESLint/Prettier/Husky | ✅ |
| Module I: helmet/rate-limit/CORS | ✅ |
| Module J: CI + 49 route tests | ✅ |
| Module K: MySQL indexes + setErrorHandler | ✅ |
| Module L: types de-any + DESIGN-TODOs | ✅ |

## M1-M3 增量 (2026-06-24)

| # | 任务 | 状态 |
|---|------|------|
| M1 | BarcodeLabel.vue + jsbarcode | ✅ |
| M2 | Hold 预约体系 (schema/service/routes/frontend) | ✅ |
| M3 | 前端组件测试 (10 tests) | ✅ |
| — | P0-P2 审计修复 (库存计数/复本释放/过期/测试覆盖) | ✅ |

## 已完成 ✅

| # | 任务 | 状态 |
|---|------|------|
| 1 | 四层架构 (routes/services/Prisma/MySQL) | ✅ |
| 2 | 11 service 文件, 234 行 routes | ✅ |
| 3 | Schema + Hold 表, Seed 3×3 规则矩阵 | ✅ |
| 4 | 分面搜索 + 封面服务 | ✅ |
| 5 | 10 UI 组件 + BarcodeLabel + 3 页面 | ✅ |
| 6 | 106 测试 (后端 96 + 前端 10), 19 文件 | ✅ |
| 7 | ARCHITECTURE.md + .env.example | ✅ |
| 8 | 前端 build 通过 | ✅ |
| 9 | 39 API 端点全部 PASS | ✅ |
| 10 | Git main 分支, GitHub remote | ✅ |
| 11 | ASSESSMENT.md 代码质量评估 | ✅ |
| 12 | Hold 预约体系 (5 端点 + 还书联动 + 前端 UI) | ✅ |
| 13 | P0 数据完整性修复 (available 计数/复本释放) | ✅ |

## 不做的事 (学生项目过重)

| 项 | 原因 |
|----|------|
| Redis 缓存 | 单机足够 |
| Sentry/Prometheus/Grafana | 无运维环境 |
| Playwright E2E | 太重 |
| JWT refresh token | 过期重登 |
