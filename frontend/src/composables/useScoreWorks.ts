import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getScopedScorers, listScoreWorks, submitScoreReview } from '@/api/admin/score'
import type {
  ScoreWorkListItem,
  ScopedScorerStats,
  SubmitScoreReviewPayload,
  SubmitScoreReviewResult,
} from '@/types/admin-score'

export function useScoreWorks() {
  const loading = ref(false)
  const actingWorkId = ref<number | null>(null)
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const list = ref<ScoreWorkListItem[]>([])
  const filterActivityId = ref<number | null>(null)
  const lastSubmitResult = ref<SubmitScoreReviewResult | null>(null)

  async function fetchList() {
    loading.value = true
    try {
      const data = await listScoreWorks({
        page: page.value,
        pageSize: pageSize.value,
        activityId: filterActivityId.value ?? undefined,
      })
      list.value = data.list
      total.value = data.total
      page.value = data.page
      pageSize.value = data.pageSize
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    page.value = 1
    fetchList()
  }

  function handleReset() {
    filterActivityId.value = null
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

  async function loadScorerStats(workId: number): Promise<ScopedScorerStats> {
    return getScopedScorers(workId)
  }

  async function submitReview(
    row: ScoreWorkListItem,
    payload: SubmitScoreReviewPayload,
  ): Promise<SubmitScoreReviewResult> {
    actingWorkId.value = row.id
    try {
      const result = await submitScoreReview(row.id, payload)
      lastSubmitResult.value = result
      ElMessage.success(result.message || '评分已提交')
      await fetchList()
      return result
    } finally {
      actingWorkId.value = null
    }
  }

  function clearLastResult() {
    lastSubmitResult.value = null
  }

  return {
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
  }
}
