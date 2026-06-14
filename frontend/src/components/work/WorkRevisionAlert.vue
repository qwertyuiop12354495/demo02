<script setup lang="ts">
defineProps<{
  feedback: string
}>()
</script>

<template>
  <el-alert
    class="work-revision-alert"
    type="warning"
    :closable="false"
    show-icon
    title="审核修改意见"
  >
    <p class="work-revision-alert__text">{{ feedback }}</p>
  </el-alert>
</template>

<style scoped>
.work-revision-alert {
  margin-bottom: var(--space-4);
}

.work-revision-alert__text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
