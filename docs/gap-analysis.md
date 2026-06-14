# 差距分析：当前代码库 vs 目标业务逻辑

> **依据文档：** [`docs/workflow-spec.md`](./workflow-spec.md)  
> **分析范围：** `backend/`、`frontend/`、`db/`（只读分析，不含代码变更）  
> **分析日期：** 2026-06-07

---

## 0. 执行摘要

当前项目是一个**已可运行的 MVP 级活动报名系统**（用户报名 → 管理员单级审核），与 `workflow-spec.md` 定义的**多级作品报名 · 四级资格审核 · 三级打分 · 辖区隔离**目标之间存在**架构级差距**。

| 维度 | 目标完成度（估算） | 说明 |
|------|-------------------|------|
| 后端核心 Service | **~25%** | 仅 `AuthService`、`ActivityService` 有部分能力；其余 7 项目标模块缺失或仅有弱等价物 |
| 前端业务页面 | **~30%** | 有活动列表/详情、我的报名、单级管理；无作品、打分、公示、角色菜单 |
| 数据库 | **~20%** | 3 张表 vs 目标 7+ 张；无 `work` 及审核/打分/公示表 |
| 权限与辖区 | **~10%** | 二元 `USER/ADMIN`，无 scope、无 8 角色 |

**结论：** 不宜在现有 `activity_registration` 流程上「打补丁」完成目标，建议按 `workflow-spec.md` §16 纵向切片重构，并明确 `RegistrationService` 与 `WorkService` 的迁移/废弃策略。

---

## 1. 当前后端已有模块

### 1.1 总览

| 目标模块 | 是否存在 | 完整度 | 实际对应 / 缺失 |
|----------|:--------:|:------:|-----------------|
| AuthService | ✓ 部分 | **35%** | `AuthService` + `AuthServiceImpl` |
| ActivityService | ✓ 部分 | **55%** | `ActivityService` + `ActivityServiceImpl` + `ActivityQuotaService` |
| WorkService | ✗ | **0%** | 无；弱相关：`RegistrationService.register` |
| UploadService | ✗ | **0%** | 无 |
| RegistrationReviewService | ✗ | **0%** | 无；弱相关：`RegistrationService.audit`（单级） |
| ScoreService | ✗ | **0%** | 无 |
| PublicNoticeService | ✗ | **0%** | 无 |
| RoleGuard | ✗ | **0%** | 无；弱相关：`@RequireAdmin` + `AuthInterceptor` |
| ScopeNameMatcher | ✗ | **0%** | 无 |
| AuthInterceptor | ✓ | **50%** | `AuthInterceptor` + `JwtTokenProvider` + `UserContext` |

---

### 1.2 AuthService

**文件：**

- `backend/src/main/java/com/example/activity/service/AuthService.java`
- `backend/src/main/java/com/example/activity/service/impl/AuthServiceImpl.java`
- `backend/src/main/java/com/example/activity/controller/AuthController.java`
- `backend/src/main/java/com/example/activity/common/auth/JwtTokenProvider.java`

**已有逻辑：**

| 能力 | 状态 |
|------|------|
| 用户名密码登录 | ✓ |
| BCrypt 校验 | ✓ |
| 禁用账号拦截（`status != 1`） | ✓ |
| JWT 签发（`sub`, `username`, `role`） | ✓ |
| 统一错误提示（用户名/密码错误） | ✓ |

**缺失逻辑（相对 workflow-spec §11）：**

| 能力 | 目标 | 现状 |
|------|------|------|
| 用户注册 | J1 | ✗ 无 `register` API（`WebMvcConfig` 已 exclude `/api/auth/register` 但 Controller 未实现） |
| 8 种角色 | J4 | ✗ 仅 `USER` / `ADMIN`（`UserRole.java`） |
| JWT 携带 scope | J4 | ✗ `LoginUser` 无省/市/区/校字段 |
| 管理员创建子账号 | J8 | ✗ |
| `/api/auth/me` | 规格/requirement | ✗ |
| 登出 / Token 黑名单 | 可选 | ✗ |

---

### 1.3 ActivityService

**文件：**

