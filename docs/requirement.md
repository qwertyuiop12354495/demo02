# 需求与开发任务：活动报名系统

> 基于 [`docs/project-context.md`](./project-context.md) 拆解的详细需求与开发任务清单。  
> 当前阶段：**Plan + Tasks** — 仅文档，不含业务代码。

---

## 文档说明

| 项 | 说明 |
|----|------|
| 上游文档 | `docs/project-context.md` |
| 本文档用途 | 功能清单、页面/接口/表结构、MVP 范围、开发顺序与可执行任务 |
| 默认假设 | 沿用 project-context 第 13 节；未决项在 MVP 中采用默认方案并标注 |

**MVP 采用的默认决策（实现前可调整）：**

| 开放问题 | MVP 默认方案 |
|----------|--------------|
| Q1 认证方式 | JWT（Bearer Token），前后端分离易联调 |
| Q2 再次报名 | 已拒绝/已取消后允许再次报名 |
| Q3 名额占用 | 待审核不占名额，审核通过时校验上限 |
| Q4 报名开始时间 | 包含 `registration_start_time`，未到开始时间不可报名 |
| Q5 报名备注 | MVP 不含自定义表单，仅可选备注字段 |
| Q6 通知 | MVP 不含邮件/站内通知 |

---

## 1. 用户端功能清单

### 1.1 账户与认证

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| U-AUTH-01 | 用户注册 | 用户名 + 密码 + 昵称注册，默认角色为普通用户 | P0 | ✓ |
| U-AUTH-02 | 用户登录 | 用户名密码登录，获取 JWT | P0 | ✓ |
| U-AUTH-03 | 用户登出 | 清除本地 Token，跳转登录页 | P0 | ✓ |
| U-AUTH-04 | 登录态保持 | Token 持久化，刷新页面保持登录 | P1 | ✓ |
| U-AUTH-05 | 获取当前用户信息 | 展示昵称、用户名等 | P1 | ✓ |

### 1.2 活动浏览

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| U-ACT-01 | 活动列表 | 分页展示**已上架**活动：标题、时间、地点、剩余名额等 | P0 | ✓ |
| U-ACT-02 | 活动搜索 | 按标题关键词搜索（模糊匹配） | P2 | ✗ |
| U-ACT-03 | 活动详情 | 查看活动完整信息、报名状态提示（可报/已截止/已满/已下架） | P0 | ✓ |
| U-ACT-04 | 活动筛选 | 按时间、状态筛选 | P2 | ✗ |

**业务约束（用户端展示）：**

- 仅展示 `status = PUBLISHED`（已上架）的活动
- 已下架活动不在列表出现；直链访问详情返回「活动不存在」

### 1.3 报名管理

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| U-REG-01 | 提交报名 | 对符合条件的活动提交报名，状态为「待审核」 | P0 | ✓ |
| U-REG-02 | 我的报名列表 | 查看本人全部报名：活动名、状态、报名时间 | P0 | ✓ |
| U-REG-03 | 报名详情 | 查看单条报名详情及审核结果 | P1 | ✓ |
| U-REG-04 | 取消报名 | 取消「待审核」或「已通过」的报名，状态变为「已取消」 | P0 | ✓ |

**报名时后端校验（R1–R4）：**

| 规则 | 用户端提示（示例） |
|------|-------------------|
| R1 不可重复报名 | 您已报名该活动 |
| R2 截止后不可报名 | 活动报名已截止 |
| R2 未到开始时间 | 报名尚未开始 |
| R3 人数已满 | 活动名额已满 |
| R4 下架不可报名 | 活动已下架，无法报名 |

### 1.4 用户端非 MVP 功能（后续迭代）

| ID | 功能 | 描述 |
|----|------|------|
| U-FUT-01 | 修改个人资料 | 修改昵称、密码 |
| U-FUT-02 | 活动收藏 | 收藏感兴趣的活动 |
| U-FUT-03 | 报名提醒 | 审核结果站内消息/邮件 |

---

## 2. 管理端功能清单

### 2.1 认证与入口

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| A-AUTH-01 | 管理员登录 | 与普通用户共用登录接口，按角色跳转管理端 | P0 | ✓ |
| A-AUTH-02 | 权限拦截 | 非管理员无法访问管理端页面与 `/api/admin/**` | P0 | ✓ |

