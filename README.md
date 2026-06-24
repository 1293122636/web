# Library Full-Stack System — README.md

## 一、项目概述

图书馆全栈管理系统，面向高校/中小型图书馆场景。具备三层深度架构：
- **书目级：** 图书编目（ISBN/标题/作者/分类）
- **复本级：** 物理复本追踪（条码号/索书号/馆藏地/品相）
- **规则引擎：** CirculationRule 矩阵（读者类型 × 资料类型 → 借阅天数/上限/续借/罚款）

- **语言：** TypeScript 全栈
- **运行时：** Node.js + tsx（TypeScript ESM 直接运行）
- **前端：** Vue 3 + Vite + Naive UI 2.x（暗色主题）
- **后端：** Fastify（ESM）+ Prisma 5 ORM
- **数据库：** MySQL 8（WSL 本机 127.0.0.1:3306）
- **认证：** JWT
- **部署路径：** `~/workplace/library-full-stack/`（WSL 开发）→ `D:\workplace\Library Full-Stack Project\`（Windows 交付）

---

## 二、技术栈明细

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Vue 3 | 3.x | SFC 组件化前端 |
| 构建 | Vite | 8.x | 前端打包/HMR |
| UI | Naive UI | 2.44 | 暗色主题组件库 |
| 路由 | Vue Router | 4.x | SPA 路由+守卫 |
| HTTP | fetch | — | 零依赖 API 调用 |
| 后端框架 | Fastify | 5.x | 高性能 Node HTTP |
| ORM | Prisma | 5.22 | 类型安全数据库操作 |
| 校验 | Zod | 3.x | 请求体 schema 校验 |
| 密码 | bcryptjs | 3.x | 密码哈希 |
| 数据库 | MySQL | 8.x | 关系型持久化 |

---

## 三、项目目录结构

```
library-full-stack/
├── frontend/                    # Vue 3 SPA
│   ├── src/
│   │   ├── main.ts              # 入口：注册 Vue/Naive UI/Pinia/Router
│   │   ├── App.vue              # 根组件：暗色主题 n-config-provider
│   │   ├── api/
│   │   │   └── index.ts         # API 客户端：fetch 封装 + auth token 管理
│   │   ├── router/
│   │   │   └── index.ts         # 路由表 + beforeEach 守卫
│   │   └── views/
│   │       ├── Login.vue        # 登录/注册
│   │       ├── admin/
│   │       │   ├── Layout.vue   # 管理员侧边栏布局（8项导航）
│   │       │   ├── Dashboard.vue# 概览统计
│   │       │   ├── Books.vue    # 图书 CRUD + 复本展开 + 加复本
│   │       │   ├── Categories.vue# 分类 CRUD
│   │       │   ├── Readers.vue  # 读者管理
│   │       │   ├── Borrows.vue  # 借阅管理 + 罚款显示
│   │       │   ├── Fines.vue    # 罚款管理（筛选+缴费）
│   │       │   ├── Stats.vue    # 统计报表
│   │       │   └── Settings.vue # 借阅规则/读者类型/资料类型
│   │       └── reader/
│   │           ├── Layout.vue   # 读者侧边栏布局
│   │           ├── Books.vue    # 图书浏览+借阅
│   │           ├── MyBorrows.vue# 我的借阅+续借+欠费明细
│   │           └── Profile.vue  # 个人信息编辑
│   ├── vite.config.ts           # Vite 配置（proxy /api → :3000）
│   ├── tsconfig.json
│   └── index.html               # 入口 HTML
├── backend/
│   ├── src/
│   │   ├── index.ts             # 服务器入口：Fastify + JWT + CORS + 路由注册
│   │   ├── routes/
│   │   │   ├── auth.ts          # 认证路由
│   │   │   ├── books.ts         # 图书 CRUD + 复本列表
│   │   │   ├── categories.ts    # 分类 CRUD
│   │   │   ├── borrows.ts       # 借阅管理（借书/还书/续借 — 规则引擎驱动）
│   │   │   ├── readers.ts       # 读者管理
│   │   │   ├── stats.ts         # 统计分析
│   │   │   ├── fines.ts         # 罚款管理（查询+缴费）
│   │   │   └── rules.ts         # 借阅规则 CRUD + 类型查询
│   │   ├── services/
│   │   │   ├── auth.service.ts
│   │   │   ├── book.service.ts
│   │   │   ├── borrow.service.ts
│   │   │   ├── category.service.ts
│   │   │   ├── user.service.ts
│   │   │   ├── stats.service.ts
│   │   │   ├── fine.service.ts
│   │   │   ├── rule.service.ts
│   │   │   ├── cover.service.ts
│   │   │   ├── rules.ts         # 规则查询引擎（getRule / checkBorrowLimit）
│   │   │   └── fines.ts         # 罚款计算（createFine / calcOverdueFine）
│   │   ├── middleware/
│   │   │   └── requireAdmin.ts  # 管理员权限中间件
│   ├── prisma/
│   │   ├── schema.prisma        # 数据模型定义（9 表）
│   │   ├── seed.ts              # 种子数据脚本
│   │   └── migrations/          # 迁移文件
│   ├── tsconfig.json
│   ├── .env / .env.example
│   └── package.json
├── AGENTS.md                    # AI 助手开发规范
└── README.md                    # 本文件
```

---

## 四、数据库模型（9 张表）

### 4.1 枚举

| 枚举 | 值 | 说明 |
|------|----|------|
| UserRole | admin, reader | 管理员/读者 |
| BookStatus | available, borrowed, removed | 书目级状态 |
| ItemCondition | normal, damaged, repairing, lost, withdrawn | 复本品相 |
| ItemStatus | available, borrowed, repairing, lost, withdrawn | 复本状态 |
| FineType | overdue, lost, damage | 罚款类型 |

### 4.2 用户体系

**PatronCategory（读者类型）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int PK | |
| name | VarChar(50) unique | 本科生/研究生/教师 |

**User（用户）** — 新增 `totalFines`（累计欠费）、`patronCategoryId`（FK → PatronCategory）

### 4.3 资料类型

**ItemType**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int PK | |
| name | VarChar(50) unique | 普通图书/新书速递/期刊 |
| loanDays | Int (default 30) | 默认借阅天数 |
| fineRate | Decimal(10,2) (default 0.10) | 日罚金（元） |

### 4.4 借阅规则引擎

**CirculationRule**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int PK | |
| patronCategoryId + itemTypeId | FK 组合 unique | 规则匹配键 |
| maxBorrows | Int (default 5) | 借阅册数上限 |
| loanDays | Int (default 30) | 每次借阅天数 |
| renewals | Int (default 1) | 可续借次数 |
| renewalDays | Int (default 15) | 续借延长天数 |
| finePerDay | Decimal(10,2) (default 0.10) | 日罚金率 |

### 4.5 图书体系（书目+复本）

**Book（书目）** — 保持不变。`total`/`available` 为冗余计数器，真实库存由 BookItem 驱动。

**BookItem（物理复本）** — 新增
| 字段 | 类型 | 说明 |
|------|------|------|
| barcode | VarChar(30) unique | 条码号 LIB-000001-1 |
| callNumber | VarChar(100) | 索书号 TP312/1002 |
| location | VarChar(100) | 馆藏地 |
| condition | ItemCondition | 品相（正常/破损/修补中/遗失/剔除） |
| status | ItemStatus | 状态（在架/借出/修补/遗失/剔除） |
| price | Decimal(10,2) | 购入价格 |
| acquiredAt | DateTime | 入藏日期 |
| bookId | FK → Book | |
| itemTypeId | FK → ItemType | 资料类型 |

**BorrowRecord** — 新增 `bookItemId`（FK → BookItem，nullable），`fines: Fine[]`

### 4.6 罚款体系

**Fine**
| 字段 | 类型 | 说明 |
|------|------|------|
| amount | Decimal(10,2) | 金额 |
| type | FineType | overdue/lost/damage |
| paid | Boolean (default false) | 是否已缴 |
| borrowRecordId | FK → BorrowRecord | |
| userId | FK → User | |

### 4.7 核心业务约束（规则引擎驱动）

- **借阅天数：** 查 CirculationRule（按读者类型 × 资料类型），不再硬编码 30 天
- **借阅上限：** `checkBorrowLimit()` 统计 active 借阅数 vs rule.maxBorrows
- **续借次数：** 查 rule.renewals，续借天数查 rule.renewalDays
- **逾期罚金：** 还书时自动计算 `max(0, returnDate - dueDate) × rule.finePerDay`
- **库存更新：** 借书时 BookItem 状态变 borrowed + Book.available 递减（$transaction）
- **还书权限：** 读者还自己 + 管理员代还
- **欠费累计：** Fine 创建时 User.totalFines += amount，缴费时递减

---

## 五、API 接口文档

所有接口前缀 `/api`，认证通过 `Authorization: Bearer <token>` 头部传递。

### 5.1 系统

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/health | 否 | 健康检查 |

### 5.2 认证（/api/auth）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | /api/auth/register | 否 | 读者注册 |
| POST | /api/auth/login | 否 | 登录 |
| GET | /api/auth/me | 是 | 当前用户 |
| GET | /api/auth/users | admin | 用户列表 |
| POST | /api/auth/admin/create | admin | 创建管理员 |

### 5.3 图书（/api/books）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/books | 否 | 列表（分页+搜索+分类筛选） |
| GET | /api/books/:id | 否 | 详情 |
| GET | /api/books/:id/items | 否 | 复本列表（条码/索书号/馆藏地/状态/品相/价格） |
| POST | /api/books | admin | 新增 |
| PUT | /api/books/:id | admin | 更新（total 修改带库存边界保护） |
| DELETE | /api/books/:id | admin | 删除 |

### 5.4 分类（/api/categories）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/categories | 否 | 列表 |
| POST | /api/categories | admin | 新增 |
| PUT | /api/categories/:id | admin | 更新 |
| DELETE | /api/categories/:id | admin | 删除（有图书则拒绝） |

### 5.5 借阅（/api/borrows）— 规则引擎驱动

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/borrows/my | 是 | 我的借阅（含 bookItem + fines） |
| GET | /api/borrows | admin | 全部借阅 |
| GET | /api/borrows/history | 是 | 我的历史 |
| POST | /api/borrows/borrow | 是 | 借书（查规则 → 上限检查 → 自动选复本 → $transaction） |
| POST | /api/borrows/return/:id | 是 | 还书（逾期自动计算罚金 + 创建 Fine） |
| POST | /api/borrows/renew/:id | 是 | 续借（查 renewalDays + renewals 上限） |

### 5.6 读者（/api/readers）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/readers | admin | 列表 |
| GET | /api/readers/:id | admin | 详情+借阅 |
| PUT | /api/readers/:id | admin | 编辑 |
| PUT | /api/readers/profile | 是 | 自编辑 |

### 5.7 统计（/api/stats）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/stats | admin | 5 项聚合 |
| GET | /api/stats/popular | admin | 热门 TOP 20 |
| GET | /api/stats/monthly | admin | 月度借阅量 |

### 5.8 罚款（/api/fines）— 新增

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/fines | admin | 全部（可筛 type + paid） |
| GET | /api/fines/my | 是 | 我的罚款 |
| POST | /api/fines/:id/pay | admin | 标记已缴（递减 totalFines） |

### 5.9 规则管理（/api/admin/rules）— 新增

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/admin/rules | 否* | 全部规则 |
| GET | /api/admin/rules/patron-categories | 否* | 读者类型列表 |
| GET | /api/admin/rules/item-types | 否* | 资料类型列表 |
| PUT | /api/admin/rules | admin | 创建/更新规则（upsert） |

---

## 六、前端架构

### 6.1 路由设计

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| /login | Login.vue | public | 登录+注册 |
| /admin | Layout.vue | admin | 侧边栏 8 项 |
| /admin/dashboard | Dashboard.vue | admin | 概览 |
| /admin/books | Books.vue | admin | 图书 CRUD + 复本展开 + 加复本 |
| /admin/categories | Categories.vue | admin | 分类 CRUD |
| /admin/readers | Readers.vue | admin | 读者管理 |
| /admin/borrows | Borrows.vue | admin | 借阅管理 + 罚款标签 |
| /admin/fines | Fines.vue | admin | 罚款管理（筛选+缴费） |
| /admin/stats | Stats.vue | admin | 统计报表 |
| /admin/settings | Settings.vue | admin | 规则管理 + 类型列表 |
| /reader | Layout.vue | reader | 侧边栏 3 项 |
| /reader/books | Books.vue | reader | 图书浏览+借阅 |
| /reader/my-borrows | MyBorrows.vue | reader | 借阅历史+续借+欠费明细 |
| /reader/profile | Profile.vue | reader | 个人信息 |

### 6.2 服务层

**rules.ts** — `getRule(prisma, patronCategoryId, itemTypeId)` 查 CirculationRule，无匹配返回默认规则。`checkBorrowLimit()` 统计 active 借阅数 vs 上限。

**fines.ts** — `createFine()` 创建罚款并更新 totalFines。`calcOverdueFine()` 计算逾期天数 × 日罚金率。

---

## 七、设计系统

- **主题：** Naive UI darkTheme，brandColor `#5e6ad2`
- **DESIGN-TODO 标记：** 全部 8 处已决策并实现 (Module L 完成)

