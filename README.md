# Library Full-Stack System (Spring Boot 版)

图书馆全栈管理系统。支持多角色图书借阅、预约、罚款等完整业务流程。页面交互参照主流高校图书馆系统设计。

本仓库是从原版 TypeScript（Fastify + Prisma + Naive UI）迁移至 **Spring Boot 3 + MyBatis + Vue 3** 的版本，功能和画面完全对齐。

---

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 · Vite · Naive UI · Pinia · Vue Router · Axios |
| 后端 | Spring Boot 3.2 · MyBatis 3.0 · Maven · MySQL 8.0 |
| 测试 | Mockito · JUnit 5 · Spring Boot Test · H2 |
| 工具 | Maven Wrapper · BCrypt · JWT · Druid · PageHelper |

---

## 快速开始

### 环境要求

| 依赖 | 版本 | 备注 |
|------|------|------|
| Java | 17+（本地环境用 21） | WSL 默认 Java 25 不兼容，编译前设 `JAVA_HOME` |
| MySQL | 8.0 | 127.0.0.1:3306 |
| Node.js | 18+ | 前端构建需要 |
| npm | 随 Node | 镜像源 `registry.npmmirror.com` |

### 1. 数据库初始化

```bash
# 创建数据库
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS library"

# 导入种子数据（3类读者 · 20种书 · 63复本 · 23借阅 · 2笔罚款 · 3个预约）
mysql -h127.0.0.1 -uroot -p library < seed.sql
```

### 2. 启动后端（:8080）

```bash
# 只需一个命令
./start.sh

# 或分步操作
./mvnw clean package -DskipTests
java -jar target/library-fullstack-*.jar --spring.profiles.active=dev

# 可通过环境变量传入数据库密码
DB_PASSWORD=*** ./start.sh
```

> 不要用 `mvn spring-boot:run`，反复调用有 OOM 问题。用 `./start.sh` 或直接 `java -jar`。

### 3. 启动前端（:5175）

```bash
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev
```

前端 dev server 自动代理 `/api` → 后端 `:8080`，开发时无需额外配置 CORS。

---

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 图书管理、借还管理、读者管理、罚款管理 |
| 本科生 | 2023110101 | reader123 | 可借 5 本，借期 30 天 |
| 研究生 | 2022110201 | reader123 | 可借 10 本，借期 60 天 |

---

## 功能概览

### 管理员

| 页面 | 路径 | 功能 |
|------|------|------|
| 仪表盘 | `/admin/dashboard` | 统计概览（馆藏/读者/借阅/逾期）、快捷操作、系统信息 |
| 图书管理 | `/admin/books` | 图书 CRUD、复本管理、条码录入、库存对账 |
| 借还管理 | `/admin/borrows` | 借阅记录、还书操作（支持预约提升）、续借处理 |
| 流通台 | `/admin/circulation` | 扫码快速借还 |
| 分类管理 | `/admin/categories` | 图书分类 CRUD，有图书的分类不可删除 |
| 读者管理 | `/admin/readers` | 读者列表、详情编辑 |
| 罚款管理 | `/admin/fines` | 罚款记录、缴费处理（按类型/已缴状态筛选） |
| 设置 | `/admin/settings` | 流通规则矩阵、读者类型、资料类型配置 |
| 统计分析 | `/admin/stats` | 热门图书 Top 20、月度借阅统计 |

### 读者

| 页面 | 路径 | 功能 |
|------|------|------|
| 图书检索 | `/reader/books` | 搜索栏 + 分类筛选 + 快捷借阅 |
| 我的借阅 | `/reader/my-borrows` | 当前借阅/历史记录、续借、预约管理（含逾期警示） |
| 个人中心 | `/reader/profile` | 编辑个人信息 |

### 公共

| 页面 | 路径 | 功能 |
|------|------|------|
| 图书搜索 | `/books` | 多维度搜索 + 分类过滤 + 排序分页，无需登录 |
| 图书详情 | `/books/:id` | 详细信息 + 馆藏列表 + 借阅/预约入口 |
| 登录 | `/login` | 深色玻璃态登录页，含注册弹窗 |

---

## 项目结构

