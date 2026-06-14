/** 报名可报状态筛选（用户端列表本地筛选，契约无对应 Query 参数） */
export type RegisterStatusFilter =
  | 'ALL'
  | 'CAN_REGISTER'
  | 'NOT_STARTED'
  | 'CLOSED'
  | 'FULL'
  | 'REGISTERED'

export interface UserActivityListItem {
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
}

export interface ActivityPageQuery {
  page?: number
  pageSize?: number
  keyword?: string
}

export interface RegistrationBrief {
  id: number
  status: string
  applyTime: string
}

export interface ActivityDetail {
  id: number
  title: string
  description: string
  location: string
  eventStartTime: string
  eventEndTime: string
  registrationStartTime: string
  registrationDeadline: string
  maxParticipants: number
  approvedCount: number
  remainingSlots: number
  status: string
  canRegister: boolean
  registerDisabledReason: string | null
  myRegistration: RegistrationBrief | null
}

export interface RegistrationCreatePayload {
  activityId: number
  remark?: string
}

export interface RegistrationVO {
  id: number
  activityId: number
  activityTitle: string
  userId: number
  status: string
  applyTime: string
  auditTime: string | null
  auditRemark: string | null
  remark: string | null
}

export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface MyRegistrationListItem {
  id: number
  activityId: number
  activityTitle: string
  activityLocation: string
  eventStartTime: string
  status: RegistrationStatus
  applyTime: string
  auditTime: string | null
  auditRemark: string | null
  remark: string | null
}

export interface RegistrationMinePageQuery {
  page?: number
  pageSize?: number
  status?: RegistrationStatus
}

export interface RegistrationCancelVO {
  id: number
  activityId: number
  status: string
  applyTime: string
  auditTime: string | null
  auditRemark: string | null
}
