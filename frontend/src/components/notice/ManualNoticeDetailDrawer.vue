<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import { getNoticeTypeLabel, getVisibleScopeLabel } from '@/composables/useNoticeLabels'
import type { ManualNoticeDetail } from '@/types/notice'

defineProps<{
  visible: boolean
  detail: ManualNoticeDetail | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

function handleClose() {
  emit('update:visible', false)
}

function formatRegion(detail: ManualNoticeDetail): string {
  const parts = [
    detail.provinceName,
    detail.cityName,
    detail.districtName,
    detail.schoolName,
  ].filter((part) => part && part.trim())
  return parts.length > 0 ? parts.join(' / ') : '—'
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    :title="detail?.title ?? '公示详情'"
    size="520px"
    destroy-on-close
    @close="handleClose"
  >
    <div v-loading="loading" class="manual-notice-detail">
      <template v-if="detail">
        <dl class="manual-notice-detail__meta">
          <div class="manual-notice-detail__row">
            <dt>类型</dt>
            <dd>{{ getNoticeTypeLabel(detail.noticeType) }}</dd>
          </div>
          <div class="manual-notice-detail__row">
            <dt>可见范围</dt>
            <dd>{{ getVisibleScopeLabel(detail.visibleScopeType) }}</dd>
          </div>
          <div class="manual-notice-detail__row">
            <dt>辖区</dt>
            <dd>{{ formatRegion(detail) }}</dd>
          </div>
          <div class="manual-notice-detail__row">
            <dt>发布人</dt>
            <dd>{{ detail.publisherName || '—' }}</dd>
          </div>
          <div class="manual-notice-detail__row">
            <dt>发布时间</dt>
            <dd>{{ formatDateTime(detail.publishTime) }}</dd>
          </div>
        </dl>

        <section class="manual-notice-detail__section">
          <h3 class="manual-notice-detail__heading">公示内容</h3>
          <div class="manual-notice-detail__content">{{ detail.content }}</div>
        </section>

        <el-alert
          v-if="detail.objectionNote"
          type="warning"
          :closable="false"
          show-icon
          title="异议说明"
          class="manual-notice-detail__alert"
        >
          <p class="manual-notice-detail__objection">{{ detail.objectionNote }}</p>
        </el-alert>
      </template>

      <el-empty v-else-if="!loading" description="暂无详情" />
    </div>
  </el-drawer>
</template>

<style scoped>
.manual-notice-detail__meta {
  margin: 0 0 var(--space-5);
  display: grid;
  gap: var(--space-2);
}

.manual-notice-detail__row {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: var(--space-2);
  font-size: 14px;
}

.manual-notice-detail__row dt {
  margin: 0;
  color: var(--color-text-secondary);
}

.manual-notice-detail__row dd {
  margin: 0;
  color: var(--color-text-primary);
}

.manual-notice-detail__section {
  margin-bottom: var(--space-4);
}

.manual-notice-detail__heading {
  margin: 0 0 var(--space-2);
  font-size: 15px;
  font-weight: 600;
}

.manual-notice-detail__content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--color-text-primary);
}

.manual-notice-detail__alert {
  margin-top: var(--space-4);
}

.manual-notice-detail__objection {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
