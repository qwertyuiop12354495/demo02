<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ActivityInfoGrid from '@/components/activity/ActivityInfoGrid.vue'
import PortalGlassCard from '@/components/portal/PortalGlassCard.vue'
import type { ActivityInfoItem } from '@/components/activity/ActivityInfoGrid.vue'
import {
  getActivityRegisterStatusLabel,
  getActivityRegisterStatusTagType,
} from '@/composables/useActivityRegisterStatus'
import { formatDateTime, formatDateTimeRange } from '@/composables/useDateTime'
import { getActivityDetail } from '@/api/activity'
import { createRegistration } from '@/api/registration'
import type { ActivityDetail } from '@/types/activity'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const registering = ref(false)
const activity = ref<ActivityDetail | null>(null)
const registerDialogVisible = ref(false)
const registerError = ref('')

const registerForm = reactive({
  remark: '',
})

const activityId = computed(() => Number(route.params.id))

const isValidId = computed(() => Number.isFinite(activityId.value) && activityId.value > 0)

const infoItems = computed<ActivityInfoItem[]>(() => {
  if (!activity.value) {
    return []
  }
  const detail = activity.value
  return [
    { label: '活动时间', value: formatDateTimeRange(detail.eventStartTime, detail.eventEndTime) },
    { label: '报名时间', value: formatDateTimeRange(detail.registrationStartTime, detail.registrationDeadline) },
    { label: '地点', value: detail.location || '—' },
    {
      label: '人数限制',
      value: `${detail.approvedCount} / ${detail.maxParticipants}（剩余 ${detail.remainingSlots}）`,
      numeric: true,
    },
  ]
})

const registerDisabledHint = computed(() => {
  if (!activity.value || activity.value.canRegister) {
    return ''
  }
  return activity.value.registerDisabledReason || '暂不可报名'
})

async function fetchDetail() {
  if (!isValidId.value) {
    activity.value = null
    return
  }

  loading.value = true
  try {
    activity.value = await getActivityDetail(activityId.value)
  } catch {
    activity.value = null
  } finally {
    loading.value = false
  }
}

function openRegisterDialog() {
  registerForm.remark = ''
  registerError.value = ''
  registerDialogVisible.value = true
}

function resolveRegisterErrorMessage(error: unknown): string {
  if (error && typeof error === 'object' && 'message' in error) {
    const message = (error as { message?: string }).message
    if (message) {
      return message
    }
  }
  return '报名失败，请稍后重试'
}

async function submitRegister() {
  if (!activity.value) {
    return
  }

  registering.value = true
  registerError.value = ''
  try {
    await createRegistration({
      activityId: activity.value.id,
      remark: registerForm.remark.trim() || undefined,
    })
    ElMessage.success('报名成功，请等待审核')
    registerDialogVisible.value = false
    await fetchDetail()
  } catch (error) {
    registerError.value = resolveRegisterErrorMessage(error)
  } finally {
    registering.value = false
  }
}

function getRegistrationStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
  }
  return map[status] ?? status
}

watch(
  () => route.params.id,
  () => {
    fetchDetail()
  },
  { immediate: true },
)
</script>

