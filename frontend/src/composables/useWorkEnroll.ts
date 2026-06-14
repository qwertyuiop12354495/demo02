import { ref, computed, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createWorkDraft,
  deleteWorkFile,
  getWorkDetail,
  listWorkFiles,
  saveWork,
  submitWork,
  uploadWorkFile,
} from '@/api/work'
import { isWorkEditable } from '@/composables/useWorkStatusLabel'
import type { WorkFile, WorkSavePayload, WorkVO } from '@/types/work'

export const WORK_CATEGORY_OPTIONS = [
  { label: '音乐', value: 'MUSIC' },
  { label: '舞蹈', value: 'DANCE' },
  { label: '美术', value: 'ART' },
  { label: '戏剧', value: 'DRAMA' },
  { label: '其他', value: 'OTHER' },
] as const

export function useWorkEnroll(activityId: Ref<number>) {
  const loading = ref(false)
  const saving = ref(false)
  const submitting = ref(false)
  const uploading = ref(false)
  const work = ref<WorkVO | null>(null)
  const files = ref<WorkFile[]>([])

  const editable = computed(() => {
    if (!work.value) {
      return false
    }
    return isWorkEditable(work.value.currentStatus)
  })

  const isRevisionRequired = computed(
    () => work.value?.currentStatus === 'REVISION_REQUIRED',
  )

  const revisionFeedback = computed(
    () => work.value?.latestRevisionFeedback?.trim() || '',
  )

  async function ensureDraft() {
    loading.value = true
    try {
      const draft = await createWorkDraft({ activityId: activityId.value })
      work.value = draft
      files.value = draft.files ?? []
      return draft
    } finally {
      loading.value = false
    }
  }

  async function loadWork(workId: number) {
    loading.value = true
    try {
      const detail = await getWorkDetail(workId)
      work.value = detail
      files.value = detail.files ?? []
      return detail
    } finally {
      loading.value = false
    }
  }

  async function refreshFiles() {
    if (!work.value) {
      return
    }
    files.value = await listWorkFiles(work.value.id)
  }

  async function saveForm(payload: WorkSavePayload) {
    if (!work.value) {
      return null
    }
    saving.value = true
    try {
      const updated = await saveWork(work.value.id, payload)
      work.value = { ...work.value, ...updated, files: files.value }
      ElMessage.success('作品已保存')
      return updated
    } finally {
      saving.value = false
    }
  }

  async function uploadFile(file: File) {
    if (!work.value) {
      return
    }
    uploading.value = true
    try {
      await uploadWorkFile(work.value.id, file)
      await refreshFiles()
      ElMessage.success('材料上传成功')
    } finally {
      uploading.value = false
    }
  }

  async function removeFile(fileId: number) {
    if (!work.value) {
      return
    }
    await deleteWorkFile(work.value.id, fileId)
    await refreshFiles()
    ElMessage.success('材料已删除')
  }

  async function confirmAndSubmit() {
    if (!work.value) {
      return null
    }
    if (files.value.length < 1) {
      ElMessage.warning('提交前请至少上传 1 个材料')
      return null
    }

    await ElMessageBox.confirm(
      '确认提交报名？提交后将进入审核流程，在审核完成前不可再编辑。',
      '提交确认',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '再检查一下',
        type: 'warning',
      },
    )

    submitting.value = true
    try {
      const updated = await submitWork(work.value.id)
      work.value = { ...work.value, ...updated, files: files.value }
      ElMessage.success(
        work.value.currentStatus === 'SUBMITTED' ? '提交成功' : '已重新提交',
      )
      return updated
    } finally {
      submitting.value = false
    }
  }

  return {
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
    refreshFiles,
    saveForm,
    uploadFile,
    removeFile,
    confirmAndSubmit,
  }
}
