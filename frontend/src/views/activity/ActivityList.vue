<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import {
  getActivityRegisterStatusLabel,
  getActivityRegisterStatusTagType,
  matchesActivityRegisterFilter,
} from '@/composables/useActivityRegisterStatus'
import { getActivities } from '@/api/activity'
import { createRegistration } from '@/api/registration'
import type { RegisterStatusFilter, UserActivityListItem } from '@/types/activity'

const router = useRouter()

const loading = ref(false)
const registering = ref(false)
const keyword = ref('')
const statusFilter = ref<RegisterStatusFilter>('ALL')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<UserActivityListItem[]>([])

const registerDialogVisible = ref(false)
const currentActivity = ref<UserActivityListItem | null>(null)

const registerForm = reactive({
  remark: '',
})

const statusOptions: { label: string; value: RegisterStatusFilter }[] = [
  { label: '全部', value: 'ALL' },
  { label: '可报名', value: 'CAN_REGISTER' },
  { label: '尚未开始', value: 'NOT_STARTED' },
  { label: '已截止', value: 'CLOSED' },
  { label: '名额已满', value: 'FULL' },
  { label: '已报名', value: 'REGISTERED' },
]

const filteredList = computed(() => {
  if (statusFilter.value === 'ALL') {
    return list.value
  }
  return list.value.filter((item) => matchesActivityRegisterFilter(item, statusFilter.value))
})

const isStatusFilterActive = computed(() => statusFilter.value !== 'ALL')

const showEmpty = computed(() => !loading.value && filteredList.value.length === 0)

const emptyDescription = computed(() => {
  if (list.value.length === 0) {
    return '暂无活动'
  }
  if (isStatusFilterActive.value) {
    return '当前筛选条件下暂无活动'
  }
  return '暂无活动'
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getActivities({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
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
  statusFilter.value = 'ALL'
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

function goDetail(row: UserActivityListItem) {
  router.push(`/activities/${row.id}`)
}

function openRegisterDialog(row: UserActivityListItem) {
  currentActivity.value = row
  registerForm.remark = ''
  registerDialogVisible.value = true
}

async function submitRegister() {
  if (!currentActivity.value) {
    return
  }
  registering.value = true
  try {
    await createRegistration({
      activityId: currentActivity.value.id,
      remark: registerForm.remark.trim() || undefined,
    })
    ElMessage.success('报名成功，请等待审核')
    registerDialogVisible.value = false
    await fetchList()
  } finally {
    registering.value = false
  }
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function formatEventTime(row: UserActivityListItem) {
  return `${formatDateTime(row.eventStartTime)} ~ ${formatDateTime(row.eventEndTime)}`
}

function formatParticipants(row: UserActivityListItem) {
  return `${row.approvedCount} / ${row.maxParticipants}（剩余 ${row.remainingSlots}）`
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="portal-page">
    <PortalPageHeader
      title="活动列表"
      description="浏览已上架活动，查看详情或提交报名申请。"
    />

    <el-card shadow="never" class="portal-glass-card portal-glass-card--float filter-card">
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
          <el-select v-model="statusFilter" class="filter-select" placeholder="全部">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
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
      <p v-if="isStatusFilterActive" class="filter-tip">
        活动状态筛选作用于当前页数据；切换页码后请重新筛选。
      </p>
    </el-card>

    <el-card shadow="never" class="portal-glass-card portal-glass-card--float table-card">
      <el-empty v-if="showEmpty" :description="emptyDescription" class="table-empty" />

      <el-table
        v-else
        v-loading="loading"
        :data="filteredList"
        stripe
          class="app-table"
      >
        <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="活动时间" min-width="240">
          <template #default="{ row }">
            {{ formatEventTime(row) }}
          </template>
        </el-table-column>
        <el-table-column label="报名截止时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationDeadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="140" show-overflow-tooltip />
        <el-table-column label="人数" min-width="150" align="center">
          <template #default="{ row }">
            <span class="tabular-nums">{{ formatParticipants(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getActivityRegisterStatusTagType(row)"
              size="small"
              effect="light"
            >
              {{ getActivityRegisterStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="goDetail(row)">查看详情</el-button>
              <el-tooltip
                :content="row.registerDisabledReason || '暂不可报名'"
                :disabled="row.canRegister"
                placement="top"
              >
                <span class="action-tooltip-wrap">
                  <el-button
                    link
                    type="primary"
                    :disabled="!row.canRegister"
                    @click="openRegisterDialog(row)"
                  >
                    立即报名
                  </el-button>
                </span>
              </el-tooltip>
            </div>
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
    </el-card>

    <el-dialog
      v-model="registerDialogVisible"
      title="立即报名"
      width="480px"
      destroy-on-close
    >
      <p v-if="currentActivity" class="dialog-subtitle">
        {{ currentActivity.title }}
      </p>
      <el-form label-width="80px">
        <el-form-item label="备注">
          <el-input
            v-model="registerForm.remark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="选填，最多 500 字"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="submitRegister">
          确认报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.action-tooltip-wrap {
  display: inline-flex;
}
</style>
