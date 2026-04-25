<template>
  <div>
    <!-- Page header -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ t("menu.accountManagement") }}</h2>
        <div class="page-subtitle">{{ t("menu.permissionMgmt") }}</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" :disabled="!can('user:create')" @click="openCreate"
          >+ {{ t("users.newUser") }}</el-button
        >
      </div>

      <!-- Pagination top -->
      <!-- <div class="pagination-bar">
        <div class="pagination-info">
          {{ t("users.show") }}
          <el-select
            v-model="size"
            style="width: 68px"
            size="small"
            @change="onSizeChange"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          {{ t("users.ofTotal", { total }) }}
        </div>
        <div class="pagination-btns">
          <button class="pg-btn" :disabled="page === 1" @click="goPage(1)">
            «
          </button>
          <button
            class="pg-btn"
            :disabled="page === 1"
            @click="goPage(page - 1)"
          >
            ‹
          </button>
          <button
            v-for="p in pageRange"
            :key="p"
            class="pg-btn"
            :class="{ active: p === page }"
            @click="goPage(p)"
          >
            {{ p }}
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(page + 1)"
          >
            ›
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(totalPages)"
          >
            »
          </button>
        </div>
      </div> -->

      <!-- Table -->
      <div class="table-wrap">
        <table class="users-table">
          <thead>
            <!-- Header row -->
            <tr class="header-row">
              <th class="th-num">#</th>
              <th class="th-sort" @click="setSort('username')">
                {{ t("users.username").toUpperCase() }}
                <SortIndicator
                  field="username"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('status')">
                TRẠNG THÁI
                <SortIndicator
                  field="status"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('role')">
                VAI TRÒ
                <SortIndicator
                  field="role"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('fullName')">
                TÊN NGƯỜI DÙNG
                <SortIndicator
                  field="fullName"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('email')">
                EMAIL
                <SortIndicator
                  field="fullName"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('createdAt')">
                THỜI GIAN TẠO
                <SortIndicator
                  field="createdAt"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-actions">HÀNH ĐỘNG</th>
            </tr>
            <!-- Filter row -->
            <tr class="filter-row">
              <td class="filter-refresh">
                <el-button
                  :icon="RefreshRight"
                  circle
                  size="small"
                  @click="resetFilters"
                />
              </td>
              <td class="filter-cell">
                <el-input
                  v-model="filterUsername"
                  size="small"
                  clearable
                  @input="onFilter"
                />
              </td>
              <td class="filter-cell">
                <el-select
                  v-model="filterStatus"
                  size="small"
                  clearable
                  @change="onFilter"
                  style="width: 100%"
                >
                  <el-option label="Hoạt động" value="ACTIVE" />
                  <el-option label="Đã thu hồi" value="REVOKED" />
                </el-select>
              </td>
              <td class="filter-cell">
                <el-select
                  v-model="filterRole"
                  size="small"
                  clearable
                  @change="onFilter"
                  style="width: 100%"
                >
                  <el-option
                    v-for="r in allRoles"
                    :key="r.roleId"
                    :label="r.displayName"
                    :value="r.displayName"
                  />
                </el-select>
              </td>
              <td />
              <td />
              <td />
              <td />
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, i) in pagedUsers" :key="user.id" class="data-row">
              <td class="td-num">{{ (page - 1) * size + i + 1 }}</td>
              <td>{{ user.username }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="user.status === 'ACTIVE' ? 'active' : 'revoked'"
                >
                  {{ user.status === "ACTIVE" ? "Hoạt động" : "Đã thu hồi" }}
                </span>
              </td>
              <td>{{ user.roleName }}</td>
              <td>{{ user.fullName }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.createdAt }}</td>
              <td class="td-actions">
                <template v-if="user.status === 'ACTIVE'">
                  <el-button
                    size="small"
                    :icon="EditPen"
                    :disabled="!can('user:update')"
                    @click="openEdit(user)"
                    >Chỉnh sửa</el-button
                  >
                  <el-button
                    size="small"
                    :icon="Key"
                    :disabled="!can('user:update')"
                    @click="openResetPwd(user)"
                    >Đặt lại mật khẩu</el-button
                  >
                  <el-button
                    size="small"
                    :icon="Delete"
                    type="warning"
                    plain
                    :disabled="!can('user:update')"
                    @click="openDelete(user)"
                    >Xoá tài khoản</el-button
                  >
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination bottom -->
      <div class="pagination-bar">
        <div class="pagination-info">
          {{ t("users.show") }}
          <el-select
            v-model="size"
            style="width: 68px"
            size="small"
            @change="onSizeChange"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          {{ t("users.ofTotal", { total }) }}
        </div>
        <div class="pagination-btns">
          <button class="pg-btn" :disabled="page === 1" @click="goPage(1)">
            «
          </button>
          <button
            class="pg-btn"
            :disabled="page === 1"
            @click="goPage(page - 1)"
          >
            ‹
          </button>
          <button
            v-for="p in pageRange"
            :key="p"
            class="pg-btn"
            :class="{ active: p === page }"
            @click="goPage(p)"
          >
            {{ p }}
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(page + 1)"
          >
            ›
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(totalPages)"
          >
            »
          </button>
        </div>
      </div>
    </el-card>

    <!-- Create User Dialog -->
    <el-dialog
      v-model="createVisible"
      width="480px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span class="dlg-title">THÊM MỚI TÀI KHOẢN</span>
      </template>
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="130px"
        label-position="left"
      >
        <el-form-item label="Tên đăng nhập:" prop="username">
          <el-input
            v-model="createForm.username"
            placeholder="Nhập tên đăng nhập"
          />
          <div class="field-hint">
            Nhập tên đăng nhập không trùng với tên đăng nhập đã có
          </div>
        </el-form-item>
        <el-form-item label="Tên người dùng:" prop="fullName">
          <el-input
            v-model="createForm.fullName"
            placeholder="Nhập tên người dùng"
          />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input
            v-model="createForm.email"
            placeholder="Nhập email nhận mã xác thực"
          />
        </el-form-item>
        <el-form-item label="Mật khẩu:" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            show-password
            placeholder="Nhập mật khẩu"
          />
        </el-form-item>
        <el-form-item label="Nhập lại MK:" prop="confirmPassword">
          <el-input
            v-model="createForm.confirmPassword"
            type="password"
            show-password
            placeholder="Nhập lại mật khẩu"
          />
        </el-form-item>
        <el-form-item label="Vai trò:" prop="roleName">
          <el-select
            v-model="createForm.roleName"
            placeholder="Chọn vai trò"
            style="width: 100%"
          >
            <el-option v-for="r in allRoles" :key="r.roleId" :label="r.displayName" :value="r.displayName" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleCreate"
            >Xác Nhận</el-button
          >
          <el-button @click="createVisible = false">Hủy Bỏ</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit User Dialog -->
    <el-dialog
      v-model="editVisible"
      width="480px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span class="dlg-title">CHỈNH SỬA THÔNG TIN TÀI KHOẢN</span>
      </template>
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="130px"
        label-position="left"
      >
        <el-form-item label="Tên đăng nhập:" prop="username">
          <el-input :value="editingUser?.username" disabled />
          <div class="field-hint">Không thể đổi tên đăng nhập</div>
        </el-form-item>
        <el-form-item label="Tên người dùng:" prop="fullName">
          <el-input v-model="editForm.fullName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="openDeleteFromEdit"
            >Xoá Tài Khoản</el-button
          >
          <div class="dlg-footer-right">
            <el-button type="primary" :loading="saving" @click="handleEdit"
              >Xác Nhận</el-button
            >
            <el-button @click="editVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- Delete Account Dialog -->
    <el-dialog
      v-model="deleteVisible"
      width="420px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span class="dlg-title">XOÁ TÀI KHOẢN</span>
      </template>
      <p class="delete-text">
        Bạn đang xóa tài khoản <strong>{{ deleteTarget?.username }}</strong> -
        <strong>{{ deleteTarget?.fullName }}</strong
        >. Hành động này chấm dứt mọi quyền truy cập và không thể hoàn tác. Nhấn
        "Xác Nhận" để xoá tài khoản.
      </p>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleDelete"
            >Xác Nhận</el-button
          >
          <el-button @click="deleteVisible = false">Hủy Bỏ</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Reset Password Dialog -->
    <el-dialog
      v-model="resetPwdVisible"
      width="420px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span class="dlg-title">ĐẶT LẠI MẬT KHẨU</span>
      </template>
      <el-form label-width="130px" label-position="left">
        <el-form-item label="Mật khẩu mới:">
          <el-input
            v-model="newPassword"
            type="password"
            show-password
            placeholder="Nhập mật khẩu mới"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleResetPwd"
            >Xác Nhận</el-button
          >
          <el-button @click="resetPwdVisible = false">Hủy Bỏ</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from "vue";
