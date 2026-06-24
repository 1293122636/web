# Library Full-Stack System — AGENTS.md

## 项目概述
图书馆全栈管理系统，四层架构（前端→路由→服务→数据）。三层业务深度（书目→复本→规则引擎）。前端 Vue 3 + Naive UI，后端 Fastify，ORM Prisma 5，数据库 MySQL（WSL 3306）。TypeScript 全栈。

**实施计划**: `PLAN.md` — 7 模块分步，每步验收通过才进下一步。
**OPAC 参考**: `resources/opac-data-collection.md` — 仅供设计参考，不集成外部 API。

## 角色与权限
- 管理员（admin）：全部管理权限，可管理图书/读者/借阅/统计
- 读者（reader）：可浏览图书、借阅历史、个人信息
- 认证方式：JWT，登录后返回 token，前端 localStorage 存储
- 路由守卫：admin 可访问 /admin/*，reader 只能访问 /reader/*

## 功能模块
| 模块 | 说明 | 管理员 | 读者 |
|------|------|--------|------|
| 图书管理 | 增删改查 + 分类 + 状态（在架/借出/下架） | CRUD | 浏览+搜索 |
| 分类管理 | 图书分类维护 | CRUD | 只读 |
| 读者管理 | 读者注册/信息/借阅历史/权限 | CRUD | 本人查看 |
| 借阅管理 | 借书/还书/续借/逾期处理 | 全部操作 | 本人操作 |
| 统计报表 | 借阅量/热门图书/逾期统计/读者数 | 查看 | 不可见 |

## 设计规范
- 全局暗色主题（Linear 风格）
- 主色 Indigo-Violet (#5e6ad2 / #7170ff)
- 字体 Inter，无 Google Fonts 依赖（系统字体栈）
- 未定设计点用 `[DESIGN-TODO: 说明]` 标记保留在 UI 上

## 错误区（先读，按时间倒序）
| # | 错误操作 | 后果 | 正确做法 |
|---|---------|------|---------|
| 9 | 没写 .env.example | AI 不知道需要哪些环境变量，瞎编配置 | 维护 .env.example，所有变量列出 |
| 8 | 各路由错误返回格式不同（throw vs reply.send） | 前端解析混乱，类型不统一 | 用 setErrorHandler 统一拦截，路由只用 throw |
| 7 | Promise.all 代替 Prisma 事务 | 借还操作无原子性，库存与记录不一致 | 借书/还书必须用 prisma.$transaction |
| 6 | 前端没用 Naive UI 只用 inline styles | AGENTS.md 与实际代码矛盾，后续 AI 可能继续用 inline | 组件强制用 Naive UI |
| 5 | 改 Prisma schema 后没重新生成客户端 | 类型错误，编译不通过 | schema 变更后立即 npx prisma generate |
| 4 | feature 分支直接 push 到 main | 打乱发布节奏 | feature 分支开发，PR 合并，release tag 发布 |
| 3 | 设计阶段不输出 3-line block 直接写代码 | 返工重改视觉样式 | 前端改动前先输出 TEMPLATE/SYSTEM/LAYOUT 三行块 |
| 2 | 改了服务端不验证 | 前端以为接口正常，实际 500 | 改后端后立即 curl 确认响应 |
| 1 | 凭经验假设文件/目录存在 | 多轮调试才发现路径不对 | 改/写前用 read_file/terminal ls 确认 |

## 架构决策（有变更时追加，不删除旧记录）
| 日期 | 决策 | 原因 |
|------|------|------|
| 2026-06-23 | 单仓库 monorepo，frontend/ + backend/ | 项目规模小，monorepo 管理简单 |
| 2026-06-23 | ORM 选 Prisma，不用 TypeORM | Prisma 类型生成更可靠，generate 后类型全 |
| 2026-06-23 | 前端组件库选 Naive UI，不用 Element Plus | Naive UI TypeScript 原生支持更好 |
| 2026-06-23 | 编译输出在各自目录内 dist/ | 不污染根目录 |
| 2026-06-24 | 借还操作必须 Prisma 事务 | 防止库存计数与借阅记录不一致 |
| 2026-06-24 | db push 仅开发用，生产用 migrate dev/deploy | 保护生产数据不丢失 |
| 2026-06-24 | 前端代理 /api → 后端 3000 | 避免 CORS，生产用 Nginx 反代或同源部署 |
| 2026-06-24 | 用 Naive UI 暗黑主题替代自定义 CSS | 降低样式维护成本，统一组件行为 |
| 2026-06-24 | Prisma 5 非 7，放弃 engineType/client 引擎 | Prisma 7 不支持 MySQL 直连，需不存在 Adapter |
| 2026-06-24 | 项目移回 WSL 原生路径 ~/workplace/ | /mnt/d 的 node_modules 无法正常安装和删除 |
| 2026-06-24 | 四层架构：前端→路由(Routes)→服务(Services)→数据(Prisma) | routes 只做 HTTP 分发，业务逻辑进 services |
| 2026-06-24 | Services 用纯函数，不用 class | 与已有 rules.ts/fines.ts 风格一致，prisma 第一参数 |
| 2026-06-24 | 前端 typed API + Pinia stores + composables | 不再裸调 `api.get()`，类型全 |
| 2026-06-24 | 零外部 API 依赖（学生项目） | 超星/豆瓣需授权，用 OpenLibrary + CSS 占位 |
| 2026-06-24 | 7 模块分步实施，每步验收不进下一步 | 见 PLAN.md |
| 2026-06-24 | 分面搜索用 MySQL GROUP BY，不调外部 OPAC | findsdust.libsp.cn 封闭系统 |
| 2026-06-24 | 43 单元测试覆盖全部 services | 9 文件, vitest + vi.mock |
| 2026-06-24 | Phase 2: 工程卓越六维度补全 | PLAN.md H-L 模块: lint/安全/CI/索引/收尾 |
| 2026-06-24 | ASSESSMENT.md — 代码质量量化评估 | 加权总分 6.0/10, 骨架完整血肉待补 |
| 2026-06-24 | ESLint v10 flat config + Prettier + Husky | Module H 完成, 0 errors 95 warnings, pre-commit 拦截 |
| 2026-06-24 | Helmet + Rate Limit + CORS 白名单 | Module I 完成, 8 security headers, 100/min, localhost-only |
| 2026-06-24 | CI + 集成测试 (34 route tests) | Module J 完成, GitHub Actions, 8 route files, 77 total tests |
| 2026-06-24 | requireAdmin 中间件替代内联 role 检查 | 减少代码重复，统一权限拦截。middleware/requireAdmin.ts 已创建。已完成 books/auth/borrows/categories(post)。待完成: categories(put/delete)/readers/stats/fines/rules (11处内联检查) |
| 2026-06-24 | Module J 集成测试补全 (49 route tests) | 15 缺口补齐：auth.admin/create, books.items/update/barcode, categories.put/delete, borrows.admin/history/renew, readers.detail/update, stats.popular/monthly, fines.pay, rules.upsert。全 92 tests pass |

## 环境变量
### 必需变量（backend/.env）
- `DATABASE_URL` — MySQL 连接串，格式：`mysql://user:***@host:port/database`
- `JWT_SECRET` — JWT 签名密钥，长度 ≥ 32 字符

### 可选变量
- `PORT` — 后端端口，默认 3000
- `NODE_ENV` — `development` 或 `production`
- `LOG_LEVEL` — pino 日志级别，默认 `info`

### 规则
- `.env` 不入 git（已在 .gitignore）
- 必须维护 `backend/.env.example` 模板文件（入 git）
- `.env.example` 格式：变量名=说明，密码字段写 `***`
- 新增环境变量必须同步更新 .env.example 和本表

## 数据库迁移策略
- 开发迭代期（schema 频繁变）：`npx prisma db push --accept-data-loss`
- Schema 稳定后：`npx prisma migrate dev --name <描述>`（生成迁移文件，入 git）
- 生产环境：`npx prisma migrate deploy`（只执行迁移，不漂移 schema）
- 规则：每次改 schema.prisma 后必须 `prisma generate`
- 规则：迁移文件（`prisma/migrations/`）必须 commit
- 规则：禁止在生产环境用 `db push`

## 错误处理规范
- 后端统一错误格式：`{ error: string, details?: any }`
- 必须用 `fastify.setErrorHandler` 统一拦截所有错误
- 路由中只用 `throw` 抛错，禁止 `reply.status(x).send()`
- HTTP 状态码映射：
  - Zod ValidationError → 400
  - Prisma NotFoundError → 404
  - JWT 认证失败 → 401
  - 权限不足 → 403
  - Prisma 唯一约束冲突 → 409
  - 其他未捕获 → 500
- 生产环境不暴露 `error.stack`
- 前端 api/index.ts 已做统一 try/catch，错误消息从 `e.message` 提取

## 日志规范
- 后端：Fastify 内置 pino
- 开发环境：`LOG_LEVEL=debug`，pino-pretty 格式化
- 生产环境：`LOG_LEVEL=info`，JSON 格式
- 必须打日志的节点：
  - 登录失败（401）
  - 借书/还书/续借操作
  - Prisma 查询耗时 > 500ms
  - 未捕获异常
- 前端：开发可用 `console.error`，生产编译移除（Vite 默认）
- 规则：PR 中不得出现 `console.log`

## API 路由表
### 认证
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/auth/register | public | 读者注册 |
| POST | /api/auth/login | public | 登录 |
| GET | /api/auth/me | authenticated | 当前用户信息 |
| GET | /api/auth/users | admin | 用户列表 |
| POST | /api/auth/admin/create | admin | 创建管理员 |

### 图书
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/books | public | 列表（分页+搜索+分面过滤） |
| GET | /api/books/facets | public | 分面计数（Module C 新增） |
| GET | /api/books/:id | public | 详情（含 holdings + cover） |
| GET | /api/books/:id/items | public | 复本列表 |
| GET | /api/book-items/:barcode | public | 按条码查复本（Module F 新增，5行） |
| POST | /api/books | admin | 新增 |
| PUT | /api/books/:id | admin | 更新 |
| DELETE | /api/books/:id | admin | 删除 |

### 分类
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/categories | public | 列表 |
| POST | /api/categories | admin | 新增 |
| PUT | /api/categories/:id | admin | 更新 |
| DELETE | /api/categories/:id | admin | 删除（有图书则拒绝） |

### 借阅
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/borrows/my | reader | 我的借阅 |
| GET | /api/borrows | admin | 全部借阅 |
| GET | /api/borrows/history | reader | 我的历史 |
| POST | /api/borrows/borrow | reader | 借书 |
| POST | /api/borrows/return/:id | reader+admin | 还书 |
| POST | /api/borrows/renew/:id | reader | 续借 |

### 读者
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/readers | admin | 读者列表 |
| GET | /api/readers/:id | admin | 读者详情+借阅 |
| PUT | /api/readers/:id | admin | 管理员编辑 |
| PUT | /api/readers/profile | reader | 读者自编辑 |

### 统计
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/stats | admin | 概览数据 |
| GET | /api/stats/popular | admin | 热门图书 |
| GET | /api/stats/monthly | admin | 月度借阅量 |

### 系统
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/health | public | 健康检查 |

### API 文档生成
- 安装 `@fastify/swagger` + `@fastify/swagger-ui`
- Schema 定义：每个路由加 `schema: { body, response, params }`
- 查看：浏览器打开 `http://localhost:3000/docs`
- 规则：新增路由必须同时写 schema 定义

## 前端规范
- 组件库：Naive UI 2.x（已安装，必须使用）
- 必须注册 `n-config-provider`，暗色主题，themeOverrides 含 brandColor #5e6ad2
- 常用组件：NDataTable、NForm、NButton、NInput、NSelect、NModal、NMessage、NCard、NPagination、NBadge、NSpin
- 样式覆盖：组件级用 Naive UI props，全局用 CSS 变量（`:root` 已在 index.html 定义）
- 禁止直接用 inline style，特殊情况用 `<style scoped>`
- Visual 设计改动先输出 TEMPLATE / SYSTEM / LAYOUT 三行
- Vite 开发代理已配：`/api` → `http://127.0.0.1:3000`
- 前端禁止写死 `localhost:3000`，统一走 `/api` 前缀
- 类型共享：API 响应的 interface 定义在 `frontend/src/api/types.ts`

## Git 分支规范
- 格式：`<type>/<YYYYMMDD>-<description>`
- type：`feature` / `fix` / `refactor` / `docs` / `chore`
- 示例：`feature/20260624-book-search`，`fix/20260625-borrow-transaction`
- 规则：main 分支保持可部署状态
- 规则：feature 分支开发，PR 合并，合并后删分支
- 规则：commit message 用英文，格式 `<type>: <verb> <noun>`，如 `feat: add book search`

## 种子数据（Seed）
- 脚本：`backend/prisma/seed.ts`
- 运行：`npx prisma db seed`
- 内容：1 个 admin（admin / admin123）、示例分类（计算机/文学/自然科学）、5 本示例书、1 个示例读者
- 规则：seed 脚本必须幂等（用 `upsert`，不重复插）
- 规则：`package.json` 配置 `"prisma": { "seed": "tsx prisma/seed.ts" }`
- 规则：schema 变更后跑 seed 验证

## Docker 开发环境
- 当前 MySQL 在 WSL mirrored 模式直连（127.0.0.1:3306）
- 无需 Docker——WSL 已提供隔离
- 未来如需纯容器化：建 `docker-compose.yml`（MySQL 8 + app）
- 规则：docker-compose.yml 入版本库
- 规则：MySQL 数据用 named volume 持久化

## 部署流程
- 前端：`cd frontend && npm run build` → `dist/`
- 后端：`tsc` → `dist/`，运行 `node dist/index.js`
- 产物打包到 `release/<version>/`
- 环境变量通过 `.env.production` 或系统环境注入
- 规则：部署前执行 `prisma migrate deploy`
- 规则：部署后执行 `curl /api/health` 确认存活

## 安全规范
- JWT secret 从环境变量读取，不硬编码
- 密码 bcrypt hash，成本因子 10
- 数据库操作统一通过 Prisma（防 SQL 注入）
- 输入校验：所有 POST/PUT 用 Zod schema（已做）
- 需要认证的路由已加 `onRequest: [app.authenticate]`
- 生产环境：加 `@fastify/helmet` + `@fastify/rate-limit` + CORS 白名单
- 规则：token 存在 localStorage 只用于 API 请求头，不在 URL 传递
- 规则：前端不做权限判断（显示/隐藏按钮），后端必须验证权限

## 缓存策略

### 当前策略：无 Redis，按需内存缓存
- 项目处于单机阶段，不引入外部缓存中间件
- 合理索引 + MySQL 查询已满足 P95 < 50ms 目标
- 未来性能瓶颈出现在具体接口时再加，加在哪见下表

### 可缓存点
| 接口 | 方式 | TTL | 失效时机 |
|------|------|-----|---------|
| GET /api/categories | 内存 Map | 按需失效 | 分类增删改时清 |
| GET /api/stats/popular | 内存 Map | 5min | TTL 自动过期 |

### 不可缓存点
| 接口 | 原因 |
|------|------|
| GET /api/books | 库存计数必须实时，缓存导致脏读 |
| POST /api/borrows/borrow | 写操作，无缓存必要 |
| POST /api/borrows/return/:id | 写操作，库存实时更新 |
| GET /api/auth/me | 用户信息改后立即可见 |

### 缓存实现约束
- 单进程内存 Map，不做序列化
- Key = 请求路径 + 查询参数
- TTL 用 Date.now() 简单实现
- 禁止引入 Redis 直到多进程部署或明确需要

## 性能基准
- API 响应目标（P95）：
  - GET 列表（无 join）< 50ms
  - GET 列表（含 join）< 150ms
  - POST/PUT < 100ms
- 借书/还书必须用事务，单次操作 < 200ms
- Prisma 查询用 `select` 限定字段，不 `select: *`
- 列表接口必须分页（默认 20 条/页，上限 50）
- 生产环境启用 Prisma query logging：慢查询 > 500ms 告警

## 技术栈明细

### 运行时
- Bun（替代 Node.js，运行 backend）
- 版本锁定：bun.lock 入版本库

### 后端（backend/）
- Fastify（TypeScript，ESM）
- Prisma ORM → MySQL 3306（WSL 本机）
- 项目端口：3000
- Zod 输入校验

### 前端（frontend/）
- Vue 3 + Vite + TypeScript
- Naive UI 组件库（暗色主题）
- API 调用用 fetch（不引入 axios）

## 开发规范
1. commit 前 npx prisma generate + bun run build 编译通过
2. 每次改 Prisma schema 后必须 prisma generate + prisma db push（开发阶段）
3. 前端改完先 bun run build 确认无编译错
4. 后端改完先 curl 确认接口响应正确
5. 新增功能组件前输出设计块（仅前端视觉改动）
6. 新功能开 feature/YYYYMMDD-xxx 分支，不要直接在 main 上改
7. 每次完成功能后更新 Architecture Decisions 表（追加，不删旧）
8. MySQL 连接信息：host 127.0.0.1:3306，数据库名 library
9. 每次新增/修改 API 必须更新本文件路由表
10. 每次改 schema.prisma 后必须运行 seed 验证
11. 提交前检查：无 console.log、无硬编码 IP/密码、.env 不在 git diff 中
12. 借书/还书操作必须用 prisma.$transaction，不得用 Promise.all

## 测试规范

### 测试技术栈
- 后端：vitest + Fastify inject() + vi.mock（纯 ESM，零配置）
- 前端：vitest + @vue/test-utils + @pinia/testing
- 回归：curl + diff + jq（对比 Step 0 基线）
- 不做 Playwright/Cypress E2E（学生项目太重）

### 测试文件规则
- 测试文件放在 `__tests__/` 下，与被测代码同级
- 命名：`<filename>.test.ts`
- 每个 service 必须有对应的测试文件
- 每个 route 必须有对应的集成测试
- borrow.service 测试用例 ≥ 10 个（涉及金钱和库存）

### 测试数据库
- 测试用例运行在独立数据库 `library_test`
- `__tests__/setup.ts` 负责：建库→ db push → seed → 提供 helpers
- 每个 test suite 结束后清理数据

### 跑测命令
```bash
cd backend && npx vitest run                        # 全部
cd backend && npx vitest run <file>                 # 单个
cd backend && npx vitest run --coverage             # 覆盖率
cd frontend && npx vitest run                       # 前端
```

### 覆盖率要求
| 层级 | 最低 | 重点 |
|------|------|------|
| services/ | 80% | borrow.service ≥ 90% |
| routes/ | 60% | auth, borrows |
| components/ | 50% | BookCard, HoldingsTable |
| stores/ | 80% | auth, books |

### 评估标准（每模块后）
| 维度 | 权重 | 通过条件 |
|------|------|---------|
| 测试通过 | 40% | 100% pass |
| tsc | 20% | 零错误 |
| 基线 diff | 20% | 零差异 |
| 代码质量 | 10% | 路由≤60行, 零裸调 prisma |
| 覆盖率 | 10% | 达标 |

## 项目目录结构
```
Library Full-Stack Project/
├── frontend/              # Vue 3 + Vite
│   ├── src/
│   │   ├── components/    # 可复用 UI 组件（11个）
│   │   ├── views/
│   │   │   ├── admin/     # 管理员页面（含流通台）
│   │   │   ├── reader/    # 读者页面
│   │   │   └── public/    # 公共页面（搜索+详情）
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── composables/   # 组合函数
│   │   ├── api/           # typed API 函数
│   │   ├── types/         # 前端类型定义
│   │   └── router/
│   ├── dist/              # 编译产物（不入库）
│   └── package.json
├── backend/               # Fastify
│   ├── src/
│   │   ├── routes/        # 路由层（thin, ≤30行/handler）
│   │   ├── services/      # 业务层（纯函数）
│   │   └── types/         # DTO 类型（api.types.ts）
│   ├── prisma/
│   │   ├── schema.prisma
│   │   ├── migrations/    # 迁移文件（入库）
│   │   └── seed.ts        # 种子数据
│   ├── dist/              # 编译产物（不入库）
│   ├── .env.example       # 环境变量模板（入库）
│   └── package.json
├── resources/             # 设计参考
│   ├── opac-data-collection.md
│   └── opac-screenshots/
├── PLAN.md                # 实施计划（7模块）
├── AGENTS.md              # 本文件
├── TODO.md                # 未完成清单
└── README.md              # 项目说明
```

## 工作流
1. Phase 1 — 需求确认（新功能先问清范围）
2. Phase 2 — 设计输出（前端出 3-line design block，后端出路由设计）
3. Phase 3 — 实现 + 验证（改完即验，curl / build / vision）
4. Phase 4 — 更新 AGENTS.md（路由表 + 架构决策）
5. Phase 5 — commit（feature/YMD-xxx → PR → main）
