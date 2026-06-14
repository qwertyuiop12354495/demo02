<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime } from '@/composables/useDateTime'
import { useAdminReviewScope } from '@/composables/useAdminReviewScope'
import type { CachedRevisionFeedback } from '@/types/admin-work-review'
import type { WorkReviewListItem } from '@/types/admin-work-review'

const props = defineProps<{
  visible: boolean
  row: WorkReviewListItem | null
  history: CachedRevisionFeedback[]
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const { getStepLabel } = useAdminReviewScope()

const sortedHistory = computed(() =>
  [...props.history].sort((a, b) => b.roundNo - a.roundNo),
)

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="修改意见历史"
    size="420px"
    destroy-on-close
    @close="handleClose"
  >
    <p v-if="row" class="history-drawer__meta">
      作品：{{ row.title }}
    </p>

    <el-timeline v-if="sortedHistory.length">
      <el-timeline-item
        v-for="item in sortedHistory"
        :key="`${item.workId}-${item.reviewStep}-${item.roundNo}`"
        :timestamp="formatDateTime(item.createdAt)"
        placement="top"
      >
        <p class="history-drawer__round">
          第 {{ item.roundNo }} 轮 · {{ getStepLabel(item.reviewStep) }}
        </p>
        <p class="history-drawer__feedback">{{ item.feedback }}</p>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-else description="暂无修改意见记录">
      <p class="history-drawer__empty-hint">
        本会话内退回的记录会显示在此。完整历史需后端提供查询接口。
      </p>
    </el-empty>
  </el-drawer>
</template>

<style scoped>
.history-drawer__meta {
  margin: 0 0 var(--space-4);
  font-size: 14px;
  color: var(--color-text-secondary);
}

.history-drawer__round {
  margin: 0 0 var(--space-1);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.history-drawer__feedback {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--color-text-secondary);
}

.history-drawer__empty-hint {
  margin: var(--space-2) 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
  text-align: center;
}
</style>