### 2.2 活动管理

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| A-ACT-01 | 活动列表（管理） | 查看全部活动（草稿/已上架/已下架），支持分页 | P0 | ✓ |
| A-ACT-02 | 发布活动 | 创建活动，初始状态为「草稿」 | P0 | ✓ |
| A-ACT-03 | 编辑活动 | 修改活动信息（标题、描述、时间、地点、人数上限等） | P0 | ✓ |
| A-ACT-04 | 上架活动 | 草稿/已下架 → 已上架 | P0 | ✓ |
| A-ACT-05 | 下架活动 | 已上架 → 已下架；新报名立即禁止（R4） | P0 | ✓ |
| A-ACT-06 | 活动详情（管理） | 查看活动详情及当前报名统计 | P1 | ✓ |
| A-ACT-07 | 删除活动 | 软删除或硬删除（有报名记录时禁止硬删） | P2 | ✗ |

**活动字段（管理端表单）：**

- 标题、描述、地点
- 报名开始时间、报名截止时间
- 活动开始时间、活动结束时间（可选）
- 人数上限（`max_participants`，≥ 1）

### 2.3 报名管理

| ID | 功能 | 描述 | 优先级 | MVP |
|----|------|------|--------|-----|
| A-REG-01 | 报名名单 | 按活动查看所有报名记录（用户、状态、时间） | P0 | ✓ |
| A-REG-02 | 审核通过 | 待审核 → 已通过；校验名额（R3） | P0 | ✓ |
| A-REG-03 | 审核拒绝 | 待审核 → 已拒绝 | P0 | ✓ |
| A-REG-04 | 报名筛选 | 按状态筛选名单 | P1 | ✓ |
| A-REG-05 | 导出名单 | 导出 CSV/Excel | P2 | ✗ |

### 2.4 管理端非 MVP 功能（后续迭代）

| ID | 功能 | 描述 |
|----|------|------|
| A-FUT-01 | 用户管理 | 查看/禁用普通用户 |
| A-FUT-02 | 管理员账号管理 | 创建子管理员 |
| A-FUT-03 | 操作日志 | 记录上下架、审核等操作 |
| A-FUT-04 | 数据统计看板 | 活动数、报名数、通过率 |

---

## 3. 页面清单

### 3.1 公共页面

| 页面 | 路由 | 角色 | 说明 | MVP |
|------|------|------|------|-----|
| 登录页 | `/login` | 游客 | 用户名 + 密码登录；登录后按角色跳转 | ✓ |
| 注册页 | `/register` | 游客 | 用户注册 | ✓ |
| 404 页 | `/:pathMatch(.*)*` | 全部 | 路由不存在 | ✓ |

### 3.2 用户端页面

| 页面 | 路由 | 角色 | 说明 | MVP |
|------|------|------|------|-----|
| 用户布局 | `/` | 用户 | 顶栏：活动列表、我的报名、登出 | ✓ |
| 活动列表 | `/activities` | 用户 | 已上架活动卡片列表 + 分页 | ✓ |
| 活动详情 | `/activities/:id` | 用户 | 活动信息 + 报名按钮/状态提示 | ✓ |
| 我的报名 | `/my-registrations` | 用户 | 报名记录列表 + 取消操作 | ✓ |
| 报名详情 | `/my-registrations/:id` | 用户 | 单条报名详情（可与列表合并为抽屉） | ✓ |

### 3.3 管理端页面

| 页面 | 路由 | 角色 | 说明 | MVP |
|------|------|------|------|-----|
| 管理布局 | `/admin` | 管理员 | 侧栏：活动管理、报名管理 | ✓ |
| 管理首页 | `/admin` 或 `/admin/dashboard` | 管理员 | MVP 可重定向到活动列表 | ✓ |
| 活动列表（管理） | `/admin/activities` | 管理员 | 全部状态活动 + 操作入口 | ✓ |
| 发布活动 | `/admin/activities/create` | 管理员 | 新建活动表单 | ✓ |
| 编辑活动 | `/admin/activities/:id/edit` | 管理员 | 编辑活动表单 | ✓ |
| 活动详情（管理） | `/admin/activities/:id` | 管理员 | 活动信息 + 上架/下架 + 跳转名单 | ✓ |
| 报名名单 | `/admin/activities/:id/registrations` | 管理员 | 该活动报名列表 + 审核操作 | ✓ |