---

## 八、部署流程

```bash
# 前端构建
cd frontend && npm run build

# 后端启动
cd backend
export DATABASE_URL="mysql://root:***@127.0.0.1:3306/library"
export JWT_SECRET="<secret>"
npx prisma migrate deploy
npx tsx src/index.ts
# 生产：npx tsc && node dist/index.js
```

---

## 九、种子数据

运行 `npx prisma db seed`：
- 管理员 admin/admin123，读者 reader/reader123
- 1 读者类型（普通读者），1 资料类型（普通图书），1 条规则（5册×30天×¥0.10/天）
- 5 分类 × 7 本书 × 每本 3 复本（共 21 个 BookItem，条码 LIB-000001-1 至 LIB-000007-3）

---

## 十、项目优点

1. **三层深度架构：** 书目→复本→规则引擎，不是简单的 CRUD 壳
2. **规则驱动借阅：** CirculationRule 矩阵决定所有借阅参数，不再硬编码
3. **复本追踪：** BookItem 含条码号/索书号/馆藏地/品相/价格，精确到物理册
4. **罚款系统：** 自动计算逾期费，累加 totalFines，支持缴费
5. **事务保证原子性：** 借书/还书三步操作（创建记录 + 改库存 + 改复本状态）通过 `$transaction` 绑定
6. **前端统一：** 全部 Naive UI 暗色主题，n-config-provider 全局配色
7. **AI 可维护：** AGENTS.md 含完整 API 路由表/错误区/架构决策历史/开发规范
8. **环境隔离：** WSL ext4 开发 → Windows NTFS 交付

