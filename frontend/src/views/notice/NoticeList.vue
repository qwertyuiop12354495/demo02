<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import ManualNoticePanel from '@/components/notice/ManualNoticePanel.vue'
import PromotionSummaryPanel from '@/components/notice/PromotionSummaryPanel.vue'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const isAdminLayout = computed(() => route.path.startsWith('/admin'))

const scopeHint = computed(() => {
  const parts = [
    userStore.user?.provinceName,
    userStore.user?.cityName,
    userStore.user?.districtName,
    userStore.user?.schoolName,
  ].filter((part) => part && part.trim())
  return parts.length > 0 ? parts.join(' / ') : null
})

const pageDescription =
  '查看自动晋级/获奖公示与手工公示。数据范围由系统根据您的账号辖区自动过滤。'
</script>

<template>
  <div :class="isAdminLayout ? 'app-page' : 'portal-page portal-page--narrow notice-list'">
    <PageHeader v-if="isAdminLayout" title="公示" :description="pageDescription">
      <template v-if="scopeHint" #extra>
        <el-tag type="info" effect="plain">当前辖区：{{ scopeHint }}</el-tag>
      </template>
    </PageHeader>

    <PortalPageHeader v-else title="公示" :description="pageDescription">
      <template v-if="scopeHint" #extra>
        <el-tag type="info" effect="plain">当前辖区：{{ scopeHint }}</el-tag>
      </template>
    </PortalPageHeader>

    <component :is="isAdminLayout ? 'el-card' : 'div'" shadow="never" class="ui-card notice-list__card">
      <el-tabs class="notice-list__tabs">
        <el-tab-pane label="自动晋级/获奖公示" lazy>
          <PromotionSummaryPanel />
        </el-tab-pane>
        <el-tab-pane label="手工公示" lazy>
          <ManualNoticePanel />
        </el-tab-pane>
      </el-tabs>
    </component>
  </div>
</template>

<style scoped>
.notice-list__card {
  padding: var(--space-4) var(--space-5) var(--space-5);
}

.notice-list__card:not(.el-card) {
  background: var(--portal-glass-bg, rgba(255, 255, 255, 0.72));
  border: 1px solid var(--portal-glass-border, rgba(255, 255, 255, 0.35));
  border-radius: 12px;
  backdrop-filter: blur(12px);
}

.notice-list__tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-4);
}
</style>
