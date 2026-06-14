export type PromotionSummaryTab = 'DISTRICT_PROMOTED' | 'CITY_PROMOTED' | 'PROVINCE_AWARD'

export type NoticeType = 'REVIEW_RESULT' | 'SCORE_RESULT' | 'AWARD_LIST' | 'GENERAL'

export type VisibleScopeType = 'PUBLIC' | 'PROVINCE' | 'CITY' | 'DISTRICT' | 'SCHOOL'

export interface PromotionSummaryItem {
  workId: number
  workTitle: string
  schoolName: string | null
  districtName: string | null
  cityName: string | null
  provinceName: string | null
  averageScore: number | null
  publishedAt: string
}

export interface PromotionSummaryQuery {
  tab: PromotionSummaryTab
  page?: number
  pageSize?: number
  activityId?: number
}

export interface ManualNoticeListItem {
  id: number
  title: string
  noticeType: NoticeType
  visibleScopeType: VisibleScopeType
  publishTime: string
  publisherName: string | null
  published: boolean
}

export interface ManualNoticeDetail {
  id: number
  title: string
  content: string
  objectionNote: string | null
  noticeType: NoticeType
  visibleScopeType: VisibleScopeType
  provinceName: string | null
  cityName: string | null
  districtName: string | null
  schoolName: string | null
  createdBy: number
  publisherName: string | null
  publishTime: string
  createdAt: string
  updatedAt: string
}

export interface ManualNoticePageQuery {
  page?: number
  pageSize?: number
}
