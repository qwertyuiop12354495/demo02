<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ScoreResultSummary from '@/components/admin/score/ScoreResultSummary.vue'
import ScoreSubmitDialog from '@/components/admin/score/ScoreSubmitDialog.vue'
import ScoreWorkTable from '@/components/admin/score/ScoreWorkTable.vue'
import WorkMaterialsDrawer from '@/components/admin/score/WorkMaterialsDrawer.vue'
import { useScorerScope } from '@/composables/useScorerScope'
import { useScoreWorks } from '@/composables/useScoreWorks'
import type { ScoreWorkListItem, ScopedScorerStats } from '@/types/admin-score'

const { pageTitle, pageDescription, scoreStepLabel } = useScorerScope()

const {
  loading,
  actingWorkId,
  page,
  pageSize,
  total,
  list,
  filterActivityId,
  lastSubmitResult,
  fetchList,
  handleSearch,
  handleReset,
  handlePageChange,
  handleSizeChange,
  loadScorerStats,
  submitReview,
  clearLastResult,
} = useScoreWorks()

const scoreDialogVisible = ref(false)
const materialsDrawerVisible = ref(false)
const activeRow = ref<ScoreWorkListItem | null>(null)
const scorerStats = ref<ScopedScorerStats | null>(null)
const statsLoading = ref(false)

const showEmpty = computed(() => !loading.value && list.value.length === 0)

async function openScoreDialog(row: ScoreWorkListItem) {
  activeRow.value = row
  scoreDialogVisible.value = true
  scorerStats.value = null
  statsLoading.value = true
  try {
    scorerStats.value = await loadScorerStats(row.id)
  } catch {
    // request 拦截器已展示后端 message
  } finally {
    statsLoading.value = false
  }
}

function openMaterialsDrawer(row: ScoreWorkListItem) {
  activeRow.value = row
  materialsDrawerVisible.value = true
}

async function handleScoreSubmit(payload: { manualScore: number; aiScore?: number | null }) {
  if (!activeRow.value) {
    return
  }
  try {
    await submitReview(activeRow.value, payload)
    scoreDialogVisible.value = false
  } catch {
    // request 拦截器已展示后端 message
  }
}

onMounted(() => {
  clearLastResult()
  fetchList()
})
</script>

<template>
  <div class="app-page">
    <PageHeader :title="pageTitle" :description="pageDescription">
      <template #extra>
        <el-tag v-if="scoreStepLabel" type="info" effect="plain">
          当前环节：{{ scoreStepLabel }}
        </el-tag>
      </template>
    </PageHeader>

    <ScoreResultSummary v-if="lastSubmitResult" :result="lastSubmitResult" />

    <el-card shadow="never" class="ui-card filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
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
    </el-card>

    <el-card shadow="never" class="ui-card table-card">
      <ScoreWorkTable
        :list="list"
        :loading="loading"
        :acting-work-id="actingWorkId"
        @score="openScoreDialog"
        @materials="openMaterialsDrawer"
      />

      <el-empty v-if="showEmpty" description="当前暂无待评作品" />

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

    <ScoreSubmitDialog
      v-model:visible="scoreDialogVisible"
      :row="activeRow"
      :stats="scorerStats"
      :stats-loading="statsLoading"
      :submitting="actingWorkId !== null"
      @submit="handleScoreSubmit"
    />

    <WorkMaterialsDrawer
      v-model:visible="materialsDrawerVisible"
      :work-id="activeRow?.id ?? null"
      :work-title="activeRow?.title"
    />
  </div>
</template>

<style scoped>
.table-card__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
