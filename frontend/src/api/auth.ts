import { request } from '@/utils/request'
import type { LoginPayload, LoginResult } from '@/types/auth'

export function login(data: LoginPayload) {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data,
  })
}
