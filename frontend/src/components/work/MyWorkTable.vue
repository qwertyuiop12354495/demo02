<script setup lang="ts">
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/composables/useDateTime'
import {
  getTeacherWorkStatusLabel,
  getWorkStatusTone,
  isWorkEditable,
} from '@/composables/useWorkStatusLabel'
import type { WorkListItem } from '@/types/work'
import WorkStatusTag from '@/components/work/WorkStatusTag.vue'

defineProps<{
  list: WorkListItem[]
  loading?: boolean
}>()

const router = useRouter()

function statusLabel(row: WorkListItem): string {
  return getTeacherWorkStatusLabel(row.currentStep, row.currentStatus, row.finalResult)
}

function goEnroll(row: WorkListItem) {
  router.push(`/works/enroll/${row.activityId}`)
}
</script>

<template>
  <el-table v-loading="loading" :data="list" stripe class="my-work-table">
    <el-table-column prop="activityTitle" label="活动" min-width="180" />
    <el-table-column prop="title" label="作品标题" min-width="160" />
    <el-table-column label="状态" min-width="220">
      <template #default="{ row }">
        <WorkStatusTag :label="statusLabel(row)" :tone="getWorkStatusTone(row.currentStatus)" />
      </template>
    </el-table-column>
    <el-table-column label="更新时间" width="170">
      <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="120" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="goEnroll(row)">
          {{ isWorkEditable(row.currentStatus) ? '继续编辑' : '查看详情' }}
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.my-work-table {
  width: 100%;
}
</style>