- `backend/src/main/java/com/example/activity/service/ActivityService.java`
- `backend/src/main/java/com/example/activity/service/impl/ActivityServiceImpl.java`
- `backend/src/main/java/com/example/activity/service/ActivityQuotaService.java`
- `backend/src/main/java/com/example/activity/controller/ActivityController.java`
- `backend/src/main/java/com/example/activity/controller/admin/AdminActivityController.java`

**已有逻辑：**

| 能力 | 状态 |
|------|------|
| 用户端已上架活动分页/详情 | ✓ |
| 管理端活动 CRUD | ✓ |
| 上下架状态机（DRAFT/PUBLISHED/OFFLINE） | ✓ |
| 报名时间/活动时间校验 | ✓ |
| `canRegister` / `registerDisabledReason` 综合判断 | ✓ |
| 名额并发控制（`ActivityQuotaService` 行锁 + 条件 UPDATE） | ✓ |

**缺失逻辑（相对 workflow-spec §5）：**

| 能力 | 目标 | 现状 |
|------|------|------|
| 活动 scope（省/市/区/校） | A6 | ✗ `Activity` 实体无 scope 字段 |
| 列表辖区隔离 | A6 | ✗ 无 `ScopeNameMatcher` 过滤 |
| 多级管理员创建活动 | A1 | ✗ 任意 `ADMIN` 即可 |
| 与 Work 提交联动 | A7 | ✗ 名额绑定 `RegistrationService.register` |
| 活动详情管理端统计（作品/审核） | 扩展 | ✗ 仅有报名统计在 `RegistrationAdminListVO` |

---

### 1.4 WorkService

**状态：不存在（0%）**

**弱等价物：** `RegistrationService` + `RegistrationServiceImpl`

| 目标（workflow-spec §6） | 现有 RegistrationService |
|--------------------------|---------------------------|
| 作品实体 `work` + `current_step/status` | ✗ 使用 `activity_registration.status` 四态 |
| 草稿 / 提交 / 退回再提交 | ✗ 直接 `PENDING`，无 DRAFT/REVISION |
| 初始 step = SCHOOL | ✗ |
| 作品标题、说明等业务字段 | ✗ 仅 `applyRemark` |
| 与 Upload 联动 | ✗ |

**相关文件（非 WorkService）：**

- `RegistrationService.java` / `RegistrationServiceImpl.java`
- `RegistrationController.java`
- `entity/ActivityRegistration.java`

---

### 1.5 UploadService

**状态：不存在（0%）**

**缺失：**

- 文件上传 Controller / Service
- `work_file` / `work_attachment` 表与实体
- 文件类型/大小校验、存储策略（本地/OSS）
- 与作品状态（仅 DRAFT/REVISION 可传）联动

---

### 1.6 RegistrationReviewService

**状态：不存在（0%）**

**弱等价物：** `RegistrationService.audit`

| 目标（workflow-spec §7） | 现有 audit |
|--------------------------|------------|
| 四级 step（SCHOOL→…→PROVINCE） | ✗ 单级 |
| 通过 / 退回 / 淘汰 | ✗ 仅 APPROVE / REJECT |
| 按角色 + step 鉴权 | ✗ 任意 ADMIN |
| 推进 `current_step` | ✗ |
| `review_record` 审计 | ✗ 仅更新 `audited_by/audit_time/audit_remark` |
| scope 隔离 | ✗ |

**相关文件：**

- `AdminRegistrationController.java`
- `RegistrationAuditRequest.java`（action: APPROVE/REJECT）

---

### 1.7 ScoreService

**状态：不存在（0%）**

**缺失全部：**

- `SCORE_DISTRICT` / `SCORE_CITY` / `SCORE_PROVINCE` 流程
- 评委角色（`*_REVIEWER`）
- 分数录入、汇总、晋级/淘汰
- `work_score_record` 表

---

### 1.8 PublicNoticeService

**状态：不存在（0%）**

**缺失全部：**

- 公示 CRUD、发布/撤回
- 公开 API `/api/public/notices`
- `public_notice` 表
- 脱敏与发布窗口

---

### 1.9 RoleGuard

**状态：不存在（0%）**

**弱等价物：**

