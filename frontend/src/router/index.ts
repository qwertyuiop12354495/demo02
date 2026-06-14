import { createRouter, createWebHistory } from 'vue-router'
import { getDefaultPathByRole } from '@/constants/menus'
import { setupRouterGuards } from '@/router/guards'
import { useUserStore } from '@/stores/user'
import type { RoleType } from '@/types/role'
import { ADMIN_ROLES, REVIEWER_ROLES } from '@/types/role'

const ALL_STAFF_ROLES: RoleType[] = [...ADMIN_ROLES, ...REVIEWER_ROLES]

const ALL_ROLES: RoleType[] = ['TEACHER', ...ALL_STAFF_ROLES]

const NOTICE_ROLES: RoleType[] = ALL_ROLES

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { public: true },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/Login.vue'),
      meta: { guest: true, public: true },
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/Forbidden403.vue'),
      meta: { public: true, title: '无权限' },
    },
    {
      path: '/',
      component: () => import('@/layouts/BasicLayout.vue'),
      children: [
        {
          path: 'works',
          name: 'MyWorkList',
          component: () => import('@/views/work/MyWorkList.vue'),
          meta: { requiresAuth: true, roles: ['TEACHER'], title: '我的作品' },
        },
        {
          path: 'works/enroll/:activityId',
          name: 'WorkEnroll',
          component: () => import('@/views/work/WorkEnroll.vue'),
          meta: { requiresAuth: true, roles: ['TEACHER'], title: '作品报名' },
        },
        {
          path: 'notices',
          name: 'NoticeList',
          component: () => import('@/views/notice/NoticeList.vue'),
          meta: { requiresAuth: true, roles: NOTICE_ROLES, title: '公示' },
        },
        {
          path: 'activities',
          redirect: '/works',
        },
        {
          path: 'activities/:id',
          redirect: (to) => ({ path: `/works/enroll/${to.params.id}` }),
        },
        {
          path: 'my-registrations',
          redirect: '/works',
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, roles: ALL_STAFF_ROLES },
      children: [
        {
          path: '',
          name: 'AdminHome',
          redirect: () => {
            const path = getDefaultPathByRole(useUserStore().roleType)
            return path.startsWith('/admin') ? path : '/admin/work-reviews'
          },
        },
        {
          path: 'activities',
          name: 'ActivityManage',
          component: () => import('@/views/admin/ActivityManage.vue'),
          meta: { roles: ['PROVINCE_ADMIN'], title: '报名活动' },
        },
        {
          path: 'activities/:activityId/registrations',
          redirect: '/admin/work-reviews',
        },
        {
          path: 'work-reviews',
          name: 'WorkReviewList',
          component: () => import('@/views/admin/WorkReviewList.vue'),
          meta: {
            roles: ['SCHOOL_ADMIN', 'DISTRICT_ADMIN', 'CITY_ADMIN', 'PROVINCE_ADMIN'],
            title: '报名信息管理',
          },
        },
        {
          path: 'enrolled',
          name: 'EnrolledList',
          component: () => import('@/views/admin/EnrolledList.vue'),
          meta: { roles: ['PROVINCE_ADMIN'], title: '已报名' },
        },
        {
          path: 'scores',
          name: 'ScoreWorks',
          component: () => import('@/views/admin/ScoreWorks.vue'),
          meta: {
            roles: ['DISTRICT_REVIEWER', 'CITY_REVIEWER', 'PROVINCE_REVIEWER'],
            title: '打分',
          },
        },
        {
          path: 'notices',
          name: 'AdminNoticeList',
          component: () => import('@/views/notice/NoticeList.vue'),
          meta: { requiresAuth: true, roles: NOTICE_ROLES, title: '公示' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

setupRouterGuards(router)

export default router
