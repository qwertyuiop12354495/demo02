import { ref } from 'vue'
import { getManualNoticeDetail, listManualNotices } from '@/api/notice'
import type { ManualNoticeDetail, ManualNoticeListItem } from '@/types/notice'

export function useManualNotices() {
  const loading = ref(false)
  const detailLoading = ref(false)
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const list = ref<ManualNoticeListItem[]>([])
  const detail = ref<ManualNoticeDetail | null>(null)

  async function fetchList() {
    loading.value = true
    try {
      const data = await listManualNotices({
        page: page.value,
        pageSize: pageSize.value,
      })
      list.value = data.list
      total.value = data.total
      page.value = data.page
      pageSize.value = data.pageSize
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(id: number) {
    detailLoading.value = true
    detail.value = null
    try {
      detail.value = await getManualNoticeDetail(id)
      return detail.value
    } finally {
      detailLoading.value = false
    }
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

  function clearDetail() {
    detail.value = null
  }

  return {
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
  }
}
