<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import {
  canOfflineActivity,
  canPublishActivity,
  getAdminActivityStatusLabel,
  getAdminActivityStatusTagType,
} from '@/composables/useAdminActivityStatus'
import { formatDateTimeRange } from '@/composables/useDateTime'
import {
  createAdminActivity,
  getAdminActivities,
  updateAdminActivity,
  updateAdminActivityStatus,
} from '@/api/admin/activity'
import { getActivityDetail } from '@/api/activity'
import type {
  ActivityFormPayload,
  ActivityStatus,
  AdminActivityListItem,
} from '@/types/admin-activity'

const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const statusUpdatingId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref<ActivityStatus | ''>('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<AdminActivityListItem[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  description: '',
  location: '',
  eventStartTime: '',
  eventEndTime: '',
  registrationStartTime: '',
  registrationDeadline: '',
  maxParticipants: 50,
})

const statusOptions: { label: string; value: ActivityStatus | '' }[] = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已上架', value: 'PUBLISHED' },
  { label: '已下架', value: 'OFFLINE' },
]

const datetimeFormat = 'YYYY-MM-DDTHH:mm:ss'

const showEmpty = computed(() => !loading.value && list.value.length === 0)

const emptyDescription = computed(() => {
  if (keyword.value.trim() || statusFilter.value) {
    return '当前筛选条件下暂无活动'
  }
  return '暂无活动，点击新增活动开始创建'
})

function validateEventEnd(_rule: unknown, value: string, callback: (error?: Error) => void) {
  if (!value || !form.eventStartTime) {
    callback()
    return
  }
  if (new Date(value).getTime() < new Date(form.eventStartTime).getTime()) {
    callback(new Error('活动结束时间不能早于开始时间'))
    return
  }
  callback()
}

function validateRegistrationEnd(
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) {
  if (!value || !form.registrationStartTime) {
    callback()
    return
  }
  if (new Date(value).getTime() <= new Date(form.registrationStartTime).getTime()) {
    callback(new Error('报名截止时间必须晚于报名开始时间'))
    return
  }
  callback()
}

const rules: FormRules = {
  title: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { max: 200, message: '活动名称不能超过200字符', trigger: 'blur' },
  ],
  eventStartTime: [{ required: true, message: '请选择活动开始时间', trigger: 'change' }],
  eventEndTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' },
    { validator: validateEventEnd, trigger: 'change' },
  ],
  registrationStartTime: [
    { required: true, message: '请选择报名开始时间', trigger: 'change' },
  ],
  registrationDeadline: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' },
    { validator: validateRegistrationEnd, trigger: 'change' },
  ],
  maxParticipants: [
    { required: true, message: '请输入人数上限', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      message: '人数上限不能小于1',
      trigger: 'change',
    },
  ],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getAdminActivities({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value || undefined,
    })
    list.value = data.list
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  pageSize.value = 10
  fetchList()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  fetchList()
}

function handleSizeChange(nextSize: number) {
  pageSize.value = nextSize
  page.value = 1
  fetchList()
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.location = ''
  form.eventStartTime = ''
  form.eventEndTime = ''
  form.registrationStartTime = ''
  form.registrationDeadline = ''
  form.maxParticipants = 50
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(row: AdminActivityListItem) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  resetForm()

  form.title = row.title
  form.location = row.location ?? ''
  form.eventStartTime = row.eventStartTime ?? ''
  form.eventEndTime = row.eventEndTime ?? ''
  form.registrationStartTime = row.registrationStartTime
  form.registrationDeadline = row.registrationDeadline
  form.maxParticipants = row.maxParticipants

  if (row.status === 'PUBLISHED') {
    try {
      const detail = await getActivityDetail(row.id)
      form.description = detail.description ?? ''
    } catch {
      // 非上架活动无法从用户端详情接口获取描述
    }
  }

  dialogVisible.value = true
}

