<template>
  <div class="header-wrap">
    <div class="left">
      <el-button text @click="appStore.toggleSidebar()">
        <el-icon size="20"><Fold v-if="!appStore.sidebarCollapsed" /><Expand v-else /></el-icon>
      </el-button>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">{{ t('header.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.name">{{ routeTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right">
      <el-button-group class="lang-switch">
        <el-button size="small" :type="locale === 'en' ? 'primary' : ''" @click="setLocale('en')">EN</el-button>
        <el-button size="small" :type="locale === 'vi' ? 'primary' : ''" @click="setLocale('vi')">VI</el-button>
      </el-button-group>
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar size="small" :style="{ background: '#2d5be3' }">
            {{ (authStore.user?.username || 'U').charAt(0).toUpperCase() }}
          </el-avatar>
          <span class="username">{{ authStore.user?.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon> {{ t('header.myProfile') }}
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon> {{ t('header.logout') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore, useAppStore } from '@/store'
import { ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const { t, locale } = useI18n()

const routeKeyMap: Record<string, string> = {
  Dashboard: 'menu.dashboard',
  Plans: 'menu.plans',
  Subscriptions: 'menu.subscriptions',
  Partners: 'menu.partners',
  PartnerDetail: 'partners.detail',
  Users: 'menu.users',
  Roles: 'menu.roles',
  Certificates: 'menu.certificates',
  Reports: 'menu.reports',
  Profile: 'header.myProfile',
}
const routeTitle = computed(() => {
  const key = routeKeyMap[route.name as string]
  return key ? t(key) : (route.meta.title as string || '')
})

function setLocale(lang: string) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

async function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm(t('header.logoutConfirm'), t('common.confirm'), { type: 'warning' })
    await authStore.doLogout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.header-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 16px;
}
.left, .right { display: flex; align-items: center; gap: 12px; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
}
.user-info:hover { background: #f5f7fa; }
.username { font-size: 14px; }
.lang-switch { display: flex; }
</style>
