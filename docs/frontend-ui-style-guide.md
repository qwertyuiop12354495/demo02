# 前端视觉规范：活动报名系统

> 技术栈：**Vue 3 + Element Plus**  
> 适用范围：`frontend/src` 全部用户端与管理端页面  
> 风格定位：**简洁、清爽、偏管理系统** — 信息密度适中、操作路径清晰、无装饰性渐变与重阴影  
> 关联文档：[`docs/project-context.md`](./project-context.md)、[`docs/api-contract.md`](./api-contract.md)

---

## 1. 设计原则

### 1.1 核心原则

| 原则 | 说明 |
|------|------|
| 一致性 | 同类元素（标题、筛选、表格、按钮、Tag）在全站使用同一套规则 |
| 清晰性 | 状态、操作、主次信息一眼可辨；不依赖颜色单独传达含义 |
| 效率 | 管理端优先表格与批量操作；用户端优先阅读与单步转化 |
| 克制 | 扁平为主、轻阴影、有限圆角；避免「AI 审美」（大面积紫渐变、超大圆角、厚重阴影） |
| 可访问 | 满足 WCAG 2.1 AA 基本要求：对比度、键盘可达、表单标签、焦点可见 |

### 1.2 用户端与管理端差异

| 维度 | 用户端（`/activities` 等） | 管理端（`/admin` 等） |
|------|---------------------------|------------------------|
| 内容载体 | 列表以表格为主，详情以描述区为主；移动端可降级为卡片 | 以表格为核心 |
| 信息密度 | 中等，留白略多 | 偏高，列信息完整 |
| 主操作 | 实心主按钮（报名） | 实心主按钮（新增）+ 表格内 link / 下拉 |
| 内容宽度 | 列表全宽；详情 `max-width: 880px` 居中 | 全宽 |

两端的**色板、间距、组件规格保持一致**，仅布局密度与操作呈现方式区分。

---

## 2. 设计令牌（Design Tokens）

实现时通过 `src/styles/tokens.css` 定义 CSS 变量，并映射到 Element Plus 主题变量（`--el-color-primary` 等）。

### 2.1 色彩

#### 品牌与功能色

| 令牌名 | 色值 | 用途 |
|--------|------|------|
| `--color-primary` | `#1677FF` | 主色：主按钮、链接、选中态、焦点环 |
| `--color-primary-hover` | `#4096FF` | 主色悬停 |
| `--color-primary-active` | `#0958D9` | 主色按下 |
| `--color-primary-light` | `#E6F4FF` | 主色浅底（提示条、选中行背景） |
| `--color-success` | `#52C41A` | 成功态 |
| `--color-warning` | `#FAAD14` | 警告 / 待处理 |
| `--color-danger` | `#FF4D4F` | 危险 / 拒绝 / 已满 / 截止 |
| `--color-info` | `#8C8C8C` | 中性 / 已取消 / 草稿 |

#### 中性色

| 令牌名 | 色值 | 用途 |
|--------|------|------|
| `--color-bg-page` | `#F5F7FA` | 页面背景（`el-main`、登录页背景） |
| `--color-bg-surface` | `#FFFFFF` | 卡片、表格、弹窗背景 |
| `--color-bg-muted` | `#FAFAFA` | 表头、筛选区次级背景 |
| `--color-border` | `#E4E7ED` | 卡片边框、表格分隔（对齐 `--el-border-color-light`） |
| `--color-border-strong` | `#DCDFE6` | 输入框、描述列表边框 |
| `--color-text-primary` | `#303133` | 标题、正文主色 |
| `--color-text-regular` | `#606266` | 正文、表格内容 |
| `--color-text-secondary` | `#909399` | 辅助说明、占位 |
| `--color-text-placeholder` | `#A8ABB2` | 输入占位 |

#### Element Plus 映射（在 `element-overrides.css` 中设置）

```css
:root {
  --el-color-primary: #1677FF;
  --el-color-success: #52C41A;
  --el-color-warning: #FAAD14;
  --el-color-danger: #FF4D4F;
  --el-color-info: #8C8C8C;
  --el-bg-color-page: #F5F7FA;
  --el-border-color-light: #E4E7ED;
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-text-color-secondary: #909399;
}
```

