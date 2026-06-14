# 数据库设计：活动报名系统

> 基于 [`docs/project-context.md`](./project-context.md) 与 [`docs/requirement.md`](./requirement.md) 设计的 MySQL 8.0+ 数据模型。  
> 建表脚本：[`db/init.sql`](../db/init.sql)（**仅生成，不自动执行**）。

---

## 1. 设计目标

| 目标 | 说明 |
|------|------|
| 契约优先 | 表结构直接支撑 API 字段与业务规则 R1–R7 |
| 边界校验 | 时间、人数、状态在数据库层增加 CHECK 约束（MySQL 8.0.16+） |
| 防重复报名 | 数据库层保证「有效报名」唯一，应用层无需单独防并发重复插入 |
| 可演进 | 字段命名稳定，扩展字段采用 nullable 追加策略 |

---

## 2. ER 关系

```
┌─────────────────┐         ┌──────────────────────────┐         ┌─────────────────┐
│    sys_user     │         │   activity_registration   │         │    activity     │
├─────────────────┤         ├──────────────────────────┤         ├─────────────────┤
│ id (PK)         │◄───┐    │ id (PK)                   │    ┌───►│ id (PK)         │
│ username (UK)   │    ├────│ user_id (FK)              │    │    │ title           │
│ password        │    │    │ activity_id (FK)          ├────┘    │ ...             │
│ nickname        │    │    │ status                    │         │ created_by (FK)─┼──┐
│ role            │    │    │ apply_time                │         └─────────────────┘  │
│ status          │    │    │ audit_time                │                              │
└─────────────────┘    │    │ audit_remark              │                              │
        ▲              │    │ uk_user_activity (UK)       │                              │
        │              │    └──────────────────────────┘                              │
        └──────────────┴──────────────── audited_by (FK) ◄────────────────────────────┘
```

**关系说明：**

- `sys_user` 1 : N `activity_registration`（用户报名）
- `activity` 1 : N `activity_registration`（活动报名记录）
- `sys_user` 1 : N `activity`（管理员创建活动，`created_by`）
- `activity_registration.audited_by` → `sys_user.id`（审核人）

---

## 3. 命名与 API 映射

数据库使用 `snake_case`；Java 实体/API 使用 `camelCase`。

### 3.1 `activity` 字段映射

| 数据库字段 | API / 业务名 | 说明 |
|------------|--------------|------|
| `title` | `title` | 活动标题 |
| `description` | `description` | 活动描述 |
| `location` | `location` | 地点/线上说明 |
| `start_time` | `eventStartTime` | 活动开始时间 |
| `end_time` | `eventEndTime` | 活动结束时间 |
| `signup_start_time` | `registrationStartTime` | 报名开始时间 |
| `signup_end_time` | `registrationDeadline` | 报名截止时间 |
| `max_count` | `maxParticipants` | 人数上限 |
| `current_count` | `approvedCount` | 当前已通过人数（冗余计数） |
| `status` | `status` | `DRAFT` / `PUBLISHED` / `OFFLINE` |

### 3.2 `activity_registration` 字段映射

| 数据库字段 | API / 业务名 | 说明 |
|------------|--------------|------|
| `user_id` | `userId` | 报名用户 |
| `activity_id` | `activityId` | 关联活动 |
| `status` | `status` | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| `apply_time` | `applyTime` | 用户提交报名时间 |
| `audit_time` | `auditedAt` | 管理员审核时间 |
| `audit_remark` | `auditRemark` | 管理员审核备注（如拒绝原因） |

---

## 4. 表结构详述

