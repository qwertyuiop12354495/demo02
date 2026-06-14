<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import { formatTeacherLabel } from '@/composables/useScorerScope'
import type { ScoreWorkListItem } from '@/types/admin-score'

defineProps<{
  list: ScoreWorkListItem[]
  loading?: boolean
  actingWorkId?: number | null
}>()

const emit = defineEmits<{
  score: [row: ScoreWorkListItem]
  materials: [row: ScoreWorkListItem]
}>()
</script>

<template>
  <el-table v-loading="loading" :data="list" stripe class="score-work-table">
    <el-table-column prop="title" label="作品名" min-width="160" show-overflow-tooltip />
    <el-table-column prop="activityTitle" label="活动" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">{{ row.activityTitle || '—' }}</template>
    </el-table-column>
    <el-table-column prop="category" label="类别" width="100">
      <template #default="{ row }">{{ row.category || '—' }}</template>
    </el-table-column>
    <el-table-column label="教师" width="110">
      <template #default="{ row }">{{ formatTeacherLabel(row.teacherId) }}</template>
    </el-table-column>
    <el-table-column label="更新时间" width="170">
      <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="emit('materials', row)">材料</el-button>
        <el-button
          link
          type="success"
          :loading="actingWorkId === row.id"
          @click="emit('score', row)"
        >
          打分
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.score-work-table {
  width: 100%;
}
</style>
