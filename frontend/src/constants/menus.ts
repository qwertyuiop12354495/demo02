import type { RoleType } from '@/types/role'

export interface AppMenuItem {
  key: string
  label: string
  path: string
  roles: RoleType[]
  /** 顶栏导航（TEACHER 等） */
  placement?: 'top' | 'sidebar' | 'both'
}

export const ALL_MENUS: AppMenuItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
    roles: ['TEACHER'],
    placement: 'top',
  },
  {
    key: 'works',
    label: '我的作品',
    path: '/works',
    roles: ['TEACHER'],
    placement: 'top',
  },
  {
    key: 'notices-teacher',
    label: '公示',
    path: '/notices',
    roles: ['TEACHER'],
    placement: 'top',
  },
  {
    key: 'notices-staff',
    label: '公示',
    path: '/admin/notices',
    roles: [
      'SCHOOL_ADMIN',
      'DISTRICT_ADMIN',
      'CITY_ADMIN',
      'PROVINCE_ADMIN',
      'DISTRICT_REVIEWER',
      'CITY_REVIEWER',
      'PROVINCE_REVIEWER',
    ],
    placement: 'sidebar',
  },
  {
    key: 'work-reviews-school',
    label: '报名信息管理（本校）',
    path: '/admin/work-reviews',
    roles: ['SCHOOL_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'work-reviews-district',
    label: '报名信息管理（本区县）',
    path: '/admin/work-reviews',
    roles: ['DISTRICT_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'work-reviews-city',
    label: '报名信息管理（本市）',
    path: '/admin/work-reviews',
    roles: ['CITY_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'work-reviews-province',
    label: '报名信息管理',
    path: '/admin/work-reviews',
    roles: ['PROVINCE_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'activities',
    label: '报名活动',
    path: '/admin/activities',
    roles: ['PROVINCE_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'enrolled',
    label: '已报名',
    path: '/admin/enrolled',
    roles: ['PROVINCE_ADMIN'],
    placement: 'sidebar',
  },
  {
    key: 'scores-district',
    label: '打分（本区县）',
    path: '/admin/scores',
    roles: ['DISTRICT_REVIEWER'],
    placement: 'sidebar',
  },
  {
    key: 'scores-city',
    label: '打分（本市）',
    path: '/admin/scores',
    roles: ['CITY_REVIEWER'],
    placement: 'sidebar',
  },
  {
    key: 'scores-province',
    label: '打分（本省）',
    path: '/admin/scores',
    roles: ['PROVINCE_REVIEWER'],
    placement: 'sidebar',
  },
]

export function getMenusByRole(
  roleType: RoleType | null | undefined,
  placement?: AppMenuItem['placement'],
): AppMenuItem[] {
  if (!roleType) {
    return []
  }
  const seen = new Set<string>()
  return ALL_MENUS.filter((item) => {
    if (!item.roles.includes(roleType)) {
      return false
    }
    if (placement && item.placement !== placement && item.placement !== 'both') {
      return false
    }
    const dedupeKey = `${item.path}:${item.label}`
    if (seen.has(dedupeKey)) {
      return false
    }
    seen.add(dedupeKey)
    return true
  })
}

export function getDefaultPathByRole(roleType: RoleType | null | undefined): string {
  switch (roleType) {
    case 'TEACHER':
      return '/'
    case 'PROVINCE_ADMIN':
      return '/admin/activities'
    case 'SCHOOL_ADMIN':
    case 'DISTRICT_ADMIN':
    case 'CITY_ADMIN':
      return '/admin/work-reviews'
    case 'DISTRICT_REVIEWER':
    case 'CITY_REVIEWER':
    case 'PROVINCE_REVIEWER':
      return '/admin/scores'
    default:
      return '/login'
  }
}
