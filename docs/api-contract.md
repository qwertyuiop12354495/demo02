# API 接口契约：活动报名系统

> 基于 [`docs/project-context.md`](./project-context.md)、[`docs/requirement.md`](./requirement.md)、[`docs/database-design.md`](./database-design.md) 设计的 REST API 契约。  
> 当前阶段：**Contract First** — 仅文档，不含实现代码。

---

## 1. 通用约定

### 1.1 Base URL

```
/api
```

完整示例：`http://localhost:8080/api/auth/login`

### 1.2 统一响应格式

所有接口（含成功与业务失败）响应体均为 JSON，结构固定：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | number | 业务状态码；`200` 表示成功，非 `200` 表示失败 |
| `message` | string | 人类可读提示；成功时通常为 `"success"` |
| `data` | object / array / null | 业务数据；失败时一般为 `null` |

### 1.3 HTTP 状态码与业务码关系

| HTTP 状态码 | 典型业务 `code` | 说明 |
|-------------|-----------------|------|
| 200 | `200` | 请求成功 |
| 400 | `40001`–`40007` | 业务规则或参数错误 |
| 401 | `40100` | 未登录或 Token 无效 |
| 403 | `40300` | 已登录但无权限 |
| 404 | `40400` | 资源不存在 |
| 500 | `50000` | 服务器内部错误 |

> 前端应以响应体 `code === 200` 判断业务成功与否；HTTP 状态码用于传输层语义。

### 1.4 认证方式

需登录接口在请求头携带 JWT：

```
Authorization: Bearer <token>
Content-Type: application/json
```

登录接口返回的 `data.token` 即为后续请求的 Token。

### 1.5 时间格式

所有时间字段使用 **ISO 8601** 字符串，无时区时按服务器本地时间解释：

```
2026-06-07T10:00:00
2026-06-07T10:00:00.000
```

### 1.6 分页约定

列表接口 `data` 统一结构：

