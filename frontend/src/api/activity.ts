import { request } from '@/utils/request'
import { pageMyWorks } from '@/api/work'
import { getHomeWorkStatusLabel } from '@/composables/useWorkStatusLabel'
import type { ActivityDetail, ActivityPageQuery, UserActivityListItem } from '@/types/activity'
import type { ActivityHomeListItem, ActivityHomePageQuery } from '@/types/activity-home'
import type { PageResult } from '@/types/api'
import type { WorkListItem } from '@/types/work'

export function getActivities(params: ActivityPageQuery) {
  return request<PageResult<UserActivityListItem>>({
    url: '/activities',
    method: 'GET',
    params,
  })
}

export function getActivityDetail(id: number) {
  return request<ActivityDetail>({
    url: `/activities/${id}`,
    method: 'GET',
  })
}

function isInRegistrationPeriod(item: UserActivityListItem, now: number): boolean {
  const start = new Date(item.registrationStartTime).getTime()
  const end = new Date(item.registrationDeadline).getTime()
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return false
  }
  return now >= start && now <= end
}

function buildHomeItem(
  activity: UserActivityListItem,
  work: WorkListItem | undefined,
): ActivityHomeListItem {
  const myWorkStatus = work?.currentStatus ?? null
  return {
    id: activity.id,
    title: activity.title,
    location: activity.location,
    eventStartTime: activity.eventStartTime,
    eventEndTime: activity.eventEndTime,
    registrationStartTime: activity.registrationStartTime,
    registrationDeadline: activity.registrationDeadline,
    maxParticipants: activity.maxParticipants,
    approvedCount: activity.approvedCount,
    remainingSlots: activity.remainingSlots,
    canRegister: activity.canRegister,
    registerDisabledReason: activity.registerDisabledReason,
    myWorkId: work?.id ?? null,
    myWorkStatus,
    myWorkStatusLabel: getHomeWorkStatusLabel(myWorkStatus),
  }
}

function paginateList<T>(list: T[], page: number, pageSize: number): PageResult<T> {
  const safePage = Math.max(1, page)
  const safeSize = Math.max(1, Math.min(pageSize, 100))
  const start = (safePage - 1) * safeSize
  return {
    list: list.slice(start, start + safeSize),
    total: list.length,
    page: safePage,
    pageSize: safeSize,
  }
}

/**
 * 教师首页活动列表（listPublishedForHome）。
 * 聚合 GET /activities + GET /works/mine，前端过滤报名期内活动并合并作品状态。
 * 后端提供 GET /activities/home 后可改为单接口。
 */
export async function listPublishedForHome(
  params: ActivityHomePageQuery = {},
): Promise<PageResult<ActivityHomeListItem>> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 12

  const [activities, works] = await Promise.all([
    getActivities({ page: 1, pageSize: 100, keyword: params.keyword }),
    pageMyWorks({ page: 1, pageSize: 100 }),
  ])

  const workByActivity = new Map<number, WorkListItem>()
  for (const work of works.list) {
    workByActivity.set(work.activityId, work)
  }

  const now = Date.now()
  const filtered = activities.list
    .filter((item) => isInRegistrationPeriod(item, now))
    .map((item) => buildHomeItem(item, workByActivity.get(item.id)))

  return paginateList(filtered, page, pageSize)
}
