import { ref } from 'vue'
import { listPromotionSummary } from '@/api/notice'
import type { PromotionSummaryItem, PromotionSummaryTab } from '@/types/notice'

export function usePromotionSummary() {
  const loading = ref(false)
  const tab = ref<PromotionSummaryTab>('DISTRICT_PROMOTED')
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const list = ref<PromotionSummaryItem[]>([])
  const filterActivityId = ref<number | null>(null)

  async function fetchList() {
    loading.value = true
    try {
      const data = await listPromotionSummary({
        tab: tab.value,
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

  function handleTabChange(nextTab: PromotionSummaryTab) {
    tab.value = nextTab
    page.value = 1
    fetchList()
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

  return {
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
  }
}