---

## 十一、项目缺点与改进方向

### 11.1 已解决（自 v0.1）
- ~~图书复本无条码~~ → BookItem 表（v0.3）
- ~~无罚款系统~~ → Fine 表 + 自动计算（v0.3）
- ~~借阅规则硬编码 30 天~~ → CirculationRule 规则引擎（v0.3）

### 11.2 仍缺失
- 无预约系统（Reservation 表）
- 无操作审计日志
- 无自动化测试/CI
- Naive UI 主 bundle 1.36MB（未按需加载）
- stats/monthly 内存分组（大数据量时需换 SQL GROUP BY）
- 无批量导入 MARC 记录

---

## 十二、开发命令速查

```bash
# 后端
cd backend
npm run dev          # 开发服务器
npm run db:push      # 同步 schema → MySQL
npm run db:generate  # 生成 Prisma Client
npm run db:seed      # 灌种子数据

# 前端
cd frontend
npm run dev          # Vite 开发服务器（5173）
npm run build        # 生产构建
```

---

## 十三、版本历史

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-06-23 | 0.1.0 | 初始骨架 |
| 2026-06-24 | 0.2.0 | P0 bug 修复 + Naive UI 重构 + seed + AGENTS.md v2 |
| 2026-06-24 | 0.3.0 | 深度升级：BookItem/CirculationRule/Fine 三层架构 + 规则引擎 + 前端 Settings/Fines 新页 |

