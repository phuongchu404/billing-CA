import {
  Avatar,
  House,
  List,
  Odometer,
  Setting,
  TrendCharts,
  UserFilled,
  Checked,
  Tools,
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
      { tag: 'report:group:view',           type: 'button', labelKey: 'menu.reportGroupTab',      permissionKey: 'report:group:view',      pattern: '/api/v1/reports/group',               method: 'GET' },
      { tag: 'report:group:view:expiring',  type: 'api',    labelKey: 'menu.reportGroupTab',      permissionKey: 'report:group:view',      pattern: '/api/v1/reports/group/expiring-soon', method: 'GET' },
      // INDIVIDUAL tab: 1 button → all INDIVIDUAL report APIs granted automatically
      { tag: 'report:individual:view',      type: 'button', labelKey: 'menu.reportIndividualTab', permissionKey: 'report:individual:view', pattern: '/api/v1/reports/individual',          method: 'GET' },
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
          { tag: 'plan:view', type: 'button', labelKey: 'common.search', permissionKey: 'plan:view', pattern: '/api/v1/individual/plan-configs', method: 'GET' },
          { tag: 'plan:create', type: 'button', labelKey: 'common.add', permissionKey: 'plan:create', pattern: '/api/v1/individual/plan-configs', method: 'POST' },
          { tag: 'plan:update', type: 'button', labelKey: 'common.edit', permissionKey: 'plan:update', pattern: '/api/v1/individual/plan-configs/{id}/**', method: 'POST' },
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
      { tag: 'group:view',   type: 'button', labelKey: 'common.search', permissionKey: 'group:view',   pattern: '/api/v1/groups',      method: 'GET' },
      { tag: 'group:create', type: 'button', labelKey: 'common.add',    permissionKey: 'group:create', pattern: '/api/v1/groups',      method: 'POST' },
      { tag: 'group:update', type: 'button', labelKey: 'common.edit',   permissionKey: 'group:update', pattern: '/api/v1/groups/{id}', method: 'PUT' },
      { tag: 'group:assign:owner', type: 'button', labelKey: 'common.assignOwner', permissionKey: 'group:assign:owner', pattern: '/api/v1/groups/{id}/owner', method: 'PATCH' },
      { tag: 'group:assign:owner:users', type: 'api', labelKey: 'common.assignOwner', permissionKey: 'group:assign:owner', pattern: '/api/v1/admin/users', method: 'GET' },
    ],
  },
  systemMenu,
  {
    tag: 'approvals',
    leaf: false,
    type: 'menu',
    labelKey: 'menu.approvals',
    icon: Checked,
    permissionKey: 'approval:view',
    children: [
      {
        tag: 'approvals:list',
        leaf: true,
        path: '/approvals',
        type: 'menu',
        labelKey: 'menu.approvalList',
        icon: Checked,
        permissionKey: 'approval:view',
        children: [],
        permissions: [
          { tag: 'approval:view',      type: 'button', labelKey: 'common.search',   permissionKey: 'approval:view',      pattern: '/api/v1/approval-requests',             method: 'GET' },
          { tag: 'subscription:update:approval-submit',   type: 'api',    labelKey: 'common.confirm',  permissionKey: 'subscription:update', pattern: '/api/v1/approval-requests/*/submit',    method: 'POST' },
          { tag: 'subscription:update:approval-resubmit', type: 'api',    labelKey: 'common.confirm',  permissionKey: 'subscription:update', pattern: '/api/v1/approval-requests/*/resubmit',  method: 'POST' },
          { tag: 'approval:level1',    type: 'button', labelKey: 'menu.approveL1', permissionKey: 'approval:level1',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
          { tag: 'approval:level2',    type: 'button', labelKey: 'menu.approveL2', permissionKey: 'approval:level2',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
          { tag: 'approval:level3',    type: 'button', labelKey: 'menu.approveL3', permissionKey: 'approval:level3',    pattern: '/api/v1/approval-requests/*/approve',    method: 'POST' },
        ],
      },
      {
        tag: 'approvals:level-config',
        leaf: true,
        path: '/approval-level-config',
        type: 'menu',
        labelKey: 'menu.approvalLevelConfig',
        icon: Setting,
        permissionKey: 'approval:config',
        children: [],
        permissions: [
          { tag: 'approval:config',       type: 'button', labelKey: 'common.edit',   permissionKey: 'approval:config', pattern: '/api/v1/approval-requests/level-configs',    method: 'POST' },
          { tag: 'approval:config:read',  type: 'api',    labelKey: 'common.search',  permissionKey: 'approval:config', pattern: '/api/v1/approval-requests/level-configs',    method: 'GET' },
          { tag: 'approval:config:put',   type: 'api',    labelKey: 'common.edit',    permissionKey: 'approval:config', pattern: '/api/v1/approval-requests/level-configs/*',  method: 'PUT' },
          { tag: 'approval:config:del',   type: 'api',    labelKey: 'common.delete',  permissionKey: 'approval:config', pattern: '/api/v1/approval-requests/level-configs/*',  method: 'DELETE' },
        ],
      },
    ],
    permissions: [],
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
