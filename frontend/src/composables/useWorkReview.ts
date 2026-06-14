import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  approveWorkReview,
  listWorkReviews,
  submitWorkRevisionFeedback,
} from '@/api/admin/work-review'
import { getActivities } from '@/api/activity'
import { useRevisionFeedbackCache } from '@/composables/useRevisionFeedbackCache'
import type { WorkReviewListItem } from '@/types/admin-work-review'
import { useUserStore } from '@/stores/user'

export function useWorkReview() {
  const userStore = useUserStore()
  const { appendFeedback } = useRevisionFeedbackCache()

  const loading = ref(false)
  const actingWorkId = ref<number | null>(null)
  const activitySearching = ref(false)
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const list = ref<WorkReviewListItem[]>([])
  const selectedActivityId = ref<number | null>(null)
  const activityOptions = ref<{ id: number; title: string }[]>([])

  async function searchActivities(keyword: string) {
    activitySearching.value = true
    try {
      const data = await getActivities({
        keyword: keyword.trim() || undefined,
        page: 1,
        pageSize: 50,
      })
      activityOptions.value = data.list.map((item) => ({
        id: item.id,
        title: item.title,
      }))
    } finally {
      activitySearching.value = false
    }
  }

  async function fetchList() {
    loading.value = true
    try {
      const data = await listWorkReviews({
        page: page.value,
        pageSize: pageSize.value,
        activityId: selectedActivityId.value ?? undefined,
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
    selectedActivityId.value = null
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

  async function approve(row: WorkReviewListItem) {
    await ElMessageBox.confirm(
      `确认通过作品「${row.title}」的报名审核？通过后将进入下一审核环节。`,
      '审核通过',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    actingWorkId.value = row.id
    try {
      await approveWorkReview(row.id)
      ElMessage.success('审核通过')
      await fetchList()
    } finally {
      actingWorkId.value = null
    }
  }

  async function submitRevision(row: WorkReviewListItem, feedback: string) {
    actingWorkId.value = row.id
    try {
      await submitWorkRevisionFeedback(row.id, { feedback })
      appendFeedback(row.id, row.currentStep, feedback, userStore.user?.id)
      ElMessage.success('已退回修改')
      await fetchList()
    } finally {
      actingWorkId.value = null
    }
  }

  return {
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
  }
}