```json
{
  "list": [],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

| Query 参数 | 类型 | 默认 | 说明 |
|------------|------|------|------|
| `page` | integer | `1` | 页码，从 1 开始 |
| `pageSize` | integer | `10` | 每页条数，最大 `100` |

### 1.7 枚举值

**用户角色 `role`：** `USER` | `ADMIN`

**活动状态 `status`（活动）：** `DRAFT` | `PUBLISHED` | `OFFLINE`

**报名状态 `status`（报名）：** `PENDING` | `APPROVED` | `REJECTED` | `CANCELLED`

**审核动作 `action`：** `APPROVE` | `REJECT`

### 1.8 全局错误码

| code | message（示例） | 说明 |
|------|-----------------|------|
| `200` | success | 成功 |
| `40001` | 您已报名该活动 | 重复报名（R1，`uk_user_activity`） |
| `40002` | 活动报名已截止 | 超过 `signupEndTime`（R2） |
| `40003` | 报名尚未开始 | 未到 `signupStartTime`（R2） |
| `40004` | 活动名额已满 | 已通过人数达上限（R3） |
| `40005` | 活动已下架 | 活动非上架状态（R4） |
| `40006` | 当前状态不允许此操作 | 状态机非法流转 |
| `40007` | 参数校验失败 | 字段缺失、格式错误等 |
| `40100` | 未登录或登录已过期 | Token 缺失/无效/过期 |
| `40300` | 无权限访问 | 非管理员访问管理接口等 |
| `40400` | 资源不存在 | 活动/报名不存在或不可见 |
| `50000` | 系统繁忙，请稍后重试 | 未预期服务端异常 |

---

## 2. 接口清单总览

| # | 接口名称 | 方法 | 路径 | 权限 |
|---|----------|------|------|------|
| 1 | 用户登录 | POST | `/api/auth/login` | 游客 |
| 2 | 活动分页查询 | GET | `/api/activities` | 已登录用户 |
| 3 | 活动详情 | GET | `/api/activities/{id}` | 已登录用户 |
| 4 | 用户报名 | POST | `/api/registrations` | 已登录用户 |
| 5 | 用户取消报名 | PATCH | `/api/registrations/{id}/cancel` | 已登录用户（本人） |
| 6 | 我的报名列表 | GET | `/api/registrations/mine` | 已登录用户 |
| 7 | 管理员新增活动 | POST | `/api/admin/activities` | 管理员 |
| 8 | 管理员编辑活动 | PUT | `/api/admin/activities/{id}` | 管理员 |
| 9 | 管理员上下架活动 | PATCH | `/api/admin/activities/{id}/status` | 管理员 |
| 10 | 管理员查看报名名单 | GET | `/api/admin/activities/{activityId}/registrations` | 管理员 |
| 11 | 管理员审核报名 | PATCH | `/api/admin/registrations/{id}/audit` | 管理员 |

---

## 3. 接口详情

---

### 3.1 用户登录

| 项 | 内容 |
|----|------|
| **接口名称** | 用户登录 |
| **请求方式** | `POST` |
| **请求路径** | `/api/auth/login` |
| **权限说明** | 游客可访问；登录成功后返回 JWT，管理员与普通用户共用此接口，通过 `data.user.role` 区分跳转 |

#### 请求参数

**Body（JSON）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 登录名，1–50 字符 |
| `password` | string | 是 | 密码，6–64 字符 |

**请求示例：**

```json
{
  "username": "user1",
  "password": "password"
}
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 2,
      "username": "user1",
      "nickname": "测试用户",
      "role": "USER"
    }
  }
}
```

**失败示例：**

```json
{
  "code": 40007,
  "message": "用户名或密码不能为空",
  "data": null
}
```

```json
{
  "code": 40007,
  "message": "用户名或密码错误",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 登录成功 |
| `40007` | 参数为空、用户名不存在或密码错误 |
| `50000` | 系统错误 |

---

### 3.2 活动分页查询

| 项 | 内容 |
|----|------|
| **接口名称** | 活动分页查询（用户端） |
| **请求方式** | `GET` |
| **请求路径** | `/api/activities` |
| **权限说明** | 需登录（`USER` 或 `ADMIN`）；仅返回 `status = PUBLISHED` 的已上架活动 |

#### 请求参数

**Query：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `page` | integer | 否 | `1` | 页码 |
| `pageSize` | integer | 否 | `10` | 每页条数，最大 100 |
| `keyword` | string | 否 | — | 标题模糊搜索（可选扩展） |

**请求示例：**

```
GET /api/activities?page=1&pageSize=10
Authorization: Bearer <token>
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "title": "2026 春季技术分享会",
        "location": "线上腾讯会议",
        "eventStartTime": "2026-06-14T14:00:00",
        "eventEndTime": "2026-06-14T17:00:00",
        "registrationStartTime": "2026-06-07T10:00:00",
        "registrationDeadline": "2026-06-12T18:00:00",
        "maxParticipants": 100,
        "approvedCount": 12,
        "remainingSlots": 88,
        "canRegister": true,
        "registerDisabledReason": null
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

**列表项字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `canRegister` | boolean | 当前用户是否可报名（综合时间、名额、是否已报名等） |
| `registerDisabledReason` | string \| null | 不可报名时原因，如 `"已报名"`、`"名额已满"` |

#### 错误码

| code | 说明 |
|------|------|
| `200` | 查询成功 |
| `40007` | `page`/`pageSize` 非法 |
| `40100` | 未登录 |
| `50000` | 系统错误 |

---

### 3.3 活动详情

| 项 | 内容 |
|----|------|
| **接口名称** | 活动详情（用户端） |
| **请求方式** | `GET` |
| **请求路径** | `/api/activities/{id}` |
| **权限说明** | 需登录；仅可查看 `PUBLISHED` 活动；下架或草稿活动返回 `40400` |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 活动 ID |

**请求示例：**

```
GET /api/activities/1
Authorization: Bearer <token>
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "2026 春季技术分享会",
    "description": "面向开发者的技术交流活动，包含主题演讲与自由讨论环节。",
    "location": "线上腾讯会议",
    "eventStartTime": "2026-06-14T14:00:00",
    "eventEndTime": "2026-06-14T17:00:00",
    "registrationStartTime": "2026-06-07T10:00:00",
    "registrationDeadline": "2026-06-12T18:00:00",
    "maxParticipants": 100,
    "approvedCount": 12,
    "remainingSlots": 88,
    "status": "PUBLISHED",
    "canRegister": true,
    "registerDisabledReason": null,
    "myRegistration": {
      "id": 10,
      "status": "PENDING",
      "applyTime": "2026-06-07T11:30:00"
    }
  }
}
```

| 字段 | 说明 |
|------|------|
| `myRegistration` | 当前用户对该活动的报名记录；未报名时为 `null` |

**失败（HTTP 404）：**

```json
{
  "code": 40400,
  "message": "活动不存在",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 查询成功 |
| `40100` | 未登录 |
| `40400` | 活动不存在或未上架 |
| `50000` | 系统错误 |

---

### 3.4 用户报名

| 项 | 内容 |
|----|------|
| **接口名称** | 用户报名 |
| **请求方式** | `POST` |
| **请求路径** | `/api/registrations` |
| **权限说明** | 需登录；普通用户与管理员均可报名；仅能为自己报名；后端校验 R1–R4 |

#### 请求参数

**Body（JSON）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `activityId` | long | 是 | 活动 ID |
| `remark` | string | 否 | 用户报名备注，最长 500 字符（对应 DB `apply_remark`） |

**请求示例：**

```json
{
  "activityId": 1,
  "remark": "期待参加"
}
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "activityId": 1,
    "activityTitle": "2026 春季技术分享会",
    "userId": 2,
    "status": "PENDING",
    "applyTime": "2026-06-07T11:30:00",
    "auditTime": null,
    "auditRemark": null,
    "remark": "期待参加"
  }
}
```

**失败示例：**

```json
{
  "code": 40001,
  "message": "您已报名该活动",
  "data": null
}
```

```json
{
  "code": 40004,
  "message": "活动名额已满",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 报名成功，状态为 `PENDING` |
| `40001` | 重复报名（`uk_user_activity` 冲突或已有有效记录） |
| `40002` | 报名已截止 |
| `40003` | 报名尚未开始 |
| `40004` | 名额已满 |
| `40005` | 活动已下架或非上架状态 |
| `40007` | `activityId` 缺失或非法 |
| `40100` | 未登录 |
| `40400` | 活动不存在 |
| `50000` | 系统错误 |

---

### 3.5 用户取消报名

| 项 | 内容 |
|----|------|
| **接口名称** | 用户取消报名 |
| **请求方式** | `PATCH` |
| **请求路径** | `/api/registrations/{id}/cancel` |
| **权限说明** | 需登录；仅可取消**本人**报名；仅 `PENDING`、`APPROVED` 可取消；取消后 `status` 变为 `CANCELLED`，已通过时释放名额 |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 报名记录 ID |

**Body：** 无（或空 JSON `{}`）

**请求示例：**

```
PATCH /api/registrations/10/cancel
Authorization: Bearer <token>
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "activityId": 1,
    "status": "CANCELLED",
    "applyTime": "2026-06-07T11:30:00",
    "auditTime": null,
    "auditRemark": null
  }
}
```

**失败示例：**

```json
{
  "code": 40006,
  "message": "当前状态不允许取消报名",
  "data": null
}
```

```json
{
  "code": 40300,
  "message": "无权操作该报名记录",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 取消成功 |
| `40006` | 报名已为 `REJECTED`/`CANCELLED` 等终态 |
| `40100` | 未登录 |
| `40300` | 非本人报名 |
| `40400` | 报名记录不存在 |
| `50000` | 系统错误 |

---

### 3.6 我的报名列表

| 项 | 内容 |
|----|------|
| **接口名称** | 我的报名列表 |
| **请求方式** | `GET` |
| **请求路径** | `/api/registrations/mine` |
| **权限说明** | 需登录；仅返回当前登录用户的报名记录，按 `applyTime` 倒序 |

#### 请求参数

**Query：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `page` | integer | 否 | `1` | 页码 |
| `pageSize` | integer | 否 | `10` | 每页条数 |
| `status` | string | 否 | — | 按报名状态筛选：`PENDING`/`APPROVED`/`REJECTED`/`CANCELLED` |

**请求示例：**

```
GET /api/registrations/mine?page=1&pageSize=10&status=PENDING
Authorization: Bearer <token>
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 10,
        "activityId": 1,
        "activityTitle": "2026 春季技术分享会",
        "activityLocation": "线上腾讯会议",
        "eventStartTime": "2026-06-14T14:00:00",
        "status": "PENDING",
        "applyTime": "2026-06-07T11:30:00",
        "auditTime": null,
        "auditRemark": null,
        "remark": "期待参加"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 查询成功 |
| `40007` | 分页参数或 `status` 枚举非法 |
| `40100` | 未登录 |
| `50000` | 系统错误 |

---

### 3.7 管理员新增活动

| 项 | 内容 |
|----|------|
| **接口名称** | 管理员新增活动 |
| **请求方式** | `POST` |
| **请求路径** | `/api/admin/activities` |
| **权限说明** | 需登录且 `role = ADMIN`；创建后默认 `status = DRAFT`；`createdBy` 为当前管理员 |

#### 请求参数

**Body（JSON）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | string | 是 | 活动标题，最长 200 字符 |
| `description` | string | 否 | 活动描述 |
| `location` | string | 否 | 地点，最长 200 字符 |
| `eventStartTime` | string | 否 | 活动开始时间（ISO 8601） |
| `eventEndTime` | string | 否 | 活动结束时间 |
| `registrationStartTime` | string | 是 | 报名开始时间 |
| `registrationDeadline` | string | 是 | 报名截止时间 |
| `maxParticipants` | integer | 是 | 人数上限，≥ 1 |

**请求示例：**

```json
{
  "title": "2026 夏季工作坊",
  "description": "动手实践工作坊",
  "location": "北京市海淀区",
  "eventStartTime": "2026-07-01T09:00:00",
  "eventEndTime": "2026-07-01T17:00:00",
  "registrationStartTime": "2026-06-10T00:00:00",
  "registrationDeadline": "2026-06-28T23:59:59",
  "maxParticipants": 50
}
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "title": "2026 夏季工作坊",
    "description": "动手实践工作坊",
    "location": "北京市海淀区",
    "eventStartTime": "2026-07-01T09:00:00",
    "eventEndTime": "2026-07-01T17:00:00",
    "registrationStartTime": "2026-06-10T00:00:00",
    "registrationDeadline": "2026-06-28T23:59:59",
    "maxParticipants": 50,
    "approvedCount": 0,
    "status": "DRAFT",
    "createdBy": 1,
    "createdAt": "2026-06-07T12:00:00",
    "updatedAt": "2026-06-07T12:00:00"
  }
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 创建成功 |
| `40007` | 参数校验失败（如时间为空、`maxParticipants < 1`、`registrationStartTime >= registrationDeadline`） |
| `40100` | 未登录 |
| `40300` | 非管理员 |
| `50000` | 系统错误 |

