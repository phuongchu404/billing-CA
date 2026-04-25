<template>
  <div>
    <!-- Page header -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ editMode ? t('roles.editTitle') : t('roles.title') }}</h2>
        <div class="page-subtitle">{{ editMode ? t('roles.title') : t('roles.matrixSubtitle') }}</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <!-- Normal mode toolbar -->
      <div v-if="!editMode" class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">{{ t('roles.newRole') }}</el-button>
        <el-button :icon="EditPen" @click="handleEdit">{{ t('roles.editRole') }}</el-button>
        <el-button
          :icon="Delete"
          type="danger"
          plain
          :loading="saving"
          :disabled="!selectedRoleId || !!selectedRole?.isSystemRole"
          @click="handleDeleteRole"
        >Xoá vai trò</el-button>
      </div>

      <!-- Edit mode top actions -->
      <div v-else class="edit-actions">
        <el-button type="primary" :loading="saving" @click="confirmEdit">{{ t('common.confirm') }}</el-button>
        <el-button @click="cancelEdit">{{ t('roles.cancelEdit') }}</el-button>
      </div>

      <!-- Matrix table -->
      <div class="matrix-wrap">
        <table class="matrix-table">
          <colgroup>
            <col class="col-perm" />
            <col v-for="role in matrixRoles" :key="'col-' + role.roleId" class="col-role" />
          </colgroup>
          <thead>
            <tr class="header-row">
              <th class="th-perm">{{ t('roles.permission') }}</th>
              <th
                v-for="role in matrixRoles"
                :key="'h-' + role.roleId"
                class="th-role"
                :class="{
                  selected: selectedRoleId === role.roleId,
                  editable: editMode && selectedRoleId === role.roleId
                }"
                @click="!editMode && selectRole(role)"
              >
                <el-icon v-if="editMode && selectedRoleId === role.roleId" class="edit-col-icon"><EditPen /></el-icon>
                {{ role.displayName }}
              </th>
            </tr>
            <tr v-if="!editMode" class="count-row">
              <td class="count-label">{{ t('roles.activeAccounts') }}</td>
              <td
                v-for="role in matrixRoles"
                :key="'cnt-' + role.roleId"
                class="count-val"
                :class="{ selected: selectedRoleId === role.roleId }"
              >
                {{ roleUserCounts[role.roleId] ?? 0 }}
              </td>
            </tr>
          </thead>
          <tbody>
            <template v-for="mod in moduleGroups" :key="mod.moduleName">
              <tr class="module-row" @click="toggleModule(mod.moduleName)">
                <td :colspan="matrixRoles.length + 1">
                  <span class="module-arrow" :class="{ expanded: expandedModules.has(mod.moduleName) }">▼</span>
                  {{ t('permissions.module.' + mod.moduleName) }}
                </td>
              </tr>

              <template v-if="expandedModules.has(mod.moduleName)">
                <template v-for="group in mod.permissionGroups" :key="group.groupName">
                  <tr class="group-row">
                    <td :colspan="matrixRoles.length + 1" class="group-name-cell">
                      {{ t('permissions.group.' + group.groupName) }}
                    </td>
                  </tr>

                  <tr v-for="perm in group.permissions" :key="perm.permissionId" class="perm-row">
                    <td class="perm-name-cell">{{ perm.displayName }}</td>
                    <td
                      v-for="role in matrixRoles"
                      :key="'c-' + role.roleId + '-' + perm.permissionId"
                      class="check-cell"
                      :class="{
                        selected: selectedRoleId === role.roleId,
                        'edit-col': editMode && selectedRoleId === role.roleId
                      }"
                    >
                      <el-checkbox
                        :model-value="hasPerm(role.roleId, perm.permissionId)"
                        :disabled="!editMode || selectedRoleId !== role.roleId"
                        @change="(val: any) => toggleEditPerm(perm.permissionId, !!val)"
                      />
                    </td>
                  </tr>
                </template>
              </template>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Edit mode bottom actions -->
      <div v-if="editMode" class="edit-actions edit-actions-bottom">
        <el-button type="primary" :loading="saving" @click="confirmEdit">{{ t('common.confirm') }}</el-button>
        <el-button @click="cancelEdit">{{ t('roles.cancelEdit') }}</el-button>
      </div>

      <!-- Footer -->
      <div v-if="lastUpdated && !editMode" class="matrix-footer">
        {{ t('roles.lastUpdated') }}: {{ lastUpdated }}
      </div>
    </el-card>

    <!-- Create Role Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="t('roles.newRole')"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('roles.roleName')" prop="roleName">
          <el-input v-model="form.roleName" placeholder="ROLE_MY_ROLE" />
        </el-form-item>
        <el-form-item :label="t('roles.displayName')" prop="displayName">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item :label="t('roles.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, EditPen, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { createRole, deleteRole, getPermissionMatrix, assignPermissions } from '@/api/roles'