```
library-full-stack/
├── start.sh                    # 后端启动脚本（构建 JAR + 运行）
├── seed.sql                    # 种子数据
├── schema.sql                  # 数据库建表 DDL（简化版）
├── AGENTS.md                   # AI 开发参考（给 AI agent 看的）
│
├── src/main/java/com/library/
│   ├── config/                 # JwtAuthFilter, CORS, 角色拦截器
│   ├── controller/             # 45 个 REST API 端点
│   ├── service/                # 业务逻辑层，@Transactional 事务管理
│   ├── mapper/                 # MyBatis Mapper 接口
│   ├── entity/                 # POJO 数据实体
│   ├── dto/request|response/   # 请求/响应 DTO
│   └── exception/              # 统一异常处理 + 中文错误消息
│
├── src/main/resources/
│   ├── mappers/                # 9 个 MyBatis XML 映射文件
│   ├── application.yml         # 通用配置
│   ├── application-dev.yml     # 开发环境（Druid + MySQL）
│   └── application-prod.yml    # 生产环境
│
├── frontend/
│   └── src/
│       ├── api/                # Axios 封装 + 类型化 API
│       ├── stores/             # Pinia 状态管理
│       ├── router/             # Vue Router（含路由守卫）
│       ├── components/         # 13 个通用组件
│       └── views/              # 17 个页面（public/admin/reader）
│
└── src/test/java/com/library/
    ├── service/                # 51 个 Service 单元测试
    └── controller/             # 4 个 Controller 集成测试
```

---

## API 概览

45 个 REST 端点，按模块分组（完整信息见 `AGENTS.md` §4）：

| 模块 | 端点数 | 公共 | 需认证（读者） | 需认证（管理员） |
|------|--------|------|---------------|----------------|
| Auth | 5 | 2 | 1 | 2 |
| Books | 8 | 4 | — | 4 |
| BookItems | 1 | — | 1 | — |
| Categories | 4 | 1 | — | 3 |
| Borrows | 6 | — | 3 | 3 |
| Holds | 6 | 1 | 3 | 2 |
| Readers | 4 | — | 1 | 3 |
| Fines | 3 | — | 1 | 2 |
| Rules | 4 | 3 | — | 1 |
| Stats | 3 | — | — | 3 |
| Health | 1 | 1 | — | — |

**认证方式：** Bearer Token（JWT），登录后返回，存 localStorage，Axios interceptor 自动注入。

---

## 配置说明

### 数据库密码与 JWT 密钥

通过 Spring Profile 隔离：

| 文件 | 用途 | DB 密码 | JWT 密钥 |
|------|------|---------|----------|
| `application-dev.yml` | 开发 | `${DB_PASSWORD:li200603}` | `${JWT_SECRET:LibraryFullStack2024JWT...}` |
| `application-prod.yml` | 生产 | `${DB_PASSWORD}` | `${JWT_SECRET}` |

默认值仅用于本地开发。生产环境必须通过环境变量设置，**禁止在配置文件中硬编码密码**。

### CORS

前端开发服务器在 :5175 运行，后端配置了跨域来源 :5173（原版 TypeScript）和 :5175（Java 版）。

---

## 种子数据

`seed.sql` 包含完整演示数据：

| 项目 | 数量 | 说明 |
|------|------|------|
| 读者分类 | 3 | 本科生/研究生/教师，不同借阅权限 |
| 资料类型 | 3 | 普通图书/教材/期刊，不同借阅天数 |
| 流通规则 | 9 | 3×3 矩阵：每种读者×每种资料 的借阅限制 |
| 用户 | 9 | 管理员 1 人 + 读者 8 人（含逾期/罚款示例） |
| 图书 | 20 | 计算机/文学/经济/科学 4 类 |
| 复本 | 63 | 部分图书有多个副本 |
| 借阅记录 | 23 | 含已逾期、已归还、正常借出 |
| 罚款 | 2 | 逾期罚款示例 |
| 预约 | 3 | 待处理 + 已就绪 |

---

## 开发说明

### Java 版本注意事项

WSL 默认 Java 25 不兼容此项目（Maven 编译报 `release version 17 not supported`）：

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./mvnw test
```

### 运行测试

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./mvnw test    # 96 tests, 0 failures required
```

### 常用命令速查

```bash
./mvnw compile                   # 编译
./mvnw test                      # 测试
./mvnw clean package -DskipTests # 构建 JAR
kill -9 $(lsof -ti:8080)         # 停后端
kill -9 $(lsof -ti:5175)         # 停前端
```

