import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { parseJwtClaims } from '@/utils/jwt'
import type { RoleType } from '@/types/role'
import { normalizeRoleType } from '@/types/role'

const TOKEN_KEY = 'activity_token'
const USER_KEY = 'activity_user'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  role: 'USER' | 'ADMIN'
  roleType?: RoleType | null
  provinceName?: string | null
  cityName?: string | null
  districtName?: string | null
  schoolName?: string | null
}

function readUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as UserInfo
  } catch {
    return null
  }
}

function enrichUserFromToken(nextUser: UserInfo, token: string): UserInfo {
  const claims = parseJwtClaims(token)
  return {
    ...nextUser,
    roleType: claims.roleType ?? normalizeRoleType(nextUser.role),
    provinceName: claims.provinceName,
    cityName: claims.cityName,
    districtName: claims.districtName,
    schoolName: claims.schoolName,
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<UserInfo | null>(readUser())

  if (token.value && user.value) {
    user.value = enrichUserFromToken(user.value, token.value)
  }

  const roleType = computed(() => user.value?.roleType ?? null)

  function setAuth(nextToken: string, nextUser: UserInfo) {
    const enriched = enrichUserFromToken(nextUser, nextToken)
    token.value = nextToken
    user.value = enriched
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(enriched))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function isLoggedIn() {
    return !!token.value
  }

  /** @deprecated 请使用 roleType / hasRole */
  function isAdmin() {
    return user.value?.role === 'ADMIN'
  }

  function hasRole(...roles: RoleType[]) {
    if (!roleType.value) {
      return false
    }
    return roles.includes(roleType.value)
  }

  return {
    token,
    user,
    roleType,
    setAuth,
    clearAuth,
    isLoggedIn,
    isAdmin,
    hasRole,
  }
})