import type { Role, PermissionModule } from '@/types'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()
const loading = ref(false)
const saving  = ref(false)

const matrixRoles    = ref<Role[]>([])
const moduleGroups   = ref<PermissionModule[]>([])
const rolePerms      = ref<Record<number, Set<number>>>({})
const roleUserCounts = ref<Record<number, number>>({})
const lastUpdated    = ref('')
const selectedRoleId = ref<number | null>(null)
const expandedModules = ref<Set<string>>(new Set())

// Edit mode
const editMode  = ref(false)
const editPerms = ref<Set<number>>(new Set())

function hasPerm(roleId: number, permId: number) {
  if (editMode.value && roleId === selectedRoleId.value) return editPerms.value.has(permId)
  return rolePerms.value[roleId]?.has(permId) ?? false
}

function selectRole(role: Role) {
  selectedRoleId.value = selectedRoleId.value === role.roleId ? null : role.roleId
}

function toggleModule(name: string) {
  const s = new Set(expandedModules.value)
  s.has(name) ? s.delete(name) : s.add(name)
  expandedModules.value = s
}

function toggleEditPerm(permId: number, checked: boolean) {
  const s = new Set(editPerms.value)
  checked ? s.add(permId) : s.delete(permId)
  editPerms.value = s
}

function handleEdit() {
  if (!selectedRoleId.value) {
    ElMessage.warning(t('roles.selectRoleFirst'))
    return
  }
  editPerms.value = new Set(rolePerms.value[selectedRoleId.value] || [])
  editMode.value = true
}

async function confirmEdit() {
  if (!selectedRoleId.value) return
  saving.value = true
  try {
    await assignPermissions(selectedRoleId.value, [...editPerms.value])
    const rp = { ...rolePerms.value }
    rp[selectedRoleId.value] = new Set(editPerms.value)
    rolePerms.value = rp
    ElMessage.success(t('roles.permissionsSaved'))
    editMode.value = false
  } finally { saving.value = false }
}

function cancelEdit() {
  editMode.value = false
}

const selectedRole = computed(() => matrixRoles.value.find(r => r.roleId === selectedRoleId.value) ?? null)

