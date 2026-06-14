<script setup lang="ts">
import { formatDateTime } from '@/composables/useDateTime'
import type { WorkFile } from '@/types/work'

defineProps<{
  files: WorkFile[]
  editable?: boolean
  uploading?: boolean
}>()

const emit = defineEmits<{
  upload: [file: File]
  remove: [fileId: number]
}>()

function formatSize(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function handleFileChange(uploadFile: { raw?: File }) {
  if (uploadFile.raw) {
    emit('upload', uploadFile.raw)
  }
}
</script>

<template>
  <section class="work-file-uploader">
    <div class="work-file-uploader__head">
      <h3 class="work-file-uploader__title">报名材料</h3>
      <p class="work-file-uploader__hint">提交前至少上传 1 个材料</p>
    </div>

    <el-upload
      v-if="editable"
      drag
      :auto-upload="false"
      :show-file-list="false"
      :disabled="uploading"
      accept=".pdf,.doc,.docx,.mp4,.jpg,.jpeg,.png,.zip"
      @change="handleFileChange"
    >
      <div class="work-file-uploader__drop">
        <p>将文件拖到此处，或点击选择</p>
        <p class="work-file-uploader__drop-sub">支持 PDF、Word、图片、MP4 等格式</p>
      </div>
    </el-upload>

    <el-table v-if="files.length" :data="files" stripe class="work-file-uploader__table">
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="上传时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column v-if="editable" label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="emit('remove', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="尚未上传材料" :image-size="72" />
  </section>
</template>

<style scoped>
.work-file-uploader {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.work-file-uploader__head {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.work-file-uploader__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.work-file-uploader__hint {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.work-file-uploader__drop {
  padding: var(--space-4);
  text-align: center;
}

.work-file-uploader__drop-sub {
  margin: var(--space-2) 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.work-file-uploader__table {
  width: 100%;
}
</style>
