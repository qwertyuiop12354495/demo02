<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import ManualNoticeDetailDrawer from '@/components/notice/ManualNoticeDetailDrawer.vue'
import ManualNoticeTable from '@/components/notice/ManualNoticeTable.vue'
import { useManualNotices } from '@/composables/useManualNotices'
import type { ManualNoticeListItem } from '@/types/notice'

const {
  loading,
  detailLoading,
  page,
  pageSize,
  total,
  list,
  detail,
  fetchList,
  loadDetail,
  handlePageChange,
  handleSizeChange,
  clearDetail,
} = useManualNotices()

const drawerVisible = ref(false)

async function openDetail(row: ManualNoticeListItem) {
  drawerVisible.value = true
  try {
    await loadDetail(row.id)
  } catch {
    drawerVisible.value = false
    clearDetail()
  }
}

watch(drawerVisible, (visible) => {
  if (!visible) {
    clearDetail()
  }
})

onMounted(() => {
  fetchList()
})

defineExpose({ refresh: fetchList })
</script>

<template>
  <div class="manual-notice-panel">
    <ManualNoticeTable :list="list" :loading="loading" @view="openDetail" />

    <el-empty v-if="!loading && list.length === 0" description="暂无手工公示" />

    <div v-if="total > pageSize" class="manual-notice-panel__pager">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <ManualNoticeDetailDrawer
      v-model:visible="drawerVisible"
      :detail="detail"
      :loading="detailLoading"
    />
  </div>
</template>

<style scoped>
.manual-notice-panel__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