function buildPayload(): ActivityFormPayload {
  return {
    title: form.title.trim(),
    description: form.description.trim() || undefined,
    location: form.location.trim() || undefined,
    eventStartTime: form.eventStartTime || undefined,
    eventEndTime: form.eventEndTime || undefined,
    registrationStartTime: form.registrationStartTime,
    registrationDeadline: form.registrationDeadline,
    maxParticipants: form.maxParticipants,
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const payload = buildPayload()
    if (dialogMode.value === 'create') {
      await createAdminActivity(payload)
      ElMessage.success('活动创建成功')
    } else if (editingId.value) {
      await updateAdminActivity(editingId.value, payload)
      ElMessage.success('活动更新成功')
    }
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row: AdminActivityListItem) {
  try {
    await ElMessageBox.confirm(`确定上架活动「${row.title}」吗？`, '上架活动', {
      confirmButtonText: '确认上架',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch {
    return
  }

  statusUpdatingId.value = row.id
  try {
    await updateAdminActivityStatus(row.id, 'PUBLISHED')
    ElMessage.success('活动已上架')
    await fetchList()
  } finally {
    statusUpdatingId.value = null
  }
}

async function handleOffline(row: AdminActivityListItem) {
  try {
    await ElMessageBox.confirm(`确定下架活动「${row.title}」吗？`, '下架活动', {
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  statusUpdatingId.value = row.id
  try {
    await updateAdminActivityStatus(row.id, 'OFFLINE')
    ElMessage.success('活动已下架')
    await fetchList()
  } finally {
    statusUpdatingId.value = null
  }
}

function goRegistrations(row: AdminActivityListItem) {
  router.push(`/admin/activities/${row.id}/registrations`)
}

function formatEventTime(row: AdminActivityListItem) {
  if (!row.eventStartTime && !row.eventEndTime) {
    return '—'
  }
  return formatDateTimeRange(row.eventStartTime, row.eventEndTime)
}

function formatRegistrationTime(row: AdminActivityListItem) {
  return formatDateTimeRange(row.registrationStartTime, row.registrationDeadline)
}

function formatParticipants(row: AdminActivityListItem) {
  return `${row.approvedCount} / ${row.maxParticipants}`
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="app-page">
    <PageHeader
      title="活动管理"
      description="管理活动信息、发布状态与报名名单。"
    >
      <template #extra>
        <el-button type="primary" @click="openCreateDialog">新增活动</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="ui-card filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="活动名称">
          <el-input
            v-model="keyword"
            class="filter-input"
            placeholder="输入活动名称搜索…"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="statusFilter" class="filter-select" placeholder="全部状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value || 'ALL'"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="ui-card table-card">
      <el-empty v-if="showEmpty" class="table-empty" :description="emptyDescription">
        <el-button type="primary" @click="openCreateDialog">新增活动</el-button>
      </el-empty>

      <template v-else>
        <el-table
          v-loading="loading"
          :data="list"
          stripe
          class="app-table"
          element-loading-text="加载中…"
        >
          <el-table-column prop="title" label="活动名称" min-width="160" show-overflow-tooltip />
          <el-table-column label="活动时间" min-width="240">
            <template #default="{ row }">
              {{ formatEventTime(row) }}
            </template>
          </el-table-column>
          <el-table-column label="报名时间" min-width="240">
            <template #default="{ row }">
              {{ formatRegistrationTime(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
          <el-table-column label="人数" width="110" align="center">
            <template #default="{ row }">
              <span class="tabular-nums">{{ formatParticipants(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getAdminActivityStatusTagType(row.status)"
                size="small"
                effect="light"
              >
                {{ getAdminActivityStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-dropdown trigger="click">
                <el-button link type="primary">
                  更多
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openEditDialog(row)">编辑</el-dropdown-item>
                    <el-dropdown-item
                      v-if="canPublishActivity(row.status)"
                      :disabled="statusUpdatingId === row.id"
                      @click="handlePublish(row)"
                    >
                      上架
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="canOfflineActivity(row.status)"
                      :disabled="statusUpdatingId === row.id"
                      @click="handleOffline(row)"
                    >
                      下架
                    </el-dropdown-item>
                    <el-dropdown-item @click="goRegistrations(row)">报名名单</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="total > 0" class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </template>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增活动' : '编辑活动'"
      width="720px"
      destroy-on-close
      class="activity-form-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <section class="form-section" aria-labelledby="form-section-basic">
          <h3 id="form-section-basic" class="form-section__title">基础信息</h3>
          <el-form-item label="活动名称" prop="title">
            <el-input
              v-model="form.title"
              maxlength="200"
              show-word-limit
              placeholder="请输入活动名称"
            />
          </el-form-item>
          <el-form-item label="活动描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="选填，活动说明"
            />
          </el-form-item>
          <el-form-item label="地点">
            <el-input
              v-model="form.location"
              maxlength="200"
              show-word-limit
              placeholder="选填"
            />
          </el-form-item>
        </section>

        <section class="form-section" aria-labelledby="form-section-time">
          <h3 id="form-section-time" class="form-section__title">时间信息</h3>
          <el-form-item label="活动开始时间" prop="eventStartTime">
            <el-date-picker
              v-model="form.eventStartTime"
              class="form-date-picker"
              type="datetime"
              :value-format="datetimeFormat"
              placeholder="选择活动开始时间"
            />
          </el-form-item>
          <el-form-item label="活动结束时间" prop="eventEndTime">
            <el-date-picker
              v-model="form.eventEndTime"
              class="form-date-picker"
              type="datetime"
              :value-format="datetimeFormat"
              placeholder="选择活动结束时间"
            />
          </el-form-item>
        </section>

        <section class="form-section" aria-labelledby="form-section-registration">
          <h3 id="form-section-registration" class="form-section__title">报名设置</h3>
          <el-form-item label="报名开始时间" prop="registrationStartTime">
            <el-date-picker
              v-model="form.registrationStartTime"
              class="form-date-picker"
              type="datetime"
              :value-format="datetimeFormat"
              placeholder="选择报名开始时间"
            />
          </el-form-item>
          <el-form-item label="报名截止时间" prop="registrationDeadline">
            <el-date-picker
              v-model="form.registrationDeadline"
              class="form-date-picker"
              type="datetime"
              :value-format="datetimeFormat"
              placeholder="选择报名截止时间"
            />
          </el-form-item>
          <el-form-item label="人数上限" prop="maxParticipants">
            <el-input-number
              v-model="form.maxParticipants"
              class="form-input-number"
              :min="1"
              :max="99999"
              controls-position="right"
            />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

