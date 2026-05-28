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
        <el-button type="primary" :icon="Plus" :disabled="!authStore.hasPermission('role:create')" @click="openCreate">{{ t('roles.newRole') }}</el-button>
        <el-tooltip
          :content="isProtectedRole ? t('roles.cannotEditAdmin') : ''"
          :disabled="!isProtectedRole"
          placement="top"
        >
          <el-button
            :icon="EditPen"
            :disabled="!authStore.hasPermission('role:update') || !selectedRoleId || isProtectedRole"
            @click="handleEdit"
          >{{ t('roles.editRole') }}</el-button>
        </el-tooltip>
        <el-tooltip
          :content="selectedRole?.roleName === 'ROLE_PARTNER' ? t('roles.cannotDeletePartner') : isProtectedRole ? t('roles.cannotDeleteAdmin') : ''"
          :disabled="!isUndeletableRole"
          placement="top"
        >
          <el-button
            :icon="Delete"
            type="danger"
            plain
            :loading="saving"
            :disabled="!authStore.hasPermission('role:update') || !selectedRoleId || isUndeletableRole"
            @click="handleDeleteRole"
          >{{ t('roles.deleteRole') }}</el-button>
        </el-tooltip>
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
                  {{ translatePermissionModule(mod.moduleName) }}
                </td>
              </tr>

              <template v-if="expandedModules.has(mod.moduleName)">
                <template v-for="group in mod.permissionGroups" :key="group.groupName">
                  <tr class="group-row">
                    <td :colspan="matrixRoles.length + 1" class="group-name-cell">
                      {{ translatePermissionGroup(group.groupName) }}
                    </td>
                  </tr>

                  <tr v-for="perm in group.permissions" :key="perm.permissionId" class="perm-row">
                    <td class="perm-name-cell">
                      <span>{{ translatePermission(perm) }}</span>
                      <small>{{ perm.permissionKey }}</small>
                    </td>
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
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <span class="dlg-title">{{ t('roles.createRoleTitle') }}</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
        <el-form-item :label="t('roles.roleName')" prop="displayName">
          <el-input v-model="form.displayName" :placeholder="t('roles.roleNamePlaceholder')" />
          <div class="field-hint">{{ t('roles.roleNameHint') }}</div>
        </el-form-item>
      </el-form>

      <!-- Permission selection table -->
      <div class="perm-select-wrap">
        <table class="perm-select-table">
          <thead>
            <tr>
              <th class="pst-th-name">{{ t('roles.permission') }}</th>
              <th class="pst-th-check">{{ t('roles.access') }}</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="mod in permTree" :key="mod.moduleName">
              <tr class="pst-module-row" @click="toggleCreateModule(mod.moduleName)">
                <td>
                  <span class="pst-arrow" :class="{ expanded: createExpandedModules.has(mod.moduleName) }">▼</span>
                  {{ translatePermissionModule(mod.moduleName) }}
                </td>
                <td class="pst-check-cell" @click.stop>
                  <el-checkbox
                    :model-value="isModuleAllChecked(mod)"
                    :indeterminate="isModuleIndeterminate(mod)"
                    @change="(val: any) => toggleModulePerms(mod, !!val)"
                  />
                </td>
              </tr>
              <template v-if="createExpandedModules.has(mod.moduleName)">
                <template v-for="group in mod.permissionGroups" :key="group.groupName">
                  <tr class="pst-group-row">
                    <td colspan="2">{{ translatePermissionGroup(group.groupName) }}</td>
                  </tr>
                  <tr v-for="perm in group.permissions" :key="perm.permissionId" class="pst-perm-row">
                    <td class="pst-perm-name">{{ translatePermission(perm) }}</td>
                    <td class="pst-check-cell">
                      <el-checkbox
                        :model-value="createSelectedPerms.has(perm.permissionId)"
                        @change="(val: any) => toggleCreatePerm(perm.permissionId, !!val)"
                      />
                    </td>
                  </tr>
                </template>
              </template>
            </template>
          </tbody>
        </table>
      </div>

      <template #footer>
        <div class="dlg-footer-right">
          <el-button type="primary" :loading="saving" @click="handleSave">{{ t('common.confirm') }}</el-button>
          <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, EditPen, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { createRole, deleteRole, getPermissionMatrix, assignPermissions, listPermissions } from '@/api/roles'
import { useAuthStore } from '@/store'
import type { Role, PermissionModule } from '@/types'
import type { FormInstance } from 'element-plus'

