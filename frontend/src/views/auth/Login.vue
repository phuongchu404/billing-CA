<template>
  <div class="login-page">
    <!-- Left panel: form -->
    <div class="login-panel">
      <div class="login-card">
        <div class="login-header">
          <div class="brand-icon">
            <el-icon size="44" color="#2d5be3"><Monitor /></el-icon>
          </div>
          <h1>RS Platform</h1>
          <p>{{ t('auth.subscriptionManagement') }}</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" :placeholder="t('auth.username')" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              :placeholder="t('auth.password')"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" style="margin-bottom: 16px;" />

          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            {{ t('auth.signIn') }}
          </el-button>
        </el-form>

        <div class="login-footer">
          <span>© 2025 MK Group. All rights reserved.</span>
        </div>
      </div>
    </div>

    <!-- Right panel: branding -->
    <div class="login-banner" :style="{ backgroundImage: `url(${bannerImg})` }"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Monitor } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import bannerImg from '@/assets/images/trang_chu.png'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({ username: '', password: '' })
const rules = computed(() => ({
  username: [{ required: true, message: t('auth.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordRequired'), trigger: 'blur' }],
}))

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''
  try {
    const res = await authStore.doLogin(form)
    if (res.success) {
      router.push('/dashboard')
    } else {
      errorMsg.value = res.message || t('auth.loginFailed')
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || t('auth.loginFailedRetry')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* ── Right banner ── */
.login-banner {
  flex: 1;
  background-color: #eaf1fb;
  background-size: contain;
  background-position: center center;
  background-repeat: no-repeat;
}

/* ── Right panel ── */
.login-panel {
  width: 460px;
  min-width: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
  padding: 48px 40px;
  overflow-y: auto;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  margin-bottom: 16px;
}

.login-header h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #1d2b45;
}

.login-header p {
  margin: 0;
  color: #888;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
  background: linear-gradient(90deg, #2d5be3 0%, #4f7ef8 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.login-btn:hover {
  background: linear-gradient(90deg, #1d4bd0 0%, #3d6de0 100%);
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  color: #bbb;
  font-size: 12px;
}

@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
    overflow: auto;
  }

  .login-banner {
    height: 240px;
    flex: none;
    padding: 24px;
  }

  .login-panel {
    width: 100%;
    min-width: unset;
    box-shadow: none;
    padding: 32px 24px;
  }
}
</style>
