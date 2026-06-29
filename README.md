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

## 开发注意事项

### 后端

- **SQL 只写 XML。** MyBatis SQL 全部在 `src/main/resources/mappers/*.xml`，不要在 Java 注解里写 SQL（历史教训：注解 SQL 和 XML 同时定义同一 statement 会报 `Mapped Statements collection already contains key`）
- **列名陷阱。** 数据库列名混合 `categoryId`（camelCase）和 `created_at`（snake_case），写 SQL 前必须 `SHOW CREATE TABLE` 确认实际列名
- **事务覆盖。** `borrow()` / `returnBook()` / `payFine()` 等多 DAO 写入必须加 `@Transactional`，缺少会因并发导致数据不一致
- **启动方式。** 用 `./start.sh`，不要 `mvn spring-boot:run`（OOM 问题）
- **创建数据库（如果尚未创建）**
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS library;"
- **导入种子数据**
mysql -h127.0.0.1 -uroot -p library < seed.sql
### 前端

- **镜像源。** 始终用 `npm install --registry=https://registry.npmmirror.com`
- **组件库。** Naive UI（`n-data-table`、`n-button` 等） + `@vicons/ionicons5` 图标
- **无 emoji。** 用 SVG 线条图标替代 emoji
- **API 代理。** 前端开发服务器 :5175 自动代理 `/api` → 后端 :8080
- **CORS。** 后端配置了两个允许来源 5173 和 5175，两版前端可同时运行对比

### 全栈

- **提交前跑测试。** `./mvnw test`（55 个测试全部通过再提交）

---

## 文档导航

| 文件 | 用途 |
|------|------|
| [AGENTS.md](AGENTS.md) | AI 开发参考 — 命令/API 路由表/编码规范/架构决策/错误陷阱 |
| seed.sql | 种子数据（3 类读者 · 20 种书 · 63 复本 · 23 借阅） |
