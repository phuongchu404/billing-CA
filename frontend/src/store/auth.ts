import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, setRefreshToken, clearAuth, getRefreshToken } from '@/utils/auth'
import type { UserAccount, LoginRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserAccount | null>(JSON.parse(localStorage.getItem('rs_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userRoles = computed(() => user.value?.roles?.map(r => r.roleName) || [])
  const isAdmin = computed(() => userRoles.value.includes('ROLE_ADMIN'))
  const isOperator = computed(() => userRoles.value.includes('ROLE_OPERATOR'))
  const isPartner = computed(() => userRoles.value.includes('ROLE_PARTNER'))
  const isManager = computed(() => userRoles.value.includes('ROLE_MANAGER'))
  const permissions = computed(() => user.value?.permissions || [])
  function hasPermission(key?: string): boolean {
    if (!key) return true
    const values = permissions.value
    return values.includes('*') || values.includes(key)
  }
  /** Kiểm tra user có bất kỳ permission nào trong danh sách không */
  function hasAnyPermission(...keys: string[]): boolean {
    return keys.some(k => hasPermission(k))
  }

  async function doLogin(credentials: LoginRequest) {
    const res = await loginApi(credentials)
    if (res.success && res.data) {
      token.value = res.data.accessToken
      setToken(res.data.accessToken)
      setRefreshToken(res.data.refreshToken)
      const loginData = res.data
      const roles = (loginData.roles || []).map((roleName: string, index: number) => ({
        roleId: index + 1,
        roleName,
        displayName: roleName,
        isSystemRole: roleName === 'ROLE_ADMIN',
        createdAt: '',
      }))
      user.value = {
        userId: loginData.userId || '',
        username: loginData.username || '',
        email: loginData.email || '',
        fullName: loginData.fullName || loginData.username || '',
        authProvider: 'LOCAL',
        status: 'ACTIVE',
        roles,
        permissions: loginData.permissions || [],
        createdAt: '',
      }
      localStorage.setItem('rs_user', JSON.stringify(user.value))
    }
    return res
  }

  async function doLogout() {
    const rt = getRefreshToken()
    if (rt) {
      await logoutApi(rt).catch(() => {})
    }
    token.value = null
    user.value = null
    clearAuth()
  }

  return { token, user, isLoggedIn, isAdmin, isOperator, isPartner, isManager, userRoles, permissions, hasPermission, hasAnyPermission, doLogin, doLogout }
})
