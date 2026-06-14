# 完整业务流程测试方案

> **文档类型：** Test Plan（接口 + 数据库 + 前端联调）  
> **依据：** [`workflow-spec.md`](./workflow-spec.md)、当前代码库 API 与页面实现  
> **方法：** 结合 TDD「先定义可失败断言再实现自动化」与调试「逐步复现、保留证据、对照规格」  
> **状态：** 待执行 | 最后更新：2026-06-07  
> **说明：** 本文档仅描述测试方案，不修改业务代码。

---

## 0. 文档目标

验证作品（Work）从**省级发布活动**到**四级审核**、**三级打分**、**公示展示**的端到端流程，并覆盖：

- 正常主路径（流程 1–13）
- 退回修改分支（流程 14–15）
- 安全负向用例（流程 16–18）

每个用例均给出：**接口调用顺序**、**关键断言**、**数据库状态变化**、**前端页面验证点**。

---

## 1. 测试环境与前置条件

### 1.1 基础设施

| 项 | 要求 |
|----|------|
| 数据库 | MySQL 8.0+，库名 `activity_registration` |
| 迁移脚本 | 按顺序执行：`db/init.sql` → `db/migration-user-scope.sql` → `db/migration-workflow.sql` → `db/migration-score-rules.sql` → `db/migration-public-notice-objection.sql` |
| 后端 | Spring Boot，`JWT_SECRET`（≥32 字符）、`DB_PASSWORD` 已配置 |
| MinIO | `MINIO_ENABLED=true`（上传/材料用例）；Bucket **私有**，依赖预签名 URL |
| 前端 | `pnpm dev`，默认代理至 `http://localhost:8080` |

### 1.2 测试账号矩阵（建议专用 Fixture）

所有账号 `status=1`（启用），密码统一由团队安全渠道分发（**勿写入仓库**）。辖区均位于 **广东省 / 深圳市 / 南山区 / 示例中学**，便于跨辖区对比。

| 用户名 | role（DB） | 角色（JWT roleType） | 辖区 scope |
|--------|------------|----------------------|------------|
| `province_admin` | `PROVINCE_ADMIN` | `PROVINCE_ADMIN` | 广东省 |
| `city_admin` | `CITY_ADMIN` | `CITY_ADMIN` | 广东省 / 深圳市 |
| `district_admin` | `DISTRICT_ADMIN` | `DISTRICT_ADMIN` | 广东省 / 深圳市 / 南山区 |
| `school_admin` | `SCHOOL_ADMIN` | `SCHOOL_ADMIN` | 广东省 / 深圳市 / 南山区 / 示例中学 |
| `teacher_a` | `TEACHER` | `TEACHER` | 同上 |
| `district_reviewer_1` | `DISTRICT_REVIEWER` | `DISTRICT_REVIEWER` | 南山区 |
| `district_reviewer_2` | `DISTRICT_REVIEWER` | `DISTRICT_REVIEWER` | 南山区 |
| `city_reviewer_1` | `CITY_REVIEWER` | `CITY_REVIEWER` | 深圳市 |
| `city_reviewer_2` | `CITY_REVIEWER` | `CITY_REVIEWER` | 深圳市 |
| `province_reviewer_1` | `PROVINCE_REVIEWER` | `PROVINCE_REVIEWER` | 广东省 |
| `province_reviewer_2` | `PROVINCE_REVIEWER` | `PROVINCE_REVIEWER` | 广东省 |
| `teacher_other` | `TEACHER` | `TEACHER` | 广东省 / 广州市 / 天河区 / 他校中学（跨辖区） |
| `admin_no_scope` | `SCHOOL_ADMIN` | `SCHOOL_ADMIN` | 辖区字段均为 NULL |

> **打分全员约束：** `ScopedScorerCounter` 按辖区内**已启用**的 `*_REVIEWER` 计数；每级至少准备 **2 名**打分员，否则无法触发晋级逻辑。

### 1.3 通用请求约定