| 组件 | 文件 | 能力 |
|------|------|------|
| `@RequireAdmin` | `common/auth/RequireAdmin.java` | 标记管理端 Controller |
| `AuthInterceptor` | `common/auth/AuthInterceptor.java` | JWT 解析 + `role == ADMIN` 校验 |

**缺失：**

- 8 角色细粒度守卫
- step 与角色匹配（如 SCHOOL_ADMIN 只能审 SCHOOL）
- 评委专用守卫
- 方法级 `@RequireRoles(...)` / `@RequireStep(...)`

---

### 1.10 ScopeNameMatcher

**状态：不存在（0%）**

**缺失：**

- 领域类 `ScopeNameMatcher`
- `UserScope` / `ScopedEntity` 模型
- `sys_user` / `activity` / `work` 上的 scope 字段
- 列表查询 Filter 构建
- 上级可见下级的包含规则

---

### 1.11 AuthInterceptor

**文件：**

- `backend/src/main/java/com/example/activity/common/auth/AuthInterceptor.java`
- `backend/src/main/java/com/example/activity/common/config/WebMvcConfig.java`

**已有逻辑：**

| 能力 | 状态 |
|------|------|
| `/api/**` 拦截（除 login/register） | ✓ |
| Bearer JWT 解析 | ✓ |
| `UserContext` 线程绑定 | ✓ |
| `@RequireAdmin` 二元鉴权 | ✓ |

**缺失逻辑：**

| 能力 | 目标 | 现状 |
|------|------|------|
| 解析 JWT scope | §11 | ✗ |
| 按路由/注解校验 8 角色 | §3 | ✗ |
| 公开公示路径白名单 | §9 | ✗ |
| 资源级 scope 校验 | §4 | ✗（应在 Service 层配合 Matcher） |

---

### 1.12 后端其他已实现（非目标清单但可复用）

| 模块 | 文件 | 复用价值 |
|------|------|----------|
| 统一响应 | `common/result/Result.java` | 高 |
| 全局异常 | `GlobalExceptionHandler.java`, `ErrorCode.java` | 高 |
| 分页 | `PageResult.java` | 高 |
| 转换器 | `ActivityConverter`, `RegistrationConverter` | 中（需扩展 Work） |
| 单元测试 | `ActivityServiceImplTest`, `RegistrationServiceImplTest`, `ActivityQuotaServiceTest` | 中（规则不同需重写） |

---

## 2. 当前前端已有模块

### 2.1 总览

| 目标模块 | 是否存在 | 完整度 | 实际对应 |
|----------|:--------:|:------:|----------|
| menus.ts | ✗ | **0%** | 菜单硬编码在 Layout 组件内 |
| 路由守卫 | ✓ 部分 | **40%** | `router/index.ts` `beforeEach` |
| 教师首页 | ✓ 部分 | **50%** | `views/Home.vue`（营销页，非教师工作台） |
| 作品报名页面 | ✗ | **0%** | 弱相关：`ActivityDetail.vue` 弹窗报名 |
| 报名信息管理页面 | ✓ 部分 | **35%** | `MyRegistration.vue`（我的报名，非作品） |
| 打分页面 | ✗ | **0%** | — |
| 公示页面 | ✗ | **0%** | — |
| 省级活动管理页面 | ✓ 部分 | **30%** | `admin/ActivityManage.vue`（通用 ADMIN，无省级/scope） |

---

### 2.2 menus.ts

**状态：不存在**

**现状：**

- 用户端：`PortalNavbar.vue` 内联 `navItems`（首页、活动列表、我的报名）
- 管理端：`AdminLayout.vue` 内联 `el-menu`（仅「活动管理」一项）
- 无按角色动态菜单、无审核/打分/公示入口

**目标差距：** 需 `menus.ts`（或 `composables/useMenus.ts`）+ 8 角色菜单矩阵（workflow-spec §3.2）

---

### 2.3 路由守卫

**文件：** `frontend/src/router/index.ts`

**已有：**

| 规则 | 状态 |
|------|------|
| 未登录 → `/login?redirect=` | ✓ |
| 已登录访问 `/login` → 按角色跳转 | ✓ |
| `requiresAdmin` → 非 ADMIN 重定向 | ✓ |
| 已登录访问 `/` → `/activities` | ✓ |

**缺失：**

