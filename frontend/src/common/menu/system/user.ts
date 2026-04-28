import { User } from '@element-plus/icons-vue'
import type { MenuItem } from '../../menutype'

export const userMenu: MenuItem = {
  tag: 'system:user',
  leaf: true,
  path: '/users',
  type: 'menu',
  labelKey: 'menu.accountManagement',
  icon: User,
  permissionKey: 'user:view',
  children: [],
  permissions: [
    { tag: 'user:view', type: 'button', labelKey: 'common.search', permissionKey: 'user:view', pattern: '/api/v1/admin/users', method: 'GET' },
    { tag: 'user:create', type: 'button', labelKey: 'common.add', permissionKey: 'user:create', pattern: '/api/v1/admin/users', method: 'POST' },
    { tag: 'user:update', type: 'button', labelKey: 'common.edit', permissionKey: 'user:update', pattern: '/api/v1/admin/users/{userId}', method: 'PUT' },
    { tag: 'user:delete', type: 'button', labelKey: 'common.delete', permissionKey: 'user:delete', pattern: '/api/v1/admin/users/{userId}', method: 'DELETE' },
    { tag: 'user:update:assign-roles', type: 'button', labelKey: 'roles.assignRoles', permissionKey: 'user:update', pattern: '/api/v1/admin/users/{userId}/roles', method: 'PUT' },
    { tag: 'user:update:reset-password', type: 'button', labelKey: 'roles.resetPassword', permissionKey: 'user:update', pattern: '/api/v1/admin/users/{userId}/password', method: 'PATCH' },
  ],
}
