import type { Component } from 'vue'
import {
  Calendar,
  CircleCheck,
  Document,
  List,
  Search,
  User,
} from '@element-plus/icons-vue'

export interface PortalFeature {
  icon: Component
  title: string
  description: string
}

export interface PortalStep {
  title: string
  description: string
}

export const PORTAL_HERO = {
  title: '活动报名系统',
  subtitle: '更省心、更高效的活动报名与审核体验',
  description:
    '面向组织方与参与者的活动报名平台，支持活动浏览、在线报名、进度跟踪与名额管理，让每一次活动报名都清晰可控。',
}

export const PORTAL_FEATURES: PortalFeature[] = [
  {
    icon: Search,
    title: '活动浏览',
    description: '集中展示已上架活动，支持按名称搜索与状态筛选，快速找到感兴趣的活动。',
  },
  {
    icon: Document,
    title: '在线报名',
    description: '填写报名备注即可提交申请，名额实时校验，避免重复报名与超额登记。',
  },
  {
    icon: CircleCheck,
    title: '进度跟踪',
    description: '在「我的报名」中查看待审核、已通过、已拒绝等状态，审核结果一目了然。',
  },
  {
    icon: List,
    title: '名额透明',
    description: '展示已通过人数与剩余名额，报名前即可判断活动是否仍可参加。',
  },
]

export const PORTAL_STEPS: PortalStep[] = [
  { title: '浏览活动', description: '查看活动列表与详情' },
  { title: '登录账号', description: '使用账号进入系统' },
  { title: '提交报名', description: '填写备注并确认报名' },
  { title: '等待审核', description: '管理员审核报名申请' },
  { title: '参与活动', description: '审核通过后按时参加' },
  { title: '管理报名', description: '查看状态或取消报名' },
]

export const PORTAL_CAPABILITIES: PortalFeature[] = [
  {
    icon: Calendar,
    title: '活动时间线',
    description: '清晰展示活动时间、报名起止时间与地点信息，便于提前规划行程。',
  },
  {
    icon: User,
    title: '个人报名记录',
    description: '统一管理所有报名记录，支持查看审核备注与跳转活动详情。',
  },
  {
    icon: CircleCheck,
    title: '状态实时同步',
    description: '报名状态与活动名额联动更新，减少人工核对成本。',
  },
  {
    icon: Document,
    title: '备注与说明',
    description: '支持填写报名备注，活动详情提供完整说明，信息传达更准确。',
  },
]