| 规则 | 目标 |
|------|------|
| 8 角色路由可见性 | ✗ |
| TEACHER 不可进管理端细分路由 | 部分（仅 ADMIN 整体拦截） |
| REVIEWER 专用打分路由 | ✗ |
| 公开公示路由 `meta.public` | ✗ |
| scope 不足时的 403 页 | ✗ |

---

### 2.4 教师首页

**文件：** `frontend/src/views/Home.vue`（`/` 公开营销首页）

**已有：**

- Hero、功能介绍、流程说明、门户风 UI
- 「立即报名」「查看活动」跳转

**与目标差距：**

| 目标 | 现状 |
|------|------|
| 登录后教师工作台（待办、我的作品摘要） | ✗ 已登录直接进 `/activities` |
| 按 `WorkStatus` 展示进度 | ✗ |
| 辖区/角色差异化首页 | ✗ |

**备注：** `views/home/HomeLanding.vue` 为遗留文件，路由已不引用。

---

### 2.5 作品报名页面

**状态：不存在（0%）**

**弱等价物：**

- `ActivityDetail.vue`：活动详情 + 「立即报名」对话框（仅 remark）
- `ActivityList.vue`：列表内也可打开报名对话框

**缺失：**

- 独立 `/works/create`、`/works/:id/edit` 路由
- 作品表单（标题、说明等）
- 附件上传 UI
- 草稿保存 / 提交 / 退回后再提交
- `current_step/status` 展示（`WorkStatus` 文案）

---

### 2.6 报名信息管理页面

**文件：** `frontend/src/views/registration/MyRegistration.vue`

**已有：**

- 我的报名列表、状态筛选、分页
- 取消报名、跳转活动详情
- `useRegistrationStatus` 四态文案

**缺失（相对目标「作品/报名信息管理」）：**

| 目标 | 现状 |
|------|------|
| 作品维度（非 registration 维度） | ✗ |
| 审核退回意见展示 | ✗ |
| 附件列表 | ✗ |
| `statusLabel`（workflow-spec §10） | ✗ |
| 多级 step 进度条 | ✗ |

**管理端弱等价：** `admin/RegistrationAudit.vue` — 单级审核，非四级 `RegistrationReviewService`

---

### 2.7 打分页面

**状态：不存在（0%）**

**缺失：** 评委任务列表、打分表单、晋级操作、分数汇总展示、对应 `api/admin/scores` 客户端

---

### 2.8 公示页面

**状态：不存在（0%）**

**缺失：** 公示列表/详情（公开或登录）、管理端公示编辑发布页、`api/public/notices`

---

### 2.9 省级活动管理页面

**文件：** `frontend/src/views/admin/ActivityManage.vue`

**已有：**

- 活动列表（全状态）、创建/编辑/上下架
- 跳转报名名单审核

**缺失：**

| 目标 | 现状 |
|------|------|
| 省级 scope 过滤 | ✗ |
| 省/市/区/校分级管理视图 | ✗ |
| 与作品/审核/打分统计联动 | ✗ |
| 角色非 ADMIN 的分级管理端 | ✗ |

---

### 2.10 前端 API / 类型层

| 文件 | 覆盖 | 目标缺口 |
|------|------|----------|
| `api/auth.ts` | login | register, me, scope |
| `api/activity.ts` | 用户端活动 | scope 参数 |
| `api/registration.ts` | 报名 CRUD | 应演进为 `api/work.ts` |
| `api/admin/activity.ts` | 管理活动 | 分级 scope |
| `api/admin/registration.ts` | 审核 | 多级 review/score/notice |
| `stores/user.ts` | USER/ADMIN | 8 角色 + scope |
| `types/*.ts` | 报名/活动类型 | WorkStep, WorkStatus, FinalResult |

---

## 3. 当前数据库表情况

**脚本：** `db/init.sql`（`db/dev-seed.sql` 为开发种子）

| 目标表 | 是否存在 | 实际表名 / 说明 |
|--------|:--------:|-----------------|
| `activity` | ✓ | `activity` — 无 scope 字段 |
| `work` | ✗ | — |
| `work_file` | ✗ | — |
| `work_revision_feedback` | ✗ | — |
| `review_record` | ✗ | — |
| `public_notice` | ✗ | — |
| `sys_user` / `user` | ✓ | `sys_user` — 无 scope 字段，role 仅 USER/ADMIN |

