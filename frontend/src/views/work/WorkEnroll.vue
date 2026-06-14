<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PortalGlassCard from '@/components/portal/PortalGlassCard.vue'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import WorkFileUploader from '@/components/work/WorkFileUploader.vue'
import WorkFormPanel from '@/components/work/WorkFormPanel.vue'
import WorkRevisionAlert from '@/components/work/WorkRevisionAlert.vue'
import WorkStatusTag from '@/components/work/WorkStatusTag.vue'
import { pageMyWorks } from '@/api/work'
import { useWorkEnroll } from '@/composables/useWorkEnroll'
import {
  getTeacherWorkStatusLabel,
  getWorkStatusTone,
  isWorkEditable,
} from '@/composables/useWorkStatusLabel'
import type { WorkSavePayload } from '@/types/work'

const route = useRoute()
const router = useRouter()

const activityId = computed(() => Number(route.params.activityId))
const isValidActivityId = computed(
  () => Number.isFinite(activityId.value) && activityId.value > 0,
)

const formModel = reactive<WorkSavePayload>({
  title: '',
  category: undefined,
  equipment: undefined,
  duration: 1,
})

const {
  loading,
  saving,
  submitting,
  uploading,
  work,
  files,
  editable,
  isRevisionRequired,
  revisionFeedback,
  ensureDraft,
  loadWork,
  saveForm,
  uploadFile,
  removeFile,
  confirmAndSubmit,
} = useWorkEnroll(activityId)

const statusLabel = computed(() => {
  if (!work.value) {
    return ''
  }
  return getTeacherWorkStatusLabel(
    work.value.currentStep,
    work.value.currentStatus,
    work.value.finalResult,
  )
})

const showRevisionPlaceholder = computed(
  () => isRevisionRequired.value && !revisionFeedback.value,
)

function syncFormFromWork() {
  if (!work.value) {
    return
  }
  formModel.title = work.value.title ?? ''
  formModel.category = work.value.category ?? undefined
  formModel.equipment = work.value.equipment ?? undefined
  formModel.duration = work.value.duration && work.value.duration > 0 ? work.value.duration : 1
}

function validateForm(): boolean {
  if (!formModel.title.trim()) {
    ElMessage.warning('请填写作品标题')
    return false
  }
  if (!formModel.duration || formModel.duration <= 0) {
    ElMessage.warning('请填写有效的作品时长')
    return false
  }
  const category = formModel.category?.trim().toUpperCase()
  if (
    (category === 'MUSIC' || formModel.category === '音乐') &&
    !formModel.equipment?.trim()
  ) {
    ElMessage.warning('音乐类作品请填写使用器材')
    return false
  }
  return true
}

async function handleSave() {
  if (!validateForm()) {
    return
  }
  await saveForm({
    title: formModel.title.trim(),
    category: formModel.category?.trim() || undefined,
    equipment: formModel.equipment?.trim() || undefined,
    duration: formModel.duration,
  })
}

async function handleSubmit() {
  if (!editable.value) {
    return
  }
  if (!validateForm()) {
    return
  }
  await saveForm({
    title: formModel.title.trim(),
    category: formModel.category?.trim() || undefined,
    equipment: formModel.equipment?.trim() || undefined,
    duration: formModel.duration,
  })
  await confirmAndSubmit()
}

async function handleRemoveFile(fileId: number) {
  await removeFile(fileId)
}

async function initPage() {
  if (!isValidActivityId.value) {
    ElMessage.error('活动 ID 无效')
    router.replace('/')
    return
  }
  try {
    await ensureDraft()
    syncFormFromWork()
  } catch {
    try {
      const mine = await pageMyWorks({ activityId: activityId.value, page: 1, pageSize: 1 })
      const existing = mine.list[0]
      if (existing) {
        await loadWork(existing.id)
        syncFormFromWork()
        return
      }
    } catch {
      // fall through
    }
    router.replace('/')
  }
}

watch(work, syncFormFromWork)

onMounted(initPage)
</script>

<template>
  <div v-loading="loading" class="portal-page portal-page--narrow work-enroll">
    <PortalPageHeader
      :title="work?.activityTitle ?? '作品报名'"
      description="填写作品信息、上传材料后提交报名。"
    >
      <template #extra>
        <el-button link type="primary" @click="router.push('/')">返回首页</el-button>
      </template>
    </PortalPageHeader>

    <div v-if="work" class="work-enroll__status">
      <WorkStatusTag :label="statusLabel" :tone="getWorkStatusTone(work.currentStatus)" />
    </div>

    <WorkRevisionAlert v-if="revisionFeedback" :feedback="revisionFeedback" />

    <el-alert
      v-else-if="showRevisionPlaceholder"
      type="warning"
      :closable="false"
      show-icon
      title="作品需修改"
      description="管理员已退回您的作品，请根据审核意见修改后重新提交。"
      class="work-enroll__alert"
    />

    <PortalGlassCard :float="false" class="work-enroll__card">
      <WorkFormPanel
        v-model="formModel"
        :disabled="!editable"
        @submit="handleSave"
      />
    </PortalGlassCard>

    <PortalGlassCard :float="false" class="work-enroll__card">
      <WorkFileUploader
        :files="files"
        :editable="editable"
        :uploading="uploading"
        @upload="uploadFile"
        @remove="handleRemoveFile"
      />
    </PortalGlassCard>

    <div v-if="editable" class="work-enroll__actions">
      <el-button :loading="saving" @click="handleSave">保存草稿</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isRevisionRequired ? '重新提交' : '提交报名' }}
      </el-button>
    </div>

    <el-alert
      v-else-if="work && !isWorkEditable(work.currentStatus)"
      type="info"
      :closable="false"
      show-icon
      title="当前作品不可编辑"
      description="作品已提交并进入审核流程，请在我的作品中查看进度。"
    />
  </div>
</template>

<style scoped>
.work-enroll__status {
  margin-bottom: var(--space-4);
}

.work-enroll__alert {
  margin-bottom: var(--space-4);
}

.work-enroll__card {
  padding: var(--space-5);
  margin-bottom: var(--space-4);
}

.work-enroll__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-2);
}
</style>