const { t, te } = useI18n()
const authStore = useAuthStore()
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
  if (isProtectedRole.value) {
    ElMessage.warning(t('roles.cannotEditAdmin'))
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
  } catch (err: any) {
    const msg = err?.response?.data?.message ?? t('roles.loadMatrixFailed')
    ElMessage.error(msg)
  } finally { saving.value = false }
}

function cancelEdit() {
  editMode.value = false
}

const selectedRole      = computed(() => matrixRoles.value.find(r => r.roleId === selectedRoleId.value) ?? null)
const isProtectedRole   = computed(() => selectedRole.value?.roleName === 'ROLE_ADMIN')
const isUndeletableRole = computed(() => {
  const name = selectedRole.value?.roleName
  return name === 'ROLE_ADMIN' || name === 'ROLE_PARTNER' || !!selectedRole.value?.isSystemRole
})

function normalizePermissionKey(value: string) {
  return value.toUpperCase().replace(/[^A-Z0-9]+/g, '_').replace(/^_|_$/g, '')
}

function translatePermissionModule(moduleName: string) {
  const key = `permissions.module.${normalizePermissionKey(moduleName)}`
  return te(key) ? t(key) : moduleName
}

function translatePermissionGroup(groupName: string) {
  const key = `permissions.group.${normalizePermissionKey(groupName)}`
  return te(key) ? t(key) : groupName
}

function translatePermission(permission: { permissionKey: string; displayName: string }) {
  const key = `permissions.permission.${normalizePermissionKey(permission.permissionKey)}`
  return te(key) ? t(key) : permission.displayName
}

async function handleDeleteRole() {
  if (!selectedRoleId.value) return
  const role = selectedRole.value
  if (!role) return
  if (role.roleName === 'ROLE_PARTNER') {
    ElMessage.warning(t('roles.cannotDeletePartner'))
    return
  }
  if (role.isSystemRole) {
    ElMessage.warning(t('roles.cannotDeleteSystemRole'))
    return
  }
  try {
    await ElMessageBox.confirm(
      t('roles.deleteRoleConfirmMsg', { name: role.displayName }),
      t('roles.deleteRole'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
  } catch {
    return
  }
  saving.value = true
  try {
    await deleteRole(role.roleId)
    ElMessage.success(t('roles.deletedMsg'))
    selectedRoleId.value = null
    await load()
  } catch (err: any) {
    const msg = err?.response?.data?.message ?? t('roles.deleteRoleFailed')
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

function applyData(data: { roles: Role[]; moduleGroups: PermissionModule[]; rolePermissions: Record<number, number[]>; roleUserCounts?: Record<number, number> }) {
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
    if (res.success && res.data) applyData(res.data)
    else ElMessage.error(res.message || t('roles.loadMatrixFailed'))
  } catch {
    matrixRoles.value = []
    moduleGroups.value = []
    rolePerms.value = {}
    roleUserCounts.value = {}
  }
  finally { loading.value = false }
}

// ── Create dialog ──
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ displayName: '', description: '' })
const rules = computed(() => ({
  displayName: [{ required: true, message: t('common.required') }],
}))

// Permission tree for create dialog
const permTree = ref<PermissionModule[]>([])
const createSelectedPerms = ref<Set<number>>(new Set())
const createExpandedModules = ref<Set<string>>(new Set())

function isModuleAllChecked(mod: PermissionModule): boolean {
  return mod.permissionGroups.every(g => g.permissions.every(p => createSelectedPerms.value.has(p.permissionId)))
}

function isModuleIndeterminate(mod: PermissionModule): boolean {
  const allPerms = mod.permissionGroups.flatMap(g => g.permissions)
  const checkedCount = allPerms.filter(p => createSelectedPerms.value.has(p.permissionId)).length
  return checkedCount > 0 && checkedCount < allPerms.length
}

function toggleModulePerms(mod: PermissionModule, checked: boolean) {
  const s = new Set(createSelectedPerms.value)
  for (const g of mod.permissionGroups)
    for (const p of g.permissions)
      checked ? s.add(p.permissionId) : s.delete(p.permissionId)
  createSelectedPerms.value = s
}

function toggleCreatePerm(permId: number, checked: boolean) {
  const s = new Set(createSelectedPerms.value)
  checked ? s.add(permId) : s.delete(permId)
  createSelectedPerms.value = s
}

function toggleCreateModule(name: string) {
  const s = new Set(createExpandedModules.value)
  s.has(name) ? s.delete(name) : s.add(name)
  createExpandedModules.value = s
}

function toRoleName(displayName: string): string {
  return 'ROLE_' + displayName.toUpperCase()
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/[^A-Z0-9]/g, '_').replace(/_+/g, '_').replace(/^_|_$/g, '')
}

