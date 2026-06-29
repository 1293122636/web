# Library Full-Stack System (Spring Boot 版)

图书馆全栈管理系统。从原 TypeScript 版（Vue 3 + Fastify + Prisma）完整迁移，功能与画面完全对齐。

**技术栈：** Vue 3 + Naive UI · Spring Boot 3.x + MyBatis · MySQL 8.0 · Maven

**AI 开发入口 → [AGENTS.md](AGENTS.md)**（命令、API 路由、编码规范、架构决策）

---

## 快速开始

### 后端

```bash
# 1. 确保 MySQL 已启动
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# 2. 导入表结构（schema 已存在则跳过）

# 3. 导入种子数据
mysql -h127.0.0.1 -uroot -p library < seed.sql

# 4. 启动后端（需要 Java 21）
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run    # → localhost:8080
```

### 前端

```bash
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev                    # → localhost:5175（自动代理 /api → 8080）
```

### 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 管理员 | 全部管理权限 |
| 2023110101 | reader123 | 本科生读者 | 张三，无罚款 |
| 2022110201 | reader123 | 研究生读者 | 李四，有逾期未还罚款 ¥60 |
| T2023001 | reader123 | 教师读者 | 王五 |

> 所有读者密码统一为 `reader123`

---

## 前端路由

| 路径 | 说明 | 权限 |
|------|------|------|
| `/` → `/books` | 公共搜索 | 公开 |
| `/books` | 图书浏览（卡片网格 + 分面搜索） | 公开 |
| `/books/:id` | 图书详情（封面 + 馆藏 + 借阅/预约） | 公开 |
| `/login` | 登录 + 注册 | 公开 |
| `/admin/*` | 管理员页面 | admin |
| `/reader/*` | 读者页面 | reader |
| `/*` (404) | 重定向到 `/books` | — |

---

## 与原版的一致性

本项目与原版 TypeScript 系统（`D:\workplace\Library Full-Stack Project`）完全对齐：

- **13 个前端组件** — BookCard, BookGrid, FacetPanel, SearchBar, StatusBadge, HoldingsTable, BookDetailSection, EmptyState, SkeletonCard, BarcodeInput, BarcodeLabel, LoginBg, AnimatedBackground
- **17 个页面** — 登录、公共搜索、图书详情、9 个管理员页面、4 个读者页面
- **45 个 API 端点** — auth, books, book-items, categories, borrows, holds, readers, fines, rules, stats, health
- **11 张数据表** — patron_categories, item_types, categories, circulation_rules, users, books, book_items, borrow_records, fines, holds, audit_logs
- **20 种图书 + 63 复本 + 23 条借阅 + 2 条罚款 + 3 条预约**

### 实质差异（不影响视觉/功能）

| 差异 | 原版 | Java 版 | 原因 |
|------|------|---------|------|
| HTTP 客户端 | fetch | Axios | 课程要求 |
| ORM | Prisma | MyBatis | 课程要求 |
| 数据库列名 | 全 snake_case | 混合 camelCase/snake_case | Schema 设计 |
| 枚举类型 | Prisma enum | String 字段 | — |
| UI 组件库 | Naive UI | Naive UI | 对齐原版 |

---

## 项目结构

```
backend/ (Maven monorepo)
├── pom.xml
├── seed.sql                                # 种子数据脚本
├── src/main/java/com/library/
│   ├── LibraryApplication.java
│   ├── config/       3 files
│   ├── controller/   11 files   # 45 端点
│   ├── service/      9 files
│   ├── mapper/       11 files
│   ├── entity/       11 files
│   ├── dto/request/  6 files
│   ├── dto/response/ 16 files
│   ├── exception/    2 files
│   └── util/         1 file

frontend/ (Vue 3 + Vite)
├── package.json + vite.config.ts
├── src/
│   ├── api/          index.ts, books.ts
│   ├── stores/       auth.ts, books.ts
│   ├── router/       index.ts
│   ├── types/        api.ts
│   ├── composables/  index.ts
│   ├── components/   13 files  # 全部原版组件
│   ├── views/
│   │   ├── admin/    9 pages
│   │   ├── reader/   4 pages
│   │   ├── public/   2 pages
│   │   └── Login.vue
│   └── App.vue
```

---

## 迁移记录

| 日期 | 决策 | 理由 |
|------|------|------|
| 2026-06-29 | MyBatis 而非 JPA | 课程教授 MyBatis |
| 2026-06-29 | 自定义 JwtAuthFilter | 减少依赖 |
| 2026-06-29 | `@Transactional` | Spring 声明式事务 |
| 2026-06-29 | Naive UI 保留 | 对齐原版视觉效果 |
| 2026-06-29 | Axios | 课程教授 |
| 2026-07-01 | seed.sql 脚本 | 可重复执行，无需重编译 |
| 2026-07-01 | 13 个组件全部移植 | 功能与画面完全对齐 |