### 4.1 `sys_user` — 系统用户表

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT UNSIGNED | N | AUTO | 主键 |
| `username` | VARCHAR(50) | N | — | 登录名，全局唯一 |
| `password` | VARCHAR(255) | N | — | BCrypt 哈希，禁止明文 |
| `nickname` | VARCHAR(50) | N | — | 显示昵称 |
| `role` | VARCHAR(20) | N | `USER` | `USER` / `ADMIN` |
| `status` | TINYINT | N | `1` | `1` 正常 `0` 禁用 |
| `created_at` | DATETIME(3) | N | CURRENT_TIMESTAMP(3) | 创建时间 |
| `updated_at` | DATETIME(3) | N | ON UPDATE | 更新时间 |

**索引：**

| 名称 | 类型 | 字段 | 用途 |
|------|------|------|------|
| `PRIMARY` | PK | `id` | 主键 |
| `uk_sys_user_username` | UNIQUE | `username` | 登录唯一性 |

---

### 4.2 `activity` — 活动表

**必选字段（需求指定）：** `title`, `description`, `location`, `start_time`, `end_time`, `signup_start_time`, `signup_end_time`, `max_count`, `current_count`, `status`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT UNSIGNED | N | AUTO | 主键 |
| `title` | VARCHAR(200) | N | — | 活动标题 |
| `description` | TEXT | Y | NULL | 活动描述 |
| `location` | VARCHAR(200) | Y | NULL | 地点/线上说明 |
| `start_time` | DATETIME(3) | Y | NULL | 活动开始时间 |
| `end_time` | DATETIME(3) | Y | NULL | 活动结束时间 |
| `signup_start_time` | DATETIME(3) | N | — | 报名开始时间 |
| `signup_end_time` | DATETIME(3) | N | — | 报名截止时间 |
| `max_count` | INT UNSIGNED | N | — | 人数上限，≥ 1 |
| `current_count` | INT UNSIGNED | N | `0` | 已通过人数冗余计数 |
| `status` | VARCHAR(20) | N | `DRAFT` | 活动状态枚举 |
| `created_by` | BIGINT UNSIGNED | N | — | 创建人，FK → `sys_user.id` |
| `created_at` | DATETIME(3) | N | CURRENT_TIMESTAMP(3) | 创建时间 |
| `updated_at` | DATETIME(3) | N | ON UPDATE | 更新时间 |

**活动状态 `status`：**

| 值 | 含义 | 用户端可见 |
|----|------|------------|
| `DRAFT` | 草稿 | 否 |
| `PUBLISHED` | 已上架 | 是，可报名（满足其他规则） |
| `OFFLINE` | 已下架 | 否 |

**CHECK 约束：**

- `max_count >= 1`
- `current_count <= max_count`
- `signup_start_time < signup_end_time`
- `start_time IS NULL OR end_time IS NULL OR start_time <= end_time`

**`current_count` 维护规则（应用层 + 事务）：**

| 事件 | `current_count` 变化 |
|------|----------------------|
| 审核通过（PENDING → APPROVED） | `+1`（需先校验 `< max_count`） |
| 取消已通过（APPROVED → CANCELLED） | `-1` |
| 其他状态变更 | 不变 |

---

### 4.3 `activity_registration` — 活动报名表

**必选字段（需求指定）：** `user_id`, `activity_id`, `status`, `apply_time`, `audit_time`, `audit_remark`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT UNSIGNED | N | AUTO | 主键 |
| `user_id` | BIGINT UNSIGNED | N | — | 报名用户，FK → `sys_user.id` |
| `activity_id` | BIGINT UNSIGNED | N | — | 关联活动，FK → `activity.id` |
| `status` | VARCHAR(20) | N | — | 报名状态枚举 |
| `apply_time` | DATETIME(3) | N | — | 用户提交报名时间 |
| `audit_time` | DATETIME(3) | Y | NULL | 审核时间（待审核时为 NULL） |
| `audit_remark` | VARCHAR(500) | Y | NULL | 审核备注（如拒绝原因） |
| `apply_remark` | VARCHAR(500) | Y | NULL | 用户报名备注（可选，API `remark`） |
| `audited_by` | BIGINT UNSIGNED | Y | NULL | 审核人，FK → `sys_user.id` |
| `created_at` | DATETIME(3) | N | CURRENT_TIMESTAMP(3) | 记录创建时间 |
| `updated_at` | DATETIME(3) | N | ON UPDATE | 记录更新时间 |

