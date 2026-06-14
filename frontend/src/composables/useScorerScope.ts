import { computed } from 'vue'
import type { RoleType } from '@/types/role'
import type { FinalResult, WorkStep } from '@/types/work'
import { useUserStore } from '@/stores/user'

const ROLE_SCORE_STEP: Record<
  'DISTRICT_REVIEWER' | 'CITY_REVIEWER' | 'PROVINCE_REVIEWER',
  WorkStep
> = {
  DISTRICT_REVIEWER: 'SCORE_DISTRICT',
  CITY_REVIEWER: 'SCORE_CITY',
  PROVINCE_REVIEWER: 'SCORE_PROVINCE',
}

const PAGE_TITLE: Record<
  'DISTRICT_REVIEWER' | 'CITY_REVIEWER' | 'PROVINCE_REVIEWER',
  string
> = {
  DISTRICT_REVIEWER: '打分（本区县）',
  CITY_REVIEWER: '打分（本市）',
  PROVINCE_REVIEWER: '打分（本省）',
}

const PAGE_DESCRIPTION: Record<
  'DISTRICT_REVIEWER' | 'CITY_REVIEWER' | 'PROVINCE_REVIEWER',
  string
> = {
  DISTRICT_REVIEWER: '为本区县待评作品提交人工分与 AI 分，晋级结果以后端为准。',
  CITY_REVIEWER: '为本市待评作品提交人工分与 AI 分，晋级结果以后端为准。',
  PROVINCE_REVIEWER: '为本省待评作品提交人工分与 AI 分，晋级结果以后端为准。',
}

const STEP_LABEL: Record<WorkStep, string> = {
  SCHOOL: '校级审核',
  DISTRICT: '区级审核',
  CITY: '市级审核',
  PROVINCE: '省级审核',
  SCORE_DISTRICT: '区级打分',
  SCORE_CITY: '市级打分',
  SCORE_PROVINCE: '省级打分',
  COMPLETED: '已完成',
}

const FINAL_RESULT_LABEL: Record<FinalResult, string> = {
  PENDING: '待定',
  PROMOTED: '已晋级',
  ELIMINATED: '已淘汰',
  AWARD: '省级获奖',
  NOT_AWARDED: '未获奖',
}

function isScorerRole(
  roleType: RoleType | null | undefined,
): roleType is keyof typeof ROLE_SCORE_STEP {
  return (
    roleType === 'DISTRICT_REVIEWER' ||
    roleType === 'CITY_REVIEWER' ||
    roleType === 'PROVINCE_REVIEWER'
  )
}

export function formatTeacherLabel(teacherId: number): string {
  return `教师 #${teacherId}`
}

/** 只读展示后端返回的状态，不计算晋级 */
export function describeScoreOutcome(result: {
  currentStep: WorkStep
  finalResult: FinalResult
}): string | null {
  if (result.finalResult === 'ELIMINATED') {
    return FINAL_RESULT_LABEL.ELIMINATED
  }
  if (result.finalResult === 'AWARD') {
    return FINAL_RESULT_LABEL.AWARD
  }
  if (result.finalResult === 'NOT_AWARDED') {
    return FINAL_RESULT_LABEL.NOT_AWARDED
  }
  if (result.finalResult === 'PROMOTED') {
    return FINAL_RESULT_LABEL.PROMOTED
  }
  if (result.currentStep === 'SCORE_CITY') {
    return '已进入市级打分'
  }
  if (result.currentStep === 'SCORE_PROVINCE') {
    return '已进入省级打分'
  }
  if (result.currentStep === 'COMPLETED') {
    return '流程已完成'
  }
  return null
}

export function useScorerScope() {
  const userStore = useUserStore()
  const roleType = computed(() => userStore.roleType)

  const scoreStep = computed(() => {
    if (!isScorerRole(roleType.value)) {
      return null
    }
    return ROLE_SCORE_STEP[roleType.value]
  })

  const pageTitle = computed(() => {
    if (!isScorerRole(roleType.value)) {
      return '打分'
    }
    return PAGE_TITLE[roleType.value]
  })

  const pageDescription = computed(() => {
    if (!isScorerRole(roleType.value)) {
      return '查看待评作品并提交评分。'
    }
    return PAGE_DESCRIPTION[roleType.value]
  })

  const scoreStepLabel = computed(() => {
    if (!scoreStep.value) {
      return ''
    }
    return STEP_LABEL[scoreStep.value]
  })

  return {
    roleType,
    scoreStep,
    scoreStepLabel,
    pageTitle,
    pageDescription,
    formatTeacherLabel,
    getStepLabel: (step: WorkStep) => STEP_LABEL[step] ?? step,
    describeScoreOutcome,
  }
}
