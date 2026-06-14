<script setup lang="ts">
import { ref, watch } from 'vue'
import { listWorkFiles } from '@/api/work'
import { formatDateTime } from '@/composables/useDateTime'
import type { WorkFile } from '@/types/work'

const props = defineProps<{
  visible: boolean
  workId: number | null
  workTitle?: string | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const loading = ref(false)
const files = ref<WorkFile[]>([])
const loadError = ref('')

function formatSize(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function openFile(url: string) {
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function loadFiles() {
  if (!props.workId) {
    files.value = []
    return
  }
  loading.value = true
  loadError.value = ''
  files.value = []
  try {
    files.value = await listWorkFiles(props.workId)
  } catch (error) {
    if (error && typeof error === 'object' && 'message' in error) {
      loadError.value = String((error as { message?: string }).message || '加载失败')
    } else {
      loadError.value = '暂无权限查看材料，需后端为打分员开放材料查询接口'
    }
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}

watch(
  () => [props.visible, props.workId] as const,
  ([open, workId]) => {
    if (open && workId) {
      loadFiles()
    }
  },
)
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="作品材料"
    size="480px"
    destroy-on-close
    @close="handleClose"
  >
    <p v-if="workTitle" class="materials-drawer__meta">作品：{{ workTitle }}</p>

    <div v-loading="loading">
      <el-alert
        v-if="loadError"
        type="warning"
        :closable="false"
        show-icon
        :title="loadError"
        class="materials-drawer__alert"
      />

      <el-table v-else-if="files.length" :data="files" stripe>
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column label="大小" width="90">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="上传时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFile(row.fileUrl)">打开</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else-if="!loading && !loadError" description="暂无材料" />
    </div>
  </el-drawer>
</template>

<style scoped>
.materials-drawer__meta {
  margin: 0 0 var(--space-4);
  font-size: 14px;
  color: var(--color-text-secondary);
}

.materials-drawer__alert {
  margin-bottom: var(--space-4);
}
</style>