---

## 十四、下一迭代蓝图（v0.4.0 → v0.5.0）

基于对 Koha/Evergreen/Ex Libris 等专业 ILS 的架构分析，当前三项结构性缺失优先补全。

### 14.1 预约系统（Holds）

**业务流程：**
```
读者发现书已借出 → 点击"预约" → 进入等待队列 → 
书归还时 → 系统检测队首读者 → 生成取书通知 → 
读者 N 天内到馆取书 → 超时取消 → 顺延下一位
```

**数据模型：**
```
Hold
├── id, userId, bookItemId
├── placedAt（预约时间）
├── expiresAt（取书截止时间）
├── pickupLocation（取书地点）
├── status: pending / waiting / ready / cancelled / expired
├── queuePosition（动态计算：同 bookItem 的 pending+waiting count）
└── notifiedAt（通知发送时间）
```

**新增 API：**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/holds | 读者预约一本书（自动排到队尾） |
| GET | /api/holds/my | 我的预约列表 |
| GET | /api/holds | 管理员查看全部预约（可按状态筛选） |
| POST | /api/holds/:id/cancel | 取消预约 |
| POST | /api/holds/:id/ready | 管理员标记可取（还书触发后自动调用） |
| POST | /api/holds/:id/checkout | 取书完成，转为借阅 |

