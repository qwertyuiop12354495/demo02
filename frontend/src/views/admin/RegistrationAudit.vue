<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { formatDateTime } from '@/composables/useDateTime'
import {
  getRegistrationStatusLabel,
  getRegistrationStatusTagType,
} from '@/composables/useRegistrationStatus'
import { getAdminActivities } from '@/api/admin/activity'
import { auditRegistration, getActivityRegistrations } from '@/api/admin/registration'
import type { RegistrationStatus } from '@/types/activity'
import type {
  ActivityRegistrationSummary,
  AdminRegistrationListItem,
} from '@/types/admin-registration'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const auditingId = ref<number | null>(null)
const activitySearching = ref(false)
const loadError = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<AdminRegistrationListItem[]>([])
const activitySummary = ref<ActivityRegistrationSummary | null>(null)

const selectedActivityId = ref<number | null>(null)
const activityOptions = ref<{ id: number; title: string }[]>([])
const statusFilter = ref<RegistrationStatus | ''>('')
const registrantKeyword = ref('')

const rejectDialogVisible = ref(false)
const rejectError = ref('')
const rejectingRow = ref<AdminRegistrationListItem | null>(null)
const rejectFormRef = ref<FormInstance>()
const rejectForm = reactive({
  auditRemark: '',
})

const rejectRules: FormRules = {
  auditRemark: [
    { required: true, message: '请填写拒绝原因', trigger: 'blur' },
    { max: 500, message: '审核备注不能超过500字符', trigger: 'blur' },
  ],
}

const statusOptions: { label: string; value: RegistrationStatus | '' }[] = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' },
]

const activityId = computed(() => {
  const raw = Number(route.params.activityId)
  return Number.isFinite(raw) && raw > 0 ? raw : null
})

const activityTitle = computed(() => activitySummary.value?.title ?? '—')

const isValidActivity = computed(() => activityId.value !== null)

const isRegistrantFilterActive = computed(() => registrantKeyword.value.trim().length > 0)

const filteredList = computed(() => {
  const keyword = registrantKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return list.value
  }
  return list.value.filter((row) => {
    const username = row.username?.toLowerCase() ?? ''
    const nickname = row.nickname?.toLowerCase() ?? ''
    return username.includes(keyword) || nickname.includes(keyword)
  })
})

const showEmpty = computed(
  () => isValidActivity.value && !loading.value && !loadError.value && filteredList.value.length === 0,
)

const emptyDescription = computed(() => {
  if (isRegistrantFilterActive.value || statusFilter.value) {
    return '当前筛选条件下暂无报名记录'
  }
  return '暂无报名记录'
})

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (error && typeof error === 'object' && 'message' in error) {
    const message = (error as { message?: string }).message
    if (message) {
      return message
    }
  }
  return fallback
}

async function searchActivities(keyword: string) {
  activitySearching.value = true
  try {
    const data = await getAdminActivities({
      keyword: keyword.trim() || undefined,
      page: 1,
      pageSize: 50,
    })
    activityOptions.value = data.list.map((item) => ({
      id: item.id,
      title: item.title,
    }))
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '活动列表加载失败'))
  } finally {
    activitySearching.value = false
  }
}

function handleActivityChange(id: number | null) {
  if (id && id !== activityId.value) {
    router.push(`/admin/activities/${id}/registrations`)
  }
}

async function fetchList() {
  if (!activityId.value) {
    list.value = []
    activitySummary.value = null
    total.value = 0
    loadError.value = ''
    loading.value = false
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const data = await getActivityRegistrations(activityId.value, {
      page: page.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined,
    })
    list.value = data.list
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
    activitySummary.value = data.activity
    selectedActivityId.value = data.activity.id

    if (!activityOptions.value.some((item) => item.id === data.activity.id)) {
      activityOptions.value = [
        { id: data.activity.id, title: data.activity.title },
        ...activityOptions.value,
      ]
    }
  } catch (error) {
    loadError.value = resolveErrorMessage(error, '报名名单加载失败，请稍后重试')
    list.value = []
    activitySummary.value = null
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (!isValidActivity.value) {
    return
  }
  page.value = 1
  fetchList()
}

function handleReset() {
  statusFilter.value = ''
  registrantKeyword.value = ''
  page.value = 1
  pageSize.value = 10
  if (isValidActivity.value) {
    fetchList()
  }
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

async function handleApprove(row: AdminRegistrationListItem) {
  auditingId.value = row.id
  try {
    await auditRegistration(row.id, { action: 'APPROVE' })
    ElMessage.success('审核通过')
    await fetchList()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '审核失败，请稍后重试'))
  } finally {
    auditingId.value = null
  }
}

function openRejectDialog(row: AdminRegistrationListItem) {
  rejectingRow.value = row
  rejectForm.auditRemark = ''
  rejectError.value = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  const valid = await rejectFormRef.value?.validate().catch(() => false)
  if (!valid || !rejectingRow.value) {
    return
  }

  auditingId.value = rejectingRow.value.id
  rejectError.value = ''
  try {
    await auditRegistration(rejectingRow.value.id, {
      action: 'REJECT',
      auditRemark: rejectForm.auditRemark.trim(),
    })
    ElMessage.success('已拒绝报名')
    rejectDialogVisible.value = false
    await fetchList()
  } catch (error) {
    rejectError.value = resolveErrorMessage(error, '拒绝失败，请稍后重试')
  } finally {
    auditingId.value = null
  }
}