**额外存在（目标规格未列但已实现）：**

| 表 | 说明 | 与目标关系 |
|----|------|------------|
| `activity_registration` | 用户-活动报名 | 目标 `work` 的**前置 MVP 实现**，字段语义不一致 |

**实体映射：**

- `entity/SysUser.java` → `sys_user`
- `entity/Activity.java` → `activity`
- `entity/ActivityRegistration.java` → `activity_registration`

---

## 4. 和目标逻辑的差距清单

### 4.1 按 workflow-spec 章节

| ID | 目标能力 | 当前状态 | 差距级别 |
|----|----------|----------|----------|
| G1 | 双主线 step 状态机（work） | 无 work 表/Service | **阻断** |
| G2 | 四级资格审核 | 单级 audit | **阻断** |
| G3 | 三级打分 | 无 | **阻断** |
| G4 | 8 角色 + 菜单 | USER/ADMIN | **阻断** |
| G5 | ScopeNameMatcher 辖区隔离 | 无 | **阻断** |
| G6 | 作品上传 | 无 | **高** |
| G7 | 公示 | 无 | **高** |
| G8 | WorkStatus 教师文案 | registration 四态 | **中** |
| G9 | Auth 注册 + scope JWT | 仅 login | **高** |
| G10 | Activity scope + 分级发布 | 通用 activity | **高** |
| G11 | 名额与 Work 生命周期一致 | 报名时预占 quota | **高**（与 spec Q2 待决） |
| G12 | 前端作品/审核/打分/公示页 | MVP 报名页 | **阻断** |
| G13 | 审核/打分审计表 | 仅 registration 字段 | **高** |

### 4.2 与 workflow-spec §15 冲突点对照

文档 [`workflow-spec.md`](./workflow-spec.md) §15 已列 C1–C18；本分析**确认均仍未解决**，其中 **C1–C7、C12–C13、C16–C17** 为实施前必须决策的阻断项。

### 4.3 可保留复用 vs 需替换

| 可保留/扩展 | 需替换或废弃 |
|-------------|--------------|
| `ActivityService` 时间/状态校验 | `activity_registration` → `work` 领域 |
| `ActivityQuotaService` 并发思路 | `RegistrationService` 整体语义 |
| JWT + Interceptor 骨架 | `@RequireAdmin` 二元鉴权 |
| 前端活动列表/详情 UI 骨架 | 「弹窗报名」→ 作品编辑流 |
| `Result` / `PageResult` / 异常体系 | `useRegistrationStatus` → `WorkStatus` |
| 管理端 `ActivityManage` 表单 | 硬编码菜单 → `menus.ts` |

---

## 5. 推荐实现顺序

按 **纵向切片 + 依赖自底向上**（对齐 `planning-and-task-breakdown` 与 `workflow-spec.md` §16）。

### Phase 0：决策与契约（1–2 天）

- [ ] 确认 workflow-spec §14 开放问题（Q1–Q12）
- [ ] 明确 `activity_registration` 迁移/废弃策略
- [ ] 更新 `database-design.md`、`api-contract.md`

**Checkpoint：** 表结构 DDL 评审通过，无阻断 open question。

---

### Phase 1：身份、角色、辖区（Foundation）

| 任务 | 产出 | 依赖 |
|------|------|------|
| P1-1 | `sys_user` 增加 scope 字段 + 8 角色枚举 | Phase 0 |
| P1-2 | `AuthService` 扩展 register/me；JWT 带 scope | P1-1 |
| P1-3 | `ScopeNameMatcher` + 单元测试 | P1-1 |
| P1-4 | `RoleGuard` / 扩展 `AuthInterceptor` | P1-2 |
| P1-5 | 前端 `user` store、`menus.ts`、路由 meta 角色 | P1-2 |

**Checkpoint：** 不同角色登录看到不同菜单；列表接口带 scope 过滤（可先 mock 空列表）。

---

### Phase 2：活动 + 作品提交（Core Path 1）

