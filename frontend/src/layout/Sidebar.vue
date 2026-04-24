<template>
  <nav class="sidebar-nav">
    <template v-for="(item, idx) in menuItems" :key="idx + (appStore.sidebarCollapsed ? '-c' : '-e')">
      <!-- Divider -->
      <div v-if="item.divider" class="nav-divider" />

      <!-- Group (expanded sidebar) -->
      <div
          v-else-if="item.group && !appStore.sidebarCollapsed"
          class="nav-group"
          :class="{ open: openGroups.has(item.group) }"
      >
        <div class="nav-group-header" @click="toggleGroup(item.group)">
          <!-- ✅ FIX: wrap with el-icon -->
          <el-icon class="header-icon">
            <component :is="item.icon" :key="item.group + '-icon'" />
          </el-icon>

          <span class="nav-label">{{ t(item.labelKey) }}</span>
          <el-icon class="chevron"><ArrowRight /></el-icon>
        </div>

        <div v-if="openGroups.has(item.group)" class="nav-group-children">
          <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.path"
              class="nav-item nav-child"
              :class="{ active: isActive(child.path) }"
          >
            <el-icon class="nav-icon">
              <component :is="child.icon" :key="child.path + '-icon'" />
            </el-icon>
            <span class="nav-label">{{ t(child.labelKey) }}</span>
          </router-link>
        </div>
      </div>

      <!-- Group (collapsed sidebar) -->
      <template v-else-if="item.group && appStore.sidebarCollapsed">
        <router-link
            v-for="child in item.children"
            :key="child.path"
            :to="child.path"
            class="nav-item"
            :class="{ active: isActive(child.path) }"
        >
          <el-icon class="nav-icon">
            <component :is="child.icon" :key="child.path + '-icon'" />
          </el-icon>
        </router-link>
      </template>

      <!-- Regular item -->
      <router-link
          v-else
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
      >
        <el-icon class="nav-icon">
          <component :is="item.icon" :key="(item.path || item.group) + '-icon'" />
        </el-icon>
        <span v-if="!appStore.sidebarCollapsed" class="nav-label">
          {{ t(item.labelKey) }}
        </span>
      </router-link>
    </template>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore, useAppStore } from '@/store'
import { useI18n } from 'vue-i18n'
import {
  Odometer, Ticket, DocumentChecked, UserFilled,
  User, Lock, TrendCharts, ArrowRight, Setting, Platform, List, Bell, Tools, Avatar, Key, House
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const { t } = useI18n()

const activeMenu = computed(() => '/' + route.path.split('/')[1])
function isActive(path: string) { return activeMenu.value === path }

// Groups that are expanded
const openGroups = ref<Set<string>>(new Set())

function toggleGroup(group: string) {
  const s = new Set(openGroups.value)
  s.has(group) ? s.delete(group) : s.add(group)
  openGroups.value = s
}

// Auto-open group if a child route is active
watch(activeMenu, (active) => {
  const s = new Set(openGroups.value)
  if (['/individual-plan-config', '/individual-usage-tracking'].includes(active)) s.add('individual')
  if (['/roles', '/users'].includes(active)) s.add('permissions')
  openGroups.value = s
}, { immediate: true })

const ALL_ITEMS = [
  { path: '/dashboard', icon: House,     labelKey: 'menu.home',      permission: 'dashboard:view' },
  { path: '/reports',   icon: Odometer,  labelKey: 'menu.dashboard', permission: 'report:view' },
  {
    group: 'individual',
    icon: Avatar,
    labelKey: 'menu.regularCustomer',
    children: [
      { path: '/individual-plan-config',     icon: Setting,     labelKey: 'menu.individualPlanConfig',    permission: 'plan:view' },
      { path: '/individual-usage-tracking',  icon: TrendCharts, labelKey: 'menu.individualUsageTracking', permission: 'subscription:view' },
    ],
  },
  { path: '/plans',      icon: UserFilled, labelKey: 'menu.agencyCustomer', permission: 'plan:view' },
  {
    group: 'permissions',
    icon: Lock,
    labelKey: 'menu.permissionMgmt',
    children: [
      { path: '/roles', icon: Lock, labelKey: 'menu.roleManagement',    permission: 'role:view' },
      { path: '/users', icon: User, labelKey: 'menu.accountManagement', permission: 'user:view' },
    ],
  },
  { path: '/audit-logs', icon: List, labelKey: 'menu.logsManagement', permission: 'audit-log:view' },
]

const menuItems = computed<Record<string, any>[]>(() => {
  return (ALL_ITEMS as Record<string, any>[]).flatMap(item => {
    if (item.children) {
      const visibleChildren = item.children.filter((c: any) => authStore.hasPermission(c.permission))
      if (visibleChildren.length === 0) return []
      return [{ ...item, children: visibleChildren }]
    }
    if (item.permission && !authStore.hasPermission(item.permission)) return []
    return [item]
  })
})
</script>

<style scoped>
.sidebar-nav {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  text-decoration: none;
  color: rgba(47, 43, 61, 0.9);
  font-size: 14px;
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover {
  background: rgba(27, 96, 203, 0.08);
  color: #1B60CB;
}
.nav-item.active {
  background: linear-gradient(to right, #68B4EC, #1557B9);
  color: #fff;
}
.nav-item.active .nav-icon { color: #fff; }

.nav-child {
  padding-left: 40px;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
  color: inherit;
}
.nav-label { flex: 1; }

/* Group parent */
.nav-group {
  border-radius: 6px;
}
.nav-group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: rgba(47, 43, 61, 0.9);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  transition: background 0.15s;
}

/* Group header icon */
.header-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.nav-group-header:hover { background: rgba(27, 96, 203, 0.08); }
.nav-group-header:hover,
.nav-group.open > .nav-group-header { color: #1B60CB; }
.nav-group-header:hover .header-icon,
.nav-group.open > .nav-group-header .header-icon { color: #1B60CB; }

.chevron {
  font-size: 12px;
  transition: transform 0.2s;
  color: inherit;
}
.nav-group.open .chevron { transform: rotate(90deg); }

.nav-group-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 0;
}

.nav-divider {
  height: 1px;
  background: rgba(47, 43, 61, 0.1);
  margin: 6px 4px;
}
</style>