```http
Authorization: Bearer <loginResponse.token>
Content-Type: application/json
```

登录：

```http
POST /api/auth/login
{ "username": "<账号>", "password": "<密码>" }
```

成功响应：`code=0`，`data.token` 非空；JWT 中应含 `roleType` 与辖区 claim。

### 1.4 TDD / 调试执行原则

1. **RED：** 每个步骤先写/执行断言，确认在故意错误数据下会失败。  
2. **GREEN：** 按本方案顺序调用接口，直至断言通过。  
3. **证据保留：** 失败时记录 `workId`、`activityId`、HTTP 状态码、`code`、`message`、相关 SQL 查询结果。  
4. **停止线规则：** 任一步失败时，不继续后续流程步骤，先修复数据或环境。  
5. **推荐自动化层次：**
   - L1：REST 集成测试（JUnit + MockMvc / RestAssured）
   - L2：Playwright E2E（按本文「前端验证点」）
   - L3：SQL 快照校验（每阶段后执行 §6 查询）

---

## 2. 主路径：端到端接口顺序（流程 1–13）

下列步骤为**单作品单活动**的推荐顺序；变量 `ACTIVITY_ID`、`WORK_ID` 由上一步响应或 SQL 取得。

---

### 阶段 A：省级管理员发布活动（流程 1）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| A1 | POST | `/api/auth/login` | `province_admin` |
| A2 | POST | `/api/admin/activities` | `province_admin` |
| A3 | PATCH | `/api/admin/activities/{ACTIVITY_ID}/status` | `province_admin` |

**A2 请求体示例：**

```json
{
  "title": "2026 南山区教师技能大赛",
  "description": "E2E 测试活动",
  "location": "示例中学",
  "registrationStartTime": "2026-06-01T00:00:00",
  "registrationDeadline": "2026-12-31T23:59:59",
  "eventStartTime": "2027-01-15T09:00:00",
  "eventEndTime": "2027-01-15T18:00:00",
  "maxParticipants": 100
}
```

**A3 请求体：**

```json
{ "status": "PUBLISHED" }
```

**关键断言：**

- A2：`code=0`，返回 `id`（记为 `ACTIVITY_ID`），活动 `status=DRAFT`
- A3：`code=0`，`status=PUBLISHED`
- 非 `PROVINCE_ADMIN` 调用 A2/A3 → `40300`（FORBIDDEN）

**数据库变化（`activity`）：**

| 字段 | 期望值 |
|------|--------|
| `status` | `DRAFT` → `PUBLISHED` |
| `created_by` | `province_admin` 用户 ID |
| `signup_start_time` / `signup_end_time` | 与请求一致 |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 报名活动管理 | `/admin/activities` | 省管登录后可见菜单；列表出现新活动；状态为「已发布」 |
| 教师首页 | `/` | 活动卡片可见（报名期内） |

---

### 阶段 B：教师报名（流程 2）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| B1 | POST | `/api/auth/login` | `teacher_a` |
| B2 | POST | `/api/works/draft` | `teacher_a` |

**B2 请求体：**

```json
{ "activityId": <ACTIVITY_ID> }
```

**关键断言：**

- `code=0`，返回 `id`（记为 `WORK_ID`）
- `currentStep=SCHOOL`，`currentStatus=DRAFT`，`finalResult=PENDING`
- `provinceName/cityName/districtName/schoolName` 与教师 JWT 辖区一致
- 同一教师对同一活动重复 B2：返回已有作品或 `WORK_ALREADY_SUBMITTED`（视状态）

**数据库变化（`work`）：**

| 字段 | 期望值 |
|------|--------|
| `activity_id` | `ACTIVITY_ID` |
| `teacher_id` | `teacher_a` ID |
| `current_step` | `SCHOOL` |
| `current_status` | `DRAFT` |
| `final_result` | `PENDING` |
| `deleted` | `0` |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 作品报名 | `/works/enroll/{ACTIVITY_ID}` | 进入报名页；显示草稿状态 |
| 我的作品 | `/works` | 列表新增一条；状态文案「草稿，待提交」 |

