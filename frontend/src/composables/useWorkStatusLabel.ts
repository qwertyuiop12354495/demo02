import type { FinalResult, WorkStatus, WorkStep } from '@/types/work'

export type WorkStatusTone = 'info' | 'warning' | 'success' | 'danger'

const HOME_TAG_STATUSES: WorkStatus[] = ['SUBMITTED', 'REVISION_REQUIRED', 'APPROVED']

const HOME_STATUS_LABEL: Record<'SUBMITTED' | 'REVISION_REQUIRED' | 'APPROVED', string> = {
  SUBMITTED: '待审核',
  REVISION_REQUIRED: '需修改',
  APPROVED: '已通过',
}

export function shouldShowHomeWorkTag(status: WorkStatus | null | undefined): boolean {
  if (!status) {
    return false
  }
  return HOME_TAG_STATUSES.includes(status)
}

export function getHomeWorkStatusLabel(status: WorkStatus | null | undefined): string | null {
  if (!status || !shouldShowHomeWorkTag(status)) {
    return null
  }
  return HOME_STATUS_LABEL[status as keyof typeof HOME_STATUS_LABEL]
}

export function getHomeWorkStatusTone(status: WorkStatus | null | undefined): WorkStatusTone {
  switch (status) {
    case 'SUBMITTED':
      return 'info'
    case 'REVISION_REQUIRED':
      return 'warning'
    case 'APPROVED':
      return 'success'
    default:
      return 'info'
  }
}

export function getWorkStatusTone(status: WorkStatus): WorkStatusTone {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'SUBMITTED':
      return 'info'
    case 'REVISION_REQUIRED':
      return 'warning'
    case 'APPROVED':
      return 'success'
    default:
      return 'info'
  }
}

/** 教师端完整状态文案（workflow-spec §10 简化） */
export function getTeacherWorkStatusLabel(
  step: WorkStep,
  status: WorkStatus,
  finalResult: FinalResult,
): string {
  if (finalResult === 'ELIMINATED') {
    return '未通过本次评审'
  }
  if (finalResult === 'AWARD' && step === 'COMPLETED') {
    return '评审完成，恭喜获奖'
  }
  if (finalResult === 'NOT_AWARDED' && step === 'COMPLETED') {
    return '评审完成，未获奖'
  }
  if (finalResult === 'PROMOTED') {
    return '已晋级'
  }

  if (status === 'DRAFT') {
    return '草稿，待提交'
  }
  if (status === 'REVISION_REQUIRED') {
    return '审核退回，请修改后重新提交'
  }

  const submittedLabels: Partial<Record<WorkStep, string>> = {
    SCHOOL: '已提交，等待学校审核',
    DISTRICT: '学校已通过，等待区级审核',
    CITY: '区级已通过，等待市级审核',
    PROVINCE: '市级已通过，等待省级审核',
    SCORE_DISTRICT: '省级已通过，等待区级评分',
    SCORE_CITY: '区级评分已通过，等待市级评分',
    SCORE_PROVINCE: '市级评分已通过，等待省级评分',
    COMPLETED: '流程已完成',
  }

  if (status === 'SUBMITTED') {
    return submittedLabels[step] ?? '已提交，等待处理'
  }
  if (status === 'APPROVED') {
    return '当前环节已通过'
  }

  return '处理中'
}

export function isWorkEditable(status: WorkStatus): boolean {
  return status === 'DRAFT' || status === 'REVISION_REQUIRED'
}