**还书触发逻辑（修改 borrows.ts return）：**
```
还书成功后 → 查该书是否有 pending holds → 
如有 → 队首 hold 状态变为 ready + 生成通知 + 
bookItem 状态变为 on_hold（不可借给其他读者）
```

**前端页面：**
- 管理员：`/admin/holds` — NDataTable，按状态筛选（all/pending/ready/expired）
- 读者：`/reader/holds` — 我的预约 + 队列位置 + 取书倒计时
- 读者 Books 页面："已借出"的书显示"预约"按钮替代"借阅"
- DESIGN-TODO: 预约队列可视化——列表还是时间线？

---

### 14.2 通知系统（Notices）

**通知类型：**
| 触发事件 | 通知内容 | 渠道 |
|---------|---------|------|
| 借书成功 | 借阅确认：书名、到期日 | — |
| 还书成功 | 归还确认 + 逾期罚款（如有） | — |
| 逾期 N 天 | 催还通知 | email/sms |
| 预约到馆 | 取书通知：书名、截止日、取书点 | email/sms |
| 预约即将过期 | 最后提醒 | email/sms |
| 账号欠费超阈值 | 冻结借阅通知 | email |
| 续借成功 | 新到期日 | — |

**数据模型：**
```
Notification
├── id, userId
├── type: overdue / hold_ready / hold_expiring / fine_threshold
├── title, body（模板渲染后的文本）
├── channel: email / sms / in_app
├── sentAt（nullable，未发送=null）
├── readAt（nullable，站内消息）
├── templateId（FK → NotificationTemplate）
└── createdAt

NotificationTemplate
├── id, name, type
├── titleTemplate, bodyTemplate（如 "《{bookTitle}》将于 {dueDate} 到期"）
├── channel: email / sms / in_app
└── active
```

**实现策略（渐进式）：**
- **Phase A（最小可用）：** 仅站内消息——登录后顶部 Bell 图标 + 未读计数
- **Phase B：** 加 email（用 nodemailer + SMTP，配置环境变量）
- **Phase C：** 加 SMS（接阿里云短信 SDK，配置环境变量）
- 任务调度：每 24h 跑一次 cron，查 overdue borrows → 生成通知

