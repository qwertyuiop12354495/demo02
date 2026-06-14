import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  ActivityFormPayload,
  ActivityStatusUpdateVO,
  AdminActivityListItem,
  AdminActivityPageQuery,
  AdminActivityVO,
} from '@/types/admin-activity'

export function getAdminActivities(params: AdminActivityPageQuery) {
  return request<PageResult<AdminActivityListItem>>({
    url: '/admin/activities',
    method: 'GET',
    params,
  })
}

export function createAdminActivity(data: ActivityFormPayload) {
  return request<AdminActivityVO>({
    url: '/admin/activities',
    method: 'POST',
    data,
  })
}

export function updateAdminActivity(id: number, data: ActivityFormPayload) {
  return request<AdminActivityVO>({
    url: `/admin/activities/${id}`,
    method: 'PUT',
    data,
  })
}

export function updateAdminActivityStatus(id: number, status: 'PUBLISHED' | 'OFFLINE') {
  return request<ActivityStatusUpdateVO>({
    url: `/admin/activities/${id}/status`,
    method: 'PATCH',
    data: { status },
  })
}
