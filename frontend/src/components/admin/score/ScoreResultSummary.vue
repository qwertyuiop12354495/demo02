<script setup lang="ts">
import type { SubmitScoreReviewResult } from '@/types/admin-score'
import { describeScoreOutcome, useScorerScope } from '@/composables/useScorerScope'

defineProps<{
  result: SubmitScoreReviewResult
}>()

const { getStepLabel } = useScorerScope()
</script>

<template>
  <el-alert type="success" :closable="false" show-icon class="score-result-summary">
    <template #title>{{ result.message }}</template>
    <div class="score-result-summary__body">
      <p v-if="describeScoreOutcome(result)" class="score-result-summary__line">
        结果：{{ describeScoreOutcome(result) }}
      </p>
      <p v-if="result.allCompleted && result.finalScore != null" class="score-result-summary__line">
        本级均分：{{ result.finalScore }}
      </p>
      <p class="score-result-summary__line">
        进度：{{ result.completedCount }} / {{ result.requiredCount }} 位打分员已完成
      </p>
      <p class="score-result-summary__line">
        当前环节：{{ getStepLabel(result.currentStep) }}
      </p>
    </div>
  </el-alert>
</template>

<style scoped>
.score-result-summary {
  margin-bottom: var(--space-4);
}

.score-result-summary__body {
  margin-top: var(--space-2);
}

.score-result-summary__line {
  margin: 0 0 var(--space-1);
  font-size: 13px;
  line-height: 1.5;
}
</style>
