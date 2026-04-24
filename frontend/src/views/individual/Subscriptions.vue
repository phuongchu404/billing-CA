<template>
  <div>
    <div class="page-header">
      <h2>{{ t('individual.subscriptionsTitle') }}</h2>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchQuery" :placeholder="t('individual.searchPlaceholder')" clearable style="width:240px" @input="onSearchInput" />
        <el-select v-model="filterStatus" clearable :placeholder="t('common.status')" style="width:130px" @change="resetAndLoad">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button :icon="Refresh" @click="load" style="margin-left:auto">{{ t('common.refresh') }}</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="55" :index="(i: number) => (page - 1) * size + i + 1" />
        <el-table-column prop="subscriptionId" :label="t('subscriptions.id')" width="70" />
        <el-table-column prop="userId" :label="t('individual.userId')" min-width="160" />
        <el-table-column prop="planCode" :label="t('subscriptions.plan')" width="140" />
        <el-table-column :label="t('common.status')" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('subscriptions.validity')" width="210">
          <template #default="{ row }">
            <span v-if="row.startDate">{{ row.startDate }} → {{ row.endDate }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('subscriptions.quota')" width="110">
          <template #default="{ row }">
            {{ row.signingQuotaUsed }} / {{ row.signingQuotaTotal }}
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="270" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" @click="viewAudit(row)">{{ t('subscriptions.audit') }}</el-button>
              <el-popconfirm v-if="row.status === 'PENDING'" :title="t('subscriptions.approveConfirm')" @confirm="doApprove(row)">
                <template #reference>
                  <el-button size="small" type="success">{{ t('subscriptions.approve') }}</el-button>
                </template>
              </el-popconfirm>
              <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" @click="doSuspend(row)">{{ t('subscriptions.suspend') }}</el-button>
              <el-button v-if="row.status === 'SUSPENDED'" size="small" type="success" @click="doReactivate(row)">{{ t('subscriptions.activate') }}</el-button>
              <el-button v-if="['ACTIVE','PENDING'].includes(row.status)" size="small" type="danger" @click="doCancel(row)">{{ t('subscriptions.cancel') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @change="load"
        />
      </div>
    </el-card>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditVisible" :title="t('subscriptions.auditLog')" width="680px">
      <el-timeline>
        <el-timeline-item
          v-for="log in auditLogs"
          :key="log.id"
          :timestamp="log.createdAt"
          placement="top"
        >
          <el-card shadow="never" size="small">
            <strong>{{ log.oldStatus || 'NEW' }} → {{ log.newStatus }}</strong>
            <div style="margin-top:4px; color: #666; font-size: 13px;">
              {{ t('subscriptions.actor') }}: {{ log.actor }}
              <span v-if="log.reason"> | {{ log.reason }}</span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { listSubscriptions, approveSubscription, cancelSubscription, suspendSubscription, reactivateSubscription, getAuditLog } from '@/api/subscriptions'
import type { Subscription } from '@/types'

const { t } = useI18n()
const list = ref<Subscription[]>([])
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const auditVisible = ref(false)
const auditLogs = ref<any[]>([])
const statuses = ['PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'SUSPENDED']

const statusType = (s: string) => ({ ACTIVE: 'success', PENDING: 'warning', EXPIRED: 'info', CANCELLED: 'danger', SUSPENDED: 'warning' }[s] || 'info') as any

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; load() }, 400)
}
function resetAndLoad() { page.value = 1; load() }

async function load() {
  loading.value = true
  try {
    const res = await listSubscriptions({
      subscriberType: 'INDIVIDUAL',
      status: filterStatus.value || undefined,
      query: searchQuery.value || undefined,
      page: page.value - 1,
      size: size.value,
    })
    if (res.success && res.data) {
      list.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally { loading.value = false }
}

async function viewAudit(row: Subscription) {
  const res = await getAuditLog(row.subscriptionId)
  if (res.success) { auditLogs.value = res.data || []; auditVisible.value = true }
}

async function doApprove(row: Subscription) {
  await approveSubscription(row.subscriptionId)
  ElMessage.success(t('subscriptions.approved'))
  load()
}

async function doCancel(row: Subscription) {
  await ElMessageBox.confirm(t('subscriptions.cancelConfirm'), t('common.confirm'), { type: 'warning' })
  await cancelSubscription(row.subscriptionId)
  ElMessage.success(t('subscriptions.cancelled'))
  load()
}

async function doSuspend(row: Subscription) {
  await suspendSubscription(row.subscriptionId)
  ElMessage.success(t('subscriptions.suspended'))
  load()
}

async function doReactivate(row: Subscription) {
  await reactivateSubscription(row.subscriptionId)
  ElMessage.success(t('subscriptions.reactivated'))
  load()
}

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { display: flex; gap: 8px; align-items: center; margin-bottom: 16px; }
.text-muted { color: #bbb; }
.action-btns { display: flex; gap: 6px; align-items: center; }
.action-btns :deep(.el-button) { margin: 0; }
</style>