### 3.4 路由守卫规则

| 条件 | 行为 |
|------|------|
| 未登录访问需登录页 | 跳转 `/login`，记录 redirect |
| 已登录访问 `/login` | 跳转对应首页（用户 `/activities`，管理员 `/admin/activities`） |
| 普通用户访问 `/admin/**` | 跳转 `/activities` 或 403 页 |
| 管理员访问用户端 | 允许（管理员也可浏览活动） |

### 3.5 页面与功能映射

```
/login ────────────────────────── U-AUTH-02
/register ─────────────────────── U-AUTH-01
/activities ───────────────────── U-ACT-01
/activities/:id ───────────────── U-ACT-03, U-REG-01
/my-registrations ─────────────── U-REG-02, U-REG-04
/admin/activities ─────────────── A-ACT-01, A-ACT-04, A-ACT-05
/admin/activities/create ──────── A-ACT-02
/admin/activities/:id/edit ──── A-ACT-03
/admin/activities/:id/registrations ─ A-REG-01, A-REG-02, A-REG-03
```

---

## 4. 后端接口清单

### 4.1 通用约定

**Base URL：** `/api`

**统一响应：**

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

**分页响应 data 结构：**

```json
{
  "list": [],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

**认证 Header：** `Authorization: Bearer <token>`

### 4.2 认证模块

| ID | 方法 | 路径 | 角色 | 描述 | MVP |
|----|------|------|------|------|-----|
| API-AUTH-01 | POST | `/api/auth/register` | 游客 | 用户注册 | ✓ |
| API-AUTH-02 | POST | `/api/auth/login` | 游客 | 登录，返回 token + 用户信息 | ✓ |
| API-AUTH-03 | GET | `/api/auth/me` | 已登录 | 获取当前用户信息 | ✓ |
| API-AUTH-04 | POST | `/api/auth/logout` | 已登录 | 登出（JWT 场景可为前端清 token，后端可选黑名单） | ✓ |

**API-AUTH-01 请求体：**

```json
{
  "username": "zhangsan",
  "password": "******",
  "nickname": "张三"
}
```

**API-AUTH-02 响应 data：**

```json
{
  "token": "eyJ...",
  "user": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "USER"
  }
}
```

### 4.3 用户端 — 活动

| ID | 方法 | 路径 | 角色 | 描述 | MVP |
|----|------|------|------|------|-----|
| API-U-ACT-01 | GET | `/api/activities` | 用户 | 已上架活动分页列表 | ✓ |
| API-U-ACT-02 | GET | `/api/activities/{id}` | 用户 | 已上架活动详情；下架返回 404 | ✓ |

**API-U-ACT-01 Query：** `page`, `pageSize`, `keyword`（非 MVP 可忽略）

**活动列表项字段：** `id`, `title`, `location`, `registrationStartTime`, `registrationDeadline`, `eventStartTime`, `maxParticipants`, `approvedCount`, `remainingSlots`, `canRegister`（综合可报状态）

### 4.4 用户端 — 报名

| ID | 方法 | 路径 | 角色 | 描述 | MVP |
|----|------|------|------|------|-----|
| API-U-REG-01 | POST | `/api/registrations` | 用户 | 提交报名 | ✓ |
| API-U-REG-02 | GET | `/api/registrations/mine` | 用户 | 我的报名分页列表 | ✓ |
| API-U-REG-03 | GET | `/api/registrations/{id}` | 用户 | 报名详情（仅本人） | ✓ |
| API-U-REG-04 | PATCH | `/api/registrations/{id}/cancel` | 用户 | 取消报名 | ✓ |

**API-U-REG-01 请求体：**

```json
{
  "activityId": 1,
  "remark": "可选备注"
}
```

**报名状态枚举：** `PENDING` | `APPROVED` | `REJECTED` | `CANCELLED`

### 4.5 管理端 — 活动

| ID | 方法 | 路径 | 角色 | 描述 | MVP |
|----|------|------|------|------|-----|
| API-A-ACT-01 | GET | `/api/admin/activities` | 管理员 | 全部活动分页列表 | ✓ |
| API-A-ACT-02 | POST | `/api/admin/activities` | 管理员 | 创建活动（草稿） | ✓ |
| API-A-ACT-03 | GET | `/api/admin/activities/{id}` | 管理员 | 活动详情（含统计） | ✓ |
| API-A-ACT-04 | PUT | `/api/admin/activities/{id}` | 管理员 | 编辑活动 | ✓ |
| API-A-ACT-05 | PATCH | `/api/admin/activities/{id}/status` | 管理员 | 上架/下架 | ✓ |

**API-A-ACT-05 请求体：**

```json
{
  "status": "PUBLISHED"
}
```

**活动状态枚举：** `DRAFT` | `PUBLISHED` | `OFFLINE`

### 4.6 管理端 — 报名

| ID | 方法 | 路径 | 角色 | 描述 | MVP |
|----|------|------|------|------|-----|
| API-A-REG-01 | GET | `/api/admin/activities/{activityId}/registrations` | 管理员 | 活动报名名单（分页、按状态筛选） | ✓ |
| API-A-REG-02 | PATCH | `/api/admin/registrations/{id}/audit` | 管理员 | 审核报名 | ✓ |

**API-A-REG-02 请求体：**

```json
{
  "action": "APPROVE"
}
```

`action` 取值：`APPROVE` | `REJECT`

### 4.7 错误码（建议）

| code | 含义 | 场景 |
|------|------|------|
| 0 | 成功 | — |
| 40100 | 未登录 | Token 缺失或无效 |
| 40300 | 无权限 | 非管理员访问管理接口 |
| 40400 | 资源不存在 | 活动/报名不存在 |
| 40001 | 重复报名 | R1 |
| 40002 | 报名已截止 | R2 |
| 40003 | 报名未开始 | R2 扩展 |
| 40004 | 名额已满 | R3 |
| 40005 | 活动已下架 | R4 |
| 40006 | 状态不允许操作 | 如审核非待审核记录、取消终态报名 |
| 40007 | 参数校验失败 | 表单校验 |
| 50000 | 系统错误 | 未预期异常 |

---

## 5. 数据库表清单

### 5.1 ER 关系概览

```
user (1) ──────< registration >────── (1) activity
  │                                      │
  │                                      └── created_by → user (管理员)
  └── role: USER | ADMIN
