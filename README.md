# Library Full-Stack System

图书馆全栈管理系统。四层架构（前端→路由→服务→数据），三层业务深度（书目→复本→规则引擎）。

**技术栈：** TypeScript · Vue 3 + Naive UI · Fastify · Prisma 5 · MySQL

---

## 快速开始

```bash
# 1. 数据库
mysql -u root -p -e "CREATE DATABASE library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# 2. 后端
cd backend
cp .env.example .env   # 编辑 DATABASE_URL + JWT_SECRET
npm install
npx prisma db push
npx prisma db seed
npm run dev             # → localhost:3000

# 3. 前端
cd frontend
npm install
npm run dev             # → localhost:5173
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

## 项目状态

| 指标 | 值 |
|------|-----|
| 版本 | v0.4.0 (14 轮审计, 95 fixes) |
| API 端点 | 45 |
| 后端测试 | 106/106 PASS (52 service + 54 route) |
| 前端构建 | `vite build` ✅ |
| ESLint | 0 errors |
| 数据库 | MySQL 8.0 (WSL 127.0.0.1:3306) |

---

## 演示数据

种子脚本 `backend/prisma/seed.ts` 提供完整演示数据，运行 `npx prisma db seed` 即可重置。

- **20 种图书**，5 分类，3 校区（青岛/泰安/济南），含中/英/日文
- **8 位读者**，覆盖本科生/研究生/教师三种类型
- **23 条借阅记录**，跨 12 个月分布，含活跃借阅、已归还、逾期未还
- **2 笔罚款**（已缴和未缴各一）
- **3 条预约**（2 pending + 1 ready）

运行后可直接体验：搜索分面、借还书、预约排队、逾期罚款、月度统计、热门排行等全部功能。

---

## 架构

**AI 开发入口 → [AGENTS.md](AGENTS.md)**（命令、API 路由、编码规范、架构决策、陷阱记录）

**人类开发者**：后端代码在 `backend/src/`，前端在 `frontend/src/`。目录结构见 AGENTS.md 第 3 章。
