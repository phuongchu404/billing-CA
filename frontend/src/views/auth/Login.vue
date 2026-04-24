<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="brand-icon">
          <el-icon size="40" color="#2d5be3"><Monitor /></el-icon>
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

        <el-button type="primary" size="large" :loading="loading" style="width: 100%;" @click="handleLogin">
          {{ t('auth.signIn') }}
        </el-button>
      </el-form>

      <div class="login-footer">
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'

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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d2b45 0%, #2d5be3 100%);
}
.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.brand-icon { margin-bottom: 12px; }
.login-header h1 { margin: 0 0 4px; font-size: 24px; color: #1d2b45; }
.login-header p { margin: 0; color: #666; font-size: 14px; }
.login-footer {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>
