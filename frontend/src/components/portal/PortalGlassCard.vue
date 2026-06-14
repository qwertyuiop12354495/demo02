<script setup lang="ts">
withDefaults(
  defineProps<{
    float?: boolean
    tag?: 'div' | 'article'
  }>(),
  { float: true, tag: 'div' },
)
</script>

<template>
  <component
    :is="tag"
    class="portal-glass-card"
    :class="{ 'portal-glass-card--float': float }"
  >
    <slot />
  </component>
</template>