---

### 3.8 管理员编辑活动

| 项 | 内容 |
|----|------|
| **接口名称** | 管理员编辑活动 |
| **请求方式** | `PUT` |
| **请求路径** | `/api/admin/activities/{id}` |
| **权限说明** | 需登录且 `role = ADMIN`；全量更新活动基础信息；`approvedCount` 不可通过此接口修改 |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 活动 ID |

**Body（JSON）：** 与「新增活动」相同，所有业务字段均需传入。

**请求示例：**

```json
{
  "title": "2026 夏季工作坊（更新）",
  "description": "动手实践工作坊，含答疑环节",
  "location": "北京市海淀区",
  "eventStartTime": "2026-07-01T09:00:00",
  "eventEndTime": "2026-07-01T18:00:00",
  "registrationStartTime": "2026-06-10T00:00:00",
  "registrationDeadline": "2026-06-30T23:59:59",
  "maxParticipants": 60
}
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "title": "2026 夏季工作坊（更新）",
    "description": "动手实践工作坊，含答疑环节",
    "location": "北京市海淀区",
    "eventStartTime": "2026-07-01T09:00:00",
    "eventEndTime": "2026-07-01T18:00:00",
    "registrationStartTime": "2026-06-10T00:00:00",
    "registrationDeadline": "2026-06-30T23:59:59",
    "maxParticipants": 60,
    "approvedCount": 0,
    "status": "DRAFT",
    "createdBy": 1,
    "createdAt": "2026-06-07T12:00:00",
    "updatedAt": "2026-06-07T13:00:00"
  }
}
```