---

### 阶段 C：教师上传材料（流程 3）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| C1 | POST | `/api/works/{WORK_ID}/files/upload` | `teacher_a` |

`multipart/form-data`，字段名 `file`，示例 PDF。

**关键断言：**

- `code=0`，返回 `fileName`、`fileUrl`（预签名 URL，短期有效）
- `work_file` 新增记录，`deleted=0`
- 库中 `file_url` 存 **objectKey**（形如 `works/{WORK_ID}/...`），非永久公开地址
- 他人（`teacher_other`）上传同一 `WORK_ID` → `40302`（SCOPE_ACCESS_DENIED）

**数据库变化（`work_file`）：**

| 字段 | 期望值 |
|------|--------|
| `work_id` | `WORK_ID` |
| `file_url` | `works/{WORK_ID}/<uuid>_<name>` |
| `deleted` | `0` |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 作品报名 | `/works/enroll/{ACTIVITY_ID}` | 材料列表出现刚上传文件；可预览/下载链接可用 |

---

### 阶段 D：教师提交报名（流程 4）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| D1 | PUT | `/api/works/{WORK_ID}` | `teacher_a` |
| D2 | POST | `/api/works/{WORK_ID}/submit` | `teacher_a` |

**D1 请求体：**

```json
{
  "title": "参赛作品-A",
  "category": "MOCK",
  "duration": 300
}
```

**关键断言：**

- D2：`code=0`，`currentStatus=SUBMITTED`，`currentStep` 仍为 `SCHOOL`
- 无附件时提交 → 校验失败
- `DRAFT` 以外状态提交 → `WORK_NOT_EDITABLE` / `INVALID_STATUS`

**数据库变化（`work`）：**

| 字段 | 变更 |
|------|------|
| `current_status` | `DRAFT` → `SUBMITTED` |
| `title` 等 | 已保存 |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 我的作品 | `/works` | 状态「已提交，等待学校审核」；操作变为「查看详情」 |

---

### 阶段 E–H：四级资格审核通过（流程 5–8）

每级审核固定模式：

| 步骤 | 方法 | 路径 |
|------|------|------|
| 列表 | GET | `/api/admin/work-reviews?activityId={ACTIVITY_ID}` |
| 通过 | POST | `/api/admin/work-reviews/{WORK_ID}/approve` |

| 阶段 | 角色 | 审核前 `(step, status)` | 审核后 `(step, status)` |
|------|------|-------------------------|-------------------------|
| E 校级 | `school_admin` | `(SCHOOL, SUBMITTED)` | `(DISTRICT, SUBMITTED)` |
| F 区级 | `district_admin` | `(DISTRICT, SUBMITTED)` | `(CITY, SUBMITTED)` |
| G 市级 | `city_admin` | `(CITY, SUBMITTED)` | `(PROVINCE, SUBMITTED)` |
| H 省级 | `province_admin` | `(PROVINCE, SUBMITTED)` | `(SCORE_DISTRICT, SUBMITTED)` |

**关键断言（每级）：**

- 列表仅包含本辖区、本 step、`SUBMITTED` 作品
- `approve` 返回 `code=0`，`currentStep` 推进一级
- `currentStatus` 保持 `SUBMITTED`（**不是** `APPROVED`）
- `review_record` 新增一条：`review_level` 对应当前审核 step，`result=APPROVED`，`final_score IS NULL`
- 跨 step 审核（如校管在 `DISTRICT` 阶段点通过）→ `REVIEW_STEP_MISMATCH`
- `DRAFT` / `REVISION_REQUIRED` 作品不出现在审核列表且不可 approve

**数据库变化（每级通过后 `work`）：**

| 阶段后 | `current_step` | `current_status` | `final_result` |
|--------|----------------|------------------|----------------|
| E | `DISTRICT` | `SUBMITTED` | `PENDING` |
| F | `CITY` | `SUBMITTED` | `PENDING` |
| G | `PROVINCE` | `SUBMITTED` | `PENDING` |
| H | `SCORE_DISTRICT` | `SUBMITTED` | `PENDING` |

