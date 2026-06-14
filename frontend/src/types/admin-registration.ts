import type { RegistrationStatus } from '@/types/activity'

export interface AdminRegistrationPageQuery {
  page?: number
  pageSize?: number
  status?: RegistrationStatus
}

export interface AdminRegistrationListItem {
  id: number
  userId: number
  username: string
  nickname: string
  status: RegistrationStatus
  applyTime: string
  auditTime: string | null
  auditRemark: string | null
  remark: string | null
  auditedBy: number | null
}

export interface ActivityRegistrationSummary {
  id: number
  title: string
  maxParticipants: number
  approvedCount: number
  pendingCount: number
}

export interface RegistrationAdminListVO {
  activity: ActivityRegistrationSummary
  list: AdminRegistrationListItem[]
  total: number
  page: number
  pageSize: number
}

export interface RegistrationAuditPayload {
  action: 'APPROVE' | 'REJECT'
  auditRemark?: string
}

export interface RegistrationAuditResult {
  id: number
  activityId: number
  userId: number
  status: RegistrationStatus
  applyTime: string
  auditTime: string | null
  auditRemark: string | null
  auditedBy: number | null
}
