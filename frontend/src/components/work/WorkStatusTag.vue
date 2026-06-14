<script setup lang="ts">
import type { WorkStatusTone } from '@/composables/useWorkStatusLabel'

defineProps<{
  label: string
  tone?: WorkStatusTone
}>()
</script>

<template>
  <el-tag :type="tone" effect="light" round class="work-status-tag">
    {{ label }}
  </el-tag>
</template>

<style scoped>
.work-status-tag {
  flex-shrink: 0;
}
</style>
