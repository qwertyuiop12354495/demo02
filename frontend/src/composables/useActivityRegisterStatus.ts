import type { RegisterStatusFilter, UserActivityListItem } from '@/types/activity'

export type ActivityRegisterTagType = 'success' | 'warning' | 'info' | 'danger'

export interface ActivityRegisterStatusSource {
  canRegister: boolean
  registerDisabledReason: string | null
}

export function getActivityRegisterStatusLabel(row: ActivityRegisterStatusSource): string {
  if (row.canRegister) {
    return '可报名'
  }
  return row.registerDisabledReason ?? '不可报名'
}

export function getActivityRegisterStatusTagType(
  row: ActivityRegisterStatusSource,
): ActivityRegisterTagType {
  if (row.canRegister) {
    return 'success'
  }

  const reason = row.registerDisabledReason
  if (reason === '已报名') {
    return 'info'
  }
  if (reason === '报名尚未开始') {
    return 'warning'
  }
  if (
    reason === '活动报名已截止' ||
    reason === '活动名额已满' ||
    reason === '活动已下架'
  ) {
    return 'danger'
  }
  return 'info'
}

export function matchesActivityRegisterFilter(
  item: UserActivityListItem,
  filter: RegisterStatusFilter,
): boolean {
  switch (filter) {
    case 'CAN_REGISTER':
      return item.canRegister
    case 'NOT_STARTED':
      return item.registerDisabledReason === '报名尚未开始'
    case 'CLOSED':
      return item.registerDisabledReason === '活动报名已截止'
    case 'FULL':
      return item.registerDisabledReason === '活动名额已满'
    case 'REGISTERED':
      return item.registerDisabledReason === '已报名'
    default:
      return true
  }
}
