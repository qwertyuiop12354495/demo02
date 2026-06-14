<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <header class="portal-page-header">
    <div>
      <h1 class="portal-page-header__title">{{ title }}</h1>
      <p v-if="description" class="portal-page-header__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.extra">
      <slot name="extra" />
    </div>
  </header>
</template>
