import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  ManualNoticeDetail,
  ManualNoticeListItem,
  ManualNoticePageQuery,
  PromotionSummaryItem,
  PromotionSummaryQuery,
} from '@/types/notice'

export function listPromotionSummary(params: PromotionSummaryQuery) {
  return request<PageResult<PromotionSummaryItem>>({
    url: '/notices/promotion-summary',
    method: 'GET',
    params,
  })
}

export function listManualNotices(params?: ManualNoticePageQuery) {
  return request<PageResult<ManualNoticeListItem>>({
    url: '/notices/manual',
    method: 'GET',
    params,
  })
}

export function getManualNoticeDetail(id: number) {
  return request<ManualNoticeDetail>({
    url: `/notices/manual/${id}`,
    method: 'GET',
  })
}
