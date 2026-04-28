import { Lock } from '@element-plus/icons-vue'
import type { MenuItem } from '../../menutype'

export const roleMenu: MenuItem = {
  tag: 'system:role',
  leaf: true,
  path: '/roles',
  type: 'menu',
  labelKey: 'menu.roleManagement',
  icon: Lock,
  permissionKey: 'role:view',
  children: [],
  permissions: [
    { tag: 'role:view', type: 'button', labelKey: 'common.search', permissionKey: 'role:view', pattern: '/api/v1/admin/roles', method: 'GET' },
    { tag: 'role:create', type: 'button', labelKey: 'common.add', permissionKey: 'role:create', pattern: '/api/v1/admin/roles', method: 'POST' },
    { tag: 'role:update', type: 'button', labelKey: 'common.edit', permissionKey: 'role:update', pattern: '/api/v1/admin/roles/{roleId}', method: 'PUT' },
    { tag: 'role:update:delete', type: 'button', labelKey: 'common.delete', permissionKey: 'role:update', pattern: '/api/v1/admin/roles/{roleId}', method: 'DELETE' },
    { tag: 'role:update:assign-permission', type: 'button', labelKey: 'roles.assignPermissions', permissionKey: 'role:update', pattern: '/api/v1/admin/roles/{roleId}/permissions', method: 'PUT' },
    { tag: 'role:view:permission-matrix', type: 'button', labelKey: 'roles.permissionMatrix', permissionKey: 'role:view', pattern: '/api/v1/admin/roles/permissions/matrix', method: 'GET' },
  ],
}
