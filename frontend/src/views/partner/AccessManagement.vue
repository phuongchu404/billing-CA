<template>
  <div>
    <div class="page-header">
      <div>
        <h2 class="page-title">Quản lý quyền truy cập đối tác</h2>
        <div class="page-subtitle">Cấp / thu hồi quyền xem báo cáo cho tài khoản đối tác</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-select
          v-model="selectedPartner"
          placeholder="Chọn đối tác"
          filterable
          clearable
          style="width: 280px"
          @change="onPartnerChange"
        >
          <el-option
            v-for="u in partnerUsers"
            :key="u.userId"
            :label="`${u.fullName} (${u.username})`"
            :value="u.userId"
          />
        </el-select>
        <el-button
          type="primary"
          :disabled="!selectedPartner || !can('partner:access:grant')"
          @click="openGrant"
        >+ Cấp quyền mới</el-button>
      </div>

      <!-- Access list -->
      <el-table :data="accessList" border empty-text="Chọn đối tác để xem danh sách quyền truy cập">
        <el-table-column type="index" width="55" label="#" />
        <el-table-column prop="groupCode" label="MÃ ĐẠI LÝ" width="120" />
        <el-table-column prop="groupName" label="TÊN ĐẠI LÝ" min-width="200" />
        <el-table-column prop="grantedBy" label="ĐƯỢC CẤP BỞI" width="150" />
        <el-table-column prop="grantedAt" label="NGÀY CẤP" width="160">
          <template #default="{ row }">{{ formatDatetime(row.grantedAt) }}</template>
        </el-table-column>
        <el-table-column label="TRẠNG THÁI" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.active" type="success">Còn hiệu lực</el-tag>
            <el-tag v-else type="info">Đã thu hồi</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="HÀNH ĐỘNG" width="130">
          <template #default="{ row }">
            <el-button
              v-if="row.active"
              size="small"
              type="danger"
              plain
              :disabled="!can('partner:access:revoke')"
              @click="handleRevoke(row)"
            >Thu hồi</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Grant Access Dialog -->
    <el-dialog v-model="grantVisible" width="440px" :close-on-click-modal="false">
      <template #header>
        <span style="font-size:15px;font-weight:700;">CẤP QUYỀN XEM BÁO CÁO</span>
      </template>
      <el-form label-width="120px" label-position="left">
        <el-form-item label="Đối tác:">
          <span>{{ partnerUsers.find(u => u.userId === selectedPartner)?.fullName }}</span>
        </el-form-item>
        <el-form-item label="Đại lý:">
          <el-select v-model="grantGroupId" filterable clearable placeholder="Chọn đại lý" style="width:100%">
            <el-option
              v-for="g in availableGroups"
              :key="g.groupId"
              :label="`${g.groupCode} — ${g.groupName}`"
              :value="g.groupId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:8px">
          <el-button type="primary" :loading="saving" :disabled="!grantGroupId" @click="handleGrant">Xác Nhận</el-button>
          <el-button @click="grantVisible = false">Hủy</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers } from '@/api/users'
import { listGroups } from '@/api/groups'
import { grantPartnerAccess, getPartnerAccessHistory, revokePartnerAccess } from '@/api/partnerAccess'
import { usePermission } from '@/composables/usePermission'
import type { UserAccount } from '@/types'
import type { GroupListItem, PartnerGroupAccess } from '@/types/group'

const { can } = usePermission()

const loading = ref(false)
const saving = ref(false)

const partnerUsers = ref<UserAccount[]>([])
const allGroups = ref<GroupListItem[]>([])
const selectedPartner = ref<string>('')
const accessList = ref<PartnerGroupAccess[]>([])

// Groups chưa được cấp quyền cho partner này
const availableGroups = computed(() => {
  const grantedIds = new Set(accessList.value.filter(a => a.active).map(a => a.groupId))
  return allGroups.value.filter(g => !grantedIds.has(g.groupId))
})

function formatDatetime(iso: string | null | undefined): string {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return iso
  const p = (n: number) => String(n).padStart(2, '0')
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`
}

async function loadInitialData() {
  loading.value = true
  try {
    const [usersRes, groupsRes] = await Promise.all([
      listUsers({ page: 0, size: 500 }),
      listGroups(),
    ])
    if (usersRes.success && usersRes.data) {
      // Lọc chỉ user có role PARTNER
      partnerUsers.value = usersRes.data.content.filter(u =>
        u.roles?.some(r => r.roleName === 'ROLE_PARTNER')
      )
    }
    if (groupsRes.success && groupsRes.data) {
      allGroups.value = groupsRes.data
    }
  } finally {
    loading.value = false
  }
}

async function onPartnerChange(partnerId: string) {
  if (!partnerId) { accessList.value = []; return }
  loading.value = true
  try {
    const res = await getPartnerAccessHistory(partnerId)
    if (res.success && res.data) accessList.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleRevoke(row: PartnerGroupAccess) {
  await ElMessageBox.confirm(
    `Thu hồi quyền xem báo cáo đại lý "${row.groupName}" của đối tác này?`,
    'Xác nhận thu hồi',
    { type: 'warning', confirmButtonText: 'Thu hồi', cancelButtonText: 'Hủy' }
  )
  saving.value = true
  try {
    await revokePartnerAccess(row.id)
    ElMessage.success('Đã thu hồi quyền truy cập')
    await onPartnerChange(selectedPartner.value)
  } finally {
    saving.value = false
  }
}

// ── Grant dialog ──
const grantVisible = ref(false)
const grantGroupId = ref<number | null>(null)

function openGrant() {
  grantGroupId.value = null
  grantVisible.value = true
}

async function handleGrant() {
  if (!selectedPartner.value || !grantGroupId.value) return
  saving.value = true
  try {
    await grantPartnerAccess(selectedPartner.value, grantGroupId.value)
    ElMessage.success('Đã cấp quyền truy cập')
    grantVisible.value = false
    await onPartnerChange(selectedPartner.value)
  } finally {
    saving.value = false
  }
}

onMounted(loadInitialData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title { margin: 0 0 2px; font-size: 20px; font-weight: 700; color: #1a1a2e; }
.page-subtitle { font-size: 13px; color: var(--el-text-color-secondary); }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
</style>