### 2.2 间距尺度

统一使用 **4px 基准**，禁止随意使用 13px、22px 等非刻度值。

| 令牌 | 值 | 典型用途 |
|------|-----|----------|
| `--space-1` | `4px` | 图标与文字间距 |
| `--space-2` | `8px` | 行内元素、Tag 间距 |
| `--space-3` | `12px` | 表单项内紧凑间距 |
| `--space-4` | `16px` | 卡片内边距（小）、区块间距 |
| `--space-5` | `20px` | 卡片内边距（默认） |
| `--space-6` | `24px` | 页面内边距、`el-main` padding |
| `--space-8` | `32px` | 大区块分隔 |
| `--space-10` | `40px` | 空状态上下留白 |

**页面级间距约定：**

- 页面容器内边距：`24px`（`--space-6`）
- 页面内相邻卡片纵向间距：`16px`（`--space-4`）
- 卡片内边距：`20px`（`--space-5`）
- 筛选表单与表格卡片间距：由页面 `gap: 16px` 统一控制

### 2.3 圆角

| 令牌 | 值 | 用途 |
|------|-----|------|
| `--radius-sm` | `4px` | Tag、小按钮 |
| `--radius-md` | `6px` | 按钮、输入框（对齐 Element Plus 默认） |
| `--radius-lg` | `8px` | 卡片 |
| `--radius-xl` | `12px` | 登录卡片、大弹窗（少用） |

### 2.4 阴影

| 令牌 | 值 | 用途 |
|------|-----|------|
| `--shadow-none` | `none` | 管理端表格卡片默认 |
| `--shadow-sm` | `0 1px 2px rgba(0, 0, 0, 0.04)` | 用户端内容卡片（可选） |
| `--shadow-md` | `0 4px 12px rgba(0, 0, 0, 0.08)` | 下拉、弹窗（Element Plus 默认即可） |

管理端卡片统一 `shadow="never"`；用户端详情卡可用 `--shadow-sm` 或保持 `never` + 边框，全站二选一后不再混用。

### 2.5 字体

```css
--font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
```

| 层级 | 字号 | 字重 | 行高 | 用途 |
|------|------|------|------|------|
| 页面标题 | `20px` | `600` | `1.4` | 每页一个 `h1` |
| 区块标题 | `16px` | `600` | `1.5` | 卡片内 `h2`、弹窗内分组标题 |
| 正文 | `14px` | `400` | `1.6` | 默认正文、表格、表单 |
| 辅助文字 | `13px` | `400` | `1.5` | 筛选提示、表格次要信息 |
| 小字 | `12px` | `400` | `1.5` | Tag 内文字、字数统计 |

- 数字列、人数、分页总数使用 `font-variant-numeric: tabular-nums`
- 标题可使用 `text-wrap: balance` 减少孤行
- 中文省略号使用 `…`，不用 `...`

---

## 3. 页面布局规范

### 3.1 页面骨架（全站统一）

每个列表型 / 管理型页面按以下四层结构组织（自上而下）：

```
┌─────────────────────────────────────────────────────────┐
│  PageHeader   页面标题 + 可选描述 + 右侧主操作              │
├─────────────────────────────────────────────────────────┤
│  FilterBar    搜索 / 筛选（el-card 或 PageHeader 下方）     │
├─────────────────────────────────────────────────────────┤
│  ContentArea  表格 / 详情 / 空状态 / 加载态                 │
├─────────────────────────────────────────────────────────┤
│  Pagination   分页（右对齐，有数据时显示）                    │
└─────────────────────────────────────────────────────────┘
```

**实现约定（Vue 组件命名建议）：**

| 区域 | 建议组件 | 说明 |
|------|----------|------|
| 页面容器 | `PageContainer` | 统一 `padding: 24px`；详情页额外 `max-width: 880px; margin: 0 auto` |
| 页面标题区 | `PageHeader` | 标题 + 描述 + 右侧 `extra` 插槽 |
| 筛选区 | `FilterCard` | 包裹 `el-form :inline="true"` |
| 内容区 | `ContentCard` | 包裹 `el-table` 或详情主体 |
| 分页区 | 内置于 `ContentCard` 底部 | 见 §3.5 |

