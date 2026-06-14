import type { WorkStatus } from '@/types/work'

export interface ActivityHomeListItem {
  id: number
  title: string
  location: string
  eventStartTime: string
  eventEndTime: string
  registrationStartTime: string
  registrationDeadline: string
  maxParticipants: number
  approvedCount: number
  remainingSlots: number
  canRegister: boolean
  registerDisabledReason: string | null
  myWorkId: number | null
  myWorkStatus: WorkStatus | null
  /** 首页标签文案；DRAFT 等为 null */
  myWorkStatusLabel: string | null
}

export interface ActivityHomePageQuery {
  page?: number
  pageSize?: number
  keyword?: string
}