**报名状态 `status`：**

| 值 | 含义 | 是否有效报名 |
|----|------|--------------|
| `PENDING` | 待审核 | 是 |
| `APPROVED` | 已通过 | 是 |
| `REJECTED` | 已拒绝 | 否（不可再次报名，见 §5） |
| `CANCELLED` | 已取消 | 否（不可再次报名，见 §5） |

**状态流转：** 见 `project-context.md` §4.2。

---

## 5. 唯一约束：防重复报名（R1）

### 5.1 业务语义

规则 R1：**同一用户对同一活动只能存在一条报名记录**，无论当前状态如何。

### 5.2 采用方案：`UNIQUE(user_id, activity_id)`

```sql
UNIQUE KEY uk_user_activity (user_id, activity_id)
```

**原理：**

- 每个 `(user_id, activity_id)` 组合在表中最多一行，从数据库层杜绝重复报名
- 并发重复提交时，第二条 `INSERT` 触发 `Duplicate entry`，应用层映射为错误码 `40001`
- `uk_user_activity` 左前缀含 `user_id`，可同时支撑「我的报名」按用户查询

### 5.3 业务影响

| 场景 | 行为 |
|------|------|
| 首次报名 | `INSERT` 新记录，`status = PENDING` |
| 重复报名（任意状态） | `INSERT` 失败，返回 40001 |
| 取消 / 拒绝后再次报名 | **不支持** `INSERT` 新行；同一行保留，`status` 为终态 |
| 状态变更 | 对已有行 `UPDATE status`，不新增行 |

> 若未来需支持「拒绝/取消后再次报名」，须调整唯一约束策略（如改为条件唯一列 `active_key`），属 schema 变更，需先更新本文档。

---

## 6. 索引设计

### 6.1 `sys_user`

| 索引名 | 类型 | 字段 | 场景 |
|--------|------|------|------|
| `PRIMARY` | PK | `id` | 主键查找 |
| `uk_sys_user_username` | UNIQUE | `username` | 登录、注册查重 |

### 6.2 `activity`

| 索引名 | 类型 | 字段 | 场景 |
|--------|------|------|------|
| `PRIMARY` | PK | `id` | 主键查找 |
| `idx_activity_status` | INDEX | `status` | 用户端列表：`WHERE status = 'PUBLISHED'` |
| `idx_activity_signup_end` | INDEX | `signup_end_time` | 按截止时间筛选/排序 |
| `idx_activity_created_by` | INDEX | `created_by` | 管理员查看自己创建的活动 |
| `idx_activity_status_signup_end` | INDEX | `status`, `signup_end_time` | 已上架活动按截止时间分页（覆盖索引候选） |

### 6.3 `activity_registration`

| 索引名 | 类型 | 字段 | 场景 |
|--------|------|------|------|
| `PRIMARY` | PK | `id` | 主键查找 |
| `uk_user_activity` | UNIQUE | `user_id`, `activity_id` | 防重复报名（R1）；左前缀覆盖 `user_id` 查询 |
| `idx_registration_activity_id` | INDEX | `activity_id` | 活动报名名单 |
| `idx_registration_activity_status` | INDEX | `activity_id`, `status` | 管理端按活动+状态筛选名单 |
| `idx_registration_user_apply_time` | INDEX | `user_id`, `apply_time` DESC | 我的报名按时间倒序 |

### 6.4 索引设计说明

- **避免过度索引**：MVP 阶段以上索引已覆盖主要查询；`title` 模糊搜索（非 MVP）可后续加全文索引
- **联合索引顺序**：等值条件列在前（`activity_id`），范围/排序列在后（`status`、`apply_time`）
- **外键**：使用 `ON DELETE RESTRICT`，防止误删用户/活动导致报名数据丢失

