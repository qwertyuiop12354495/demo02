import type { NoticeType, PromotionSummaryTab, VisibleScopeType } from '@/types/notice'

const PROMOTION_TAB_LABEL: Record<PromotionSummaryTab, string> = {
  DISTRICT_PROMOTED: '区县晋级',
  CITY_PROMOTED: '市级晋级',
  PROVINCE_AWARD: '省级获奖',
}

const NOTICE_TYPE_LABEL: Record<NoticeType, string> = {
  REVIEW_RESULT: '审核结果',
  SCORE_RESULT: '打分结果',
  AWARD_LIST: '获奖名单',
  GENERAL: '综合公示',
}

const VISIBLE_SCOPE_LABEL: Record<VisibleScopeType, string> = {
  PUBLIC: '公开',
  PROVINCE: '省级可见',
  CITY: '市级可见',
  DISTRICT: '区县级可见',
  SCHOOL: '校级可见',
}

export const PROMOTION_TAB_OPTIONS: { label: string; value: PromotionSummaryTab }[] = [
  { label: PROMOTION_TAB_LABEL.DISTRICT_PROMOTED, value: 'DISTRICT_PROMOTED' },
  { label: PROMOTION_TAB_LABEL.CITY_PROMOTED, value: 'CITY_PROMOTED' },
  { label: PROMOTION_TAB_LABEL.PROVINCE_AWARD, value: 'PROVINCE_AWARD' },
]

export function getPromotionTabLabel(tab: PromotionSummaryTab): string {
  return PROMOTION_TAB_LABEL[tab]
}

export function getNoticeTypeLabel(type: NoticeType): string {
  return NOTICE_TYPE_LABEL[type] ?? type
}

export function getVisibleScopeLabel(scope: VisibleScopeType): string {
  return VISIBLE_SCOPE_LABEL[scope] ?? scope
}
