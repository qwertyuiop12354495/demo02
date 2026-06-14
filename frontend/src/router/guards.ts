import type { Router } from 'vue-router'
import { getDefaultPathByRole } from '@/constants/menus'
import type { RoleType } from '@/types/role'
import { useUserStore } from '@/stores/user'

function resolveRequiredRoles(meta: { roles?: RoleType[] }): RoleType[] | null {
  if (!meta.roles || meta.roles.length === 0) {
    return null
  }
  return meta.roles
}

/** 登录后 redirect 仅在校管/教师等有权限的目标路由上生效 */
export function isPathAllowedForRole(
  router: Router,
  path: string,
  roleType: RoleType | null,
): boolean {
  if (!roleType) {
    return false
  }
  const resolved = router.resolve(path)
  for (const record of resolved.matched) {
    const roles = resolveRequiredRoles(record.meta)
    if (roles) {
      return roles.includes(roleType)
    }
  }
  return true
}

export function resolveLoginRedirect(
  router: Router,
  redirect: string | undefined,
  roleType: RoleType | null,
): string {
  const fallback = getDefaultPathByRole(roleType)
  if (!redirect || redirect === '/login') {
    return fallback
  }
  if (!isPathAllowedForRole(router, redirect, roleType)) {
    return fallback
  }
  return redirect
}

export function setupRouterGuards(router: Router) {
  router.beforeEach((to) => {
    const userStore = useUserStore()

    if (to.meta.guest && userStore.isLoggedIn()) {
      return getDefaultPathByRole(userStore.roleType)
    }

    if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    const requiredRoles = resolveRequiredRoles(to.meta)
    if (requiredRoles && userStore.isLoggedIn()) {
      const roleType = userStore.roleType
      if (!roleType || !requiredRoles.includes(roleType)) {
        return { path: '/403' }
      }
    }

    return true
  })
}
