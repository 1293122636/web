# Library Full-Stack System (Spring Boot 版)

图书馆全栈管理系统。从原版 TypeScript（Fastify + Prisma + NaiveUI）完整迁移，功能画面完全对齐。

**技术栈：** Vue 3 + Naive UI · Spring Boot 3.x + MyBatis · MySQL 8.0 · Maven

---

## 快速开始

```bash
# 后端（构建JAR + 启动 → :8080）
./start.sh

# 种子数据（首次需要）
mysql -h127.0.0.1 -uroot -p library < seed.sql

# 前端（→ :5175）
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev
```

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| 2023110101 | reader123 | 本科生读者 |
| 2022110201 | reader123 | 研究生读者 |

---

## 项目状态

45 API endpoints · 71 Java files · 31 Vue files · 8 XML mappers · 55 tests ✅

---

## 文档导航

| 文件 | 用途 |
|------|------|
| [AGENTS.md](AGENTS.md) | AI 开发参考 — 命令/API 路由表/编码规范/架构决策/错误陷阱 |
| seed.sql | 种子数据（3 类读者 · 20 种书 · 63 复本 · 23 借阅） |
