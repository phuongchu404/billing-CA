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
      <el-table
        class="users-el-table"
        :data="pagedUsers"
        border
        @sort-change="handleSortChange"
      >
        <el-table-column
          width="55"
          type="index"
          :index="(i: number) => (page - 1) * size + i + 1"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button
                :icon="RefreshRight"
                link
                @click.stop="resetFilters"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="username" sortable="custom" min-width="150">
          <template #header>
            <div class="col-label">{{ t("users.username").toUpperCase() }}</div>
            <div class="col-filter" @click.stop>
              <el-input
                v-model="filterUsername"
                size="small"
                clearable
                @input="onFilter"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable="custom" width="145">
          <template #header>
            <div class="col-label">{{ t('common.status').toUpperCase() }}</div>
            <div class="col-filter" @click.stop>
              <el-select
                v-model="filterStatus"
                size="small"
                clearable
                @change="onFilter"
                style="width: 100%"
              >
                <el-option :label="t('users.statusActive')" value="ACTIVE" />
                <el-option :label="t('users.statusRevoked')" value="REVOKED" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <span
              class="status-badge"
              :class="row.status === 'ACTIVE' ? 'active' : 'revoked'"
            >
              {{ row.status === "ACTIVE" ? t('users.statusActive') : t('users.statusRevoked') }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="roleName" sortable="custom" min-width="150">
          <template #header>
            <div class="col-label">{{ t('users.roles').toUpperCase() }}</div>
            <div class="col-filter" @click.stop>
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
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fullName" sortable="custom" min-width="170">
          <template #header>
            <div class="col-label">{{ t('users.fullName').toUpperCase() }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="managerName" sortable="custom" min-width="160">
          <template #header>
            <div class="col-label">{{ t('users.managerLabel').toUpperCase() }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <span v-if="row.managerName">{{ row.managerName }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="email" sortable="custom" min-width="190">
          <template #header>
            <div class="col-label">EMAIL</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" sortable="custom" width="175">
          <template #header>
            <div class="col-label">{{ t('users.createdAtLabel').toUpperCase() }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column fixed="right" width="320">
          <template #header>
            <div class="col-label">{{ t('common.actions').toUpperCase() }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="td-actions">
              <template v-if="row.status === 'ACTIVE'">
                <el-button
                  size="small"
                  :icon="EditPen"
                  :disabled="!can('user:update')"
                  @click="openEdit(row)"
                  >{{ t('users.editAction') }}</el-button
                >
                <el-button
                  size="small"
                  :icon="Connection"
                  :disabled="!can('user:update')"
                  @click="openAssignManager(row)"
                  >{{ t('users.assignManagerAction') }}</el-button
                >
                <el-button
                  size="small"
                  :icon="Key"
                  :disabled="!can('user:update')"
                  @click="openResetPwd(row)"
                  >{{ t('users.resetPassword') }}</el-button
                >
                <el-button
                  size="small"
                  :icon="Delete"
                  type="warning"
                  plain
                  :disabled="!can('user:update')"
                  @click="openDelete(row)"
                  >{{ t('users.deleteAccountAction') }}</el-button
                >
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
        <span class="dlg-title">{{ t('users.createTitle') }}</span>
      </template>
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="130px"
        label-position="left"
      >
        <el-form-item :label="t('users.username')" prop="username">
          <el-input
            v-model="createForm.username"
            :placeholder="t('users.usernamePlaceholder')"
          />
          <div class="field-hint">{{ t('users.usernameHint') }}</div>
        </el-form-item>
        <el-form-item :label="t('users.fullName')" prop="fullName">
          <el-input
            v-model="createForm.fullName"
            :placeholder="t('users.fullNamePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('users.email')" prop="email">
          <el-input
            v-model="createForm.email"
            :placeholder="t('users.emailPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('users.password')" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            show-password
            :placeholder="t('users.passwordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('users.confirmPasswordLabel')" prop="confirmPassword">
          <el-input
            v-model="createForm.confirmPassword"
            type="password"
            show-password
            :placeholder="t('users.confirmPasswordPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('users.roles')" prop="roleId">
          <el-select
            v-model="createForm.roleId"
            :placeholder="t('users.roleSelectPlaceholder')"
            style="width: 100%"
          >
            <el-option v-for="r in allRoles" :key="r.roleId" :label="r.displayName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('users.managerSelectLabel')">
          <el-select
            v-model="createForm.managerUserId"
            :placeholder="t('users.managerSelectPlaceholder')"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="u in users"
              :key="u.id"
              :label="`${u.fullName} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleCreate">{{ t('common.confirm') }}</el-button>
          <el-button @click="createVisible = false">{{ t('common.cancel') }}</el-button>
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
        <span class="dlg-title">{{ t('users.editTitle') }}</span>
      </template>
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="130px"
        label-position="left"
      >
        <el-form-item :label="t('users.username') + ':'" prop="username">
          <el-input :value="editingUser?.username" disabled />
          <div class="field-hint">{{ t('users.cannotChangeUsername') }}</div>
        </el-form-item>
        <el-form-item :label="t('users.fullName') + ':'" prop="fullName">
          <el-input v-model="editForm.fullName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="openDeleteFromEdit"
            >{{ t('users.deleteAccountAction') }}</el-button
          >
          <div class="dlg-footer-right">
            <el-button type="primary" :loading="saving" @click="handleEdit"
              >{{ t('common.confirm') }}</el-button
            >
            <el-button @click="editVisible = false">{{ t('common.cancel') }}</el-button>
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
        <span class="dlg-title">{{ t('users.deleteTitle') }}</span>
      </template>
      <p class="delete-text">
        {{ t('users.deleteConfirmText', { username: deleteTarget?.username, fullName: deleteTarget?.fullName }) }}
      </p>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleDelete"
            >{{ t('common.confirm') }}</el-button
          >
          <el-button @click="deleteVisible = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Assign Manager Dialog -->
    <el-dialog v-model="assignManagerVisible" width="420px" :close-on-click-modal="false">
      <template #header>
        <span class="dlg-title">{{ t('users.assignManagerTitle') }}</span>
      </template>
      <el-form label-width="130px" label-position="left">
        <el-form-item :label="t('users.username') + ':'">
          <span>{{ assignManagerTarget?.fullName }} ({{ assignManagerTarget?.username }})</span>
        </el-form-item>
        <el-form-item :label="t('users.managerSelectLabel') + ':'">
          <el-select v-model="assignManagerId" clearable :placeholder="t('users.managerSelectClearPlaceholder')" style="width:100%">
            <el-option
              v-for="u in users.filter(u => u.id !== assignManagerTarget?.id)"
              :key="u.id"
              :label="`${u.fullName} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleAssignManager">{{ t('common.confirm') }}</el-button>
          <el-button @click="assignManagerVisible = false">{{ t('common.cancel') }}</el-button>
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
        <span class="dlg-title">{{ t('users.resetPwdTitle') }}</span>
      </template>
      <el-form label-width="130px" label-position="left">
        <el-form-item :label="t('users.newPassword') + ':'">
          <el-input
            v-model="newPassword"
            type="password"
            show-password
            :placeholder="t('users.newPwdPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleResetPwd"
            >{{ t('common.confirm') }}</el-button
          >
          <el-button @click="resetPwdVisible = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import {
  Plus,
  EditPen,
  Key,
  RefreshRight,
  Delete,
  Connection,
} from "@element-plus/icons-vue";
import { usePermission } from "@/composables/usePermission";

const { can } = usePermission();
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import {
  listUsers,
  createUser,
  updateUser,
  resetUserPassword,
  deleteUser,
  assignManager,
} from "@/api/users";
import { listRoles } from "@/api/roles";
import type { FormInstance } from "element-plus";
import type { Role } from "@/types";

const { t } = useI18n();

// ── Data model ──
interface UserRow {
  id: number;
  username: string;
  fullName: string;
  email: string;
  status: "ACTIVE" | "REVOKED";
  roleName: string;
  managerName: string;
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

function handleSortChange({
  prop,
  order,
}: {
  prop: string;
  order: "ascending" | "descending" | null;
}) {
  sortField.value = order ? prop : "";
  sortDir.value = order === "descending" ? "desc" : "asc";
  page.value = 1;
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
        managerName: u.managerName ?? "",
        createdAt: formatDate(u.createdAt),
      }));
    }
  } catch {
    ElMessage.error(t("users.loadError"));
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
  roleId: null as number | null,
  managerUserId: null as number | null,
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
        if (value !== createForm.password) callback(new Error(t("users.passwordMismatch")));
        else callback();
      },
    },
  ],
  roleId: [{ required: true, message: t("common.required") }],
}));

