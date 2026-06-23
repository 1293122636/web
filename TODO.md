# Library Full-Stack System — 未完成事项与优化清单

## 一、DESIGN-TODO（UI 设计待定 — 浏览器可见 NTag 标记）

| # | 位置 | 决策点 |
|---|------|--------|
| 1 | Dashboard | 借阅动态——列表 / 时序图 / 卡片？ |
| 2 | Stats | 热门书柱状图 + 月度折线图——用 ECharts / Chart.js / Naive UI 图？ |
| 3 | admin/Books 展开面板 | 复本展示方式——NCard 小卡片 / 内嵌 NDataTable / NList？条码号用等宽字体？ |
| 4 | admin/Books 加复本 Modal | 批量条码——每行一个 input / +号动态加行 / Textarea 换行分隔？ |
| 5 | admin/Borrows 还书确认 | 逾期弹窗——显示逾期天数+金额后再确认 / 直接还书后提示？金额颜色强调？ |
| 6 | reader/MyBorrows | 欠费总额——顶部统计卡片 / 列表行内 / 独立 Badge？ |
| 7 | admin/Fines | 全页面新设计——筛选放顶部还是侧边？列表+分页 / 卡片？ |
| 8 | admin/Settings | 规则矩阵编辑器——表格内 NSelect 交叉编辑 / 独立编辑表单？读者类型×资料类型如何可视化？ |

## 二、功能缺失

| # | 功能 | 说明 | 优先级 |
|---|------|------|--------|
| 1 | 预约系统 | 已借出图书可排队预约，到期提醒取书 | P1 |
| 2 | 操作审计日志 | 记录谁在何时做了何种操作（AuditLog 表） | P1 |
| 3 | CSV/PDF 导出 | 报表/图书列表/罚款导出 | P2 |
| 4 | 批量导入 | MARC 记录 / CSV 批量编目 | P2 |
| 5 | 通知系统 | 逾期提醒 / 预约到馆通知 / 账单通知（Email/SMS） | P2 |
| 6 | 图书封面 | 封面图片上传/URL（cover 字段已预留） | P3 |
| 7 | 多馆藏地 | 分校区/分馆藏地管理 + 转库操作 | P3 |
| 8 | 期刊管理 | 连续性出版物：订阅/预计到刊/签到/催缺 | P3 |
| 9 | 读者自助 | 密码重置、在线缴费 | P3 |
| 10 | 国际化 | 英文界面支持 | P3 |

## 三、性能优化

| # | 位置 | 现状 | 方案 |
|---|------|------|------|
| 1 | stats/monthly | JS 内存分组聚合全量记录 | 改用 `GROUP BY DATE_FORMAT(borrow_date, '%Y-%m')` |
| 2 | Naive UI bundle | 主 chunk 1.36MB 未 tree-shaking | 按需引入或 CDN 分离 |
| 3 | 缓存 | 仅文档写了策略，未实现 | 对 GET /categories 加内存 Map 5min TTL |
| 4 | 列表接口 | borrows 全量返回无分页 | 加 page/limit 参数 |
| 5 | Book 列表 | 每次 include category + _count items | 对分类不变的数据可加缓存 |

## 四、安全加固

| # | 位置 | 现状 | 方案 |
|---|------|------|------|
| 1 | CORS | `origin: true` 允许所有来源 | 生产改为白名单 |
| 2 | 速率限制 | 无 | 加 `@fastify/rate-limit` |
| 3 | JWT | 无刷新 token，过期即断 | 加 refresh token 轮换机制 |
| 4 | Helmet | 未安装 | 加 `@fastify/helmet` 安全头 |
| 5 | 前端权限 | 按钮 show/hide 纯前端控制 | 后端已做权限校验，前端加固 |

## 五、工程化

| # | 位置 | 现状 | 方案 |
|---|------|------|------|
| 1 | 自动化测试 | 零测试 | 加 vitest 单元测试 + supertest 集成测试 |
| 2 | CI/CD | 无流水线 | GitHub Actions：lint → test → build |
| 3 | 数据库迁移 | 只用 db push | Schema 稳定后切 migrate dev |
| 4 | 前后端类型共享 | 各自定义 | 抽 shared/types.ts |
| 5 | 错误处理统一 | AGENTS.md 写了，路由未全改 | 所有路由改用 throw + setErrorHandler |
| 6 | 前端错误粒度 | 全部 message.error(e.message) | 字段级错误绑定到 NForm |
| 7 | 日志结构化 | Fastify 用 pino 但无自定义字段 | 加 requestId 追踪 |
| 8 | BorrowRecord.status | String（无枚举约束） | 改为 enum BorrowStatus |
| 9 | .env.example | AGENTS.md 要求但未创建文件 | 创建 backend/.env.example |

## 六、数据完整性

| # | 位置 | 问题 | 方案 |
|---|------|------|------|
| 1 | Book.total/available | 冗余计数器，可能因异常与 BookItem 不一致 | 周期性校验：`COUNT(items WHERE status='available')` |
| 2 | 删除图书 | 有活跃借阅时外键报错，无前置友好提示 | 加前置检查返回明确错误 |
| 3 | 还书 status | borrowed→returned 后若实际上已逾期但 isOverdue 计算在事务外 | 还书 query 中直接 MySQL 计算 |

## 七、种子数据增强

| # | 内容 | 说明 |
|---|------|------|
| 1 | 多种读者类型 | 本科生(5册30天) / 研究生(10册60天) / 教师(20册180天) |
| 2 | 多种资料类型 | 普通图书 / 新书速递(7天) / 工具书(不外借) |
| 3 | 对应规则矩阵 | 3×3=9 条 CirculationRule |
| 4 | 示例借阅+罚款数据 | 至少 1 条逾期记录用于测试 |

## 八、Git 管理

| 项目 | 状态 |
|------|------|
| WSL 开发仓库 (`~/workplace/library-full-stack/`) | ✅ `git init` + initial commit (41 files, `main` 分支) |
| Windows 交付仓库 (`D:\workplace\...`) | ✅ `git init` + docs commit |
| .gitignore | ✅ 排除 node_modules/dist/.env/*.log |
| 分支规范 | `feature/YYYYMMDD-description` / `fix/YYYYMMDD-description` |

```
提交规则：
1. feature 分支开发，不要直接在 main 上改
2. commit message 格式：<type>: <description>
   type: feat / fix / refactor / docs / chore
3. PR 合并后删除 feature 分支
4. tag 格式：v0.X.Y
```

---

> 此文件按优先级排序：P1 = 下个迭代必做，P2 = 近期，P3 = 远期。每完成一项从此清单删除。建议每次迭代后更新。
