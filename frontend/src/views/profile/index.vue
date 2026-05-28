<template>
  <div>
    <div class="page-header"><h2>{{ t('header.myProfile') }}</h2></div>

    <el-row :gutter="24">
      <el-col :span="12">
        <el-card shadow="never" :header="t('profile.info')">
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="t('users.username')">{{ profile?.username }}</el-descriptions-item>
            <el-descriptions-item :label="t('users.fullName')">{{ profile?.fullName }}</el-descriptions-item>
            <el-descriptions-item :label="t('users.email')">{{ profile?.email }}</el-descriptions-item>
            <el-descriptions-item :label="t('users.roles')">
              <el-tag v-for="r in profile?.roles" :key="r.roleId" size="small" style="margin-right:4px">{{ r.displayName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="t('users.lastLogin')">{{ profile?.lastLoginAt?.slice(0,16) || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" :header="t('profile.changePassword')">
          <el-form ref="pwFormRef" :model="pwForm" :rules="pwRules" label-width="170" label-position="left">
            <el-form-item :label="t('profile.currentPassword')" prop="currentPassword">
              <el-input v-model="pwForm.currentPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.newPassword')" prop="newPassword">
              <el-input v-model="pwForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
              <el-input v-model="pwForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleChangePassword">{{ t('profile.changePassword') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getMyProfile, changePassword } from '@/api/users'
import type { UserAccount } from '@/types'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()
const profile = ref<UserAccount | null>(null)
const saving = ref(false)
const pwFormRef = ref<FormInstance>()
const pwForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const pwRules = computed(() => ({
  currentPassword: [{ required: true, message: t('common.required') }],
  newPassword: [
    { required: true, message: t('common.required') },
    { min: 8, message: t('users.minPassword') },
  ],
  confirmPassword: [
    { required: true, message: t('common.required') },
    {
      validator: (_: unknown, value: string, cb: (e?: Error) => void) => {
        value !== pwForm.newPassword ? cb(new Error(t('profile.passwordMismatch'))) : cb()
      },
    },
  ],
}))

async function handleChangePassword() {
  if (!await pwFormRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    await changePassword({ currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword })
    ElMessage.success(t('profile.passwordChanged'))
    Object.assign(pwForm, { currentPassword: '', newPassword: '', confirmPassword: '' })
    pwFormRef.value?.resetFields()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const res = await getMyProfile()
  if (res.success) profile.value = res.data!
})
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
