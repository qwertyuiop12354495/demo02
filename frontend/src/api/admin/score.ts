import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  ScoreWorkListItem,
  ScoreWorkPageQuery,
  ScopedScorerStats,
  SubmitScoreReviewPayload,
  SubmitScoreReviewResult,
} from '@/types/admin-score'

export function listScoreWorks(params?: ScoreWorkPageQuery) {
  return request<PageResult<ScoreWorkListItem>>({
    url: '/admin/scores/works',
    method: 'GET',
    params,
  })
}

export function getScopedScorers(workId: number) {
  return request<ScopedScorerStats>({
    url: `/admin/scores/works/${workId}/scorers`,
    method: 'GET',
  })
}

export function submitScoreReview(workId: number, data: SubmitScoreReviewPayload) {
  return request<SubmitScoreReviewResult>({
    url: `/admin/scores/works/${workId}/review`,
    method: 'POST',
    data,
  })
}