import {
  Plus,
  EditPen,
  Key,
  RefreshRight,
  DCaret,
  CaretTop,
  CaretBottom,
  Delete,
} from "@element-plus/icons-vue";
import { usePermission } from "@/composables/usePermission";

const { can } = usePermission();
import { ElMessage, ElIcon } from "element-plus";
import { useI18n } from "vue-i18n";
import {
  listUsers,
  createUser,
  updateUser,
  resetUserPassword,
  deleteUser,
} from "@/api/users";
import { listRoles } from "@/api/roles";
import type { FormInstance } from "element-plus";
import type { Role } from "@/types";

const { t } = useI18n();

// ── Sort indicator component ──
const SortIndicator = defineComponent({
  props: { field: String, current: String, dir: String },
  setup(props) {
    return () => {
      if (props.field !== props.current)
        return h(ElIcon, { class: "sort-icon" }, () => h(DCaret));
      return h(ElIcon, { class: "sort-icon active" }, () =>
        props.dir === "asc" ? h(CaretTop) : h(CaretBottom),
      );
    };
  },
});

// ── Data model ──
interface UserRow {
  id: string;
  username: string;
  fullName: string;
  email: string;
  status: "ACTIVE" | "REVOKED";
  roleName: string;
  createdAt: string;
}

const users = ref<UserRow[]>([]);
const allRoles = ref<Role[]>([]);

