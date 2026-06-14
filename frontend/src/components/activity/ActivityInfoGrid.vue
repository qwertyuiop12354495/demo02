<script setup lang="ts">
export interface ActivityInfoItem {
  label: string
  value: string
  numeric?: boolean
}

defineProps<{
  items: ActivityInfoItem[]
}>()
</script>

<template>
  <div class="info-grid" role="list">
    <div
      v-for="item in items"
      :key="item.label"
      class="info-grid__item"
      role="listitem"
    >
      <span class="info-grid__label">{{ item.label }}</span>
      <span class="info-grid__value" :class="{ 'tabular-nums': item.numeric }">{{ item.value }}</span>
    </div>
  </div>
</template>

<style scoped>
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.info-grid__item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-muted);
  min-width: 0;
}

.info-grid__label {
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text-secondary);
}

.info-grid__value {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-primary);
  word-break: break-word;
}

.info-grid__value.tabular-nums {
  font-variant-numeric: tabular-nums;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
