<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import WorkReviewTable from '@/components/admin/work-review/WorkReviewTable.vue'
import WorkRevisionDialog from '@/components/admin/work-review/WorkRevisionDialog.vue'
import WorkRevisionHistoryDrawer from '@/components/admin/work-review/WorkRevisionHistoryDrawer.vue'
import { useAdminReviewScope } from '@/composables/useAdminReviewScope'
import { useRevisionFeedbackCache } from '@/composables/useRevisionFeedbackCache'
import { useWorkReview } from '@/composables/useWorkReview'
import type { WorkReviewListItem } from '@/types/admin-work-review'

const { pageTitle, pageDescription, reviewStepLabel, canReviewWork } = useAdminReviewScope()
const { listByWork } = useRevisionFeedbackCache()

const {
  loading,
  actingWorkId,
  activitySearching,
  page,
  pageSize,
  total,
  list,
  selectedActivityId,
  activityOptions,
  searchActivities,
  fetchList,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange,
  approve,
  submitRevision,
} = useWorkReview()

const revisionDialogVisible = ref(false)
const historyDrawerVisible = ref(false)
const activeRow = ref<WorkReviewListItem | null>(null)

const showEmpty = computed(() => !loading.value && list.value.length === 0)

const historyItems = computed(() => {
  if (!activeRow.value) {
    return []
  }
  return listByWork(activeRow.value.id, activeRow.value.currentStep)
})

function openRevisionDialog(row: WorkReviewListItem) {
  activeRow.value = row
  revisionDialogVisible.value = true
}

function openHistoryDrawer(row: WorkReviewListItem) {
  activeRow.value = row
  historyDrawerVisible.value = true
}

async function handleApprove(row: WorkReviewListItem) {
  try {
    await approve(row)
  } catch {
    // request 拦截器已展示后端 message
  }
}

async function handleRevisionSubmit(feedback: string) {
  if (!activeRow.value) {
    return
  }
  try {
    await submitRevision(activeRow.value, feedback)
    revisionDialogVisible.value = false
  } catch {
    // request 拦截器已展示后端 message
  }
}

onMounted(async () => {
  try {
    await searchActivities('')
  } catch {
    // 活动筛选加载失败不阻塞待审列表
  }
  await fetchList()
})
</script>

<template>
  <div class="app-page">
    <PageHeader :title="pageTitle" :description="pageDescription">
      <template #extra>
        <el-tag v-if="reviewStepLabel" type="info" effect="plain">
          当前环节：{{ reviewStepLabel }}
        </el-tag>
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
            clearable
            reserve-keyword
            placeholder="全部活动（可搜索筛选）…"
            :remote-method="searchActivities"
            :loading="activitySearching"
          >
            <el-option
              v-for="item in activityOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="ui-card table-card">
      <WorkReviewTable
        :list="list"
        :loading="loading"
        :acting-work-id="actingWorkId"
        :can-review="canReviewWork"
        @approve="handleApprove"
        @revision="openRevisionDialog"
        @history="openHistoryDrawer"
      />

      <el-empty v-if="showEmpty" description="当前暂无待审核作品" />

      <div v-if="total > pageSize" class="table-card__pager">
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
    </el-card>

    <WorkRevisionDialog
      v-model:visible="revisionDialogVisible"
      :row="activeRow"
      :submitting="actingWorkId !== null"
      @submit="handleRevisionSubmit"
    />

    <WorkRevisionHistoryDrawer
      v-model:visible="historyDrawerVisible"
      :row="activeRow"
      :history="historyItems"
    />
  </div>
</template>

<style scoped>
.filter-activity {
  width: 280px;
}

.table-card__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
