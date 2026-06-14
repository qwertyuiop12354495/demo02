import type { Component } from 'vue'
import {
  Bell,
  CircleCheck,
  DataAnalysis,
  Document,
  List,
  Promotion,
} from '@element-plus/icons-vue'

export interface HomeFeature {
  icon: Component
  title: string
  description: string
}

export interface HomeStep {
  title: string
  description: string
}

export const HOME_HERO = {
  title: '活动报名管理系统',
  subtitle: '一站式活动发布、报名、审核与管理平台',
}

export const HOME_FEATURES: HomeFeature[] = [
  {
    icon: Promotion,
    title: '活动发布',
    description: '管理员可创建并上架活动，设置时间、地点、名额与报名规则，信息集中展示。',
  },
  {
    icon: Document,
    title: '在线报名',
    description: '参与者在线浏览活动详情，填写备注即可提交报名，名额实时校验防止超额。',
  },
  {
    icon: CircleCheck,
    title: '报名审核',
    description: '管理员对待审核报名进行通过或拒绝操作，审核结果即时同步至参与者。',
  },
  {
    icon: List,
    title: '名单管理',
    description: '按活动查看报名名单，支持筛选状态、导出思路清晰，便于现场签到与核对。',
  },
  {
    icon: Bell,
    title: '状态通知',
    description: '报名状态变更后可在「我的报名」中查看，待审核、已通过、已拒绝一目了然。',
  },
  {
    icon: DataAnalysis,
    title: '数据统计',
    description: '展示已通过人数、剩余名额与报名进度，帮助组织方掌握活动参与情况。',
  },
]

export const HOME_STEPS: HomeStep[] = [
  { title: '查看活动', description: '浏览活动列表，了解时间与名额' },
  { title: '填写信息', description: '登录后填写报名备注等信息' },
  { title: '提交报名', description: '确认提交，等待系统校验名额' },
  { title: '管理员审核', description: '组织方审核报名申请' },
  { title: '报名成功', description: '审核通过后即可按时参加活动' },
]