async function handleDeleteRole() {
  if (!selectedRoleId.value) return
  const role = selectedRole.value
  if (!role) return
  if (role.isSystemRole) {
    ElMessage.warning('Không thể xoá vai trò hệ thống')
    return
  }
  try {
    await ElMessageBox.confirm(
      `Bạn đang xoá vai trò "${role.displayName}". Hành động này không thể hoàn tác. Xác nhận xoá?`,
      'Xoá vai trò',
      { confirmButtonText: 'Xác nhận', cancelButtonText: 'Huỷ', type: 'warning' }
    )
  } catch {
    return
  }
  saving.value = true
  try {
    await deleteRole(role.roleId)
    ElMessage.success(`Đã xoá vai trò "${role.displayName}"`)
    selectedRoleId.value = null
    await load()
  } catch (err: any) {
    const msg = err?.response?.data?.message ?? 'Xoá vai trò thất bại'
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

// ── Fallback mock data ──
const FALLBACK_DATA = {
  roles: [
    { roleId: 1, roleName: 'ROLE_ADMIN',   displayName: 'Admin',          isSystemRole: true  },
    { roleId: 2, roleName: 'ROLE_LEVEL_1', displayName: 'Vai trò cấp 1', isSystemRole: false },
    { roleId: 3, roleName: 'ROLE_LEVEL_2', displayName: 'Vai trò cấp 2', isSystemRole: false },
    { roleId: 4, roleName: 'ROLE_LEVEL_3', displayName: 'Vai trò cấp 3', isSystemRole: false },
    { roleId: 5, roleName: 'ROLE_LEVEL_4', displayName: 'Vai trò cấp 4', isSystemRole: false },
  ],
  moduleGroups: [
    {
      moduleName: 'DASHBOARD',
      permissionGroups: [
        { groupName: 'DASHBOARD', permissions: [
          { permissionId: 1, permissionCode: 'dashboard:view', displayName: 'Xem thông tin' },
        ]},
      ],
    },
    {
      moduleName: 'SUBSCRIPTION_MANAGEMENT',
      permissionGroups: [
        { groupName: 'PLAN', permissions: [
          { permissionId: 2, permissionCode: 'plan:view',   displayName: 'Xem thông tin theo thiết bị'  },
          { permissionId: 3, permissionCode: 'plan:create', displayName: 'Xem thông tin theo chi nhánh' },
          { permissionId: 4, permissionCode: 'plan:update', displayName: 'Thêm chi nhánh'               },
          { permissionId: 5, permissionCode: 'plan:delete', displayName: 'Đổi chi nhánh cho thiết bị'   },
        ]},
        { groupName: 'SUBSCRIPTION', permissions: [
          { permissionId: 6, permissionCode: 'subscription:view',   displayName: 'Xem thông tin'             },
          { permissionId: 7, permissionCode: 'subscription:create', displayName: 'Tải lên firmware'           },
          { permissionId: 8, permissionCode: 'subscription:update', displayName: 'Cấu hình cập nhật firmware' },
        ]},
      ],
    },
    {
      moduleName: 'SYSTEM_CONFIGURATION',
      permissionGroups: [
        { groupName: 'USER', permissions: [
          { permissionId: 9,  permissionCode: 'user:view',   displayName: 'Xem danh sách người dùng' },
          { permissionId: 10, permissionCode: 'user:create', displayName: 'Tạo người dùng'           },
          { permissionId: 11, permissionCode: 'user:update', displayName: 'Chỉnh sửa người dùng'     },
        ]},
        { groupName: 'ROLE', permissions: [
          { permissionId: 12, permissionCode: 'role:view',   displayName: 'Xem danh sách vai trò' },
          { permissionId: 13, permissionCode: 'role:create', displayName: 'Tạo vai trò'           },
          { permissionId: 14, permissionCode: 'role:update', displayName: 'Chỉnh sửa vai trò'     },
        ]},
        { groupName: 'AUDIT_LOG', permissions: [
          { permissionId: 15, permissionCode: 'audit-log:view', displayName: 'Xem nhật ký hệ thống' },
        ]},
      ],
    },
    {
      moduleName: 'ANALYTICS',
      permissionGroups: [
        { groupName: 'REPORT', permissions: [
          { permissionId: 16, permissionCode: 'report:view', displayName: 'Xem báo cáo' },
        ]},
      ],
    },
  ],
  rolePermissions: { 1: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16], 2: [1,2,3,6,9,12,16], 3: [1,2,3], 4: [1,2,3,4,5,6,9,12], 5: [2,3] } as Record<number, number[]>,
  roleUserCounts: { 1: 4, 2: 3, 3: 2, 4: 4, 5: 1 } as Record<number, number>,
}

function applyData(data: typeof FALLBACK_DATA) {
  const roles = data.roles as Role[]
  const mods  = data.moduleGroups as PermissionModule[]
  matrixRoles.value  = roles
  moduleGroups.value = mods
  const rp: Record<number, Set<number>> = {}
  for (const role of roles) rp[role.roleId] = new Set(data.rolePermissions[role.roleId] || [])
  rolePerms.value       = rp
  roleUserCounts.value  = data.roleUserCounts || {}
  expandedModules.value = new Set(mods.map(m => m.moduleName))
  const now = new Date()
  lastUpdated.value = now.toLocaleDateString('vi-VN') + ' ' + now.toLocaleTimeString('vi-VN')
}

async function load() {
  loading.value = true
  try {
    const res = await getPermissionMatrix()
    if (res.success && res.data?.roles?.length) applyData(res.data)
    else applyData(FALLBACK_DATA)
  } catch { applyData(FALLBACK_DATA) }
  finally { loading.value = false }
}

// ── Create dialog ──
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ roleName: '', displayName: '', description: '' })
const rules = computed(() => ({
  roleName:    [{ required: true, message: t('common.required') }, { pattern: /^ROLE_/, message: t('roles.mustStartWithRole') }],
  displayName: [{ required: true, message: t('common.required') }],
}))