async function openCreate() {
  Object.assign(form, { displayName: '', description: '' })
  createSelectedPerms.value = new Set()
  if (permTree.value.length === 0) {
    try {
      const res = await listPermissions()
      if (res.success && res.data) permTree.value = res.data
    } catch {}
  }
  createExpandedModules.value = new Set(permTree.value.map(m => m.moduleName))
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    const roleName = toRoleName(form.displayName)
    const res = await createRole({ roleName, displayName: form.displayName, description: form.description })
    if (res.data && createSelectedPerms.value.size > 0) {
      await assignPermissions(res.data.roleId, [...createSelectedPerms.value])
    }
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
.th-perm { text-align: left !important; width: 440px;}
.th-role { cursor: pointer; width: 10rem;}
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
  background: #1B60CB14;
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
}
.module-row:hover td { background: var(--el-color-primary-light-8); }
.module-arrow {
  display: inline-block;
  margin-right: 6px;
  font-size: 11px;
  transition: transform 0.2s;
  transform: rotate(-90deg);
}
.module-arrow.expanded { transform: rotate(0deg); }

/* Group rows */
.group-row td { padding: 0.75rem 2.5rem; border-top: 1px solid var(--el-border-color-lighter); }
.group-name-cell {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
  background-color: var(--color-light-grey);
}

/* Permission rows */
.perm-row td { border-top: 1px solid var(--el-border-color-lighter); padding: 0.75rem 2.5rem; }
.perm-row:hover td { background: #fafafa; }
.perm-name-cell { padding-left: 40px; font-size: 15px; color: var(--el-text-color-primary); }
.perm-name-cell span,
.perm-name-cell small {
  display: block;
}
.perm-name-cell small {
  margin-top: 2px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.check-cell { text-align: center; }
.check-cell.selected { background: #f0f5ff; }
.check-cell.edit-col { background: #eef4ff; }
:deep(.el-checkbox__input.is-disabled.is-checked .el-checkbox__inner) {
  background-color: var(--el-color-primary) !important;
  border: none;
}

:deep(.el-checkbox__input.is-disabled.is-checked .el-checkbox__inner:after) {
  border-color: #fff !important;  
  border-width: 2px;
}
/* Footer */
.matrix-footer {
  text-align: right;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 12px;
  font-style: italic;
}

/* ── Create Role Dialog ── */
.dlg-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.02em;
}
.field-hint {
  margin-top: 4px;
}
.dlg-footer-right {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}

/* Permission select table */
.perm-select-wrap {
  max-height: 380px;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  margin-top: 4px;
}
.perm-select-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.perm-select-table thead th {
  background: #f5f7fa;
  padding: 9px 14px;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  border-bottom: 1px solid var(--el-border-color-lighter);
  position: sticky;
  top: 0;
  z-index: 1;
}
.pst-th-name { text-align: left; }
.pst-th-check { text-align: center; width: 64px; }

.pst-module-row td {
  background: #1B60CB;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 8px 14px;
  cursor: pointer;
  user-select: none;
  border-top: 1px solid #1550b0;
}
.pst-module-row:hover td { background: #1550b0; }
.pst-arrow {
  display: inline-block;
  margin-right: 6px;
  font-size: 11px;
  transition: transform 0.2s;
  transform: rotate(-90deg);
}
.pst-arrow.expanded { transform: rotate(0deg); }

.pst-group-row td {
  background: #f5f7fa;
  padding: 7px 14px 7px 28px;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  border-top: 1px solid var(--el-border-color-lighter);
}

.pst-perm-row td {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 7px 14px;
  color: #303133;
}
.pst-perm-row:hover td { background: #fafafa; }
.pst-perm-name { padding-left: 42px !important; font-size: 14px; }
.pst-check-cell {
  text-align: center;
  width: 64px;
}
/* White checkbox on blue module row */
.pst-module-row :deep(.el-checkbox__inner) {
  background-color: transparent;
  border-color: rgba(255,255,255,0.8);
}
.pst-module-row :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #fff;
  border-color: #fff;
}
.pst-module-row :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #1B60CB;
}
.pst-module-row :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background-color: #fff;
  border-color: #fff;
}
.pst-module-row :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner::before) {
  background-color: #1B60CB;
}
</style>
