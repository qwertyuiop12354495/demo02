<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(
  defineProps<{
    icon: Component
    title: string
    description: string
    revealDelay?: number
  }>(),
  { revealDelay: 0 },
)
</script>

<template>
  <article
    class="portal-feature-card portal-reveal"
    :style="revealDelay ? { animationDelay: `${revealDelay}ms` } : undefined"
  >
    <div class="portal-feature-card__icon">
      <el-icon :size="24">
        <component :is="icon" />
      </el-icon>
    </div>
    <h3 class="portal-feature-card__title">{{ title }}</h3>
    <p class="portal-feature-card__desc">{{ description }}</p>
  </article>
</template>
