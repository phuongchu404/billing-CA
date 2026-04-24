import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth, isTokenExpired } from './auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const request = axios.create({
  baseURL: '',
  timeout: 15000,
})

// Prevent duplicate "session expired" messages when multiple requests fire at once
let sessionExpiredNotified = false

function forceLogout(message = 'Your session has expired. Please log in again.') {
  if (sessionExpiredNotified) return
  sessionExpiredNotified = true
  clearAuth()
  ElMessage.warning(message)
  setTimeout(() => { window.location.href = '/login' }, 1500)
}

request.interceptors.request.use(
  (config) => {
    NProgress.start()
    const token = getToken()
    if (token) {
      if (isTokenExpired(token)) {
        NProgress.done()
        forceLogout()
        return Promise.reject(new Error('Token expired'))
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    NProgress.done()
    return response.data
  },
  (error) => {
    NProgress.done()
    
    // Mock successful responses for all requests to bypass backend
    const mockResponse = {
      success: true,
      code: '0000',
      message: 'Success (Mocked)',
      data: [] as any
    }

    // Customize mock data based on URL if needed
    const url = error.config?.url || ''
    if (url.includes('summary')) {
      mockResponse.data = {
        active: 125,
        pending: 12,
        expired: 5,
        cancelled: 3,
        suspended: 2,
        newCertsThisMonth: 45,
        usageThisMonth: 1250,
        expiringSoon: 8
      }
    } else if (url.includes('subscriptions')) {
      mockResponse.data = [
        { id: 1, planName: 'Premium Plan (Partner)', status: 'ACTIVE', createdAt: '2026-04-23T10:00:00Z', endDate: '2026-12-31', subscriberType: 'GROUP', groupId: 'MK-001', userId: null },
        { id: 2, planName: 'Basic Plan (Individual)', status: 'PENDING', createdAt: '2026-04-20T15:30:00Z', endDate: '2026-11-30', subscriberType: 'INDIVIDUAL', groupId: null, userId: 'user_99' },
        { id: 3, planName: 'Standard Plan', status: 'ACTIVE', createdAt: '2026-04-22T08:00:00Z', endDate: '2027-04-22', subscriberType: 'GROUP', groupId: 'MK-002', userId: null }
      ]
    } else if (url.includes('reports')) {
      // General reports mock
      mockResponse.data = [
        { id: 101, planName: 'Premium Plan', status: 'ACTIVE', createdAt: '2026-04-23', endDate: '2026-12-31' }
      ]
    } else if (url.includes('roles/permissions/matrix')) {
      mockResponse.data = {
        roles: [
          { roleId: 1, roleName: 'ROLE_ADMIN',   displayName: 'Admin',          isSystemRole: true  },
          { roleId: 2, roleName: 'ROLE_LEVEL_1',  displayName: 'Vai trò cấp 1', isSystemRole: false },
          { roleId: 3, roleName: 'ROLE_LEVEL_2',  displayName: 'Vai trò cấp 2', isSystemRole: false },
          { roleId: 4, roleName: 'ROLE_LEVEL_3',  displayName: 'Vai trò cấp 3', isSystemRole: false },
          { roleId: 5, roleName: 'ROLE_LEVEL_4',  displayName: 'Vai trò cấp 4', isSystemRole: false },
        ],
        moduleGroups: [
          {
            moduleName: 'DASHBOARD',
            permissionGroups: [
              {
                groupName: 'DASHBOARD',
                permissions: [
                  { permissionId: 1, permissionCode: 'dashboard:view', displayName: 'Xem thông tin' },
                ],
              },
            ],
          },
          {
            moduleName: 'SUBSCRIPTION_MANAGEMENT',
            permissionGroups: [
              {
                groupName: 'PLAN',
                permissions: [
                  { permissionId: 2, permissionCode: 'plan:view',   displayName: 'Xem thông tin theo thiết bị'   },
                  { permissionId: 3, permissionCode: 'plan:create', displayName: 'Xem thông tin theo chi nhánh'  },
                  { permissionId: 4, permissionCode: 'plan:update', displayName: 'Thêm chi nhánh'                },
                  { permissionId: 5, permissionCode: 'plan:delete', displayName: 'Đổi chi nhánh cho thiết bị'    },
                ],
              },
              {
                groupName: 'SUBSCRIPTION',
                permissions: [
                  { permissionId: 6, permissionCode: 'subscription:view',   displayName: 'Xem thông tin'              },
                  { permissionId: 7, permissionCode: 'subscription:create', displayName: 'Tải lên firmware'            },
                  { permissionId: 8, permissionCode: 'subscription:update', displayName: 'Cấu hình cập nhật firmware'  },
                ],
              },
            ],
          },
          {
            moduleName: 'SYSTEM_CONFIGURATION',
            permissionGroups: [
              {
                groupName: 'USER',
                permissions: [
                  { permissionId: 9,  permissionCode: 'user:view',   displayName: 'Xem danh sách người dùng' },
                  { permissionId: 10, permissionCode: 'user:create', displayName: 'Tạo người dùng'           },
                  { permissionId: 11, permissionCode: 'user:update', displayName: 'Chỉnh sửa người dùng'     },
                ],
              },
              {
                groupName: 'ROLE',
                permissions: [
                  { permissionId: 12, permissionCode: 'role:view',   displayName: 'Xem danh sách vai trò' },
                  { permissionId: 13, permissionCode: 'role:create', displayName: 'Tạo vai trò'           },
                  { permissionId: 14, permissionCode: 'role:update', displayName: 'Chỉnh sửa vai trò'     },
                ],
              },
              {
                groupName: 'AUDIT_LOG',
                permissions: [
                  { permissionId: 15, permissionCode: 'audit-log:view', displayName: 'Xem nhật ký hệ thống' },
                ],
              },
            ],
          },
          {
            moduleName: 'ANALYTICS',
            permissionGroups: [
              {
                groupName: 'REPORT',
                permissions: [
                  { permissionId: 16, permissionCode: 'report:view', displayName: 'Xem báo cáo' },
                ],
              },
            ],
          },
        ],
        rolePermissions: {
          1: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],
          2: [1,2,3,6,9,12,16],
          3: [1,2,3],
          4: [1,2,3,4,5,6,9,12],
          5: [2,3],
        },
        roleUserCounts: { 1: 4, 2: 3, 3: 2, 4: 4, 5: 1 },
      }
    } else if (url.includes('/roles')) {
      mockResponse.data = [
        { roleId: 1, roleName: 'ROLE_ADMIN',   displayName: 'Admin',          isSystemRole: true  },
        { roleId: 2, roleName: 'ROLE_LEVEL_1', displayName: 'Vai trò cấp 1', isSystemRole: false },
        { roleId: 3, roleName: 'ROLE_LEVEL_2', displayName: 'Vai trò cấp 2', isSystemRole: false },
        { roleId: 4, roleName: 'ROLE_LEVEL_3', displayName: 'Vai trò cấp 3', isSystemRole: false },
        { roleId: 5, roleName: 'ROLE_LEVEL_4', displayName: 'Vai trò cấp 4', isSystemRole: false },
      ]
    } else if (url.includes('users')) {
      mockResponse.data = [
        { userId: 1, username: 'admin', roles: [{ roleName: 'ROLE_ADMIN' }] }
      ]
    } else if (url.includes('login')) {
      mockResponse.data = {
        accessToken: 'mock_access_token_header.' + btoa(JSON.stringify({
          sub: 'admin',
          username: 'admin',
          roles: ['ROLE_ADMIN'],
          permissions: ['*'],
          exp: Math.floor(Date.now() / 1000) + 3600
        })) + '.mock_signature',
        refreshToken: 'mock_refresh_token'
      }
    }

    console.log(`[Mock API] ${url}`, mockResponse)
    return Promise.resolve(mockResponse)
  }
)

export default request