function openCreate() {
  Object.assign(form, { roleName: '', displayName: '', description: '' })
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    await createRole(form)
    ElMessage.success(t('roles.createdMsg'))
    dialogVisible.value = false
    load()
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title  { margin: 0 0 2px; font-size: 20px; font-weight: 700; color: #1a1a2e; }
.page-subtitle { font-size: 13px; color: var(--el-text-color-secondary); }

.toolbar { display: flex; gap: 8px; margin-bottom: 16px; }

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 16px;
}
.edit-actions-bottom {
  margin-bottom: 0;
  margin-top: 16px;
}

/* ── Matrix table ── */
.matrix-wrap { overflow-x: auto; border: 1px solid var(--el-border-color-lighter); border-radius: 6px; }

.matrix-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  min-width: max-content;
}

.col-perm { min-width: 280px; }
.col-role { width: 120px; }

/* Header row */
.header-row th {
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 10px 16px;
  font-weight: 600;
  font-size: 14px;
  color: #1a1a2e;
  text-align: center;
  white-space: nowrap;
  user-select: none;
}
.th-perm { text-align: left !important; }
.th-role { cursor: pointer; }
.th-role.selected  { background: #e8f0fe; color: #1B60CB; }
.th-role.editable  { background: #e8f0fe; color: #1B60CB; cursor: default; }

.edit-col-icon {
  font-size: 13px;
  margin-right: 4px;
  vertical-align: middle;
}

/* Count row */
.count-row td {
  background: #fafafa;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 6px 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  text-align: center;
}
.count-label { text-align: left; font-style: italic; }
.count-val { font-weight: 600; color: #303133; }
.count-val.selected { background: #e8f0fe; }

/* Module rows */
.module-row td {
  background: #1B60CB;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
  border-top: 1px solid #1550b0;
}
.module-row:hover td { background: #1550b0; }
.module-arrow {
  display: inline-block;
  margin-right: 6px;
  font-size: 11px;
  transition: transform 0.2s;
  transform: rotate(-90deg);
}
.module-arrow.expanded { transform: rotate(0deg); }

/* Group rows */
.group-row td { padding: 0; border-top: 1px solid var(--el-border-color-lighter); }
.group-name-cell {
  padding: 7px 16px 7px 28px;
  font-weight: 600;
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
}

/* Permission rows */
.perm-row td { border-top: 1px solid var(--el-border-color-lighter); padding: 7px 16px; }
.perm-row:hover td { background: #fafafa; }
.perm-name-cell { padding-left: 40px; font-size: 14px; color: #303133; }
.check-cell { text-align: center; }
.check-cell.selected { background: #f0f5ff; }
.check-cell.edit-col { background: #eef4ff; }

/* Footer */
.matrix-footer {
  text-align: right;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 12px;
  font-style: italic;
}
</style>
