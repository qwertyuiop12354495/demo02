<script setup lang="ts">
defineProps<{
  title: string
  subtitle: string
  description?: string
}>()
</script>

<template>
  <section class="portal-hero">
    <h1 class="portal-hero__title">{{ title }}</h1>
    <p class="portal-hero__subtitle">{{ subtitle }}</p>
    <p v-if="description" class="portal-hero__desc">{{ description }}</p>
    <div v-if="$slots.actions" class="portal-hero__actions">
      <slot name="actions" />
    </div>
  </section>
</template>
