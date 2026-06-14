<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import {
  formatScopeRegion,
  formatTeacherLabel,
} from '@/composables/useAdminReviewScope'
import type { WorkReviewListItem } from '@/types/admin-work-review'
import WorkStatusTag from '@/components/work/WorkStatusTag.vue'

defineProps<{
  list: WorkReviewListItem[]
  loading?: boolean
  actingWorkId?: number | null
  canReview: (row: WorkReviewListItem) => boolean
}>()

const emit = defineEmits<{
  approve: [row: WorkReviewListItem]
  revision: [row: WorkReviewListItem]
  history: [row: WorkReviewListItem]
}>()

function statusLabel(status: string): string {
  if (status === 'SUBMITTED') {
    return '待审核'
  }
  if (status === 'REVISION_REQUIRED') {
    return '需修改'
  }
  if (status === 'APPROVED') {
    return '已通过'
  }
  if (status === 'DRAFT') {
    return '草稿'
  }
  return status
}

function statusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
  if (status === 'SUBMITTED') {
    return 'info'
  }
  if (status === 'REVISION_REQUIRED') {
    return 'warning'
  }
  if (status === 'APPROVED') {
    return 'success'
  }
  return 'info'
}
</script>

<template>
  <el-table v-loading="loading" :data="list" stripe class="work-review-table">
    <el-table-column prop="title" label="作品名" min-width="160" show-overflow-tooltip />
    <el-table-column label="教师" width="110">
      <template #default="{ row }">{{ formatTeacherLabel(row.teacherId) }}</template>
    </el-table-column>
    <el-table-column prop="schoolName" label="学校" min-width="140" show-overflow-tooltip>
      <template #default="{ row }">{{ row.schoolName || '—' }}</template>
    </el-table-column>
    <el-table-column label="辖区" min-width="180" show-overflow-tooltip>
      <template #default="{ row }">{{ formatScopeRegion(row) }}</template>
    </el-table-column>
    <el-table-column prop="activityTitle" label="活动" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ row.activityTitle || '—' }}</template>
    </el-table-column>
    <el-table-column label="当前状态" width="100">
      <template #default="{ row }">
        <WorkStatusTag :label="statusLabel(row.currentStatus)" :tone="statusTone(row.currentStatus)" />
      </template>
    </el-table-column>
    <el-table-column label="提交时间" width="170">
      <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="220" fixed="right">
      <template #default="{ row }">
        <template v-if="canReview(row)">
          <el-button
            link
            type="success"
            :loading="actingWorkId === row.id"
            @click="emit('approve', row)"
          >
            通过
          </el-button>
          <el-button
            link
            type="warning"
            :disabled="actingWorkId === row.id"
            @click="emit('revision', row)"
          >
            退回修改
          </el-button>
        </template>
        <el-button link type="primary" @click="emit('history', row)">修改意见</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.work-review-table {
  width: 100%;
}
</style>
