<template>
  <nav class="sidebar-nav">
    <template v-for="item in menuItems" :key="item.tag">
      <el-tooltip
        v-if="item.leaf && appStore.sidebarCollapsed"
        :content="t(item.labelKey)"
        placement="right"
      >
        <router-link
          :to="item.path || '/'"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        </router-link>
      </el-tooltip>

      <router-link
        v-else-if="item.leaf"
        :to="item.path || '/'"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ t(item.labelKey) }}</span>
      </router-link>

      <template v-else>
        <el-tooltip
          v-if="appStore.sidebarCollapsed"
          :content="t(item.labelKey)"
          placement="right"
        >
          <button class="nav-item nav-button" :class="{ active: isGroupActive(item) }" @click="toggleGroup(item.tag)">
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          </button>
        </el-tooltip>

        <div v-else class="nav-group" :class="{ open: openGroups.has(item.tag), active: isGroupActive(item) }">
          <button class="nav-group-header" @click="toggleGroup(item.tag)">
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ t(item.labelKey) }}</span>
            <el-icon class="chevron"><ArrowRight /></el-icon>
          </button>
          <div v-show="openGroups.has(item.tag)" class="nav-group-children">
            <router-link
              v-for="child in item.children"
              :key="child.tag"
              :to="child.path || '/'"
              class="nav-item nav-child"
              :class="{ active: isActive(child.path) }"
            >
              <el-icon class="nav-icon"><component :is="child.icon" /></el-icon>
              <span class="nav-label">{{ t(child.labelKey) }}</span>
            </router-link>
          </div>
        </div>
      </template>
    </template>
  </nav>
  <div class="sidebar-footer">
    <div class="app-version" v-show="!appStore.sidebarCollapsed">
      {{ t('common.version') }} {{ appVersion }}
    </div>
    <el-tooltip
      v-if="appStore.sidebarCollapsed"
      :content="t('common.logout')"
      placement="right"
    >
      <el-button type="primary" class="nav-item nav-button logout-btn" @click="handleLogout">
        <el-icon class="nav-icon"><Right /></el-icon>
      </el-button>
    </el-tooltip>

    <el-button v-else type="primary" class="nav-item nav-button logout-btn" @click="handleLogout">
      <span class="nav-label" style="margin-right: 0.25rem;">{{ t('common.logout') }}</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from '@element-plus/icons-vue'
import { getNavData } from '@/common/nav'
import type { MenuItem } from '@/common/menutype'
import { useAppStore, useAuthStore } from '@/store'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const { t } = useI18n()

const appVersion = import.meta.env.VITE_APP_VERSION || 'v1.0.0'

const handleLogout = async () => {
  await ElMessageBox.confirm(t('header.logoutConfirm'), t('common.confirm'), { type: 'warning' })
  await authStore.doLogout()
  router.push('/login')
}

const openGroups = ref<Set<string>>(new Set())
const activeRoot = computed(() => '/' + route.path.split('/')[1])

const menuItems = computed(() => filterMenus(getNavData()))

function filterMenus(items: MenuItem[]): MenuItem[] {
  return items.flatMap((item) => {
    if (item.hidden) return []
    if (item.leaf) return item.isWhiteList || authStore.hasPermission(item.permissionKey) ? [item] : []

    const children = filterMenus(item.children ?? [])
    if (!children.length) return []
    return [{ ...item, children }]
  })
}

function isActive(path?: string) {
  return !!path && activeRoot.value === path
}

function isGroupActive(item: MenuItem) {
  return !!item.children?.some(child => isActive(child.path))
}

function toggleGroup(tag: string) {
  const next = new Set(openGroups.value)
  next.has(tag) ? next.delete(tag) : next.add(tag)
  openGroups.value = next
}

watch(
  [activeRoot, menuItems],
  () => {
    const next = new Set(openGroups.value)
    for (const item of menuItems.value) {
      if (!item.leaf && isGroupActive(item)) next.add(item.tag)
    }
    openGroups.value = next
  },
  { immediate: true }
)
</script>

<style scoped>
.sidebar-nav {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item,
.nav-group-header {
  min-height: 40px;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 20px;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: var(--el-text-color-primary);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 500;
  text-align: left;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  transition: background 0.15s ease, color 0.15s ease;
  box-sizing: border-box;
}

.nav-button {
  justify-content: center;
  padding: 9px 20px;
}

.nav-item:hover,
.nav-group-header:hover {
  background: #eef5ff;
  color: var(--el-color-primary);
}

.nav-item.active,
.nav-group.active > .nav-group-header {
  background: var(--el-color-primary);
  color: #fff;
}

.nav-icon {
  width: 18px;
  height: 18px;
  font-size: 18px;
  flex-shrink: 0;
  color: inherit;
}

.nav-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-group-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 0 2px;
}

.nav-child {
  min-height: 36px;
  padding-left: 48px;
  font-size: 13px;
}

.nav-child.active {
  background: #e7f0ff;
  color: var(--el-color-primary);
  font-weight: 600;
}

.chevron {
  width: 14px;
  height: 14px;
  font-size: 14px;
  transition: transform 0.2s ease;
}

.nav-group.open .chevron {
  transform: rotate(90deg);
}

/* Footer styles */
.sidebar-footer {
  flex-shrink: 0;
  border-top: 1px solid var(--el-border-color-light); 
  padding: 1rem 0.75rem;
  margin-top: auto; 
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.logout-btn {
  width: 100%;
  background-color: transparent;
  color: var(--el-text-color-regular);
}

.logout-btn:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-7);
}

.app-version {
  font-size: 14px;
  color: var(--el-text-color-regular);
  text-align: center;
}
</style>
