<template>
  <div>
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ t('partnerAccess.title') }}</h2>
        <div class="page-subtitle">{{ t('partnerAccess.subtitle') }}</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <!-- Toolbar -->
      <div class="toolbar">
        <el-select
          v-model="selectedPartner"
          :placeholder="t('partnerAccess.selectPartnerPlaceholder')"
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
        >{{ t('partnerAccess.grantNew') }}</el-button>
      </div>

      <!-- Access list -->
      <el-table :data="pagedAccessList" border :empty-text="t('partnerAccess.emptyText')">
        <el-table-column width="55" label="#">
          <template #default="{ $index }">{{ (currentPage - 1) * pageSize + $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="groupCode" :label="t('partnerAccess.colGroupCode').toUpperCase()" width="120" />
        <el-table-column prop="groupName" :label="t('partnerAccess.colGroupName').toUpperCase()" min-width="200" />
        <el-table-column prop="grantedBy" :label="t('partnerAccess.colGrantedBy').toUpperCase()" width="150" />
        <el-table-column prop="grantedAt" :label="t('partnerAccess.colGrantedAt').toUpperCase()" width="160">
          <template #default="{ row }">{{ formatDatetime(row.grantedAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.status').toUpperCase()" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.active" type="success">{{ t('partnerAccess.statusActive') }}</el-tag>
            <el-tag v-else type="info">{{ t('partnerAccess.statusRevoked') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="t('common.actions').toUpperCase()" width="130">
          <template #default="{ row }">
            <el-button
              v-if="row.active"
              size="small"
              type="danger"
              plain
              :disabled="!can('partner:access:revoke')"
              @click="handleRevoke(row)"
            >{{ t('partnerAccess.revokeAction') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="accessList.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- Grant Access Dialog -->
    <el-dialog v-model="grantVisible" width="440px" :close-on-click-modal="false">
      <template #header>
        <span class="dlg-title">{{ t('partnerAccess.grantDialogTitle') }}</span>
      </template>
      <el-form label-width="120px" label-position="left">
        <el-form-item :label="t('partnerAccess.partnerLabel') + ':'">
          <span>{{ partnerUsers.find(u => u.userId === selectedPartner)?.fullName }}</span>
        </el-form-item>
        <el-form-item :label="t('partnerAccess.groupLabel') + ':'">
          <el-select v-model="grantGroupId" filterable clearable :placeholder="t('partnerAccess.selectGroupPlaceholder')" style="width:100%">
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
          <el-button type="primary" :loading="saving" :disabled="!grantGroupId" @click="handleGrant">{{ t('common.confirm') }}</el-button>
          <el-button @click="grantVisible = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { listPartnerUsers } from '@/api/users'
import { listGroups } from '@/api/groups'
import { grantPartnerAccess, getPartnerAccessHistory, revokePartnerAccess } from '@/api/partnerAccess'
import { usePermission } from '@/composables/usePermission'
import type { UserAccount } from '@/types'
import type { GroupListItem, PartnerGroupAccess } from '@/types/group'

const { t } = useI18n()
const { can } = usePermission()

const loading = ref(false)
const saving = ref(false)

const partnerUsers = ref<UserAccount[]>([])
const allGroups = ref<GroupListItem[]>([])
const selectedPartner = ref<number | null>(null)
const accessList = ref<PartnerGroupAccess[]>([])

const currentPage = ref(1)
const pageSize = ref(10)

const pagedAccessList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return accessList.value.slice(start, start + pageSize.value)
})

const availableGroups = computed(() => {
  const grantedIds = new Set(accessList.value.filter(a => a.active).map(a => a.groupId))
  const groups = Array.isArray(allGroups.value) ? allGroups.value : []
  return groups.filter(g => !grantedIds.has(g.groupId))
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
      listPartnerUsers({ page: 0, size: 200 }),
      listGroups(),
    ])
    if (usersRes.success && usersRes.data) {
      partnerUsers.value = usersRes.data.content
    }
    if (groupsRes.success && groupsRes.data) {
      allGroups.value = groupsRes.data.list ?? []
    }
  } finally {
    loading.value = false
  }
}

async function onPartnerChange(partnerId: number | null) {
  if (partnerId == null) { accessList.value = []; currentPage.value = 1; return }
  currentPage.value = 1
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
    t('partnerAccess.revokeConfirmMsg', { groupName: row.groupName }),
    t('partnerAccess.revokeConfirmTitle'),
    { type: 'warning', confirmButtonText: t('partnerAccess.revokeAction'), cancelButtonText: t('common.cancel') }
  )
  saving.value = true
  try {
    await revokePartnerAccess(row.id)
    ElMessage.success(t('partnerAccess.revokedSuccess'))
    await onPartnerChange(selectedPartner.value)
  } finally {
    saving.value = false
  }
}

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
    ElMessage.success(t('partnerAccess.grantedSuccess'))
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
.dlg-title { font-size: 15px; font-weight: 700; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
