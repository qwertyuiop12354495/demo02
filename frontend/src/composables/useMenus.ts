import { computed } from 'vue'
import { getMenusByRole, type AppMenuItem } from '@/constants/menus'
import { useUserStore } from '@/stores/user'

export function useMenus(placement?: AppMenuItem['placement']) {
  const userStore = useUserStore()

  const menus = computed(() => getMenusByRole(userStore.roleType, placement))

  return {
    menus,
    roleType: computed(() => userStore.roleType),
  }
}
