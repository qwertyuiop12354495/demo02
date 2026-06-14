import { request } from '@/utils/request'
import type {
  AdminRegistrationPageQuery,
  RegistrationAdminListVO,
  RegistrationAuditPayload,
  RegistrationAuditResult,
} from '@/types/admin-registration'

export function getActivityRegistrations(activityId: number, params: AdminRegistrationPageQuery) {
  return request<RegistrationAdminListVO>({
    url: `/admin/activities/${activityId}/registrations`,
    method: 'GET',
    params,
  })
}

export function auditRegistration(id: number, data: RegistrationAuditPayload) {
  return request<RegistrationAuditResult>({
    url: `/admin/registrations/${id}/audit`,
    method: 'PATCH',
    data,
  })
}
