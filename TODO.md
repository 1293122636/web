# Library Full-Stack System — TODO

> 2026-06-24 · 13 轮审计完成 · 82 fixes · 零待办

## 已完成全部

| 类别 | 内容 | 状态 |
|------|------|------|
| Phase 1 (A-G) | 四层架构 + 分面搜索 + 流通台 + 集成 | ✅ |
| Phase 2 (H-L) | ESLint/安全/CI/索引/错误处理/types | ✅ |
| M1-M3 | BarcodeLabel + Hold 预约 + 组件测试 | ✅ |
| R1-R13 | 十三轮全代码审计 (82 fixes) | ✅ |

## 当前度量

| 指标 | 值 |
|------|-----|
| API 端点 | 40 |
| 后端服务测试 | 52/52 PASS (10 files) |
| 前端测试 | 18/18 PASS (4 files) |
| ESLint | 0 errors, 95 warnings |
| TSC errors | 10 (test mock + Prisma 类型桥接) |

## 不做

| 项 | 原因 |
|----|------|
| E2E (Playwright) | 学生项目过重 |
| JWT refresh | 过期重登即可 |
| Redis | 单机足够 |
| DB CHECK 约束 | Prisma 不支持，需 raw SQL |
