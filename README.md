# Library Full-Stack System

图书馆全栈管理系统。三层深度：书目 → 复本 → 规则引擎。

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

默认账号：admin / admin123，reader / reader123

---

## 文档导航

| 文档 | 内容 | 受众 |
|------|------|------|
| [AGENTS.md](AGENTS.md) | 架构决策 · API 路由表 · 开发规范 · 错误区 | AI 代理 / 开发者 |
| [ASSESSMENT.md](ASSESSMENT.md) | 代码质量评估 · 类型安全 · 测试覆盖率 | 代码审查 |
| [PLAN.md](PLAN.md) | 模块化实施计划 · 依赖图 | 项目管理 |
| [SUMMARY.md](SUMMARY.md) | 今日工作总结 | 日报 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 四层架构文档 | 架构参考 |

---

## 项目状态

- **当前版本：** v0.3.3（13 轮审计完成，82 修复）
- **API 端点：** 40 个，全测试覆盖
- **测试：** 后端 52/52 服务层 PASS · 前端 18/18 PASS
- **评分：** 8.5/10（见 ASSESSMENT.md）
- **仓库：** https://github.com/Mrappleking/library-full-stack
