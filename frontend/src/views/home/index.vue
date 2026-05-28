<template>
  <div class="home-page">
    <h2 class="page-title">{{ t('home.title') }}</h2>

    <!-- Banner -->
    <div class="banner">
      <img
        src="@/assets/images/trang_chu.png"
        :alt="t('home.title')"
        class="banner-img"
      />
    </div>

    <!-- User card -->
    <div class="user-card">
      <div class="user-avatar">
        <el-icon size="52" color="#b0c4de"><UserFilled /></el-icon>
      </div>
      <div class="user-name">
        {{ authStore.user?.fullName || t('home.defaultUserName') }}
      </div>
      <div class="user-actions">
        <el-button @click="editInfoVisible = true"
          >{{ t('home.editInfo') }}</el-button
        >
        <el-button @click="changePassVisible = true">{{ t('profile.changePassword') }}</el-button>
        <el-button type="danger" @click="handleLogout">{{ t('header.logout') }}</el-button>
      </div>
    </div>

    <!-- Dialog: Đổi Mật Khẩu -->
    <el-dialog
      v-model="changePassVisible"
      :title="t('profile.changePasswordTitle')"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="passForm"
        label-position="left"
        label-width="130px"
        size="default"
      >
        <el-form-item :label="t('profile.oldPasswordLabel')">
          <el-input
            v-model="passForm.oldPassword"
            :type="showOld ? 'text' : 'password'"
            :placeholder="t('profile.oldPasswordPlaceholder')"
          >
            <template #suffix>
              <el-icon class="eye-icon" @click="showOld = !showOld">
                <component :is="showOld ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="t('profile.newPasswordLabel')">
          <el-input
            v-model="passForm.newPassword"
            :type="showNew ? 'text' : 'password'"
            :placeholder="t('profile.newPasswordPlaceholder')"
          >
            <template #suffix>
              <el-icon class="eye-icon" @click="showNew = !showNew">
                <component :is="showNew ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="t('profile.confirmPasswordLabel')">
          <el-input
            v-model="passForm.confirmPassword"
            :type="showConfirm ? 'text' : 'password'"
            :placeholder="t('profile.confirmPasswordPlaceholder')"
          >
            <template #suffix>
              <el-icon class="eye-icon" @click="showConfirm = !showConfirm">
                <component :is="showConfirm ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmChangePass"
          >{{ t('common.confirm') }}</el-button
        >
        <el-button @click="changePassVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Chỉnh Sửa Thông Tin Tài Khoản -->
    <el-dialog
      v-model="editInfoVisible"
      :title="t('home.editAccountTitle')"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="infoForm"
        label-position="left"
        label-width="130px"
        size="default"
      >
        <el-form-item :label="t('users.username')">
          <div style="width: 100%">
            <el-input :value="authStore.user?.username" disabled />
            <div class="field-hint">{{ t('home.usernameReadonly') }}</div>
          </div>
        </el-form-item>
        <el-form-item :label="t('users.fullName')">
          <el-input v-model="infoForm.fullName" :placeholder="t('users.fullName')" />
        </el-form-item>
        <el-form-item :label="t('users.email')">
          <el-input v-model="infoForm.email" :placeholder="t('users.email')" />
        </el-form-item>
        <el-form-item :label="t('users.roles')">
          <el-select v-model="infoForm.role" style="width: 100%">
            <el-option :label="t('home.roleLevel', { level: 1 })" value="1" />
            <el-option :label="t('home.roleLevel', { level: 2 })" value="2" />
            <el-option :label="t('home.roleLevel', { level: 3 })" value="3" />
            <el-option :label="t('home.roleLevel', { level: 4 })" value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="handleDeleteAccount"
            >{{ t('home.deleteAccount') }}</el-button
          >
          <div>
            <el-button type="primary" @click="confirmEditInfo"
              >{{ t('common.confirm') }}</el-button
            >
            <el-button @click="editInfoVisible = false">{{ t('common.cancel') }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/store";
import { ElMessage } from "element-plus";
import { UserFilled, View, Hide } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n";
import { changePassword } from "@/api/users";

const router = useRouter();
const authStore = useAuthStore();
const { t } = useI18n();

const changePassVisible = ref(false);
const editInfoVisible = ref(false);

const showOld = ref(false);
const showNew = ref(false);
const showConfirm = ref(false);

const passForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const infoForm = reactive({
  fullName: authStore.user?.fullName || "",
  email: authStore.user?.email || "",
  role: "4",
});

async function handleLogout() {
  await authStore.doLogout();
  router.push("/login");
}

async function confirmChangePass() {
  if (
    !passForm.oldPassword ||
    !passForm.newPassword ||
    !passForm.confirmPassword
  ) {
    ElMessage.warning(t("home.fillAllFields"));
    return;
  }
  if (passForm.newPassword !== passForm.confirmPassword) {
    ElMessage.error(t("profile.passwordMismatch"));
    return;
  }
  const res = await changePassword({ currentPassword: passForm.oldPassword, newPassword: passForm.newPassword });
  if (!res.success) return;
  ElMessage.success(t("profile.passwordChanged"));
  changePassVisible.value = false;
  await authStore.doLogout();
  router.push("/login");
}

function confirmEditInfo() {
  ElMessage.success(t("home.updateInfoSuccess"));
  editInfoVisible.value = false;
}

function handleDeleteAccount() {
  ElMessage.warning(t("home.deleteNotSupported"));
}
</script>

<style scoped>
.home-page {
  padding-bottom: 32px;
}

.page-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

/* Banner */
.banner {
  border-radius: 12px;
  margin-bottom: 32px;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 360px;
  display: block;
  object-fit: cover;
  object-position: center;
}

/* User card */
.user-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  padding: 24px 32px;
}

.user-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #e8f0fd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-name {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* Dialogs */
.eye-icon {
  cursor: pointer;
  color: #909399;
}
.eye-icon:hover {
  color: #1b60cb;
}

.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
</style>
