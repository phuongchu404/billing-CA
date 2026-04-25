import { useAuthStore } from '@/store'

export function usePermission() {
  const authStore = useAuthStore()
  const can = (permission: string): boolean => authStore.hasPermission(permission)
  return { can }
}
