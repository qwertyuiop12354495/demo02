<script setup lang="ts">
import { onMounted } from 'vue'
import PromotionSummaryTable from '@/components/notice/PromotionSummaryTable.vue'
import { PROMOTION_TAB_OPTIONS } from '@/composables/useNoticeLabels'
import { usePromotionSummary } from '@/composables/usePromotionSummary'
import type { PromotionSummaryTab } from '@/types/notice'

const {
  loading,
  tab,
  page,
  pageSize,
  total,
  list,
  filterActivityId,
  fetchList,
  handleTabChange,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange,
} = usePromotionSummary()

function onTabChange(name: string | number) {
  handleTabChange(name as PromotionSummaryTab)
}

onMounted(() => {
  fetchList()
})

defineExpose({ refresh: fetchList })
</script>

<template>
  <div class="promotion-summary-panel">
    <el-tabs :model-value="tab" class="promotion-summary-panel__tabs" @tab-change="onTabChange">
      <el-tab-pane
        v-for="item in PROMOTION_TAB_OPTIONS"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <div class="promotion-summary-panel__toolbar">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="活动 ID">
          <el-input-number
            v-model="filterActivityId"
            :min="1"
            :controls="false"
            placeholder="可选"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <PromotionSummaryTable :list="list" :loading="loading" />

    <el-empty v-if="!loading && list.length === 0" description="当前页签下暂无公示数据" />

    <div v-if="total > pageSize" class="promotion-summary-panel__pager">
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
  </div>
</template>

<style scoped>
.promotion-summary-panel__tabs {
  margin-bottom: var(--space-4);
}

.promotion-summary-panel__toolbar {
  margin-bottom: var(--space-4);
}

.promotion-summary-panel__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