页面内模块间距：`display: flex; flex-direction: column; gap: 16px`。

### 3.2 PageHeader（页面标题区）

**结构：**

```
[ 页面标题 h1 ]                    [ 主操作按钮（可选）]
[ 页面描述（可选，13px 次要色）]
```

**规则：**

- 每页有且仅有一个 `h1`，文案与路由目的一致（如「活动列表」「我的报名」「活动管理」「报名审核」）
- 详情页 `h1` 为活动名称；列表页 `h1` 为固定页面名，**不用活动名占满标题区**
- 主操作按钮仅放**页面级**操作：新增活动、导出（若有）；行级操作不放此处
- 返回链接放在 `PageHeader` 上方或左侧，使用 `el-button link` + 图标，文案：「返回活动列表」

**示例布局（管理端活动管理）：**

```
活动管理                                    [ + 新增活动 ]
管理活动信息、上架状态与报名名单
```

### 3.3 FilterBar（搜索 / 筛选区）

**容器：** `el-card`，`shadow="never"`，`border-radius: 8px`，内边距 `20px`。

**表单：** `el-form :inline="true"`，`label-width` 统一 `auto`（由 `el-form-item label` 自然宽度），表单项间距遵循 Element Plus 默认。

**字段排列：**

1. 关键词输入（宽度 `220px`）
2. 下拉筛选（宽度 `140px`；活动选择器 `280px`）
3. 操作按钮组：`搜索`（primary）+ `重置`（default）

**规则：**

- 输入框 `clearable`；占位符以 `…` 结尾，如 `输入活动名称搜索…`
- 回车触发搜索（`@keyup.enter`）
- 筛选提示（如本地筛选说明）放在表单下方，`13px` 次要色，间距 `8px`
- 移动端（`<768px`）：筛选表单项换行；可选折叠为「筛选」按钮 + `el-drawer`

**不要**在筛选区放置表格行级操作（编辑、审核等）。

### 3.4 ContentArea（内容区）

**容器：** `el-card`，`shadow="never"`，`border-radius: 8px`，内边距 `20px`（表格可 `padding-bottom: 20px`，表格贴顶）。

**状态处理：**

| 状态 | 表现 |
|------|------|
| 加载中 | `v-loading` 作用于表格或整卡 |
| 有数据 | 展示表格 / 详情 |
| 无数据 | **隐藏空表头**，使用 `el-empty` 居中；附主操作或引导链接 |
| 错误 | `el-result` 或 `el-alert` + 重试按钮 |

### 3.5 Pagination（分页区）

**位置：** `ContentCard` 底部，右对齐，`margin-top: 16px`。

**桌面端 layout：**

```html
layout="total, sizes, prev, pager, next, jumper"
```

**属性：** `background`、`:page-sizes="[10, 20, 50]"`、默认 `pageSize: 10`。

**移动端 layout（`<768px`）：**

```html
layout="prev, pager, next"
```

**规则：**

- `total === 0` 时不显示分页
- 切换 `pageSize` 时重置到第 1 页
- 筛选条件变化时重置到第 1 页

### 3.6 布局壳（Layout）

#### 用户端 `BasicLayout`

- 顶栏高度 `56px`，白底，`border-bottom: 1px solid var(--color-border)`
- 品牌名左对齐，`18px / 600`
- 导航 `el-menu mode="horizontal"`，`border-bottom: none`
- 用户区右对齐：昵称 + `退出` link 按钮
- `el-main`：背景 `--color-bg-page`，padding `24px`

#### 管理端 `AdminLayout`

- 侧栏宽 `220px`，白底，右边框
- 侧栏品牌区 `padding: 20px 16px`
- 顶栏右对齐用户操作；`el-main` 同用户端

#### 响应式断点

| 断点 | 宽度 | 说明 |
|------|------|------|
| `sm` | `<768px` | 导航折叠、分页精简、表格横向滚动或卡片化 |
| `md` | `768px–1199px` | 常规模板 |
| `lg` | `≥1200px` | 宽屏；内容区可不限制 max-width（详情页除外） |