**失败示例：**

```json
{
  "code": 40007,
  "message": "人数上限不能小于已通过人数",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 更新成功 |
| `40007` | 参数校验失败；`maxParticipants` 小于当前 `approvedCount` |
| `40100` | 未登录 |
| `40300` | 非管理员 |
| `40400` | 活动不存在 |
| `50000` | 系统错误 |

---

### 3.9 管理员上下架活动

| 项 | 内容 |
|----|------|
| **接口名称** | 管理员上下架活动 |
| **请求方式** | `PATCH` |
| **请求路径** | `/api/admin/activities/{id}/status` |
| **权限说明** | 需登录且 `role = ADMIN`；用于上架（`PUBLISHED`）或下架（`OFFLINE`）；草稿可首次上架 |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 活动 ID |

**Body（JSON）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | 是 | 目标状态：`PUBLISHED`（上架）或 `OFFLINE`（下架） |

**合法状态流转：**

| 当前状态 | 允许目标 |
|----------|----------|
| `DRAFT` | `PUBLISHED` |
| `PUBLISHED` | `OFFLINE` |
| `OFFLINE` | `PUBLISHED` |

**请求示例：**

```json
{
  "status": "PUBLISHED"
}
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "title": "2026 夏季工作坊",
    "status": "PUBLISHED",
    "updatedAt": "2026-06-07T14:00:00"
  }
}
```

**失败示例：**

```json
{
  "code": 40006,
  "message": "不允许的状态变更",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 状态变更成功 |
| `40006` | 非法状态流转（如 `DRAFT` → `OFFLINE`） |
| `40007` | `status` 缺失或枚举非法 |
| `40100` | 未登录 |
| `40300` | 非管理员 |
| `40400` | 活动不存在 |
| `50000` | 系统错误 |

---

### 3.10 管理员查看报名名单

| 项 | 内容 |
|----|------|
| **接口名称** | 管理员查看报名名单 |
| **请求方式** | `GET` |
| **请求路径** | `/api/admin/activities/{activityId}/registrations` |
| **权限说明** | 需登录且 `role = ADMIN`；查看指定活动下全部报名记录 |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `activityId` | long | 是 | 活动 ID |

**Query：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `page` | integer | 否 | `1` | 页码 |
| `pageSize` | integer | 否 | `10` | 每页条数 |
| `status` | string | 否 | — | 报名状态筛选 |

**请求示例：**

```
GET /api/admin/activities/1/registrations?page=1&pageSize=20&status=PENDING
Authorization: Bearer <token>
```

#### 返回示例

**成功（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activity": {
      "id": 1,
      "title": "2026 春季技术分享会",
      "maxParticipants": 100,
      "approvedCount": 12,
      "pendingCount": 5
    },
    "list": [
      {
        "id": 10,
        "userId": 2,
        "username": "user1",
        "nickname": "测试用户",
        "status": "PENDING",
        "applyTime": "2026-06-07T11:30:00",
        "auditTime": null,
        "auditRemark": null,
        "remark": "期待参加",
        "auditedBy": null
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 查询成功 |
| `40007` | 分页或 `status` 参数非法 |
| `40100` | 未登录 |
| `40300` | 非管理员 |
| `40400` | 活动不存在 |
| `50000` | 系统错误 |

---

### 3.11 管理员审核报名

| 项 | 内容 |
|----|------|
| **接口名称** | 管理员审核报名 |
| **请求方式** | `PATCH` |
| **请求路径** | `/api/admin/registrations/{id}/audit` |
| **权限说明** | 需登录且 `role = ADMIN`；仅可审核 `PENDING` 状态；通过时校验名额（R3）并递增 `approvedCount` |

#### 请求参数

**Path：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 报名记录 ID |

**Body（JSON）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `action` | string | 是 | `APPROVE`（通过）或 `REJECT`（拒绝） |
| `auditRemark` | string | 否 | 审核备注；拒绝时建议填写原因，最长 500 字符 |

**请求示例（通过）：**

```json
{
  "action": "APPROVE"
}
```

**请求示例（拒绝）：**

```json
{
  "action": "REJECT",
  "auditRemark": "名额优先留给内部成员"
}
```

#### 返回示例

**成功 — 审核通过（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "activityId": 1,
    "userId": 2,
    "status": "APPROVED",
    "applyTime": "2026-06-07T11:30:00",
    "auditTime": "2026-06-07T15:00:00",
    "auditRemark": null,
    "auditedBy": 1
  }
}
```

**成功 — 审核拒绝（HTTP 200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "activityId": 1,
    "userId": 2,
    "status": "REJECTED",
    "applyTime": "2026-06-07T11:30:00",
    "auditTime": "2026-06-07T15:00:00",
    "auditRemark": "名额优先留给内部成员",
    "auditedBy": 1
  }
}
```

