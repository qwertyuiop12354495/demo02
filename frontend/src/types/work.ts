export type WorkStatus = 'DRAFT' | 'SUBMITTED' | 'REVISION_REQUIRED' | 'APPROVED'

export type WorkStep =
  | 'SCHOOL'
  | 'DISTRICT'
  | 'CITY'
  | 'PROVINCE'
  | 'SCORE_DISTRICT'
  | 'SCORE_CITY'
  | 'SCORE_PROVINCE'
  | 'COMPLETED'

export type FinalResult =
  | 'PENDING'
  | 'PROMOTED'
  | 'ELIMINATED'
  | 'AWARD'
  | 'NOT_AWARDED'

export type WorkCategory = 'MUSIC' | 'DANCE' | 'ART' | 'DRAMA' | 'OTHER'

export interface WorkFile {
  id: number
  workId: number
  fileName: string
  fileUrl: string
  fileType: string
  fileSize: number
  createdAt: string
}

export interface WorkVO {
  id: number
  activityId: number
  activityTitle: string | null
  teacherId: number
  title: string
  category: string | null
  equipment: string | null
  duration: number | null
  provinceName: string | null
  cityName: string | null
  districtName: string | null
  schoolName: string | null
  currentStep: WorkStep
  currentStatus: WorkStatus
  finalScore: number | null
  finalResult: FinalResult
  createdAt: string
  updatedAt: string
  files: WorkFile[]
  /** 后端扩展字段：最新退回修改意见 */
  latestRevisionFeedback?: string | null
}

export interface WorkListItem {
  id: number
  activityId: number
  activityTitle: string | null
  title: string
  category: string | null
  currentStep: WorkStep
  currentStatus: WorkStatus
  finalResult: FinalResult
  createdAt: string
  updatedAt: string
}

export interface WorkMinePageQuery {
  page?: number
  pageSize?: number
  activityId?: number
  status?: WorkStatus
}

export interface WorkCreateDraftPayload {
  activityId: number
}

export interface WorkSavePayload {
  title: string
  category?: string
  equipment?: string
  duration: number
}

export interface WorkFileRegisterPayload {
  fileName: string
  fileUrl: string
  fileType: string
  fileSize: number
}