**前端验证点：**

| 页面 | 路径 | 角色 | 验证 |
|------|------|------|------|
| 报名信息管理 | `/admin/work-reviews` | 各级 ADMIN | 仅本级待审作品；通过后列表减少 |
| 我的作品 | `/works` | `teacher_a` | 文案随 step 变化（如「学校已通过，等待区级审核」） |

---

### 阶段 I：区县打分晋级（流程 9）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| I1 | GET | `/api/admin/scores/works?activityId={ACTIVITY_ID}` | `district_reviewer_1` |
| I2 | GET | `/api/admin/scores/works/{WORK_ID}/scorers` | `district_reviewer_1` |
| I3 | POST | `/api/admin/scores/works/{WORK_ID}/review` | `district_reviewer_1` |
| I4 | POST | `/api/admin/scores/works/{WORK_ID}/review` | `district_reviewer_2` |

**I3/I4 请求体（示例，使均分 > 60）：**

```json
{ "manualScore": 70, "aiScore": 0 }
```

```json
{ "manualScore": 65, "aiScore": 0 }
```

均分 = 67.50 > 60（实现为 **严格大于** `60`）。

**关键断言：**

- I1：列表含 `WORK_ID`，且该打分员尚未打过分
- I2：`requiredCount=2`，`completedCount` 随提交递增
- I3：`allCompleted=false`，`message` 含 `待其他打分员（1/2）`
- I4：`allCompleted=true`；`currentStep=SCORE_CITY`，`finalResult=PENDING`，`finalScore≈67.50`
- `review_record` 本层 2 条：`final_score` 非空，`result` 最终更新为 `PROMOTED`
- 第三名本区打分员再提交 → `40016`（SCORE_ALREADY_SUBMITTED）

**数据库变化（`work`）：**

| 字段 | 期望值 |
|------|--------|
| `current_step` | `SCORE_CITY` |
| `current_status` | `SUBMITTED` |
| `final_result` | `PENDING`（晋级后清空中间态） |
| `final_score` | `67.50` |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 打分 | `/admin/scores` | 区评员可见待打分作品；提交后进度 `(2/2)`；完成后作品从待办列表消失 |

---

### 阶段 J：市级打分晋级（流程 10）

与阶段 I 相同模式，角色换为 `city_reviewer_1/2`，作品 step 为 `SCORE_CITY`。

**分数示例：** `72` + `68` → 均分 `70.00` > 60。

**关键断言（完成后）：**

- `currentStep=SCORE_PROVINCE`
- `finalResult=PENDING`
- 市级 `review_record.result=PROMOTED`

**前端验证点：** `/admin/scores`（市评员）；教师端文案「市级评分已通过，等待省级评分」。

---

### 阶段 K：省级打分获奖（流程 11）

角色：`province_reviewer_1/2`，作品 step 为 `SCORE_PROVINCE`。

**分数示例：** `95` + `92` → 均分 `93.50` > 90。

**关键断言（最后一名省评员提交后）：**

- `currentStep=COMPLETED`
- `currentStatus=APPROVED`
- `finalResult=AWARD`
- `finalScore≈93.50`
- 省级 `review_record.result=AWARD`

**数据库变化（`work` 终态）：**

| 字段 | 期望值 |
|------|--------|
| `current_step` | `COMPLETED` |
| `current_status` | `APPROVED` |
| `final_result` | `AWARD` |
| `final_score` | `93.50` |

**前端验证点：** `/admin/scores`（省评员）；教师端见流程 12。

---

### 阶段 L：教师端显示报名成功（流程 12）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| L1 | GET | `/api/works/mine` | `teacher_a` |
| L2 | GET | `/api/works/{WORK_ID}` | `teacher_a` |

**关键断言：**

