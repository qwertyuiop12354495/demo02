<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMenus } from '@/composables/useMenus'
import { useUserStore } from '@/stores/user'

const props = withDefaults(
  defineProps<{
    mode?: 'public' | 'app'
  }>(),
  { mode: 'public' },
)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { menus: topMenus } = useMenus('top')

const isLoggedIn = computed(() => userStore.isLoggedIn())

const navItems = computed(() => {
  if (props.mode === 'app' && userStore.roleType === 'TEACHER') {
    return topMenus.value.map((item) => ({
      label: item.label,
      path: item.path,
      requiresAuth: true,
    }))
  }
  if (props.mode === 'public') {
    return [{ label: '首页', path: '/', requiresAuth: false }]
  }
  return []
})

function isActive(path: string) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path === path || route.path.startsWith(`${path}/`)
}

function handleNav(path: string, requiresAuth?: boolean) {
  if (requiresAuth && !isLoggedIn.value) {
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}

function goLogin() {
  router.push('/login')
}

function goWorks() {
  if (isLoggedIn.value) {
    router.push('/')
  } else {
    router.push({ path: '/login', query: { redirect: '/' } })
  }
}

function logout() {
  userStore.clearAuth()
  router.push('/')
}
</script>

<template>
  <header class="portal-navbar">
    <div class="portal-navbar__brand" @click="router.push('/')">活动报名系统</div>
    <nav v-if="navItems.length" class="portal-navbar__nav" aria-label="主导航">
      <a
        v-for="item in navItems"
        :key="item.path"
        href="#"
        class="portal-navbar__link"
        :class="{ 'is-active': isActive(item.path) }"
        @click.prevent="handleNav(item.path, item.requiresAuth)"
      >
        {{ item.label }}
      </a>
    </nav>
    <div class="portal-navbar__actions">
      <template v-if="isLoggedIn">
        <span class="portal-navbar__user">{{ userStore.user?.nickname }}</span>
        <el-button class="portal-navbar__logout" link @click="logout">退出</el-button>
      </template>
      <template v-else>
        <el-button class="portal-btn-ghost" @click="goLogin">登录</el-button>
        <el-button class="portal-btn-primary" @click="goWorks">作品报名</el-button>
      </template>
    </div>
  </header>
</template>