```

### 5.2 表：`user`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 登录名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密 |
| nickname | VARCHAR(50) | NOT NULL | 显示昵称 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | `USER` / `ADMIN` |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1 正常 0 禁用 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引：** `uk_username (username)`

### 5.3 表：`activity`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| title | VARCHAR(200) | NOT NULL | 活动标题 |
| description | TEXT | | 活动描述 |
| location | VARCHAR(200) | | 地点/线上说明 |
| registration_start_time | DATETIME | NOT NULL | 报名开始时间 |
| registration_deadline | DATETIME | NOT NULL | 报名截止时间 |
| event_start_time | DATETIME | | 活动开始时间 |
| event_end_time | DATETIME | | 活动结束时间 |
| max_participants | INT | NOT NULL | 人数上限，≥ 1 |
| approved_count | INT | NOT NULL, DEFAULT 0 | 已通过人数（冗余计数） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | `DRAFT` / `PUBLISHED` / `OFFLINE` |
| created_by | BIGINT | NOT NULL, FK → user.id | 创建人 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引：**

- `idx_status (status)`
- `idx_registration_deadline (registration_deadline)`
- `idx_created_by (created_by)`

### 5.4 表：`registration`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL, FK → user.id | 报名用户 |
| activity_id | BIGINT | NOT NULL, FK → activity.id | 关联活动 |
| status | VARCHAR(20) | NOT NULL | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| remark | VARCHAR(500) | | 用户备注（可选） |
| audited_by | BIGINT | FK → user.id | 审核人（管理员） |
| audited_at | DATETIME | | 审核时间 |
| created_at | DATETIME | NOT NULL | 报名时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引与约束：**

- `idx_user_id (user_id)`
- `idx_activity_id (activity_id)`
- `idx_activity_status (activity_id, status)` — 名单查询、统计
- **有效报名唯一性：** 应用层保证同一 `(user_id, activity_id)` 在 `PENDING` 或 `APPROVED` 状态下仅一条；历史 `REJECTED`/`CANCELLED` 记录保留，允许再次报名

> 说明：不在数据库层做简单 UNIQUE(user_id, activity_id)，以支持「拒绝/取消后再次报名」。通过 Service 层查询有效状态实现 R1。

### 5.5 初始化数据

| 数据 | 说明 |
|------|------|
| 管理员账号 | `username: admin`，预置 `role = ADMIN`，密码部署时配置 |
| 示例活动 | 开发环境可选 seed 1–2 条测试活动 |

### 5.6 非 MVP 表（后续）

| 表 | 用途 |
|----|------|
| `operation_log` | 管理操作审计 |
| `notification` | 站内通知 |

---

## 6. 最小可运行版本（MVP）范围

### 6.1 MVP 目标

交付一条完整业务闭环：

> **管理员发布并上架活动 → 用户注册登录 → 浏览活动 → 提交报名 → 管理员审核 → 用户查看报名状态 → 用户可取消报名**

### 6.2 MVP 包含

| 维度 | 范围 |
|------|------|
| **用户端功能** | U-AUTH-01~05、U-ACT-01/03、U-REG-01~04 |
| **管理端功能** | A-AUTH-01~02、A-ACT-01~06、A-REG-01~04 |
| **页面** | 第 3 节中标注 MVP ✓ 的全部页面 |
| **接口** | 第 4 节中标注 MVP ✓ 的全部接口 |
| **数据表** | `user`、`activity`、`registration` |
| **业务规则** | R1–R7 全部在后端 enforced |
| **认证** | JWT + 角色鉴权 |
| **测试** | 核心业务规则 Service 层单元测试 + 关键 API 集成测试 |

### 6.3 MVP 不包含

| 项 | 说明 |
|----|------|
| 活动搜索/高级筛选 | U-ACT-02/04 |
| 删除活动 | A-ACT-07 |
| 导出名单 | A-REG-05 |
| 邮件/站内通知 | U-FUT-03 |
| 用户管理、操作日志、数据看板 | A-FUT-01~04 |
| 修改个人资料 | U-FUT-01 |
| Swagger UI 生产部署 | 可开发环境启用 |
| Docker / CI/CD | 可手工本地运行 |

### 6.4 MVP 验收标准

- [ ] 用户可注册、登录、登出，未登录无法报名
- [ ] 用户仅能看到已上架活动，可查看详情并提交报名
- [ ] 重复报名、截止后报名、人满报名、下架报名均被拒绝并返回明确错误码
- [ ] 用户可查看我的报名列表，可取消待审核/已通过的报名
- [ ] 管理员可 CRUD 活动（创建为草稿）、上架/下架
- [ ] 管理员可查看报名名单，可审核通过/拒绝；通过时校验名额
- [ ] 普通用户无法访问 `/api/admin/**` 及管理端页面
- [ ] 所有接口返回统一 JSON 格式
- [ ] 前后端本地联调可完成完整演示流程

---

## 7. 推荐开发顺序

采用 **纵向切片（Vertical Slice）**：每个阶段交付可运行、可验证的功能，而非先做完所有后端再做所有前端。

### 7.1 阶段总览

```
Phase 0  工程脚手架
   ↓
Phase 1  认证（注册/登录/JWT）
   ↓
Phase 2  管理端活动 CRUD + 上下架
   ↓
Phase 3  用户端活动浏览
   ↓
Phase 4  报名提交 + 我的报名 + 取消
   ↓
Phase 5  管理端报名名单 + 审核
   ↓
Phase 6  联调、规则补测、MVP 验收
```

### 7.2 Phase 0：工程脚手架

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T0-1 | 初始化 Spring Boot 项目（`backend/`） | `./mvnw spring-boot:run` 启动成功 | 无 |
| T0-2 | 初始化 Vue3 + Vite 项目（`frontend/`） | `pnpm dev` 启动成功 | 无 |
| T0-3 | 配置 MySQL 连接与库 `activity_registration` | 后端可连接数据库 | T0-1 |
| T0-4 | 后端统一响应 `ApiResponse` + 全局异常处理 | 示例接口返回 `{code,message,data}` | T0-1 |
| T0-5 | 前端 Axios 封装 + 路由骨架 + 布局占位 | 页面可切换，API 基址可配置 | T0-2 |
| T0-6 | 执行建表 SQL（user / activity / registration） | 三张表创建成功 | T0-3 |

**Checkpoint 0：** 前后端均可启动，数据库表就绪，健康检查接口可访问。

---

### 7.3 Phase 1：认证模块

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T1-1 | 实现 `user` 实体、Mapper/Repository | 可读写用户 | T0-6 |
| T1-2 | 实现注册 API（API-AUTH-01） | 重复用户名返回错误 | T1-1 |
| T1-3 | 实现登录 API（API-AUTH-02）+ JWT 签发 | 返回 token 与 role | T1-1 |
| T1-4 | 实现 JWT 过滤器 + `/api/auth/me` | 携带 token 可获取当前用户 | T1-3 |
| T1-5 | 实现角色鉴权（`@PreAuthorize` 或拦截器） | 非 ADMIN 访问 admin 接口返回 403 | T1-4 |
| T1-6 | 前端登录页、注册页 | 可注册、登录并存储 token | T0-5, T1-2, T1-3 |
| T1-7 | 前端路由守卫（登录态、角色） | 未登录跳转 login | T1-6 |
| T1-8 | Seed 管理员账号 | admin 可登录进管理端 | T1-1 |

**Checkpoint 1：** 用户可注册登录；管理员与普通用户登录后跳转不同首页；鉴权生效。

---

### 7.4 Phase 2：管理端活动管理

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T2-1 | `activity` 实体与 CRUD Service | 可创建草稿活动 | T0-6 |
| T2-2 | POST/GET/PUT 管理端活动 API（API-A-ACT-01~04） | 管理员可增删改查 | T2-1, T1-5 |
| T2-3 | PATCH 上下架 API（API-A-ACT-05） | 状态在 DRAFT/PUBLISHED/OFFLINE 间切换 | T2-2 |
| T2-4 | 活动表单校验（时间逻辑：开始 < 截止） | 非法时间返回 40007 | T2-2 |
| T2-5 | 前端管理布局 + 活动列表页 | 展示全部状态活动 | T1-7 |
| T2-6 | 前端发布/编辑活动页 | 可创建、编辑活动 | T2-2, T2-5 |
| T2-7 | 前端活动详情（管理）+ 上下架按钮 | 一键上架/下架 | T2-3, T2-5 |

**Checkpoint 2：** 管理员可完成活动创建 → 上架全流程；数据库有已上架活动可供用户端使用。

---

### 7.5 Phase 3：用户端活动浏览

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T3-1 | 用户端活动列表 API（API-U-ACT-01） | 仅返回 PUBLISHED | T2-3 |
| T3-2 | 用户端活动详情 API（API-U-ACT-02） | OFFLINE 返回 404 | T2-3 |
| T3-3 | 列表/详情返回 `canRegister` 等综合状态 | 截止/已满/未开始正确 | T3-1, T3-2 |
| T3-4 | 前端用户布局 + 活动列表页 | 卡片展示已上架活动 | T1-7 |
| T3-5 | 前端活动详情页 | 展示详情与报名入口（按钮先占位） | T3-2, T3-4 |

**Checkpoint 3：** 普通用户登录后可浏览已上架活动详情；下架活动不可见。

---

### 7.6 Phase 4：报名（用户侧）

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T4-1 | `registration` 实体与 Service | 可创建报名记录 | T0-6 |
| T4-2 | 实现报名规则校验 R1–R4 | 单元测试覆盖各拒绝场景 | T4-1, T3-2 |
| T4-3 | POST 报名 API（API-U-REG-01） | 成功创建 PENDING 记录 | T4-2, T1-4 |
| T4-4 | GET 我的报名 API（API-U-REG-02/03） | 仅返回当前用户数据 | T4-1, T1-4 |
| T4-5 | PATCH 取消报名 API（API-U-REG-04） | PENDING/APPROVED 可取消；释放名额 | T4-1 |
| T4-6 | 前端活动详情报名按钮 | 可提交报名并提示结果 | T4-3, T3-5 |
| T4-7 | 前端我的报名列表页 | 展示状态、可取消 | T4-4, T4-5 |

**Checkpoint 4：** 用户可报名、查看我的报名、取消报名；业务规则 R1–R4 生效。

---

### 7.7 Phase 5：报名审核（管理侧）

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T5-1 | GET 报名名单 API（API-A-REG-01） | 按活动分页，支持状态筛选 | T4-1, T1-5 |
| T5-2 | PATCH 审核 API（API-A-REG-02） | APPROVE/REJECT；通过时校验 R3 | T4-1, T4-2 |
| T5-3 | 审核通过时 `approved_count` 原子递增 | 并发下不超卖 | T5-2 |
| T5-4 | 前端报名名单页 | 列表 + 通过/拒绝操作 | T5-1, T5-2 |
| T5-5 | 活动详情（管理）展示报名统计 | 待审核/已通过数量 | T5-1, T2-7 |

**Checkpoint 5：** 管理员可审核报名；通过后用户端状态同步；名额满时审核/报名均拒绝。

---

### 7.8 Phase 6：MVP 收尾

| 任务 | 描述 | 验收 | 依赖 |
|------|------|------|------|
| T6-1 | 补全错误码与前端统一错误提示 | 各业务错误友好展示 | Phase 1–5 |
| T6-2 | 后端核心业务规则测试补全 | `mvn test` 通过 | Phase 4–5 |
| T6-3 | 前端关键页面样式与空状态 | 无数据、加载、错误态 | Phase 3–5 |
| T6-4 | 端到端手工验收（按 6.4 清单） | 全部勾选 | 全部 |
| T6-5 | 编写 `README.md` 本地启动说明 | 新人可按文档跑起项目 | T0 |

**Checkpoint 6（MVP 完成）：** 满足第 6.4 节全部验收标准。

---

### 7.9 并行化建议

| 可并行 | 说明 |
|--------|------|
| T0-1 与 T0-2 | 前后端脚手架互不依赖 |
| T1-6 与 T1-4 | 前端页面可在 API mock 下先行 |
| T2-5~T2-7 与 T2-2 | 管理端 UI 与 API 可并行（先定接口契约） |
| T6-2 与 T6-3 | 测试与 UI 打磨可并行 |

| 必须串行 | 说明 |
|----------|------|
| 建表 → 实体 → Service → API | 数据层依赖 |
| 活动上架 → 用户端列表 | 需要有可展示数据 |
| 报名 API → 审核 API | 审核依赖报名记录 |

---

### 7.10 风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 审核通过并发超卖 | 高 | `approved_count` 乐观锁或 `SELECT FOR UPDATE`；审核时二次校验 |
| JWT 与跨域 | 中 | 开发环境 Vite proxy；生产配置 CORS |
| 有效报名唯一性实现复杂 | 中 | Service 层显式查询 PENDING/APPROVED；写清单元测试 |
| 前后端字段不一致 | 中 | 共用命名约定；可先写 `docs/api-spec.md` 细化 |

---

## 8. 任务统计

| 维度 | MVP 数量 |
|------|----------|
| 用户端功能 | 12 项 |
| 管理端功能 | 11 项 |
| 页面 | 13 个 |
| 后端接口 | 17 个 |
| 数据表 | 3 张 |
| 开发任务（T0–T6） | 38 项 |
| 开发阶段 | 7 个 Phase |

---

## 9. 文档关系

```
docs/project-context.md   ← 项目上下文、业务规则、技术约定
        ↓
docs/requirement.md       ← 本文档：功能/页面/接口/表/MVP/开发顺序
        ↓（建议下一步）
docs/api-spec.md          ← 接口请求/响应 JSON 完整示例
docs/data-model.md        ← DDL、索引、迁移脚本
docs/tasks.md             ← 从本文档 T0–T6 拆为带 Files 字段的可执行任务
```

---

*最后更新：2026-06-07 | 状态：待 Review*
