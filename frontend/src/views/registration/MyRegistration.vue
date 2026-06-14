<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import { formatDateTime } from '@/composables/useDateTime'
import {
  getRegistrationStatusLabel,
  getRegistrationStatusTagType,
} from '@/composables/useRegistrationStatus'
import { cancelRegistration, getMyRegistrations } from '@/api/registration'
import type { MyRegistrationListItem, RegistrationStatus } from '@/types/activity'

const router = useRouter()

const loading = ref(true)
const cancellingId = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<MyRegistrationListItem[]>([])

const statusOptions: { label: string; value: RegistrationStatus | '' }[] = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' },
]

const statusFilter = ref<RegistrationStatus | ''>('')

const showEmpty = computed(() => !loading.value && list.value.length === 0)

const emptyDescription = computed(() => {
  if (statusFilter.value) {
    return '当前筛选条件下暂无报名记录'
  }
  return '暂无报名记录'
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMyRegistrations({
      page: page.value,
      pageSize: pageSize.value,
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

function handleFilterChange() {
  page.value = 1
  fetchList()
}

function handleReset() {
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

function goActivityDetail(row: MyRegistrationListItem) {
  router.push(`/activities/${row.activityId}`)
}

function goActivityList() {
  router.push('/activities')
}

function canCancel(row: MyRegistrationListItem) {
  return row.status === 'PENDING' || row.status === 'APPROVED'
}

async function handleCancel(row: MyRegistrationListItem) {
  try {
    await ElMessageBox.confirm(
      `确定要取消报名「${row.activityTitle}」吗？取消后如需参加需重新报名。`,
      '取消报名',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  cancellingId.value = row.id
  try {
    await cancelRegistration(row.id)
    ElMessage.success('已取消报名')
    await fetchList()
  } catch {
    // 失败提示由 request 拦截器展示后端 message
  } finally {
    cancellingId.value = null
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="portal-page">
    <PortalPageHeader
      title="我的报名"
      description="查看报名记录与审核进度，支持取消待审核或已通过的报名。"
    />

    <el-card shadow="never" class="portal-glass-card portal-glass-card--float filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="报名状态">
          <el-select
            v-model="statusFilter"
            class="filter-select"
            placeholder="全部状态"
            @change="handleFilterChange"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value || 'ALL'"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="portal-glass-card portal-glass-card--float table-card">
      <el-empty v-if="showEmpty" class="table-empty" :description="emptyDescription">
        <el-button type="primary" @click="goActivityList">去浏览活动</el-button>
      </el-empty>

      <template v-else>
        <el-table
          v-loading="loading"
          :data="list"
          stripe
          class="app-table"
          element-loading-text="加载中…"
        >
          <el-table-column
            prop="activityTitle"
            label="活动名称"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column label="活动时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.eventStartTime) }}
            </template>
          </el-table-column>
          <el-table-column label="报名时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="报名状态" width="110" align="center">
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
          <el-table-column
            label="审核备注"
            min-width="140"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              {{ row.auditRemark || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="goActivityDetail(row)">
                  查看活动
                </el-button>
                <el-button
                  v-if="canCancel(row)"
                  link
                  type="danger"
                  :loading="cancellingId === row.id"
                  @click="handleCancel(row)"
                >
                  取消报名
                </el-button>
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
      </template>
    </el-card>
  </div>
</template>

