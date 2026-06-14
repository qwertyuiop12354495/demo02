<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GlowBackground from '@/components/portal/GlowBackground.vue'
import PortalGlassCard from '@/components/portal/PortalGlassCard.vue'
import PortalNavbar from '@/components/portal/PortalNavbar.vue'
import { login } from '@/api/auth'
import { resolveLoginRedirect } from '@/router/guards'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password,
    })

    userStore.setAuth(data.token, {
      id: data.user.id,
      username: data.user.username,
      nickname: data.user.nickname,
      role: data.user.role,
    })

    const redirect = resolveLoginRedirect(
      router,
      route.query.redirect as string | undefined,
      userStore.roleType,
    )
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="portal-shell login-shell">
    <GlowBackground />
    <PortalNavbar mode="public" />
    <div class="login-shell__content">
      <PortalGlassCard :float="false" class="login-card">
        <h1 class="login-card__title">欢迎登录</h1>
        <p class="login-card__desc">登录后即可浏览活动并提交报名申请</p>
        <el-form label-width="80px" @submit.prevent="handleLogin">
          <el-form-item label="用户名">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名…"
              autocomplete="username"
              spellcheck="false"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码…"
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              class="portal-btn-primary login-card__submit"
              native-type="submit"
              :loading="loading"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </PortalGlassCard>
    </div>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
}

.login-shell__content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: var(--space-6) var(--space-4);
}

.login-card {
  width: min(440px, 100%);
  padding: var(--space-6);
}

.login-card__title {
  margin: 0 0 var(--space-2);
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  text-align: center;
}

.login-card__desc {
  margin: 0 0 var(--space-6);
  font-size: 14px;
  color: var(--color-text-secondary);
  text-align: center;
}

.login-card__submit {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
}
</style>
