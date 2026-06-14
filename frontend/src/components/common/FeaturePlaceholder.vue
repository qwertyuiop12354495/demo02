<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="feature-placeholder">
    <h1 class="feature-placeholder__title">{{ title }}</h1>
    <p v-if="description" class="feature-placeholder__desc">{{ description }}</p>
    <p class="feature-placeholder__hint">功能开发中，敬请期待。</p>
  </div>
</template>

<style scoped>
.feature-placeholder {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  text-align: center;
}

.feature-placeholder__title {
  margin: 0 0 var(--space-3);
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.feature-placeholder__desc {
  margin: 0 0 var(--space-2);
  color: var(--color-text-secondary);
}

.feature-placeholder__hint {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-muted, #94a3b8);
}
</style>