| 任务 | 产出 | 依赖 |
|------|------|------|
| P2-1 | `activity` scope 字段 + `ActivityService` 辖区过滤 | Phase 1 |
| P2-2 | `work` + `work_file` 表与实体 | Phase 0 |
| P2-3 | `WorkService` 草稿/提交/我的作品 | P2-2 |
| P2-4 | `UploadService` + 存储配置 | P2-2 |
| P2-5 | 前端作品报名页、我的作品页 | P2-3, P2-4 |

**Checkpoint：** 教师可创建作品、上传、提交；`current_step=SCHOOL`, `status=SUBMITTED`。

---

### Phase 3：四级资格审核（Core Path 2）

| 任务 | 产出 | 依赖 |
|------|------|------|
| P3-1 | `review_record` / `work_revision_feedback` 表 | Phase 0 |
| P3-2 | `RegistrationReviewService` + 校/区/市/省 API | Phase 2, Phase 1 |
| P3-3 | 管理端分级审核页（按 step） | P3-2 |
| P3-4 | `WorkStatus` 文案解析（前后端） | P3-2 |

**Checkpoint：** 作品可从 SCHOOL 推进到 PROVINCE，支持退回与淘汰。

---

### Phase 4：逐级打分（Core Path 3）

| 任务 | 产出 | 依赖 |
|------|------|------|
| P4-1 | `work_score_record` 表 | Phase 0 |
| P4-2 | `ScoreService` + 评委 API | Phase 3, Phase 1 |
| P4-3 | 前端打分页（REVIEWER 角色） | P4-2 |

**Checkpoint：** 省审通过后进入打分主线直至 `COMPLETED` + `final_result`。

---

### Phase 5：公示与收尾

| 任务 | 产出 | 依赖 |
|------|------|------|
| P5-1 | `public_notice` + `PublicNoticeService` | Phase 1 |
| P5-2 | 公示公开页 + 管理发布页 | P5-1 |
| P5-3 | 废弃/迁移 `RegistrationService` 与前端 my-registrations | Phase 2–4 |
| P5-4 | E2E：完整时间线（workflow-spec §13） | 全部 |

**Checkpoint：** T0–T11 时间线可演示；旧 MVP 路径下线或只读兼容。

---

### 并行化建议

| 可并行 | 必须串行 |
|--------|----------|
| P1-5 前端菜单 vs P1-3 Matcher 测试 | Phase 1 → Phase 2 |
| P5-2 公示 UI vs P4-2 Score API（契约先定） | Work 表 → Review → Score |
| 文档/API 契约 vs Phase 1 开发 | scope 字段 → 所有列表 API |

---

## 6. 输出文件清单

### 6.1 后端待新增（主要）

```
backend/src/main/java/com/example/activity/
├── common/
│   ├── auth/RequireRoles.java          # RoleGuard
│   └── scope/ScopeNameMatcher.java
├── common/enums/
│   ├── WorkStep.java
│   ├── WorkStatus.java
│   └── FinalResult.java
├── entity/
│   ├── Work.java
│   ├── WorkFile.java
│   ├── WorkRevisionFeedback.java
│   ├── WorkReviewRecord.java
│   ├── WorkScoreRecord.java
│   └── PublicNotice.java
├── service/
│   ├── WorkService.java
│   ├── UploadService.java
│   ├── RegistrationReviewService.java
│   ├── ScoreService.java
│   └── PublicNoticeService.java
├── service/impl/                       # 对应实现
├── controller/                         # works, reviews, scores, notices
└── mapper/                             # 新表 Mapper
```

### 6.2 后端待修改（主要）

```
AuthService.java / AuthServiceImpl.java
AuthController.java
JwtTokenProvider.java / LoginUser.java
UserRole.java
Activity.java / ActivityServiceImpl.java
AuthInterceptor.java
WebMvcConfig.java
```

### 6.3 后端待废弃或迁移（决策后）

```
RegistrationService.java / RegistrationServiceImpl.java   → 迁移至 Work + Review
ActivityRegistration.java / activity_registration 表       → 数据迁移后废弃
AdminRegistrationController.java                          → 拆为 Review/Score
```

### 6.4 前端待新增（主要）