---

## 4. 组件样式规范

### 4.1 卡片（el-card）

| 属性 | 值 |
|------|-----|
| `shadow` | `never`（全站统一） |
| `border-radius` | `8px`（通过 class 或 CSS 变量） |
| 边框 | `1px solid var(--color-border)`（Element Plus card 默认） |
| 内边距 | `20px`（`el-card__body`） |
| 页头 | 一般不用 `el-card` 的 `#header`；标题交给 `PageHeader` |

**例外：** 登录页卡片宽 `420px`（`min(420px, 100% - 32px)`），可使用 `#header` 显示「登录」。

### 4.2 按钮（el-button）

#### 类型与层级

| 层级 | 类型 | 尺寸 | 场景 |
|------|------|------|------|
| 一级主操作 | `type="primary"` | `default` | 搜索、确认、新增、登录、报名（详情页可用 `large`） |
| 二级操作 | `default` | `default` | 重置、取消 |
| 表格内主操作 | `type="primary"` `link` | `default` | 查看详情、编辑 |
| 表格内次操作 | `type="primary"` `link` | `default` | 报名名单、查看活动 |
| 成功操作 | `type="success"` `link` | `default` | 审核通过、上架 |
| 警告操作 | `type="warning"` `link` | `default` | 下架 |
| 危险操作 | `type="danger"` `link` | `default` | 拒绝、取消报名 |
| 导航 / 返回 | `link` `type="primary"` | `default` | 返回列表、返回用户端 |

#### 规则

- **同一行操作不超过 3 个 link 按钮**；超过时收进 `el-dropdown`「更多」
- 破坏性操作（取消报名、拒绝、下架）必须二次确认（`ElMessageBox`）
- 请求进行中：`loading` 态，禁止重复提交
- 禁用按钮外包 `span` 时，用 `el-tooltip` 说明原因（已有实践，保持）
- 按钮文案用动词：「搜索」「重置」「新增活动」「确认报名」「保存」；不用「确定」「继续」等模糊词
- 图标按钮必须配 `aria-label`（引入 `@element-plus/icons-vue` 后）

### 4.3 表格（el-table）

| 属性 | 值 | 说明 |
|------|-----|------|
| `stripe` | `true` | 斑马纹 |
| `border` | `false` | **全站去掉纵向边框**，仅保留行底分隔 |
| `style` | `width: 100%` | 撑满卡片 |
| `show-overflow-tooltip` | 长文本列开启 | 标题、地点、备注 |
| `fixed="right"` | 操作列 | 宽屏固定操作列 |

**表头：** 背景 `--color-bg-muted`（`#FAFAFA`），文字 `14px / 600`，颜色 `--color-text-regular`。

**行高：** 使用 Element Plus 默认；内容垂直居中。

**列宽原则：**

- 状态列：`width: 100px–120px`
- 时间列：`min-width: 170px`；双时间合并列 `min-width: 240px`
- 操作列：按按钮数量 `width: 160px–200px`；超过 3 个操作时 `width: 120px` + 下拉
- 主标题列：`min-width: 180px`

**空数据：** 不渲染仅含表头的空表；用 `el-empty` 替代（见 §3.4）。

**数字列：** 人数 `已通过 / 上限（剩余 N）` 右对齐或居中，等宽数字。

### 4.4 表单（el-form）

#### 筛选表单（inline）

- `label` 文案简短：活动名称、报名状态、活动状态
- 不写 `label-width` 硬编码像素；保持 inline 自然对齐
- 按钮组紧跟最后一个表单项，间距 `8px`

#### 弹窗表单

| 属性 | 值 |
|------|-----|
| `label-width` | `120px` |
| 弹窗宽度 | 简单表单 `480px`；活动编辑 `640px` |
| `destroy-on-close` | `true` |
| 分组 | 字段多时分「基本信息」「时间设置」「名额设置」小标题（`16px / 600`） |

**输入：**

- 必填项 `prop` + `rules`；错误信息展示在字段下方（Element Plus 默认）
- 提交时聚焦第一个错误字段
- 文本域 `show-word-limit`；`maxlength` 与契约一致
- 日期时间 `el-date-picker type="datetime"`，`style="width: 100%"`
- 用户名 / 密码框设置 `autocomplete`（登录页已具备）