---

## 7. 外键约束

| 子表 | 字段 | 父表 | 删除策略 |
|------|------|------|----------|
| `activity` | `created_by` | `sys_user.id` | RESTRICT |
| `activity_registration` | `user_id` | `sys_user.id` | RESTRICT |
| `activity_registration` | `activity_id` | `activity.id` | RESTRICT |
| `activity_registration` | `audited_by` | `sys_user.id` | RESTRICT |

---

## 8. 初始化数据

`db/init.sql` 末尾包含可选 Seed：

| 数据 | 说明 |
|------|------|
| 管理员 `admin` | `role = ADMIN`；密码为占位 BCrypt，**部署前必须修改** |
| 测试用户 `user1` | 开发联调用 |
| 示例活动 | 1 条已上架活动，便于用户端联调 |

---

## 9. 质量审查（Code Review）

按 `code-review-and-quality` 五轴对本次数据库设计自检：

### 9.1 Correctness（正确性）

| 检查项 | 结论 |
|--------|------|
| 满足需求必选字段 | ✓ 三张表字段齐全 |
| R1 防重复报名 | ✓ `uk_user_activity` 唯一索引 |
| R3 人数上限 | ✓ `current_count` + CHECK + 审核时事务校验 |
| 状态枚举与 API 一致 | ✓ 与 requirement.md 对齐 |
| 拒绝/取消后不可再报名 | ✓ 由 `uk_user_activity` 保证，同一用户活动仅一行 |

### 9.2 Readability（可读性）

| 检查项 | 结论 |
|--------|------|
| 表名、字段名语义清晰 | ✓ `signup_*`、`apply_time` 等符合业务语言 |
| 文档含 ER 图与 API 映射 | ✓ |
| SQL 文件含分段注释 | ✓ |

### 9.3 Architecture（架构）

| 检查项 | 结论 |
|--------|------|
| 与 project-context 领域模型一致 | ✓ |
| 冗余 `current_count` 换查询性能 | ✓ 需在 Service 层事务维护 |
| 扩展性 | ✓ 可选字段 nullable，后续可加 `operation_log` 等 |

### 9.4 Security（安全）

| 检查项 | 结论 |
|--------|------|
| 密码字段长度适配 BCrypt | ✓ VARCHAR(255) |
| 无硬编码生产密码 | ✓ Seed 仅开发占位并注释警告 |
| 外键 RESTRICT 防级联误删 | ✓ |

### 9.5 Performance（性能）

| 检查项 | 结论 |
|--------|------|
| 列表查询有索引支撑 | ✓ |
| 无未约束的大字段滥用 | ✓ `description` 为 TEXT，可接受 |
| 审核并发 | ⚠ 审核通过需 `UPDATE activity SET current_count = current_count + 1 WHERE id = ? AND current_count < max_count` 乐观控制 |

### 9.6 审查结论

**Approve（文档与 SQL 可进入实现阶段）**

| 级别 | 项 | 说明 |
|------|-----|------|
| FYI | 拒绝/取消后再次报名 | 当前 schema 不支持；需改唯一约束策略 |
| FYI | 时区 | 建议应用层统一 UTC 存储，展示层本地化 |

---

## 10. 使用说明

```bash
# 手动执行（请勿在未确认的环境中自动运行）
mysql -u root -p < db/init.sql
```

或在 MySQL 客户端中：

```sql
SOURCE /path/to/rpskilldeom/db/init.sql;
```

---

## 11. 文档关系

```
docs/project-context.md  → 业务规则、技术约定
docs/requirement.md      → 功能、接口、MVP
docs/database-design.md  → 本文档
db/init.sql              → 可执行 DDL + Seed
```

---

*最后更新：2026-06-07 | 状态：待 Review*