---

## 开发规范

### 新增一个功能的标准流程

```
① Issue → ② Fork / 建分支 → ③ 实现 → ④ 自测 → ⑤ PR → ⑥ Review → ⑦ 合并
```

**① 提 Issue**：描述需求/问题，明确预期结果和验收标准。见 GitHub Issues 模板。

**② 分支命名**：`fix/xxx`（修复）、`feat/xxx`（新功能）、`refactor/xxx`（重构）。

**③ 实现检查清单**：

| 检查项 | 说明 |
|--------|------|
| 数据库列名 | 用 `SHOW CREATE TABLE 表名` 确认实际列名（驼峰/下划线混用）**
| 新增 API | Controller 参数用 `@RequestParam` 收集，禁止直接 `HttpServletRequest` |
| 错误消息 | 全部中文，格式与 `AppException` 全局处理一致 |
| 权限 | 写操作必须有鉴权，区分管理员/读者角色 |
| 事务 | 多 DAO 写入必须加 `@Transactional` |
| MyBatis SQL | 复杂动态 SQL 写到 XML 文件，不要嵌在注解字符串里 |

**④ 自测标准**：

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./mvnw test    # 全量测试必须通过
```

- 新增 >50 行功能代码：必须附带单元测试
- 新增 >20 行：强烈建议加测试
- 至少覆盖：正常路径 + 边界条件 + 错误路径

**⑤ PR 编写**：

```text
PR 标题: <type>: <简短描述> (closes #N)
PR 正文: 
  - 改了什么（逐条）
  - 关联 issue 编号
  - 测试结果截图/日志
```

**⑥ Review 标准**：

Reviewer 必须逐条对照 issue 原文验证，不可凭"扫视觉得没问题"就合并。详见 GitHub Projects 看板中的审查清单。

**⑦ 合并**：squash merge，保持 main 历史干净。

---

### 多维度查询设计规范（重要）

当新增多个同层级查询（如多个 facet 维度、多个统计聚合）时，**必须列出维度覆盖矩阵**逐格验证参数穿透，禁止凭直觉逐个写 SQL。

正确做法示例（以 facet 查询为例）：

```
                 search  categoryId  language  yearMin  yearMax  campus  location
language facet     ✅        ✅                  ✅       ✅
subject facet      ✅                   ✅      ✅       ✅
yearRange facet    ✅        ✅        ✅      ✅       ✅
campus facet       ✅        ✅        ✅      ✅       ✅        —        ✅
location facet     ✅        ✅        ✅      ✅       ✅        ✅        —
```

矩阵完成后，每个 `❌` 必须有一个设计理由（为什么这个维度不需要这个参数），否则就是遗漏。

### 代码风格

| 项 | 规范 |
|----|------|
| Java | Spring Boot 3 + MyBatis 标准三层 |
| 前端 | Vue 3 Composition API + TypeScript |
| 数据库列名 | 新表统一全小写下划线（`created_at`），与 MyBatis `map-underscore-to-camel-case` 配合 |
| 错误消息 | 全中文，`AppException` 统一抛出 |
| 测试 | Mockito + JUnit 5，service 层测试继承 `AbstractServiceTest` |
| 提交信息 | `type: 中文描述`（type: fix/feat/refactor/docs/chore/test） |

### 提交流程速查

```bash
# 假设在功能分支上
git add -A
git commit -m "fix: 修复XXX问题"
git push origin <分支名>
# → GitHub 上开 PR，关联 issue
```

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `JAVA_HOME` | 必设 | WSL 必须指向 Java 21 |
| `DB_PASSWORD` | `li200603` | 数据库密码（dev profile） |
| `JWT_SECRET` | 内置默认 | 仅开发用，生产必须改 |

**安全：** 禁止在注释、commit message、seed.sql 中写密码。禁止提交含密码的配置文件。

---

## 项目状态

45 API · 71 Java 文件 · 31 Vue 文件 · 9 XML Mapper · **96 测试** ✅

---

## 相关文档

| 文件 | 用途 |
|------|------|
| [AGENTS.md](AGENTS.md) | AI 开发参考 — 命令/API 路由表/编码规范/架构决策/错误陷阱 |
| [schema.sql](schema.sql) | 数据库建表 DDL（简化版） |
| [seed.sql](seed.sql) | 种子数据 |