function openCreate() {
  Object.assign(createForm, {
    username: "",
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    roleId: null,
    managerUserId: null,
  });
  createVisible.value = true;
}

async function handleCreate() {
  if (!(await createFormRef.value?.validate().catch(() => false))) return;
  saving.value = true;
  try {
    await createUser({
      username: createForm.username,
      email: createForm.email,
      fullName: createForm.fullName,
      password: createForm.password,
      confirmPassword: createForm.confirmPassword,
      roleIds: createForm.roleId ? [createForm.roleId] : [],
      managerUserId: createForm.managerUserId,
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

// ── Assign Manager dialog ──
const assignManagerVisible = ref(false);
const assignManagerTarget = ref<UserRow | null>(null);
const assignManagerId = ref<number | null>(null);

function openAssignManager(user: UserRow) {
  assignManagerTarget.value = user;
  assignManagerId.value = null;
  assignManagerVisible.value = true;
}

async function handleAssignManager() {
  if (!assignManagerTarget.value) return;
  saving.value = true;
  try {
    await assignManager(assignManagerTarget.value.id, assignManagerId.value || null);
    ElMessage.success(t("users.managerUpdated"));
    assignManagerVisible.value = false;
    await load();
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

.col-label {
  color: #606266;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.35;
  text-transform: uppercase;
}
.col-filter {
  min-height: 32px;
}
:deep(.users-el-table th.el-table__cell) {
  padding: 8px 0;
  vertical-align: top;
}
:deep(.users-el-table td.el-table__cell) {
  padding: 9px 0;
}
.td-actions {
  display: flex;
  gap: 6px;
  text-align: left;
  white-space: nowrap;
}
.td-actions :deep(.el-button) {
  margin: 0;
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




