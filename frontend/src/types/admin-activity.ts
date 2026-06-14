export type ActivityStatus = 'DRAFT' | 'PUBLISHED' | 'OFFLINE'

export interface AdminActivityListItem {
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
  status: ActivityStatus
}

export interface AdminActivityPageQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: ActivityStatus
}

export interface ActivityFormPayload {
  title: string
  description?: string
  location?: string
  eventStartTime?: string
  eventEndTime?: string
  registrationStartTime: string
  registrationDeadline: string
  maxParticipants: number
}

export interface AdminActivityVO extends ActivityFormPayload {
  id: number
  approvedCount: number
  status: ActivityStatus
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface ActivityStatusUpdateVO {
  id: number
  title: string
  status: ActivityStatus
  updatedAt: string
}