**弹窗底部：**

```
[ 取消 (default) ]  [ 确认主操作 (primary, loading) ]
```

右对齐，间距 `8px`，与 Element Plus `footer` 插槽一致。

### 4.5 描述列表（el-descriptions）

用于详情页信息展示、报名审核摘要。

| 场景 | `column` |
|------|----------|
| 活动详情 | `1`（移动端友好） |
| 审核摘要（桌面） | `2` 或拆为 4 个 stat 卡片 |
| 审核摘要（移动） | `1` |

`border` 开启；标签宽 `120px`；内容与表格文字同色。

### 4.6 弹窗（el-dialog）

| 类型 | 宽度 |
|------|------|
| 报名确认 | `480px` |
| 拒绝审核 | `480px` |
| 新增 / 编辑活动 | `640px` |

标题使用动词短语：「立即报名」「拒绝报名」「新增活动」「编辑活动」。

### 4.7 空状态（el-empty）

- 文案具体：「暂无报名记录」「暂无活动，点击新增活动」「活动不存在或已下架」
- 可附 `el-button type="primary" link` 引导，如「去浏览活动」
- 上下留白 `40px`

### 4.8 提示（el-alert）

用于详情页「已报名」「不可报名原因」：

- 已报名：`type="info"`，`show-icon`，不可关闭
- 不可报名：`type="warning"`，`show-icon`，不可关闭
- 与主内容间距 `16px`

---

## 5. 状态 Tag 规范

全站状态**必须**通过统一映射函数 / 组件渲染（建议 `StatusTag.vue` 或 `useStatusTag()`），禁止各页面自行散落 `if/else` 后不一致。

### 5.1 Tag 通用样式

```html
<el-tag :type="tagType" size="small" effect="light">
  {{ label }}
</el-tag>
```

| 属性 | 值 | 说明 |
|------|-----|------|
| `size` | `small` | 表格、列表内 |
| `size` | `default` | 详情页标题旁状态（仅 1 处） |
| `effect` | `light` | 全站统一浅色底，更清爽 |

详情页标题旁可用 `size="default"`，其余一律 `small`。

### 5.2 用户端 · 报名可用状态（活动列表 / 详情）

依据 `canRegister` 与 `registerDisabledReason`：

| 条件 | 展示文案 | `type` | `effect` |
|------|----------|--------|----------|
| `canRegister === true` | 可报名 | `success` | `light` |
| `registerDisabledReason === '已报名'` | 已报名 | `info` | `light` |
| `registerDisabledReason === '报名尚未开始'` | 尚未开始 | `warning` | `light` |
| `registerDisabledReason === '活动报名已截止'` | 已截止 | `danger` | `light` |
| `registerDisabledReason === '活动名额已满'` | 名额已满 | `danger` | `light` |
| 其他 | 不可报名 | `info` | `light` |

**说明：** 文案与后端 `registerDisabledReason` 保持一致；Tag 旁不重复长句说明（详情页用 `el-alert` 补充）。

### 5.3 管理端 · 活动发布状态

依据 `ActivityStatus`：`DRAFT` | `PUBLISHED` | `OFFLINE`

| 枚举值 | 展示文案 | `type` |
|--------|----------|--------|
| `DRAFT` | 草稿 | `info` |
| `PUBLISHED` | 已上架 | `success` |
| `OFFLINE` | 已下架 | `warning` |

### 5.4 报名审核状态

依据 `RegistrationStatus`：`PENDING` | `APPROVED` | `REJECTED` | `CANCELLED`

| 枚举值 | 展示文案 | `type` | 使用场景 |
|--------|----------|--------|----------|
| `PENDING` | 待审核 | `warning` | 我的报名、报名审核 |
| `APPROVED` | 已通过 | `success` | 同上 |
| `REJECTED` | 已拒绝 | `danger` | 同上；可配合 `auditRemark` 列 |
| `CANCELLED` | 已取消 | `info` | 同上 |

### 5.5 筛选器与 Tag 的区分

筛选下拉使用**中文标签**，不与 Tag 样式混用：

