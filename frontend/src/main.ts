import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { getToken, clearAuth, isTokenExpired } from '@/utils/auth'
import { useAuthStore } from '@/store'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

router.beforeEach((to, _from, next) => {
  setActivePinia(pinia)
  const token = getToken()
  if (to.path === '/login') {
    if (token && !isTokenExpired(token)) return next('/dashboard')
    clearAuth()
    return next()
  }

  if (!token || isTokenExpired(token)) {
    clearAuth()
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  const authStore = useAuthStore()
  const requiredPermission = to.meta.permission as string | undefined
  if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
    return next('/dashboard')
  }
  return next()
})

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(i18n)
app.mount('#app')
