<script setup lang="ts">
import { computed } from 'vue'
import {
  getHomeWorkStatusLabel,
  getHomeWorkStatusTone,
  shouldShowHomeWorkTag,
} from '@/composables/useWorkStatusLabel'
import { formatDateTimeRange } from '@/composables/useDateTime'
import type { ActivityHomeListItem } from '@/types/activity-home'
import WorkStatusTag from '@/components/work/WorkStatusTag.vue'

const props = defineProps<{
  activity: ActivityHomeListItem
}>()

const emit = defineEmits<{
  enroll: [activityId: number]
  continueEdit: [activityId: number, workId: number]
}>()

const showTag = computed(() => shouldShowHomeWorkTag(props.activity.myWorkStatus))

const tagLabel = computed(
  () => props.activity.myWorkStatusLabel ?? getHomeWorkStatusLabel(props.activity.myWorkStatus),
)

const tagTone = computed(() => getHomeWorkStatusTone(props.activity.myWorkStatus))

const actionLabel = computed(() => {
  if (props.activity.myWorkId && props.activity.myWorkStatus === 'DRAFT') {
    return '继续编辑'
  }
  if (props.activity.myWorkId && props.activity.myWorkStatus === 'REVISION_REQUIRED') {
    return '修改并提交'
  }
  if (props.activity.myWorkId) {
    return '查看作品'
  }
  return '去报名'
})

function handleAction() {
  if (props.activity.myWorkId) {
    emit('continueEdit', props.activity.id, props.activity.myWorkId)
    return
  }
  emit('enroll', props.activity.id)
}
</script>

<template>
  <article class="activity-home-card portal-glass-card">
    <div class="activity-home-card__head">
      <h3 class="activity-home-card__title">{{ activity.title }}</h3>
      <WorkStatusTag v-if="showTag && tagLabel" :label="tagLabel" :tone="tagTone" />
    </div>

    <dl class="activity-home-card__meta">
      <div class="activity-home-card__row">
        <dt>活动时间</dt>
        <dd>{{ formatDateTimeRange(activity.eventStartTime, activity.eventEndTime) }}</dd>
      </div>
      <div class="activity-home-card__row">
        <dt>报名时间</dt>
        <dd>{{ formatDateTimeRange(activity.registrationStartTime, activity.registrationDeadline) }}</dd>
      </div>
      <div class="activity-home-card__row">
        <dt>地点</dt>
        <dd>{{ activity.location || '—' }}</dd>
      </div>
      <div class="activity-home-card__row">
        <dt>名额</dt>
        <dd>
          剩余 {{ activity.remainingSlots }} / {{ activity.maxParticipants }}
        </dd>
      </div>
    </dl>

    <div class="activity-home-card__footer">
      <el-button type="primary" @click="handleAction">{{ actionLabel }}</el-button>
    </div>
  </article>
</template>

<style scoped>
.activity-home-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-5);
  height: 100%;
}

.activity-home-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.activity-home-card__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.activity-home-card__meta {
  margin: 0;
  display: grid;
  gap: var(--space-2);
  flex: 1;
}

.activity-home-card__row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: var(--space-2);
  font-size: 14px;
}

.activity-home-card__row dt {
  margin: 0;
  color: var(--color-text-secondary);
}

.activity-home-card__row dd {
  margin: 0;
  color: var(--color-text-primary);
}

.activity-home-card__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--space-2);
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
