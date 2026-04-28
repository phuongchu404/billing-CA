import { Key } from '@element-plus/icons-vue'
import type { MenuItem } from '../../menutype'

export const partnerAccessMenu: MenuItem = {
  tag: 'partner-access',
  leaf: true,
  path: '/partner-access',
  type: 'menu',
  labelKey: 'menu.partnerAccess',
  icon: Key,
  permissionKey: 'partner:access:grant',
  children: [],
  permissions: [
    { tag: 'partner:access:grant',  type: 'button', labelKey: 'menu.partnerAccessGrant',  permissionKey: 'partner:access:grant',  pattern: '/api/v1/partner-access',   method: 'POST'   },
    { tag: 'partner:access:revoke', type: 'button', labelKey: 'menu.partnerAccessRevoke', permissionKey: 'partner:access:revoke', pattern: '/api/v1/partner-access/**', method: 'DELETE' },
  ],
}