**新增 API：**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notifications | 我的通知列表（按未读优先） |
| POST | /api/notifications/:id/read | 标记已读 |
| GET | /api/notifications/unread-count | 未读数（Bell 图标用） |
| GET | /api/admin/notifications/templates | 模板列表 |
| PUT | /api/admin/notifications/templates/:id | 编辑模板 |

**前端：**
- 全局 Bell 图标：`header` 或 Layout 右上角，Badge 显示未读数
- 通知下拉面板：`n-popover` 显示最近 5 条
- 完整通知页：`/reader/notifications` — 列表 + 已读/未读筛选
- DESIGN-TODO: Bell 图标用什么？通知面板弹出动画？

---

### 14.3 报表增强（Reports）

**当前状态：** 3 个简单统计端点（概览/热门书/月度借阅）。

**增强后报表矩阵：**

| 报表 | 维度 | 数据来源 | 输出 |
|------|------|---------|------|
| 流通量报表 | 时间 × 读者类型 × 资料类型 | borrow_records | 表 + 图 |
| 馆藏分析 | 分类 × 状态 × 馆藏地 | books + book_items | 表 |
| 利用率报表 | 图书 × 借阅次数 ÷ 入藏天数 | 计算字段 | 排名表 |
| 罚款统计 | 时间 × 类型 × 缴费状态 | fines | 汇总表 |
| 读者活跃度 | 借阅次数分段（0/1-5/6-20/>20） | borrow_records GROUP BY | 分布表 |
| 逾期分析 | 逾期天数分段 × 读者类型 | 计算字段 | 表 |
| 预约统计 | 预约量/满足率/平均等待天数 | holds | 表 |

**技术方案：**
- 所有报表后端生成，不实时算——加 `report_cache` 表存快照
- 参数化：起止日期/读者类型/分类/资料类型可筛
- 输出：NDataTable + 导出 CSV 按钮
- 图表：ECharts（柱状图/折线图/饼图）——这是之前 DESIGN-TODO 的落地

**新增 API：**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/reports/circulation | 流通量报表（参数：日期范围/读者类型/资料类型） |
| GET | /api/reports/collection | 馆藏分析 |
| GET | /api/reports/utilization | 利用率排名 |
| GET | /api/reports/fines | 罚款统计 |
| GET | /api/reports/patrons | 读者活跃度 |
| GET | /api/reports/overdues | 逾期分析 |

**前端：**
- 重构 `/admin/stats` → `/admin/reports`，统一报表中心
- 左侧报表导航 + 右侧报表内容区
- 每个报表：顶部筛选栏 + ECharts 图表 + NDataTable + 导出按钮
- DESIGN-TODO: ECharts vs Chart.js？配色方案？表格和图表的排版比例？

---

### 14.4 实施路线图

| 版本 | 内容 | 数据库 | 前端 |
|------|------|--------|------|
| v0.4.0 | 预约系统 | Hold 表 | /admin/holds + /reader/holds + Books 页按钮改造 |
| v0.4.1 | 还书→预约联动 | 回改 borrows.ts | — |
| v0.4.2 | 通知系统（站内） | Notification + NotificationTemplate 表 | Bell 图标 + 通知页 |
| v0.4.3 | 通知系统（email） | nodemailer 集成 | 模板管理页 |
| v0.5.0 | 报表中心 | 新报表 API | /admin/reports 重构 + ECharts |

### 14.5 P2 远期模块（v0.6+）

| 模块 | 说明 |
|------|------|
| MARC 编目 | 引入 marc4js 解析/生成 MARC 记录，书目表加 biblio 字段存储原始 MARC |
| 采访模块 | 供应商表 + 采购订单 + 预算表 + 验收入库流程 |
| 罚款支付 | Fine 加 paymentMethod + 在线支付接口预留 |
| 全文检索 | Elasticsearch 替换 MySQL LIKE |
| 期刊管理 | Subscription 表 + ExpectedIssue + CheckIn |

---

## 十五、v0.4.0 详细设计：UI 深度升级（零新表）

### 15.1 图书详情页（/reader/books → 点击书名弹出）

**触发：** 读者 Books 表格中点击书名或封面，弹出详情抽屉/对话框。

