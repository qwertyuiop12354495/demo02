<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
    variant?: 'dark' | 'light'
    reveal?: boolean
  }>(),
  { variant: 'dark', reveal: false },
)
</script>

<template>
  <section
    :class="[
      variant === 'light' ? 'portal-section-light' : 'portal-section',
      { 'portal-reveal': reveal },
    ]"
  >
    <h2 class="portal-section__title">{{ title }}</h2>
    <p v-if="description" class="portal-section__desc">{{ description }}</p>
    <slot />
  </section>
</template>

<style scoped>
.portal-section {
  position: relative;
  z-index: 1;
  padding: 64px var(--space-6);
}
</style>