```
frontend/src/
├── constants/menus.ts                    # 或 config/menus.ts
├── composables/useWorkStatus.ts
├── api/work.ts
├── api/upload.ts
├── api/admin/review.ts
├── api/admin/score.ts
├── api/public/notice.ts
├── types/work.ts
├── views/work/WorkEdit.vue
├── views/work/MyWorks.vue
├── views/admin/ReviewManage.vue
├── views/admin/ScoreManage.vue
├── views/public/NoticeList.vue
└── views/public/NoticeDetail.vue
```

### 6.5 前端待修改（主要）

```
router/index.ts
stores/user.ts
layouts/AdminLayout.vue
components/portal/PortalNavbar.vue
views/activity/ActivityDetail.vue         # 改为进入作品流
views/admin/ActivityManage.vue            # scope + 统计扩展
```

### 6.6 数据库待新增脚本

```
db/migrations/
├── V2__user_scope_and_roles.sql
├── V3__work_and_files.sql
├── V4__review_and_feedback.sql
├── V5__score_record.sql
├── V6__public_notice.sql
└── V7__migrate_registration_to_work.sql  # 可选
```

### 6.7 文档待同步

```
docs/database-design.md
docs/api-contract.md
docs/requirement.md                       # MVP 范围修订
docs/workflow-spec.md                     # 关闭 §14 问题后更新
```

---

## 7. 风险点

| # | 风险 | 影响 | 缓解 |
|---|------|------|------|
| R1 | **领域模型切换**（registration → work）导致前后端大面积重写 | 高 | Phase 0 明确迁移策略；保留兼容层或只读旧数据 |
| R2 | **名额语义冲突**（现：报名即占坑 vs spec：可能提交/校审占坑） | 高 | 先定 Q2；统一 `ActivityQuotaService` 调用点 |
| R3 | **8 角色 + scope 组合爆炸** | 中 | Matcher + RoleGuard 单测矩阵；集成测试覆盖典型角色 |
| R4 | **JWT 携带 scope 变更不生效** | 中 | scope 变更强制重新登录；或 me 接口刷新 |
| R5 | **现有 Registration 测试失效** | 中 | Phase 3 后重写测试对齐 Work 状态机 |
| R6 | **前端无 menus.ts 导致多角色菜单散落** | 中 | Phase 1 先立菜单配置，禁止 Layout 硬编码 |
| R7 | **文件上传安全**（类型、大小、路径遍历） | 高 | UploadService 边界校验 + 隔离存储目录 |
| R8 | **打分汇总/晋级规则未定**（Q3/Q4） | 中 | Phase 4 前锁定算法；活动级配置 |
| R9 | **双 `/` 路由结构**（Home vs BasicLayout）增加守卫复杂度 | 低 | 路由 refactor 时一并整理 |
| R10 | **文档与代码长期分叉** | 中 | 每 Phase checkpoint 回写 workflow-spec / api-contract |

---

## 8. 质量审查摘要（code-review-and-quality 五轴）

| 轴 | 对「当前 MVP」 | 对「目标规格」 |
|----|----------------|----------------|
| Correctness | 报名/审核/名额并发逻辑较完整 | 未覆盖多级流程，**不满足** workflow-spec |
| Architecture | 清晰三层，但单一 Registration 域 | 需引入 Work 域与 Matcher，**需重构** |
| Security | JWT + Admin 拦截；无 scope | 越权风险在目标场景下**不可接受** |
| Performance | 分页 + 行锁 quota 合理 | 打分汇总、大文件上传待设计 |
| Readability | 结构清晰 | 命名冲突（Registration vs Work）将增加认知负担 |

**审查结论：** 当前 MVP **可继续作为演示基线**，但**不能**视为 workflow-spec 的部分实现；应按 Phase 1–5 演进，而非增量补丁。

---

## 9. 文档关系

```
docs/workflow-spec.md    ← 目标业务逻辑（事实来源）
docs/gap-analysis.md     ← 本文档（现状 vs 目标）
docs/requirement.md      ← 旧 MVP 需求（需修订）
docs/database-design.md  ← 待扩展 work/scope 表
docs/api-contract.md     ← 待扩展 works/reviews API
```

---

*分析版本：1.0.0 | 只读分析，未修改任何业务代码或数据库*