**布局（从上到下）：**
```
┌─────────────────────────────────────────┐
│  [封面大图 180×240]                      │
│  算法导论（第3版）                       │
│  Thomas H. Cormen 著 | 机械工业出版社     │
│  出版年: 2013 | ISBN: 978-7-111-58444-5  │
│  分类: 计算机科学 | 索书号: TP312/1002    │
├─────────────────────────────────────────┤
│  [DESIGN-TODO: 封面图——豆瓣API/本地占位]  │
├─────────────────────────────────────────┤
│  复本状态                                │
│  ┌──────────┬──────┬──────┬──────────┐  │
│  │ 条码号     │ 馆藏地 │ 状态  │ 操作      │  │
│  │ LIB-...-1 │ A区3楼│ 在架  │ [借阅]    │  │
│  │ LIB-...-2 │ A区3楼│ 借出  │ [预约]    │  │
│  │ LIB-...-3 │ A区3楼│ 在架  │ [借阅]    │  │
│  └──────────┴──────┴──────┴──────────┘  │
├─────────────────────────────────────────┤
│  图书简介                                │
│  本书全面论述了算法在计算机科学各领域...    │
├─────────────────────────────────────────┤
│  [DESIGN-TODO: 相关推荐/读者评论/标签]     │
└─────────────────────────────────────────┘
```

**技术：** NDrawer 或 NModal（大尺寸） + NGrid 布局。不需要新 API——复用 `GET /api/books/:id/items`。
**封面图：** 优先豆瓣 API（`https://api.douban.com/v2/book/isbn/:isbn`），失败则用 OpenLibrary（`https://covers.openlibrary.org/b/isbn/:isbn-L.jpg`），都失败用 CSS 渐变色占位+书名文字。

**DESIGN-TODO 标记（浏览器可见）：**
- 封面区：占位图样式——渐变色+书名 / 纯色+图标 / 骨架屏？
- 复本状态表格：在架=绿色/借出=橙色/预约中=蓝色——颜色方案？
- 底部推荐区：要不要？放什么？

---

### 15.2 流通台（/admin/circulation — 新页面）

**触发：** 管理员侧边栏新增"流通台"入口。

**页面布局（收银模式）：**
```
┌──────────────────────────────────────────────────┐
│  [扫描框: ████████████████████████████ ] [⌨️键盘] │
│   输入读者条码或图书条码后自动识别                   │
├────────────────────┬─────────────────────────────┤
│  当前读者           │  操作队列                     │
│  ┌──────────────┐  │  #1 借 《算法导论》           │
│  │ 张三          │  │     条码 LIB-000001-1       │
│  │ 本科生        │  │     到期 2026-07-23         │
│  │ 已借 2/5      │  │                             │
│  │ 欠费 ¥0.00    │  │  #2 借 《深入理解》          │
│  │              │  │     条码 LIB-000002-2       │
│  │ [更换读者]    │  │     到期 2026-07-23         │
│  └──────────────┘  │                             │
│                    │  [确认全部] [清空队列]        │
├────────────────────┴─────────────────────────────┤
│  当前扫描的图书                                    │
│  《算法导论》 Thomas H. Cormen | ISBN 978-7-111  │
│  条码 LIB-000001-1 | 分类：计算机科学              │
│  [借书]  [还书]  [续借]                           │
└──────────────────────────────────────────────────┘
```

**操作逻辑：**
1. **扫描读者条码** → 左侧显示读者信息（已借数量/欠费/借阅上限）
2. **扫描图书条码** → 自动识别是借书还是还书：
   - 如果该书被当前读者借出 → 显示"还书"操作
   - 如果该书在架 → 显示"借书"操作
   - 加入操作队列（右侧）
3. **点击"确认全部"** → 批量提交 `$transaction`
4. **扫描另一个读者** → 自动切换（或点"更换读者"）