function formatDate(iso: string | null | undefined): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

// ── State ──
const loading = ref(false);
const saving = ref(false);
const page = ref(1);
const size = ref(10);

const filterUsername = ref("");
const filterStatus = ref("");
const filterRole = ref("");
const sortField = ref("");
const sortDir = ref<"asc" | "desc">("asc");

// ── Computed ──
const filteredUsers = computed(() => {
  let list = [...users.value];
  if (filterUsername.value)
    list = list.filter(
      (u) =>
        u.username.includes(filterUsername.value) ||
        u.fullName.includes(filterUsername.value),
    );
  if (filterStatus.value) {
    if (filterStatus.value === "ACTIVE")
      list = list.filter((u) => u.status === "ACTIVE");
    else
      list = list.filter((u) => u.status !== "ACTIVE");
  }
  if (filterRole.value)
    list = list.filter((u) => u.roleName === filterRole.value);
  if (sortField.value) {
    list.sort((a, b) => {
      const va = (a as any)[sortField.value] as string;
      const vb = (b as any)[sortField.value] as string;
      return sortDir.value === "asc"
        ? va.localeCompare(vb)
        : vb.localeCompare(va);
    });
  }
  return list;
});

const total = computed(() => filteredUsers.value.length);
const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / size.value)),
);
const pagedUsers = computed(() => {
  const start = (page.value - 1) * size.value;
  return filteredUsers.value.slice(start, start + size.value);
});
const pageRange = computed(() => {
  const tp = totalPages.value;
  const cur = page.value;
  const maxShow = 5;
  let start = Math.max(1, cur - Math.floor(maxShow / 2));
  let end = start + maxShow - 1;
  if (end > tp) {
    end = tp;
    start = Math.max(1, end - maxShow + 1);
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

function setSort(field: string) {
  if (sortField.value === field)
    sortDir.value = sortDir.value === "asc" ? "desc" : "asc";
  else {
    sortField.value = field;
    sortDir.value = "asc";
  }
}

function goPage(p: number) {
  page.value = Math.max(1, Math.min(p, totalPages.value));
}

function onSizeChange() {
  page.value = 1;
}
function onFilter() {
  page.value = 1;
}

function resetFilters() {
  filterUsername.value = "";
  filterStatus.value = "";
  filterRole.value = "";
  sortField.value = "";
  page.value = 1;
}

async function load() {
  loading.value = true;
  try {
    const [usersRes, rolesRes] = await Promise.all([
      listUsers({ page: 0, size: 500 }),
      listRoles(),
    ]);
    if (rolesRes.success && rolesRes.data) {
      allRoles.value = rolesRes.data;
    }
    if (usersRes.success && usersRes.data) {
      users.value = usersRes.data.content.map((u) => ({
        id: u.userId,
        username: u.username,
        fullName: u.fullName,
        email: u.email,
        status: u.status === "ACTIVE" ? "ACTIVE" : "REVOKED",
        roleName: u.roles?.[0]?.displayName ?? "-",
        createdAt: formatDate(u.createdAt),
      }));
    }
  } catch {
    ElMessage.error("Không thể tải danh sách người dùng");
  } finally {
    loading.value = false;
  }
}

// ── Create dialog ──
const createVisible = ref(false);
const createFormRef = ref<FormInstance>();
const createForm = reactive({
  username: "",
  fullName: "",
  email: "",
  password: "",
  confirmPassword: "",
  roleName: "",
});
const createRules = computed(() => ({
  username: [{ required: true, message: t("common.required") }],
  fullName: [{ required: true, message: t("common.required") }],
  email: [{ required: true, message: t("common.required") }],
  password: [{ required: true, message: t("common.required") }],
  confirmPassword: [
    { required: true, message: t("common.required") },
    {
      validator: (_: any, value: string, callback: Function) => {
        if (value !== createForm.password) callback(new Error("Mật khẩu nhập lại không khớp"));
        else callback();
      },
    },
  ],
  roleName: [{ required: true, message: t("common.required") }],
}));

function openCreate() {
  Object.assign(createForm, {
    username: "",
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    roleName: "",
  });
  createVisible.value = true;
}

async function handleCreate() {
  if (!(await createFormRef.value?.validate().catch(() => false))) return;
  saving.value = true;
  try {
    const roleMatch = allRoles.value.find((r) => r.displayName === createForm.roleName);
    await createUser({
      username: createForm.username,
      email: createForm.email,
      fullName: createForm.fullName,
      password: createForm.password,
      confirmPassword: createForm.confirmPassword,
      roleIds: roleMatch ? [roleMatch.roleId] : [],
    });
    ElMessage.success(t("users.createdMsg"));
    createVisible.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}

// ── Edit dialog ──
const editVisible = ref(false);
const editFormRef = ref<FormInstance>();
const editingUser = ref<UserRow | null>(null);
const editForm = reactive({ fullName: "", email: "", roleName: "" });
const editRules = computed(() => ({
  fullName: [{ required: true, message: t("common.required") }],
  email: [{ required: true, message: t("common.required") }],
  roleName: [{ required: true, message: t("common.required") }],
}));

function openEdit(user: UserRow) {
  editingUser.value = user;
  Object.assign(editForm, {
    fullName: user.fullName,
    email: user.email,
    roleName: user.roleName,
  });
  editVisible.value = true;
}

async function handleEdit() {
  if (!(await editFormRef.value?.validate().catch(() => false))) return;
  if (!editingUser.value) return;
  saving.value = true;
  try {
    await updateUser(editingUser.value.id, {
      fullName: editForm.fullName,
      email: editForm.email,
    });
    ElMessage.success(t("users.updatedMsg"));
    editVisible.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}

// ── Delete dialog ──
const deleteVisible = ref(false);
const deleteTarget = ref<UserRow | null>(null);

function openDelete(user: UserRow) {
  deleteTarget.value = user;
  deleteVisible.value = true;
}

function openDeleteFromEdit() {
  deleteTarget.value = editingUser.value;
  editVisible.value = false;
  deleteVisible.value = true;
}

async function handleDelete() {
  saving.value = true;
  try {
    await deleteUser(deleteTarget.value!.id);
    ElMessage.success(t("users.deleted"));
    deleteVisible.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}

// ── Reset password dialog ──
const resetPwdVisible = ref(false);
const resetTarget = ref<UserRow | null>(null);
const newPassword = ref("");

function openResetPwd(user: UserRow) {
  resetTarget.value = user;
  newPassword.value = "";
  resetPwdVisible.value = true;
}

async function handleResetPwd() {
  if (!newPassword.value) {
    ElMessage.warning(t("common.required"));
    return;
  }
  saving.value = true;
  try {
    await resetUserPassword(resetTarget.value!.id, newPassword.value);
    ElMessage.success(t("users.passwordReset"));
    resetPwdVisible.value = false;
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}
.page-title {
  margin: 0 0 2px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}
.page-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.toolbar {
  margin-bottom: 12px;
}

/* ── Pagination bar ── */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0;
  font-size: 13px;
  color: #606266;
}
.pagination-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pagination-btns {
  display: flex;
  gap: 4px;
}
.pg-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) {
  border-color: #1b60cb;
  color: #1b60cb;
}
.pg-btn.active {
  background: #1b60cb;
  border-color: #1b60cb;
  color: #fff;
  font-weight: 600;
}
.pg-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ── Table ── */
.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

/* Header */
.header-row th {
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 10px 12px;
  font-weight: 600;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  text-align: left;
}
.th-num {
  width: 48px;
  text-align: center;
}
.th-actions {
  width: 220px;
  text-align: center;
}
.th-sort {
  cursor: pointer;
  user-select: none;
}
.th-sort:hover {
  color: #1b60cb;
}

:deep(.sort-icon) {
  font-size: 12px;
  vertical-align: middle;
  margin-left: 2px;
  color: #c0c4cc;
}
:deep(.sort-icon.active) {
  color: #1b60cb;
}

/* Filter row */
.filter-row {
  background: #fafafa;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.filter-row td {
  padding: 6px 8px;
}
.filter-refresh {
  text-align: center;
}
.filter-cell {
  min-width: 100px;
}

/* Data rows */
.data-row td {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 9px 12px;
  color: #303133;
  font-size: 14px;
}
.data-row:hover td {
  background: #f5f7fa;
}
.td-num {
  text-align: center;
  color: #909399;
}
.td-actions {
  text-align: left;
  white-space: nowrap;
}
.td-actions .el-button + .el-button {
  margin-left: 6px;
}

/* Status badge */
.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-badge.active {
  background: #1b60cb;
  color: #fff;
}
.status-badge.revoked {
  background: #f0f0f0;
  color: #909399;
  border: 1px solid #d9d9d9;
}

/* ── Dialogs ── */
.dlg-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.02em;
}
.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
.dlg-footer-right {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}
.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.delete-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  margin: 0;
}
</style>
