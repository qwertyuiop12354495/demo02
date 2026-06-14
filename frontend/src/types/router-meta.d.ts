import type { RoleType } from '@/types/role'

export {}

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    guest?: boolean
    requiresAuth?: boolean
    roles?: RoleType[]
    title?: string
  }
}
