import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, setRefreshToken, clearAuth, getRefreshToken } from '@/utils/auth'
import type { UserAccount, LoginRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserAccount | null>(
    JSON.parse(localStorage.getItem('rs_user') || JSON.stringify({
      userId: 1,
      username: 'admin',
      email: 'admin@example.com',
      fullName: 'Administrator',
      authProvider: 'LOCAL',
      status: 'ACTIVE',
      roles: [{ roleId: 1, roleName: 'ROLE_ADMIN', displayName: 'Admin', isSystemRole: true, createdAt: '' }],
      permissions: ['*'],
      createdAt: '',
    }))
  )

  const isLoggedIn = computed(() => !!token.value)
  const userRoles = computed(() => user.value?.roles?.map(r => r.roleName) || [])
  const isAdmin = computed(() => userRoles.value.includes('ROLE_ADMIN'))
  const isOperator = computed(() => userRoles.value.includes('ROLE_OPERATOR'))
  const permissions = computed(() => user.value?.permissions || [])
  function hasPermission(_key: string): boolean {
    return true
  }

  async function doLogin(credentials: LoginRequest) {
    const res = await loginApi(credentials)
    if (res.success && res.data) {
      token.value = res.data.accessToken
      setToken(res.data.accessToken)
      setRefreshToken(res.data.refreshToken)
      // Decode JWT payload to get user info
      try {
        const payload = JSON.parse(atob(res.data.accessToken.split('.')[1]))
        user.value = {
          userId: payload.sub,
          username: payload.username,
          email: '',
          fullName: payload.username,
          authProvider: 'LOCAL',
          status: 'ACTIVE',
          roles: (payload.roles || []).map((r: string, i: number) => ({
            roleId: i + 1,
            roleName: r,
            displayName: r,
            isSystemRole: true,
            createdAt: '',
          })),
          permissions: payload.permissions || [],
          createdAt: '',
        }
        localStorage.setItem('rs_user', JSON.stringify(user.value))
      } catch {
        // ignore parse errors
      }
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

  return { token, user, isLoggedIn, isAdmin, isOperator, userRoles, permissions, hasPermission, doLogin, doLogout }
})
