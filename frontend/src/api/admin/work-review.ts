import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  WorkReviewActionResult,
  WorkReviewListItem,
  WorkReviewPageQuery,
  WorkRevisionFeedbackPayload,
} from '@/types/admin-work-review'

export function listWorkReviews(params?: WorkReviewPageQuery) {
  return request<PageResult<WorkReviewListItem>>({
    url: '/admin/work-reviews',
    method: 'GET',
    params,
  })
}

export function approveWorkReview(workId: number) {
  return request<WorkReviewActionResult>({
    url: `/admin/work-reviews/${workId}/approve`,
    method: 'POST',
  })
}

export function submitWorkRevisionFeedback(workId: number, data: WorkRevisionFeedbackPayload) {
  return request<WorkReviewActionResult>({
    url: `/admin/work-reviews/${workId}/revision-feedback`,
    method: 'POST',
    data,
  })
}
