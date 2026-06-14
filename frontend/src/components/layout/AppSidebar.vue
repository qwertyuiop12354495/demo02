<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenus } from '@/composables/useMenus'

const route = useRoute()
const { menus } = useMenus('sidebar')

const activePath = computed(() => route.path)
</script>

<template>
  <el-menu router :default-active="activePath" class="app-sidebar__menu">
    <el-menu-item v-for="item in menus" :key="item.key" :index="item.path">
      <span>{{ item.label }}</span>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.app-sidebar__menu {
  border-right: none;
}
</style>
