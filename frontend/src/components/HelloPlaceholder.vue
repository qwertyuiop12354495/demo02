<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span>{{ title }}</span>
    </template>
    <el-empty :description="description || '页面占位，业务功能待实现'" />
  </el-card>
</template>
