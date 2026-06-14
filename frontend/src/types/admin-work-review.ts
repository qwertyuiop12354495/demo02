import type { WorkStatus, WorkStep } from '@/types/work'

export interface WorkReviewListItem {
  id: number
  activityId: number
  activityTitle: string | null
  title: string
  category: string | null
  teacherId: number
  provinceName: string | null
  cityName: string | null
  districtName: string | null
  schoolName: string | null
  currentStep: WorkStep
  currentStatus: WorkStatus
  createdAt: string
  updatedAt: string
}

export interface WorkReviewPageQuery {
  page?: number
  pageSize?: number
  activityId?: number
}

export interface WorkRevisionFeedbackPayload {
  feedback: string
}

export interface WorkReviewActionResult {
  workId: number
  currentStep: WorkStep
  currentStatus: WorkStatus
}

export interface CachedRevisionFeedback {
  workId: number
  reviewStep: WorkStep
  roundNo: number
  feedback: string
  reviewerId?: number
  createdAt: string
}
