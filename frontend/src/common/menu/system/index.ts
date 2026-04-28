import { Lock } from '@element-plus/icons-vue'
import type { MenuItem } from '../../menutype'
import { roleMenu } from './role'
import { userMenu } from './user'
import { partnerAccessMenu } from './partnerAccess'

export const systemMenu: MenuItem = {
  tag: 'system',
  leaf: false,
  type: 'menu',
  labelKey: 'menu.permissionMgmt',
  icon: Lock,
  permissionKey: 'role:view',
  children: [roleMenu, userMenu, partnerAccessMenu],
  permissions: [],
}
