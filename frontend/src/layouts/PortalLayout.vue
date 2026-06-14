<script setup lang="ts">
import GlowBackground from '@/components/portal/GlowBackground.vue'
import PortalFooter from '@/components/portal/PortalFooter.vue'
import PortalNavbar from '@/components/portal/PortalNavbar.vue'
</script>

<template>
  <div class="portal-shell">
    <GlowBackground />
    <PortalNavbar mode="public" />
    <main>
      <slot />
    </main>
    <PortalFooter />
  </div>
</template>
