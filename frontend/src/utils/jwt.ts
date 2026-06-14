import { normalizeRoleType, type RoleType } from '@/types/role'

export interface JwtClaims {
  roleType: RoleType | null
  provinceName: string | null
  cityName: string | null
  districtName: string | null
  schoolName: string | null
}

/**
 * 仅用于前端菜单/路由体验，不可作为安全边界。
 * 签名与权限校验由后端 API 负责。
 */
export function parseJwtClaims(token: string): JwtClaims {
  const empty: JwtClaims = {
    roleType: null,
    provinceName: null,
    cityName: null,
    districtName: null,
    schoolName: null,
  }
  if (!token) {
    return empty
  }
  const parts = token.split('.')
  if (parts.length < 2) {
    return empty
  }
  try {
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))) as Record<
      string,
      unknown
    >
    const roleType = normalizeRoleType(payload.roleType ?? payload.role)
    return {
      roleType,
      provinceName: readClaim(payload.provinceName),
      cityName: readClaim(payload.cityName),
      districtName: readClaim(payload.districtName),
      schoolName: readClaim(payload.schoolName),
    }
  } catch {
    return empty
  }
}

function readClaim(value: unknown): string | null {
  if (typeof value !== 'string') {
    return null
  }
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : null
}
