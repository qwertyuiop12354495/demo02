<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ScoreWorkListItem, ScopedScorerStats } from '@/types/admin-score'

const props = defineProps<{
  visible: boolean
  row: ScoreWorkListItem | null
  stats: ScopedScorerStats | null
  statsLoading?: boolean
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [payload: { manualScore: number; aiScore?: number | null }]
}>()

const formRef = ref<FormInstance>()
const form = reactive({
  manualScore: undefined as number | undefined,
  aiScore: undefined as number | undefined,
})

const rules: FormRules = {
  manualScore: [
    { required: true, message: '请输入人工分', trigger: 'blur' },
    {
      type: 'number',
      min: 0,
      max: 100,
      message: '人工分必须在 0-100 之间',
      trigger: 'blur',
    },
  ],
  aiScore: [
    {
      type: 'number',
      min: 0,
      max: 100,
      message: 'AI 分必须在 0-100 之间',
      trigger: 'blur',
    },
  ],
}

watch(
  () => props.visible,
  (open) => {
    if (open) {
      form.manualScore = undefined
      form.aiScore = undefined
      formRef.value?.clearValidate()
    }
  },
)

function handleClose() {
  emit('update:visible', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || form.manualScore == null) {
    return
  }
  emit('submit', {
    manualScore: form.manualScore,
    aiScore: form.aiScore ?? null,
  })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="提交评分"
    width="520px"
    destroy-on-close
    @close="handleClose"
  >
    <p v-if="row" class="score-dialog__hint">
      作品「{{ row.title }}」· 活动「{{ row.activityTitle || '—' }}」
    </p>

    <div v-loading="statsLoading" class="score-dialog__stats">
      <el-tag v-if="stats" type="info" effect="plain">
        本级打分进度：{{ stats.completedCount }} / {{ stats.requiredCount }}
      </el-tag>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-form-item label="人工分" prop="manualScore" required>
        <el-input-number
          v-model="form.manualScore"
          :min="0"
          :max="100"
          :precision="2"
          :step="1"
          controls-position="right"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="AI 分" prop="aiScore">
        <el-input-number
          v-model="form.aiScore"
          :min="0"
          :max="100"
          :precision="2"
          :step="1"
          controls-position="right"
          style="width: 180px"
        />
        <p class="score-dialog__field-hint">可选；若活动配置了 AI 权重，将参与后端加权计算</p>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        提交评分
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.score-dialog__hint {
  margin: 0 0 var(--space-3);
  font-size: 14px;
  color: var(--color-text-secondary);
}

.score-dialog__stats {
  min-height: 32px;
  margin-bottom: var(--space-4);
}

.score-dialog__field-hint {
  margin: var(--space-1) 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
