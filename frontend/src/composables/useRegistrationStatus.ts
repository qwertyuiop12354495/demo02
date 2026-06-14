import type { RegistrationStatus } from '@/types/activity'

export type RegistrationStatusTagType = 'success' | 'warning' | 'info' | 'danger'

const STATUS_LABEL_MAP: Record<RegistrationStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED: '已取消',
}

const STATUS_TAG_TYPE_MAP: Record<RegistrationStatus, RegistrationStatusTagType> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'info',
}

export function getRegistrationStatusLabel(status: RegistrationStatus): string {
  return STATUS_LABEL_MAP[status]
}

export function getRegistrationStatusTagType(status: RegistrationStatus): RegistrationStatusTagType {
  return STATUS_TAG_TYPE_MAP[status]
}
