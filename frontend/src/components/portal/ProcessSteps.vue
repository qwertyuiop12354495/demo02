<script setup lang="ts">
export interface ProcessStepItem {
  title: string
  description: string
}

withDefaults(
  defineProps<{
    steps: ProcessStepItem[]
    reveal?: boolean
  }>(),
  { reveal: false },
)
</script>

<template>
  <ol class="portal-steps">
    <li
      v-for="(step, index) in steps"
      :key="step.title"
      class="portal-step"
      :class="{ 'portal-reveal': reveal }"
      :style="reveal ? { animationDelay: `${120 + index * 100}ms` } : undefined"
    >
      <span class="portal-step__num">{{ index + 1 }}</span>
      <h4 class="portal-step__title">{{ step.title }}</h4>
      <p class="portal-step__desc">{{ step.description }}</p>
    </li>
  </ol>
</template>
