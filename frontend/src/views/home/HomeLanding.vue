<script setup lang="ts">
import { useRouter } from 'vue-router'
import FeatureCard from '@/components/portal/FeatureCard.vue'
import PortalHero from '@/components/portal/PortalHero.vue'
import PortalSection from '@/components/portal/PortalSection.vue'
import ProcessSteps from '@/components/portal/ProcessSteps.vue'
import PortalLayout from '@/layouts/PortalLayout.vue'
import {
  PORTAL_CAPABILITIES,
  PORTAL_FEATURES,
  PORTAL_HERO,
  PORTAL_STEPS,
} from '@/constants/portal-content'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function goLogin() {
  router.push('/login')
}

function goActivities() {
  if (userStore.isLoggedIn()) {
    router.push('/activities')
  } else {
    router.push({ path: '/login', query: { redirect: '/activities' } })
  }
}
</script>

<template>
  <PortalLayout>
    <PortalHero
      :title="PORTAL_HERO.title"
      :subtitle="PORTAL_HERO.subtitle"
      :description="PORTAL_HERO.description"
    >
      <template #actions>
        <el-button size="large" class="portal-btn-primary" @click="goLogin">立即登录</el-button>
        <el-button size="large" class="portal-btn-ghost" @click="goActivities">浏览活动</el-button>
      </template>
    </PortalHero>

    <PortalSection
      title="核心功能"
      description="围绕活动报名全流程设计，覆盖浏览、报名、跟踪与管理。"
      variant="light"
    >
      <div class="portal-section__grid">
        <FeatureCard
          v-for="item in PORTAL_FEATURES"
          :key="item.title"
          :icon="item.icon"
          :title="item.title"
          :description="item.description"
        />
      </div>
    </PortalSection>

    <PortalSection title="报名流程" description="六步完成从发现活动到参与活动的完整路径。">
      <ProcessSteps :steps="PORTAL_STEPS" />
    </PortalSection>

    <PortalSection
      title="系统能力"
      description="为活动组织与参与者提供清晰、可追溯的报名体验。"
      variant="light"
    >
      <div class="portal-section__grid">
        <FeatureCard
          v-for="item in PORTAL_CAPABILITIES"
          :key="item.title"
          :icon="item.icon"
          :title="item.title"
          :description="item.description"
        />
      </div>
      <div class="portal-hero__actions" style="margin-top: 48px">
        <el-button size="large" class="portal-btn-primary" @click="goActivities">
          开始使用
        </el-button>
      </div>
    </PortalSection>
  </PortalLayout>
</template>
