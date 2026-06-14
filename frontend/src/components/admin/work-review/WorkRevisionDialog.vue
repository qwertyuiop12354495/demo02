<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { WorkReviewListItem } from '@/types/admin-work-review'

const props = defineProps<{
  visible: boolean
  row: WorkReviewListItem | null
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [feedback: string]
}>()

const formRef = ref<FormInstance>()
const form = reactive({
  feedback: '',
})

const rules: FormRules = {
  feedback: [
    { required: true, message: '请填写修改意见', trigger: 'blur' },
    { min: 1, max: 2000, message: '修改意见长度为 1-2000 字', trigger: 'blur' },
  ],
}

watch(
  () => props.visible,
  (open) => {
    if (open) {
      form.feedback = ''
      formRef.value?.clearValidate()
    }
  },
)

function handleClose() {
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', form.feedback.trim())
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="退回修改"
    width="520px"
    destroy-on-close
    @close="handleClose"
  >
    <p v-if="row" class="revision-dialog__hint">
      作品「{{ row.title }}」将被退回，教师修改后可重新提交。
    </p>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="修改意见" prop="feedback">
        <el-input
          v-model="form.feedback"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="请填写需要教师修改的内容…"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="warning" :loading="submitting" @click="handleSubmit">
        确认退回
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.revision-dialog__hint {
  margin: 0 0 var(--space-4);
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