**失败示例：**

```json
{
  "code": 40004,
  "message": "活动名额已满，无法审核通过",
  "data": null
}
```

```json
{
  "code": 40006,
  "message": "仅待审核状态的报名可审核",
  "data": null
}
```

#### 错误码

| code | 说明 |
|------|------|
| `200` | 审核成功 |
| `40004` | 审核通过时名额已满（R3） |
| `40006` | 非 `PENDING` 状态、重复审核 |
| `40007` | `action` 缺失或非法 |
| `40100` | 未登录 |
| `40300` | 非管理员 |
| `40400` | 报名记录不存在 |
| `50000` | 系统错误 |

---

## 4. 数据库字段与 API 字段对照

| 数据库（`activity`） | API 字段 |
|----------------------|----------|
| `start_time` | `eventStartTime` |
| `end_time` | `eventEndTime` |
| `signup_start_time` | `registrationStartTime` |
| `signup_end_time` | `registrationDeadline` |
| `max_count` | `maxParticipants` |
| `current_count` | `approvedCount` |

| 数据库（`activity_registration`） | API 字段 |
|-----------------------------------|----------|
| `apply_time` | `applyTime` |
| `audit_time` | `auditTime` |
| `audit_remark` | `auditRemark` |
| `apply_remark` | `remark` |

---

## 5. 契约变更说明

| 项 | `requirement.md` | 本文档 |
|----|------------------|--------|
| 成功业务码 | `code: 0` | **`code: 200`**（按最新约定） |
| 重复报名 | 应用层 + 可选 DB 策略 | **`uk_user_activity` 数据库唯一约束**，拒绝/取消后不可再次 `INSERT` |

后续若调整成功码或唯一约束策略，须同步更新本文档与前端拦截器。

---

## 6. 文档关系

```
docs/project-context.md   → 业务规则 R1–R7
docs/requirement.md       → 功能与接口清单
docs/database-design.md   → 表结构与索引
docs/api-contract.md      → 本文档（前后端联调契约）
```

---

*最后更新：2026-06-07 | 状态：待 Review*