- `finalResult=AWARD`，`currentStep=COMPLETED`，`currentStatus=APPROVED`
- 未获奖路径（`NOT_AWARDED` + `SUBMITTED`）**不应**被理解为「报名成功/已通过」

**省管已报名列表（可选）：**

```http
GET /api/admin/work-reviews/enrolled?activityId={ACTIVITY_ID}
```

- 获奖作品应出现在列表（筛选 `current_status=APPROVED`）

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 我的作品 | `/works` | 状态标签文案：**「评审完成，恭喜获奖」**（`getTeacherWorkStatusLabel`） |
| 作品详情 | `/works/enroll/{ACTIVITY_ID}` | 只读；不可再编辑 |

> 注：`/admin/enrolled` 前端当前为占位页；以 API `GET /admin/work-reviews/enrolled` 为准做接口断言。

---

### 阶段 M：公示页展示晋级与获奖（流程 13）

| 序号 | 方法 | 路径 | 角色 |
|------|------|------|------|
| M1 | GET | `/api/notices/promotion-summary?tab=DISTRICT_PROMOTED&activityId={ACTIVITY_ID}` | 任意已登录 |
| M2 | GET | `/api/notices/promotion-summary?tab=CITY_PROMOTED&activityId={ACTIVITY_ID}` | 同上 |
| M3 | GET | `/api/notices/promotion-summary?tab=PROVINCE_AWARD&activityId={ACTIVITY_ID}` | 同上 |

**关键断言：**

- M1：列表含 `WORK_ID`，`averageScore≈67.50`，晋级层级 `SCORE_DISTRICT`
- M2：列表含 `WORK_ID`，市级晋级记录
- M3：列表含 `WORK_ID`，省级获奖记录
- 跨辖区用户（`teacher_other`）不应看到南山区作品（列表为空或无权数据）
- `review_record` 中 `result` 与 tab 对应：`PROMOTED` / `AWARD`

**数据库依据（`review_record`）：**

| tab | `review_level` | `result` |
|-----|----------------|----------|
| `DISTRICT_PROMOTED` | `SCORE_DISTRICT` | `PROMOTED` |
| `CITY_PROMOTED` | `SCORE_CITY` | `PROMOTED` |
| `PROVINCE_AWARD` | `SCORE_PROVINCE` | `AWARD` |

**前端验证点：**

| 页面 | 路径 | 验证 |
|------|------|------|
| 公示 | `/notices` 或 `/admin/notices` | Tab「区县晋级」「市级晋级」「省级获奖」均可查到该作品 |
| 辖区提示 | 页头 Tag | 显示当前用户辖区 |

---

## 3. 分支路径：审核退回与重新提交（流程 14–15）

建议在**独立活动/作品**上执行，避免污染主路径数据。

### 3.1 接口顺序

| 序号 | 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|------|
| R1 | （B–D） | 创建并提交作品 | `teacher_a` | 得到 `WORK_ID_B` |
| R2 | POST | `/api/admin/work-reviews/{WORK_ID_B}/revision-feedback` | `school_admin` | 退回修改 |
| R3 | PUT | `/api/works/{WORK_ID_B}` | `teacher_a` | 修改内容 |
| R4 | POST | `/api/works/{WORK_ID_B}/submit` | `teacher_a` | 再次提交 |
| R5 | POST | `/api/admin/work-reviews/{WORK_ID_B}/approve` | `school_admin` | 验证可继续审核 |

**R2 请求体：**

```json
{ "feedback": "请补充作品说明" }
```

### 3.2 关键断言

| 检查点 | 期望 |
|--------|------|
| R2 后 `work` | `current_step=SCHOOL`（不变），`current_status=REVISION_REQUIRED` |
| R2 后 `work_revision_feedback` | 新增记录，`round_no=1` |
| R2 后直接 approve | `WORK_NOT_REVIEWABLE`（仅 `SUBMITTED` 可审） |
| R4 后 | `current_status=SUBMITTED`，step 仍为 `SCHOOL` |
| 审核列表 | `REVISION_REQUIRED` 期间**不出现**；重新提交后出现 |