| 筛选项 | 下拉文案 |
|--------|----------|
| 报名可报筛选 | 全部 / 可报名 / 尚未开始 / 已截止 / 名额已满 / 已报名 |
| 报名审核筛选 | 全部状态 / 待审核 / 已通过 / 已拒绝 / 已取消 |
| 活动状态筛选 | 全部状态 / 草稿 / 已上架 / 已下架 |

### 5.6 色弱与可访问性

- Tag 必须包含**文字**，禁止仅色块
- `danger` / `warning` 状态在详情页用 `el-alert` 再强调一次
- Tag 对比度依赖 Element Plus `effect="light"` 默认配色；定制主题时需验证 4.5:1

---

## 6. 按页面应用一览

| 页面 | PageHeader 标题 | 主操作 | FilterBar | Content | 备注 |
|------|-----------------|--------|-----------|---------|------|
| 登录 | — | 登录（卡片内全宽 primary） | — | 单卡片表单 | 无 PageHeader |
| 活动列表 | 活动列表 | — | 活动名称 + 报名状态 + 搜索/重置 | 表格 + 报名弹窗 | 补空状态 |
| 活动详情 | 活动名称（h1） | 立即报名（primary large） | — | 描述 + 说明 + alert | `max-width: 880px` |
| 我的报名 | 我的报名 | — | 审核状态下拉 | 表格 | 空状态优化 |
| 活动管理 | 活动管理 | 新增活动 | 活动名称 + 活动状态 + 搜索/重置 | 表格 + 编辑弹窗 | 操作收进更多 |
| 报名审核 | 报名审核 | 返回活动管理（link） | 活动选择 + 报名状态 + 搜索/重置 | 摘要 + 表格 | 摘要用 descriptions 2 列 |

---

## 7. 日期时间与文案

### 7.1 时间展示

- 格式：`YYYY-MM-DD HH:mm`（与现实现一致）
- 区间：`开始 ~ 结束`，缺失显示 `—`
- 后续可迁移至 `Intl.DateTimeFormat('zh-CN', …)`，全站统一封装 `formatDateTime()`

### 7.2 文案语气

- 使用第二人称提示：「您已报名该活动」
- 按钮 Title Case 风格中文：「确认报名」「确认拒绝」
- 加载态：「加载中…」「保存中…」
- 错误提示包含下一步：不只说「失败」，应说明可操作项

---

## 8. 文件与实现路线图（后续开发参考）

本阶段仅文档，不改代码。实施时建议顺序：

```
frontend/src/
├── styles/
│   ├── tokens.css              # §2 设计令牌
│   ├── base.css                # 字体、body、标题
│   └── element-overrides.css   # §2.1 Element Plus 映射
├── components/common/
│   ├── PageContainer.vue
│   ├── PageHeader.vue
│   ├── FilterCard.vue
│   ├── ContentCard.vue
│   ├── StatusTag.vue           # §5 统一 Tag
│   └── EmptyState.vue
└── composables/
    ├── useDateTime.ts
    └── useStatusTag.ts
```

在 `main.ts` 中按顺序引入：

```ts
import '@/styles/tokens.css'
import '@/styles/base.css'
import '@/styles/element-overrides.css'
```

---

## 9. 验收清单

页面改造完成后，逐项自检：

- [ ] 主色、页面背景、卡片圆角 / 边框全站一致
- [ ] 列表页均含 PageHeader + FilterBar + ContentCard + Pagination
- [ ] 表格 `border=false`、`stripe=true`；无空表头空数据
- [ ] 按钮层级符合 §4.2；Destructive 有确认
- [ ] 所有状态 Tag 经统一映射，文案 / 颜色符合 §5
- [ ] 表单弹窗 `label-width: 120px`；筛选 inline 规则一致
- [ ] 焦点可见、表单有 label、图标按钮有 `aria-label`
- [ ] 移动端顶栏 / 分页 / 摘要区不溢出（`768px` 断点验证）
- [ ] 无 inline `style="width: …"` 硬编码（改用语义 class）

---

## 10. 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-06-07 | 首版：Vue 3 + Element Plus 活动报名系统视觉规范 |
