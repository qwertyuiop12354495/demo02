import { computed } from 'vue'
import type { RoleType } from '@/types/role'
import type { WorkReviewListItem } from '@/types/admin-work-review'
import type { WorkStep } from '@/types/work'
import { useUserStore } from '@/stores/user'

const ROLE_REVIEW_STEP: Record<
  'SCHOOL_ADMIN' | 'DISTRICT_ADMIN' | 'CITY_ADMIN' | 'PROVINCE_ADMIN',
  WorkStep
> = {
  SCHOOL_ADMIN: 'SCHOOL',
  DISTRICT_ADMIN: 'DISTRICT',
  CITY_ADMIN: 'CITY',
  PROVINCE_ADMIN: 'PROVINCE',
}

const PAGE_TITLE: Record<
  'SCHOOL_ADMIN' | 'DISTRICT_ADMIN' | 'CITY_ADMIN' | 'PROVINCE_ADMIN',
  string
> = {
  SCHOOL_ADMIN: '报名信息管理（本校）',
  DISTRICT_ADMIN: '报名信息管理（本区县）',
  CITY_ADMIN: '报名信息管理（本市）',
  PROVINCE_ADMIN: '报名信息管理',
}

const PAGE_DESCRIPTION: Record<
  'SCHOOL_ADMIN' | 'DISTRICT_ADMIN' | 'CITY_ADMIN' | 'PROVINCE_ADMIN',
  string
> = {
  SCHOOL_ADMIN: '审核本校教师已提交的作品报名，支持通过或退回修改。',
  DISTRICT_ADMIN: '审核本区县范围内已提交至区级环节的作品报名。',
  CITY_ADMIN: '审核本市范围内已提交至市级环节的作品报名。',
  PROVINCE_ADMIN: '审核本省范围内已提交至省级环节的作品报名。',
}

const STEP_LABEL: Record<WorkStep, string> = {
  SCHOOL: '校级审核',
  DISTRICT: '区级审核',
  CITY: '市级审核',
  PROVINCE: '省级审核',
  SCORE_DISTRICT: '区级评分',
  SCORE_CITY: '市级评分',
  SCORE_PROVINCE: '省级评分',
  COMPLETED: '已完成',
}

function isReviewAdminRole(
  roleType: RoleType | null | undefined,
): roleType is keyof typeof ROLE_REVIEW_STEP {
  return (
    roleType === 'SCHOOL_ADMIN' ||
    roleType === 'DISTRICT_ADMIN' ||
    roleType === 'CITY_ADMIN' ||
    roleType === 'PROVINCE_ADMIN'
  )
}

export function formatScopeRegion(row: Pick<
  WorkReviewListItem,
  'provinceName' | 'cityName' | 'districtName'
>): string {
  const parts = [row.provinceName, row.cityName, row.districtName].filter(
    (part) => part && part.trim(),
  )
  return parts.length > 0 ? parts.join(' / ') : '—'
}

export function formatTeacherLabel(teacherId: number): string {
  return `教师 #${teacherId}`
}

export function useAdminReviewScope() {
  const userStore = useUserStore()

  const roleType = computed(() => userStore.roleType)

  const reviewStep = computed(() => {
    if (!isReviewAdminRole(roleType.value)) {
      return null
    }
    return ROLE_REVIEW_STEP[roleType.value]
  })

  const pageTitle = computed(() => {
    if (!isReviewAdminRole(roleType.value)) {
      return '报名信息管理'
    }
    return PAGE_TITLE[roleType.value]
  })

  const pageDescription = computed(() => {
    if (!isReviewAdminRole(roleType.value)) {
      return '审核辖区内教师提交的作品报名。'
    }
    return PAGE_DESCRIPTION[roleType.value]
  })

  const reviewStepLabel = computed(() => {
    if (!reviewStep.value) {
      return ''
    }
    return STEP_LABEL[reviewStep.value]
  })

  function canReviewWork(row: WorkReviewListItem): boolean {
    if (!reviewStep.value) {
      return false
    }
    return row.currentStatus === 'SUBMITTED' && row.currentStep === reviewStep.value
  }

  return {
    roleType,
    reviewStep,
    reviewStepLabel,
    pageTitle,
    pageDescription,
    canReviewWork,
    formatScopeRegion,
    formatTeacherLabel,
    getStepLabel: (step: WorkStep) => STEP_LABEL[step] ?? step,
  }
}