<template>
  <div v-loading="loading" class="portal-page portal-page--narrow">
    <div class="detail-nav">
      <el-button link class="portal-back-link" @click="router.push('/activities')">
        返回活动列表
      </el-button>
    </div>

    <PortalGlassCard v-if="!loading && !activity" :float="false" class="detail-empty-wrap">
      <el-empty description="活动不存在或已下架" class="table-empty" />
    </PortalGlassCard>

    <template v-else-if="activity">
      <header class="detail-hero portal-glass-card">
        <div class="detail-hero__main">
          <h1 class="detail-hero__title">{{ activity.title }}</h1>
          <el-tag
            :type="getActivityRegisterStatusTagType(activity)"
            size="default"
            effect="light"
            class="detail-hero__tag"
          >
            {{ getActivityRegisterStatusLabel(activity) }}
          </el-tag>
        </div>
      </header>

      <div class="detail-layout">
        <div class="detail-main">
          <el-card shadow="never" class="portal-glass-card portal-glass-card--float content-card">
            <ActivityInfoGrid :items="infoItems" />

            <section class="description-section" aria-labelledby="activity-description-title">
              <h2 id="activity-description-title" class="section-title">活动说明</h2>
              <div class="description-panel">
                <p class="description-text">
                  {{ activity.description || '暂无活动说明' }}
                </p>
              </div>
            </section>

            <el-alert
              v-if="activity.myRegistration"
              type="info"
              :closable="false"
              show-icon
              class="status-alert"
              :title="`您已报名该活动（${getRegistrationStatusLabel(activity.myRegistration.status)}）`"
            >
              <template #default>
                报名时间：{{ formatDateTime(activity.myRegistration.applyTime) }}
              </template>
            </el-alert>

            <el-alert
              v-else-if="!activity.canRegister && activity.registerDisabledReason"
              type="warning"
              :closable="false"
              show-icon
              class="status-alert"
              :title="activity.registerDisabledReason"
            />
          </el-card>
        </div>

        <aside class="detail-aside">
          <el-card shadow="never" class="portal-glass-card portal-glass-card--float action-card">
            <h2 class="action-card__title">报名操作</h2>

            <p v-if="registerDisabledHint" class="action-card__hint">
              {{ registerDisabledHint }}
            </p>
            <p v-else class="action-card__hint action-card__hint--success">
              当前可提交报名申请，审核通过后即可参加。
            </p>

            <el-tooltip
              :content="registerDisabledHint"
              :disabled="activity.canRegister"
              placement="top"
            >
              <span class="action-card__button-wrap">
                <el-button
                  size="large"
                  class="portal-btn-primary action-card__button"
                  :disabled="!activity.canRegister"
                  @click="openRegisterDialog"
                >
                  立即报名
                </el-button>
              </span>
            </el-tooltip>
          </el-card>
        </aside>
      </div>
    </template>

    <el-dialog
      v-model="registerDialogVisible"
      title="立即报名"
      width="480px"
      destroy-on-close
      @closed="registerError = ''"
    >
      <p v-if="activity" class="dialog-subtitle">
        {{ activity.title }}
      </p>

      <el-alert
        v-if="registerError"
        type="error"
        :title="registerError"
        :closable="false"
        show-icon
        class="form-alert"
        role="alert"
      />

      <el-form label-width="80px">
        <el-form-item label="备注">
          <el-input
            v-model="registerForm.remark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="选填，最多 500 字"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="submitRegister">
          确认报名
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-nav {
  margin-bottom: var(--space-4);
}

.detail-hero {
  margin-bottom: var(--space-4);
}

.detail-hero__main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.detail-hero__title {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--color-text-primary);
  text-wrap: balance;
}

.detail-hero__tag {
  flex-shrink: 0;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: var(--space-4);
  align-items: start;
}

.description-section {
  margin: 0;
}

.section-title {
  margin: 0 0 var(--space-3);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.description-panel {
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-surface);
}

.description-text {
  margin: 0;
  line-height: 1.8;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  word-break: break-word;
}

.status-alert {
  margin: 0;
}

.action-card__title {
  margin: 0 0 var(--space-4);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.action-card__hint {
  margin: 0 0 var(--space-4);
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
}

.action-card__hint--success {
  color: var(--el-color-success);
}

.action-card__button-wrap {
  display: block;
  width: 100%;
}

.action-card__button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .detail-hero__title {
    font-size: 20px;
  }

  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-aside {
    order: 2;
  }

  .detail-main {
    order: 1;
  }

  .action-card {
    position: sticky;
    bottom: var(--space-4);
    z-index: 10;
  }
}
</style>
