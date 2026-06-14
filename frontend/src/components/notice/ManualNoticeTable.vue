<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import { getNoticeTypeLabel, getVisibleScopeLabel } from '@/composables/useNoticeLabels'
import type { ManualNoticeListItem } from '@/types/notice'

defineProps<{
  list: ManualNoticeListItem[]
  loading?: boolean
}>()

const emit = defineEmits<{
  view: [row: ManualNoticeListItem]
}>()
</script>

<template>
  <el-table v-loading="loading" :data="list" stripe class="manual-notice-table">
    <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
    <el-table-column label="类型" width="110">
      <template #default="{ row }">{{ getNoticeTypeLabel(row.noticeType) }}</template>
    </el-table-column>
    <el-table-column label="可见范围" width="120">
      <template #default="{ row }">{{ getVisibleScopeLabel(row.visibleScopeType) }}</template>
    </el-table-column>
    <el-table-column prop="publisherName" label="发布人" width="120">
      <template #default="{ row }">{{ row.publisherName || '—' }}</template>
    </el-table-column>
    <el-table-column label="发布时间" width="170">
      <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="90" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="emit('view', row)">查看</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.manual-notice-table {
  width: 100%;
}
</style>
