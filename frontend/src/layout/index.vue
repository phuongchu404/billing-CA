<template>
  <el-container class="layout-container">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '260px'" class="aside">
      <div class="logo">
        <img
          :src="appStore.sidebarCollapsed ? logoCollapsed : logoFull"
          class="logo-img"
          :class="{ collapsed: appStore.sidebarCollapsed }"
          alt="MK Group"
        />
      </div>
      <Sidebar />
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <Header />
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import logoFull from '@/assets/images/logo.jpg'
import logoCollapsed from '@/assets/images/logo-collapsed.png'

const appStore = useAppStore()
</script>

<style scoped>
.layout-container { height: 100vh; }
.aside {
  background: #fff;
  border-right: 1px solid #E1E7F1;
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(47, 43, 61, 0.06);
}
.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 16px;
  border-bottom: 1px solid #E1E7F1;
  height: 60px;
}
.logo-img {
  height: 34px;
  width: auto;
  max-width: 100%;
  object-fit: contain;
}
.logo-img.collapsed {
  height: 32px;
}
.header {
  height: 60px;
  padding: 0;
  background: #fff;
  border-bottom: 1px solid #E1E7F1;
}
.main-container { overflow: hidden; }
.main-content {
  background: #F0F2F7;
  overflow-y: auto;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
