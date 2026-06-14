import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'

const TOKEN_KEY = 'activity_token'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 200) {
        return payload.data as never
      }
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(payload)
    }
    return response.data as never
  },
  (error) => {
    const message =
      error.response?.data?.message ?? error.message ?? '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return http.request<unknown, T>(config)
}

export default http
