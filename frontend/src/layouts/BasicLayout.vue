<script setup lang="ts">
import GlowBackground from '@/components/portal/GlowBackground.vue'
import PortalNavbar from '@/components/portal/PortalNavbar.vue'
</script>

<template>
  <div class="portal-shell portal-shell--app">
    <GlowBackground />
    <PortalNavbar mode="app" />
    <el-main class="portal-main">
      <router-view />
    </el-main>
  </div>
</template>
