<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import type { PromotionSummaryItem } from '@/types/notice'

defineProps<{
  list: PromotionSummaryItem[]
  loading?: boolean
}>()

function formatScore(value: number | null | undefined): string {
  if (value == null || Number.isNaN(Number(value))) {
    return '—'
  }
  return Number(value).toFixed(2)
}
</script>

<template>
  <el-table v-loading="loading" :data="list" stripe class="promotion-summary-table">
    <el-table-column prop="workTitle" label="作品名" min-width="160" show-overflow-tooltip />
    <el-table-column prop="schoolName" label="学校" min-width="140" show-overflow-tooltip>
      <template #default="{ row }">{{ row.schoolName || '—' }}</template>
    </el-table-column>
    <el-table-column prop="districtName" label="区县" min-width="120" show-overflow-tooltip>
      <template #default="{ row }">{{ row.districtName || '—' }}</template>
    </el-table-column>
    <el-table-column prop="cityName" label="市" min-width="120" show-overflow-tooltip>
      <template #default="{ row }">{{ row.cityName || '—' }}</template>
    </el-table-column>
    <el-table-column prop="provinceName" label="省" min-width="120" show-overflow-tooltip>
      <template #default="{ row }">{{ row.provinceName || '—' }}</template>
    </el-table-column>
    <el-table-column label="均分" width="90" align="right">
      <template #default="{ row }">{{ formatScore(row.averageScore) }}</template>
    </el-table-column>
    <el-table-column label="公示时间" width="170">
      <template #default="{ row }">{{ formatDateTime(row.publishedAt) }}</template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.promotion-summary-table {
  width: 100%;
}
</style>