### 3.3 前端验证点

| 页面 | 验证 |
|------|------|
| `/works` | 退回后文案「审核退回，请修改后重新提交」；可「继续编辑」 |
| `/works/enroll/{id}` | 展示修改意见（若前端有缓存/接口） |
| `/admin/work-reviews` | 校管可写修改意见；重新提交后作品回到待审列表 |

---

## 4. 负向安全用例（流程 16–18）

### 4.1 跨辖区访问被拒绝（流程 16）

| # | 操作 | 角色 | 期望 |
|---|------|------|------|
| N1 | `GET /api/admin/work-reviews` | `school_admin`（其他学校） | 列表不含南山区 `WORK_ID` |
| N2 | `POST .../work-reviews/{WORK_ID}/approve` | 跨区校管 | `40302` SCOPE_ACCESS_DENIED |
| N3 | `POST .../scores/works/{WORK_ID}/review` | 广州市 `DISTRICT_REVIEWER` | `40302` 或 `REVIEW_STEP_MISMATCH` |
| N4 | `GET /api/works/{WORK_ID}` | `teacher_other` | `40302` |
| N5 | `GET /api/notices/promotion-summary?tab=PROVINCE_AWARD` | 外省用户 | 列表不含本作品 |

**前端验证点：** 跨角色访问 `/admin/work-reviews` 或 `/admin/scores` → 跳转 `/403`。

### 4.2 重复打分被拒绝（流程 17）

| # | 操作 | 期望 |
|---|------|------|
| N6 | 同一 `DISTRICT_REVIEWER` 对同一 `WORK_ID` 第二次 `POST .../review` | `40016` SCORE_ALREADY_SUBMITTED |
| N7 | 完成后该作品自打分待办列表消失 | `GET /admin/scores/works` 不含该 `WORK_ID` |

### 4.3 未配置辖区用户不能操作（流程 18）

| # | 操作 | 角色 | 期望 |
|---|------|------|------|
| N8 | `GET /api/admin/work-reviews` | `admin_no_scope` | `40301` SCOPE_NOT_CONFIGURED |
| N9 | `GET /api/admin/scores/works` | 辖区为空的 `DISTRICT_REVIEWER` | `40301` |
| N10 | `POST /api/works/draft` | 辖区为空的 `TEACHER` | `40301` |

**前端验证点：** 登录后进入管理端，相关接口报错提示「账号辖区未配置」。

---

## 5. 全流程状态机对照表

便于自动化断言 `work` 表：

| 里程碑 | current_step | current_status | final_result |
|--------|--------------|----------------|--------------|
| 创建草稿 | SCHOOL | DRAFT | PENDING |
| 教师提交 | SCHOOL | SUBMITTED | PENDING |
| 校审通过 | DISTRICT | SUBMITTED | PENDING |
| 区审通过 | CITY | SUBMITTED | PENDING |
| 市审通过 | PROVINCE | SUBMITTED | PENDING |
| 省审通过 | SCORE_DISTRICT | SUBMITTED | PENDING |
| 区评晋级 | SCORE_CITY | SUBMITTED | PENDING |
| 市评晋级 | SCORE_PROVINCE | SUBMITTED | PENDING |
| 省评获奖 | COMPLETED | APPROVED | AWARD |
| 审核退回 | SCHOOL | REVISION_REQUIRED | PENDING |
| 区评淘汰（均分≤60） | COMPLETED | SUBMITTED | ELIMINATED |
| 省评未获奖（均分≤90） | COMPLETED | SUBMITTED | NOT_AWARDED |

---

## 6. SQL 校验脚本（每阶段可执行）

将 `:WORK_ID`、`:ACTIVITY_ID` 替换为实际值。

