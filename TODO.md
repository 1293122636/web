# Library Full-Stack System — 未完成事项与优化清单

> 按 PLAN.md Phase 1 (A-G) + Phase 2 (H-L) 对齐。

## Phase 1 剩余 (feature 功能)

| # | 任务 | 归属 | 状态 |
|---|------|------|------|
| 1 | 还书→预约联动 (returnBook hook + Hold 表) | G/远期 | ⏳ |
| 2 | BarcodeLabel.vue (JsBarcode) | D/远期 | ⏳ |
| 3 | API 集成测试 (8 route files, ~24 cases) | 远期 | ⏳ |
| 4 | 前端组件测试 | 远期 | ⏳ |

## Phase 2 待做 (engineering 工程)

### Module H: 工程化工具链
| # | 任务 | 归属 |
|---|------|------|
| 1 | ESLint 配置 (.eslintrc.js) | H | ✅ |
| 2 | Prettier 配置 (.prettierrc) | H | ✅ |
| 3 | Husky + lint-staged (pre-commit hook) | H | ✅ |

### Module I: 安全加固
| # | 任务 | 归属 |
|---|------|------|
| 1 | @fastify/helmet 安全头 | I | ✅ |
| 2 | @fastify/rate-limit 速率限制 | I | ✅ |
| 3 | CORS 白名单 (禁止 origin: true) | I | ✅ |

### Module J: CI + 集成测试
| # | 任务 | 归属 |
|---|------|------|
| 1 | GitHub Actions workflow (lint→test→build) | J | ✅ |
| 2 | Fastify inject 集成测试 (8 route files) | J | ✅ |

### Module K: 性能优化
| # | 任务 | 归属 |
|---|------|------|
| 1 | MySQL 索引: Book.title, BookItem.campus | K |
| 2 | MySQL 索引: BorrowRecord(userId,status), Fine.userId | K |
| 3 | setErrorHandler 统一错误拦截 | K |

### Module L: 收尾
| # | 任务 | 归属 |
|---|------|------|
| 1 | 8 个 DESIGN-TODO 决策 + 实现 | L |
| 2 | 去掉 index.ts 中的 console.log | L |
| 3 | 前端 types 中去 any 化 | L |

## 已完成 ✅

| # | 任务 | 状态 |
|---|------|------|
| 1 | 四层架构 (routes/services/Prisma/MySQL) | ✅ |
| 2 | 11 service 文件, 336 行 routes | ✅ |
| 3 | Schema +7 字段, Seed 3×3 规则矩阵 | ✅ |
| 4 | 分面搜索 + 封面服务 | ✅ |
| 5 | 10 UI 组件 + 3 页面 + 流通台 | ✅ |
| 6 | 43 单元测试 (9 files, 100% service) | ✅ |
| 7 | ARCHITECTURE.md + .env.example | ✅ |
| 8 | 前端 build 通过 | ✅ |
| 9 | 39 API 端点全部 PASS | ✅ |
| 10 | Git main 分支合并 | ✅ |
| 11 | ASSESSMENT.md 代码质量评估 | ✅ |

## 不做的事 (学生项目过重)

| 项 | 原因 |
|----|------|
| Redis 缓存 | 单机足够 |
| Sentry/Prometheus/Grafana | 无运维环境 |
| Playwright E2E | 太重 |
| JWT refresh token | 过期重登 |