function formatRegistrant(row: AdminRegistrationListItem) {
  if (row.nickname && row.username) {
    return `${row.nickname}（${row.username}）`
  }
  return row.nickname || row.username || '—'
}

function isPending(row: AdminRegistrationListItem) {
  return row.status === 'PENDING'
}

watch(
  () => route.params.activityId,
  () => {
    selectedActivityId.value = activityId.value
    registrantKeyword.value = ''
    page.value = 1
    fetchList()
  },
)

onMounted(async () => {
  selectedActivityId.value = activityId.value
  await searchActivities('')
  await fetchList()
})
</script>

<template>
  <div class="app-page">
    <PageHeader
      title="报名审核"
      description="选择活动并审核报名申请，支持按状态与报名人筛选。"
    >
      <template #extra>
        <el-button link type="primary" @click="router.push('/admin/activities')">
          返回活动管理
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="ui-card filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="活动名称">
          <el-select
            v-model="selectedActivityId"
            class="filter-activity"
            filterable
            remote
            reserve-keyword
            placeholder="搜索并选择活动…"
            :remote-method="searchActivities"
            :loading="activitySearching"
            @change="handleActivityChange"
          >
            <el-option
              v-for="item in activityOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报名状态">
          <el-select v-model="statusFilter" class="filter-select" placeholder="全部状态">
            <el-option
              v-for="item in statusOptions"
              :key="item.value || 'ALL'"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报名人">
          <el-input
            v-model="registrantKeyword"
            class="filter-input"
            placeholder="输入昵称或用户名…"
            clearable
            :disabled="!isValidActivity"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :disabled="!isValidActivity" @click="handleSearch">
            搜索
          </el-button>
          <el-button :disabled="!isValidActivity" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <p v-if="isRegistrantFilterActive" class="filter-tip">
        报名人筛选作用于当前页数据；切换页码后请重新筛选。
      </p>
    </el-card>

    <div v-if="activitySummary" class="summary-grid">
      <div class="summary-card">
        <span class="summary-card__label">活动名称</span>
        <span class="summary-card__value">{{ activitySummary.title }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-card__label">人数上限</span>
        <span class="summary-card__value tabular-nums">{{ activitySummary.maxParticipants }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-card__label">已通过</span>
        <span class="summary-card__value tabular-nums summary-card__value--success">
          {{ activitySummary.approvedCount }}
        </span>
      </div>
      <div class="summary-card">
        <span class="summary-card__label">待审核</span>
        <span class="summary-card__value tabular-nums summary-card__value--warning">
          {{ activitySummary.pendingCount }}
        </span>
      </div>
    </div>

    <el-card shadow="never" class="ui-card table-card">
      <el-empty v-if="!isValidActivity" class="table-empty" description="请先选择活动" />

      <template v-else>
        <el-alert
          v-if="loadError"
          type="error"
          :title="loadError"
          :closable="false"
          show-icon
          class="form-alert"
        >
          <template #default>
            <el-button type="primary" link @click="fetchList">重新加载</el-button>
          </template>
        </el-alert>

        <el-empty v-else-if="showEmpty" class="table-empty" :description="emptyDescription" />

        <template v-else>
          <el-table
            v-loading="loading"
            :data="filteredList"
            stripe
            class="app-table"
            element-loading-text="加载中…"
          >
            <el-table-column label="报名人" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ formatRegistrant(row) }}
              </template>
            </el-table-column>
            <el-table-column label="活动名称" min-width="180" show-overflow-tooltip>
              <template #default>
                {{ activityTitle }}
              </template>
            </el-table-column>
            <el-table-column label="报名时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.applyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="getRegistrationStatusTagType(row.status)"
                  size="small"
                  effect="light"
                >
                  {{ getRegistrationStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核备注" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.auditRemark || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right" align="center">
              <template #default="{ row }">
                <div v-if="isPending(row)" class="table-actions">
                  <el-button
                    link
                    type="success"
                    :loading="auditingId === row.id"
                    @click="handleApprove(row)"
                  >
                    通过
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    :disabled="auditingId === row.id"
                    @click="openRejectDialog(row)"
                  >
                    拒绝
                  </el-button>
                </div>
                <span v-else class="no-action">—</span>
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
      </template>
    </el-card>

    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝报名"
      width="480px"
      destroy-on-close
      @closed="rejectError = ''"
    >
      <p v-if="rejectingRow" class="dialog-text">
        报名人：{{ formatRegistrant(rejectingRow) }}
      </p>

      <el-alert
        v-if="rejectError"
        type="error"
        :title="rejectError"
        :closable="false"
        show-icon
        class="form-alert"
        role="alert"
      />

      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="90px">
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input
            v-model="rejectForm.auditRemark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请填写拒绝原因…"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="auditingId === rejectingRow?.id"
          @click="submitReject"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

