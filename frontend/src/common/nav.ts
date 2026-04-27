import {
  Avatar,
  House,
  List,
  Odometer,
  Setting,
  TrendCharts,
  UserFilled,
  Key,
  Checked,
} from '@element-plus/icons-vue'
import { systemMenu } from './menu/system'
import type { BackPermission, MenuItem, TreeItem } from './menutype'

export const getNavData = (): MenuItem[] => [
  {
    tag: 'dashboard',
    leaf: true,
    path: '/dashboard',
    type: 'menu',
    labelKey: 'menu.home',
    icon: House,
    permissionKey: 'dashboard:view',
    isWhiteList: true,
    children: [],
    permissions: [
      { tag: 'dashboard:view', type: 'button', labelKey: 'common.detail', permissionKey: 'dashboard:view', isWhiteList: true },
    ],
  },
  {
    tag: 'reports',
    leaf: true,
    path: '/reports',
    type: 'menu',
    labelKey: 'menu.dashboard',
    icon: Odometer,
    permissionKey: 'report:view',
    children: [],
    permissions: [
      // GROUP tab: 1 button → all GROUP report APIs granted automatically
      { tag: 'reports:group:view',          type: 'button', labelKey: 'menu.reportGroupTab',      permissionKey: 'report:group:view',      pattern: '/api/v1/reports/group',               method: 'GET' },
      { tag: 'reports:group:expiring',      type: 'api',    labelKey: 'menu.reportGroupTab',      permissionKey: 'report:group:view',      pattern: '/api/v1/reports/group/expiring-soon', method: 'GET' },
      // INDIVIDUAL tab: 1 button → all INDIVIDUAL report APIs granted automatically
      { tag: 'reports:individual:view',     type: 'button', labelKey: 'menu.reportIndividualTab', permissionKey: 'report:individual:view', pattern: '/api/v1/reports/individual',          method: 'GET' },
    ],
  },
  {
    tag: 'individual',
    leaf: false,
    type: 'menu',
    labelKey: 'menu.regularCustomer',
    icon: Avatar,
    permissionKey: 'plan:view',
    children: [
      {
        tag: 'individual:plan-config',
        leaf: true,
        path: '/individual-plan-config',
        type: 'menu',
        labelKey: 'menu.individualPlanConfig',
        icon: Setting,
        permissionKey: 'plan:view',
        children: [],
        permissions: [
          { tag: 'individual:plan-config:view', type: 'button', labelKey: 'common.search', permissionKey: 'plan:view', pattern: '/api/v1/individual/plan-configs', method: 'GET' },
          { tag: 'individual:plan-config:create', type: 'button', labelKey: 'common.add', permissionKey: 'plan:create', pattern: '/api/v1/individual/plan-configs', method: 'POST' },
          { tag: 'individual:plan-config:update', type: 'button', labelKey: 'common.edit', permissionKey: 'plan:update', pattern: '/api/v1/individual/plan-configs/{id}/**', method: 'POST' },
        ],
      },
      {
        tag: 'individual:usage',
        leaf: true,
        path: '/individual-usage-tracking',
        type: 'menu',
        labelKey: 'menu.individualUsageTracking',
        icon: TrendCharts,
        permissionKey: 'individual:usage:view',
        children: [],
        permissions: [
          { tag: 'individual:usage:view', type: 'button', labelKey: 'common.search', permissionKey: 'individual:usage:view', pattern: '/api/v1/individual/usage-tracking', method: 'GET' },
        ],
      },
    ],
    permissions: [],
  },
  {
    tag: 'plans',
    leaf: true,
    path: '/plans',
    type: 'menu',
    labelKey: 'menu.agencyCustomer',
    icon: UserFilled,
    permissionKey: 'group:view',
    children: [],
    permissions: [
      { tag: 'plans:view',   type: 'button', labelKey: 'common.search', permissionKey: 'group:view',   pattern: '/api/v1/groups',      method: 'GET' },
      { tag: 'plans:create', type: 'button', labelKey: 'common.add',    permissionKey: 'group:create', pattern: '/api/v1/groups',      method: 'POST' },
      { tag: 'plans:update', type: 'button', labelKey: 'common.edit',   permissionKey: 'group:update', pattern: '/api/v1/groups/{id}', method: 'PUT' },
    ],
  },
  systemMenu,
  {
    tag: 'partner-access',
    leaf: true,
    path: '/partner-access',
    type: 'menu',
    labelKey: 'menu.partnerAccess',
    icon: Key,
    permissionKey: 'partner:access:grant',
    children: [],
    permissions: [
      { tag: 'partner-access:grant',  type: 'button', labelKey: 'menu.partnerAccessGrant',  permissionKey: 'partner:access:grant',  pattern: '/api/v1/partner-access',   method: 'POST'   },
      { tag: 'partner-access:revoke', type: 'button', labelKey: 'menu.partnerAccessRevoke', permissionKey: 'partner:access:revoke', pattern: '/api/v1/partner-access/**', method: 'DELETE' },
    ],
  },
  {
    tag: 'approvals',
    leaf: true,
    path: '/approvals',
    type: 'menu',
    labelKey: 'menu.approvals',
    icon: Checked,
    permissionKey: 'approval:view',
    children: [],
    permissions: [
      // Sale: xem danh sách + submit + resubmit
      { tag: 'approval:view',      type: 'button', labelKey: 'common.search',   permissionKey: 'approval:view',      pattern: '/api/v1/approval-requests',             method: 'GET' },
      { tag: 'approval:submit',    type: 'api',    labelKey: 'common.confirm',  permissionKey: 'subscription:update', pattern: '/api/v1/approval-requests/*/submit',    method: 'POST' },
      { tag: 'approval:resubmit',  type: 'api',    labelKey: 'common.confirm',  permissionKey: 'subscription:update', pattern: '/api/v1/approval-requests/*/resubmit',  method: 'POST' },
      // Approver cấp 1
      { tag: 'approval:level1',    type: 'button', labelKey: 'menu.approveL1', permissionKey: 'approval:level1',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
      // Approver cấp 2
      { tag: 'approval:level2',    type: 'button', labelKey: 'menu.approveL2', permissionKey: 'approval:level2',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
      // Approver cấp 3
      { tag: 'approval:level3',    type: 'button', labelKey: 'menu.approveL3', permissionKey: 'approval:level3',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
    ],
  },
  {
    tag: 'audit-log',
    leaf: true,
    path: '/audit-logs',
    type: 'menu',
    labelKey: 'menu.logsManagement',
    icon: List,
    permissionKey: 'audit-log:view',
    children: [],
    permissions: [
      { tag: 'audit-log:view', type: 'button', labelKey: 'common.search', permissionKey: 'audit-log:view', pattern: '/api/v1/admin/audit-logs', method: 'GET' },
    ],
  },
]

export function getPermissionTree(permissionKeys: readonly string[] = []): TreeItem[] {
  return buildTree(getNavData(), new Set(permissionKeys))
}

export function getPermissions(): BackPermission[] {
  const list: BackPermission[] = []
  collectPermissions(getNavData(), list)
  return list
}

export function createMenuPermissionMapping() {
  const mapping = new Map<string, string[]>()
  const processMenu = (items: MenuItem[]) => {
    for (const item of items) {
      if (item.leaf && item.permissions) {
        const buttons = item.permissions.filter(p => p.type === 'button')
        const apiPerms = item.permissions.filter(p => p.type === 'api')
        for (const button of buttons) mapping.set(button.tag, apiPerms.map(p => p.tag))
      } else if (item.children?.length) {
        processMenu(item.children)
      }
    }
  }
  processMenu(getNavData())
  return mapping
}

function collectPermissions(items: readonly MenuItem[], acc: BackPermission[]) {
  for (const item of items) {
    acc.push({ tag: item.tag, type: item.type, permissionKey: item.permissionKey, isWhiteList: item.isWhiteList })
    if (item.leaf) {
      for (const permission of item.permissions ?? []) {
        acc.push({
          tag: permission.tag,
          type: permission.type,
          permissionKey: permission.permissionKey,
          isWhiteList: permission.isWhiteList,
          method: permission.method,
          pattern: permission.pattern,
        })
      }
    } else {
      collectPermissions(item.children ?? [], acc)
    }
  }
}

function buildTree(items: readonly MenuItem[], permissionSet: ReadonlySet<string>): TreeItem[] {
  const result: TreeItem[] = []
  for (const menu of items) {
    const node: TreeItem = { tag: menu.tag, labelKey: menu.labelKey, permissionKey: menu.permissionKey, children: [] }
    if (menu.leaf) {
      // Leaf menu: ẩn nếu không có permission truy cập
      if (!canShow(menu.permissionKey, permissionSet, menu.isWhiteList)) continue
      for (const permission of menu.permissions ?? []) {
        if (permission.type === 'api') continue
        if (!canShow(permission.permissionKey, permissionSet, permission.isWhiteList)) continue
        node.children?.push({ tag: permission.tag, labelKey: permission.labelKey, permissionKey: permission.permissionKey })
      }
      result.push(node)
    } else {
      // Group menu: hiện nếu ít nhất 1 menu con được phép (không kiểm tra permissionKey cha)
      node.children = buildTree(menu.children ?? [], permissionSet)
      if (node.children.length > 0) result.push(node)
    }
  }
  return result
}

function canShow(permissionKey: string | undefined, permissionSet: ReadonlySet<string>, whiteListed = false) {
  return whiteListed || !permissionKey || permissionSet.has('*') || permissionSet.has(permissionKey)
}
