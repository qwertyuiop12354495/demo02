import { request } from '@/utils/request'
import type {
  MyRegistrationListItem,
  RegistrationCancelVO,
  RegistrationCreatePayload,
  RegistrationMinePageQuery,
  RegistrationVO,
} from '@/types/activity'
import type { PageResult } from '@/types/api'

export function createRegistration(data: RegistrationCreatePayload) {
  return request<RegistrationVO>({
    url: '/registrations',
    method: 'POST',
    data,
  })
}

export function getMyRegistrations(params: RegistrationMinePageQuery) {
  return request<PageResult<MyRegistrationListItem>>({
    url: '/registrations/mine',
    method: 'GET',
    params,
  })
}

export function cancelRegistration(id: number) {
  return request<RegistrationCancelVO>({
    url: `/registrations/${id}/cancel`,
    method: 'PATCH',
  })
}
