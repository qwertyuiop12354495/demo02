import type { ActivityStatus } from '@/types/admin-activity'

export type AdminActivityStatusTagType = 'success' | 'warning' | 'info'

const STATUS_LABEL_MAP: Record<ActivityStatus, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已上架',
  OFFLINE: '已下架',
}

const STATUS_TAG_TYPE_MAP: Record<ActivityStatus, AdminActivityStatusTagType> = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  OFFLINE: 'warning',
}

export function getAdminActivityStatusLabel(status: ActivityStatus): string {
  return STATUS_LABEL_MAP[status]
}

export function getAdminActivityStatusTagType(status: ActivityStatus): AdminActivityStatusTagType {
  return STATUS_TAG_TYPE_MAP[status]
}

export function canPublishActivity(status: ActivityStatus): boolean {
  return status === 'DRAFT' || status === 'OFFLINE'
}

export function canOfflineActivity(status: ActivityStatus): boolean {
  return status === 'PUBLISHED'
}
