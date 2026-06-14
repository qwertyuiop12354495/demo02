import type { FinalResult, WorkStatus, WorkStep } from '@/types/work'

export interface ScoreWorkListItem {
  id: number
  activityId: number
  activityTitle: string | null
  title: string
  category: string | null
  teacherId: number
  currentStep: WorkStep
  currentStatus: WorkStatus
  finalResult: FinalResult
  updatedAt: string
}

export interface ScoreWorkPageQuery {
  page?: number
  pageSize?: number
  activityId?: number
}

export interface SubmitScoreReviewPayload {
  manualScore: number
  aiScore?: number | null
}

export interface ScopedScorerStats {
  reviewLevel: string
  requiredCount: number
  completedCount: number
}

export interface SubmitScoreReviewResult {
  workId: number
  currentStep: WorkStep
  currentStatus: WorkStatus
  finalResult: FinalResult
  finalScore: number | null
  requiredCount: number
  completedCount: number
  allCompleted: boolean
  message: string
}
