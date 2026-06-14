<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import GlowBackground from '@/components/portal/GlowBackground.vue'
import PortalNavbar from '@/components/portal/PortalNavbar.vue'
import FeatureCard from '@/components/portal/FeatureCard.vue'
import PortalHero from '@/components/portal/PortalHero.vue'
import PortalSection from '@/components/portal/PortalSection.vue'
import ProcessSteps from '@/components/portal/ProcessSteps.vue'
import PortalLayout from '@/layouts/PortalLayout.vue'
import TeacherHome from '@/views/teacher/TeacherHome.vue'
import { HOME_FEATURES, HOME_HERO, HOME_STEPS } from '@/constants/home-content'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isTeacherHome = computed(
  () => userStore.isLoggedIn() && userStore.roleType === 'TEACHER',
)

function goRegister() {
  if (userStore.isLoggedIn()) {
    router.push('/')
    return
  }
  router.push({ path: '/login', query: { redirect: '/' } })
}

function goActivities() {
  if (userStore.isLoggedIn()) {
    router.push('/')
    return
  }
  router.push({ path: '/login', query: { redirect: '/' } })
}
</script>

<template>
  <div v-if="isTeacherHome" class="portal-shell portal-shell--app">
    <GlowBackground />
    <PortalNavbar mode="app" />
    <el-main class="portal-main">
      <TeacherHome />
    </el-main>
  </div>

  <PortalLayout v-else>
    <PortalHero
      class="portal-reveal"
      :title="HOME_HERO.title"
      :subtitle="HOME_HERO.subtitle"
    >
      <template #actions>
        <el-button size="large" class="portal-btn-primary" @click="goRegister">
          立即报名
        </el-button>
        <el-button size="large" class="portal-btn-ghost" @click="goActivities">
          查看活动
        </el-button>
      </template>
    </PortalHero>

    <PortalSection
      title="核心功能"
      description="覆盖活动发布、报名、审核与管理的完整能力，助力组织高效运营。"
      variant="light"
      reveal
    >
      <div class="portal-section__grid portal-section__grid--features">
        <FeatureCard
          v-for="(item, index) in HOME_FEATURES"
          :key="item.title"
          :icon="item.icon"
          :title="item.title"
          :description="item.description"
          :reveal-delay="100 + index * 80"
        />
      </div>
    </PortalSection>

    <PortalSection
      title="报名流程"
      description="五步完成从发现活动到成功报名的完整路径。"
      reveal
    >
      <ProcessSteps :steps="HOME_STEPS" reveal />
    </PortalSection>
  </PortalLayout>
</template>