```sql
-- 作品主状态
SELECT id, activity_id, teacher_id, current_step, current_status, final_result, final_score, deleted
FROM work WHERE id = :WORK_ID;

-- 审核通过记录（无分数）
SELECT id, work_id, review_level, reviewer_id, result, final_score
FROM review_record
WHERE work_id = :WORK_ID AND final_score IS NULL
ORDER BY id;

-- 打分记录（有分数）
SELECT id, work_id, review_level, reviewer_id, manual_score, final_score, result
FROM review_record
WHERE work_id = :WORK_ID AND final_score IS NOT NULL
ORDER BY review_level, id;

-- 修改意见
SELECT work_id, review_step, round_no, feedback, reviewer_id
FROM work_revision_feedback
WHERE work_id = :WORK_ID
ORDER BY round_no;

-- 材料
SELECT id, work_id, file_name, file_url, deleted
FROM work_file
WHERE work_id = :WORK_ID;

-- 活动
SELECT id, title, status, created_by, signup_start_time, signup_end_time
FROM activity WHERE id = :ACTIVITY_ID;
```

---

## 7. 推荐测试套件拆分（TDD 落地）

| 套件 ID | 名称 | 覆盖流程 | 建议工具 |
|---------|------|----------|----------|
| TP-01 | `ActivityPublishTest` | 1 | JUnit + MockMvc |
| TP-02 | `WorkEnrollSubmitTest` | 2–4 | JUnit + MockMvc |
| TP-03 | `WorkFileUploadTest` | 3 | JUnit + Testcontainers MinIO |
| TP-04 | `ReviewChainTest` | 5–8, 14–15 | JUnit 集成 |
| TP-05 | `ScorePromotionTest` | 9–11 | JUnit 集成 + 多用户 Token |
| TP-06 | `NoticePromotionSummaryTest` | 13 | JUnit + SQL fixture |
| TP-07 | `ScopeIsolationTest` | 16, 18 | JUnit 参数化 |
| TP-08 | `DuplicateScoreTest` | 17 | JUnit |
| TP-09 | `WorkflowE2E` | 1–13 | Playwright |

**Prove-It 回归：** 每个已修复缺陷应新增一个 TP 子用例，失败时保留最小复现数据（单条 `work` + 对应用户）。

---

## 8. 执行检查清单

- [ ] 迁移脚本已全部执行
- [ ] 测试账号与 6 名打分员已入库且 `status=1`
- [ ] `JWT_SECRET`、`DB_PASSWORD`、MinIO 配置正确
- [ ] 主路径 TP-01 → TP-06 全部通过
- [ ] 分支 TP-04（退回修改）通过
- [ ] 负向 TP-07、TP-08 通过
- [ ] Playwright TP-09 与接口结果一致
- [ ] 失败用例已附 SQL 快照与请求/响应日志

---

## 9. 与规格差异说明（测试时注意）

| 项 | workflow-spec | 当前实现 | 测试处理 |
|----|---------------|----------|----------|
| 活动管理角色 | 规格矩阵写多级 ADMIN | 后端仅 `PROVINCE_ADMIN` 可 CRUD | 用例 1 以省管为准；他级 ADMIN 访问返回 403 |
| 晋级阈值 | 活动可配置 | 硬编码 `>60` / `>90` | 按现实现写断言；边界 `60.00`/`90.00` 判淘汰 |
| 已报名页 | 省管查看已通过作品 | 前端 `/admin/enrolled` 为占位 | 接口 `GET /admin/work-reviews/enrolled` 为主断言 |
| 活动辖区 | 活动应有 scope | `activity` 表暂无辖区字段 | 通过 `created_by` 省内过滤间接验证 |

---

## 10. 参考 API 索引

| 模块 | 基础路径 |
|------|----------|
| 认证 | `/api/auth/login` |
| 活动（教师） | `/api/activities` |
| 活动（省管） | `/api/admin/activities` |
| 作品 | `/api/works` |
| 材料 | `/api/works/{workId}/files` |
| 资格审核 | `/api/admin/work-reviews` |
| 打分 | `/api/admin/scores` |
| 公示 | `/api/notices` |

完整规格见 [`workflow-spec.md`](./workflow-spec.md)。