**偷懒实现（v0.4.0 最小可行）：**
- 不用真实扫码枪——用 `<input>` 模拟条码输入（扫码枪本质是键盘模拟器，扫完自动回车）
- 复用现有 `POST /api/borrows/borrow` 和 `POST /api/borrows/return` API
- 不建新表，不改后端
- `GET /api/book-items?barcode=xxx` 查条码对应的复本（需新增此接口）

**需要新增的后端接口（极小改动）：**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/book-items/:barcode | 按条码查复本（含 book 信息 + 当前借阅状态） |

**DESIGN-TODO 标记：**
- 扫码框：焦点样式？placeholder 文字？输入后动画反馈？
- 操作队列：卡片式还是紧凑列表？借/还颜色区分？
- 声音反馈：借书成功"嘀"一声，还书"叮"一声？用 Web Audio API？
- 读者信息卡：头像占位怎么处理？

---

### 15.3 修订后路线图

| 版本 | 内容 | 数据库 | 前端 | 后端 |
|------|------|--------|------|------|
| **v0.4.0** | UI 深度升级 | 零新表 | 图书详情页 + 流通台 + 1 个 `/book-items/:barcode` API | 5 行代码 |
| **v0.4.1** | 预约系统（Holds） | Hold 表 | /admin/holds + /reader/holds + 详情页预约按钮 | 6 API |
| v0.4.2 | 还书→预约联动 | — | — | 改 borrows.ts return |
| v0.4.3 | 通知系统（站内） | Notification + Template 表 | Bell 图标 + 通知页 | 5 API |
| v0.5.0 | 报表中心 | 报表 API 重构 | /admin/reports + ECharts | 6 API |

---

## 十六、山东科技大学联动方案

### 16.1 校情背景
- 山东科技大学（SDUST）有三个校区：青岛主校区、泰安校区、济南校区
- 图书馆大概率使用**汇文 Libsys**（国内大学图书馆占有率最高）
- 校内系统通过校园网访问（lib.sdust.edu.cn 外网 403）

### 16.2 联动路径（渐进式）

| 阶段 | 联动内容 | 需要校方配合 | 技术方案 |
|------|---------|-------------|---------|
| **一期（无需配合）** | 借阅规则对齐山科大真实规则 | 否 | 在 Settings 页面手工录入：本科生 5册/30天、研究生 10册/60天、教师 20册/180天 |
| | 馆藏地使用山科大真实名称 | 否 | seed 填：青岛馆A区/B区、泰安馆、济南馆 |
| | 读者用学号注册 | 否 | username=学号，读者自注册 |
| **二期（需配合）** | CAS 统一认证登录 | 是 | 接 `https://cas.sdust.edu.cn/` OAuth，用学号+统一密码登录 |
| | 一卡通作为读者条码 | 否* | 校园卡号作为 User.barcode，流通台扫码即识别 |
| | 书目数据导入 | 是 | 从 Libsys 导出 MARC/CSV，批量导入我们的 Book 表 |
| **三期（深度对接）** | SIP2 流通协议 | 是 | 与 Libsys 的 SIP2 端口对接，双向同步借还记录 |
| | Z39.50 联邦检索 | 是 | 可通过山科大馆检索其他高校图书馆书目 |

### 16.3 不依赖校方的自闭环方案
即使校方零配合，系统也能独立运行——用学号注册、手工录入规则和馆藏地、自行录入书目数据。一期联动即可让系统在山科大校园网环境内正常使用。

---

## 十七、v0.4.0 资源清单

| 资源 | 用途 | 获取方式 |
|------|------|---------|
| 豆瓣图书 API | 封面图 | `https://api.douban.com/v2/book/isbn/:isbn`（免费，需设置 Referer） |
| OpenLibrary API | 封面图备用 | `https://covers.openlibrary.org/b/isbn/:isbn-L.jpg`（免费，无认证） |
| ECharts | 后续报表图表 | npm install echarts |
| Naive UI | 全部 UI 组件 | 已安装 |
| JsBarcode | 条码生成（可选） | npm install jsbarcode |

---

## 十八、版本历史（续）

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-06-24 | 0.3.1 | 联网修复 + 迭代蓝图 + UI 设计文档 + SDUST 联动方案 |

