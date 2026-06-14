/** 与后端 RoleTypeEnum 对齐 */
export type RoleType =
  | 'TEACHER'
  | 'SCHOOL_ADMIN'
  | 'DISTRICT_ADMIN'
  | 'CITY_ADMIN'
  | 'PROVINCE_ADMIN'
  | 'DISTRICT_REVIEWER'
  | 'CITY_REVIEWER'
  | 'PROVINCE_REVIEWER'

export const ADMIN_ROLES: RoleType[] = [
  'SCHOOL_ADMIN',
  'DISTRICT_ADMIN',
  'CITY_ADMIN',
  'PROVINCE_ADMIN',
]

export const REVIEWER_ROLES: RoleType[] = [
  'DISTRICT_REVIEWER',
  'CITY_REVIEWER',
  'PROVINCE_REVIEWER',
]

export function isAdminRole(roleType: RoleType | null | undefined): boolean {
  return !!roleType && ADMIN_ROLES.includes(roleType)
}

export function isReviewerRole(roleType: RoleType | null | undefined): boolean {
  return !!roleType && REVIEWER_ROLES.includes(roleType)
}

export function normalizeRoleType(value: unknown): RoleType | null {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }
  const normalized = value.trim()
  if (normalized === 'USER') {
    return 'TEACHER'
  }
  if (normalized === 'ADMIN') {
    return 'PROVINCE_ADMIN'
  }
  const roles: RoleType[] = [
    'TEACHER',
    'SCHOOL_ADMIN',
    'DISTRICT_ADMIN',
    'CITY_ADMIN',
    'PROVINCE_ADMIN',
    'DISTRICT_REVIEWER',
    'CITY_REVIEWER',
    'PROVINCE_REVIEWER',
  ]
  return roles.includes(normalized as RoleType) ? (normalized as RoleType) : null
}
