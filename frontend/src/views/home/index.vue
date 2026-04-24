<template>
  <div class="home-page">
    <h2 class="page-title">TRANG CHỦ</h2>

    <!-- Banner -->
    <div class="banner">
      <img src="@/assets/images/trang_chu.png" alt="Trang chủ" class="banner-img" />
    </div>

    <!-- User card -->
    <div class="user-card">
      <div class="user-avatar">
        <el-icon size="52" color="#b0c4de"><UserFilled /></el-icon>
      </div>
      <div class="user-name">{{ authStore.user?.fullName || '[Tên người dùng]' }}</div>
      <div class="user-actions">
        <el-button @click="editInfoVisible = true">Chỉnh Sửa Thông Tin</el-button>
        <el-button @click="changePassVisible = true">Đổi Mật Khẩu</el-button>
        <el-button type="danger" @click="handleLogout">Đăng Xuất</el-button>
      </div>
    </div>

    <!-- Dialog: Đổi Mật Khẩu -->
    <el-dialog v-model="changePassVisible" title="ĐỔI MẬT KHẨU" width="420px" :close-on-click-modal="false">
      <el-form :model="passForm" label-position="left" label-width="130px" size="default">
        <el-form-item label="Mật khẩu cũ:">
          <el-input
            v-model="passForm.oldPassword"
            :type="showOld ? 'text' : 'password'"
            placeholder="Nhập mật khẩu cũ"
          >
            <template #suffix>
              <el-icon class="eye-icon" @click="showOld = !showOld">
                <component :is="showOld ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="Mật khẩu mới:">
          <el-input
            v-model="passForm.newPassword"
            :type="showNew ? 'text' : 'password'"
            placeholder="Nhập mật khẩu mới"
          >
            <template #suffix>
              <el-icon class="eye-icon" @click="showNew = !showNew">
                <component :is="showNew ? View : Hide" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="Nhập lại mật khẩu:">
          <el-input
            v-model="passForm.confirmPassword"
            :type="showConfirm ? 'text' : 'password'"
            placeholder="Nhập lại mật khẩu mới"
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
        <el-button type="primary" @click="confirmChangePass">Xác Nhận</el-button>
        <el-button @click="changePassVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Chỉnh Sửa Thông Tin Tài Khoản -->
    <el-dialog v-model="editInfoVisible" title="CHỈNH SỬA THÔNG TIN TÀI KHOẢN" width="460px" :close-on-click-modal="false">
      <el-form :model="infoForm" label-position="left" label-width="130px" size="default">
        <el-form-item label="Tên đăng nhập:">
          <div style="width: 100%">
            <el-input :value="authStore.user?.username" disabled />
            <div class="field-hint">Không thể đổi tên đăng nhập</div>
          </div>
        </el-form-item>
        <el-form-item label="Tên người dùng:">
          <el-input v-model="infoForm.fullName" placeholder="Tên người dùng" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="infoForm.email" placeholder="Email" />
        </el-form-item>
        <el-form-item label="Vai trò:">
          <el-select v-model="infoForm.role" style="width: 100%">
            <el-option label="Vai trò cấp 1" value="1" />
            <el-option label="Vai trò cấp 2" value="2" />
            <el-option label="Vai trò cấp 3" value="3" />
            <el-option label="Vai trò cấp 4" value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="handleDeleteAccount">Xóa Tài Khoản</el-button>
          <div>
            <el-button type="primary" @click="confirmEditInfo">Xác Nhận</el-button>
            <el-button @click="editInfoVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { ElMessage } from 'element-plus'
import { UserFilled, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const changePassVisible = ref(false)
const editInfoVisible = ref(false)

const showOld = ref(false)
const showNew = ref(false)
const showConfirm = ref(false)

const passForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const infoForm = reactive({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  role: '4',
})

async function handleLogout() {
  await authStore.doLogout()
  router.push('/login')
}

function confirmChangePass() {
  if (!passForm.oldPassword || !passForm.newPassword || !passForm.confirmPassword) {
    ElMessage.warning('Vui lòng nhập đầy đủ thông tin')
    return
  }
  if (passForm.newPassword !== passForm.confirmPassword) {
    ElMessage.error('Mật khẩu mới không khớp')
    return
  }
  ElMessage.success('Đổi mật khẩu thành công')
  changePassVisible.value = false
  passForm.oldPassword = ''
  passForm.newPassword = ''
  passForm.confirmPassword = ''
}

function confirmEditInfo() {
  ElMessage.success('Cập nhật thông tin thành công')
  editInfoVisible.value = false
}

function handleDeleteAccount() {
  ElMessage.warning('Chức năng xóa tài khoản chưa được hỗ trợ')
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
  height: 180px;
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
.eye-icon:hover { color: #1B60CB; }

.field-hint {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
</style>
