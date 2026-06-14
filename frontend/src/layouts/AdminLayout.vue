<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import { getDefaultPathByRole } from '@/constants/menus'
import { isAdminRole } from '@/types/role'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.clearAuth()
  router.push('/login')
}

function goPortalHome() {
  const path = getDefaultPathByRole(userStore.roleType)
  router.push(path)
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">管理后台</div>
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ userStore.user?.nickname }}</span>
        <el-button v-if="isAdminRole(userStore.roleType)" link type="primary" @click="goPortalHome">
          工作台
        </el-button>
        <el-button link type="primary" @click="logout">退出</el-button>
      </el-header>
      <el-main class="main app-layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  border-right: 1px solid var(--el-border-color-light);
}

.brand {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 600;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}
</style>
