<template>
  <div>
    <div class="page-header">
      <h2>{{ t('certificates.title') }}</h2>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchUserId" :placeholder="t('certificates.searchPlaceholder')" clearable style="width:220px" @input="onSearchInput" />
        <el-input v-model="searchSerial" :placeholder="t('certificates.searchBySerial')" clearable style="width:220px" @input="onSerialInput" />
        <el-select v-model="statusFilter" clearable :placeholder="t('certificates.allStatuses')" style="width:160px" @change="resetAndLoad">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="validityFilter" clearable :placeholder="t('certificates.allValidity')" style="width:140px" @change="applyClientFilter">
          <el-option :label="t('certificates.valid')" value="valid" />
          <el-option :label="t('certificates.expired')" value="expired" />
        </el-select>
        <el-select v-model="certTypeFilter" clearable :placeholder="t('certificates.allCertTypes')" style="width:180px" @change="applyClientFilter">
          <el-option :label="t('certificates.certTypeIndividual')" :value="1" />
          <el-option :label="t('certificates.certTypeIndividualOfOrg')" :value="2" />
          <el-option :label="t('certificates.certTypeOrganization')" :value="3" />
        </el-select>
        <el-button :icon="Refresh" @click="load" style="margin-left:auto">{{ t('common.refresh') }}</el-button>
      </div>
      <el-table :data="displayedCertificates" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="55" :index="(i: number) => (currentPage - 1) * pageSize + i + 1" />
        <el-table-column prop="userId" :label="t('certificates.userId')" min-width="160" />
        <el-table-column :label="t('certificates.certType')" width="180">
          <template #default="{ row }">
            <el-tag :type="certTypeTagType(row.certType)" size="small">{{ certTypeLabel(row.certType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateId" :label="t('certificates.serialNumber')" min-width="180" show-overflow-tooltip />
        <el-table-column :label="t('certificates.usageCount')" width="120" align="center">
          <template #default="{ row }">{{ row.usageCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column :label="t('certificates.validity')" width="260">
          <template #default="{ row }">
            <span v-if="row.issuedAt && row.expiresAt">
              {{ formatDate(row.issuedAt) }} → {{ formatDate(row.expiresAt) }}
              <el-tag :type="(isValid(row) ? 'success' : 'danger') as 'success' | 'danger'" size="small" style="margin-left:6px">
                {{ isValid(row) ? t('certificates.valid') : t('certificates.expired') }}
              </el-tag>
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('certificates.subscription')" width="160">
          <template #default="{ row }">
            <span v-if="row.planName">{{ row.planName }}</span>
            <span v-else-if="row.subscriptionId">#{{ row.subscriptionId }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('certificates.status')" width="150">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('certificates.createdAt')" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button :icon="InfoFilled" size="small" circle @click="openDetail(row)" />
            <el-button
              v-if="row.status === 'FAILED' || row.status === 'FAILED_PERMANENT'"
              size="small"
              type="warning"
              @click="doRetry(row)"
            >{{ t('certificates.retry') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="load"
        />
      </div>
    </el-card>

    <!-- Detail drawer -->
    <el-drawer v-model="detailVisible" :title="t('certificates.detail')" direction="rtl" size="520px" @open="onDrawerOpen">
      <template v-if="selectedCert">
        <el-tabs v-model="drawerTab">
          <el-tab-pane :label="t('certificates.details')" name="details">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item :label="t('certificates.userId')">{{ selectedCert.userId }}</el-descriptions-item>
              <el-descriptions-item :label="t('certificates.certType')">
                <el-tag :type="certTypeTagType(selectedCert.certType)" size="small">{{ certTypeLabel(selectedCert.certType) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="t('certificates.serialNumber')">{{ selectedCert.certificateId || '—' }}</el-descriptions-item>
              <el-descriptions-item :label="t('certificates.keyId')">{{ selectedCert.keyId || '—' }}</el-descriptions-item>
              <el-descriptions-item :label="t('certificates.status')">
                <el-tag :type="statusType(selectedCert.status)" size="small">{{ selectedCert.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="t('certificates.validity')">
                <span v-if="selectedCert.issuedAt && selectedCert.expiresAt">
                  {{ formatDate(selectedCert.issuedAt) }} → {{ formatDate(selectedCert.expiresAt) }}
                  <el-tag :type="(isValid(selectedCert) ? 'success' : 'danger') as 'success' | 'danger'" size="small" style="margin-left:6px">
                    {{ isValid(selectedCert) ? t('certificates.valid') : t('certificates.expired') }}
                  </el-tag>
                </span>
                <span v-else class="text-muted">—</span>
              </el-descriptions-item>
              <el-descriptions-item :label="t('certificates.usageCount')">{{ selectedCert.usageCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('certificates.subscription')">
                <span v-if="selectedCert.planName">{{ selectedCert.planName }}</span>
                <span v-else-if="selectedCert.subscriptionId">#{{ selectedCert.subscriptionId }}</span>
                <span v-else class="text-muted">—</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedCert.failureReason" :label="t('certificates.failureReason')">{{ selectedCert.failureReason }}</el-descriptions-item>
              <el-descriptions-item :label="t('certificates.createdAt')">{{ formatDate(selectedCert.createdAt) }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="t('certificates.usageHistory')" name="history" lazy>
            <template v-if="selectedCert.certificateId">
              <el-table :data="usageHistory" v-loading="usageLoading" stripe size="small" style="width:100%">
                <el-table-column type="index" label="#" width="45" :index="(i: number) => (usagePage - 1) * usagePageSize + i + 1" />
                <el-table-column prop="userId" :label="t('certificates.userId')" min-width="140" show-overflow-tooltip />
                <el-table-column :label="t('certificates.usedAt')" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.usedAt) }}</template>
                </el-table-column>
              </el-table>
              <div style="margin-top:12px; display:flex; justify-content:flex-end">
                <el-pagination
                  v-model:current-page="usagePage"
                  v-model:page-size="usagePageSize"
                  :total="usageTotal"
                  layout="total, prev, pager, next"
                  @change="loadUsageHistory"
                />
              </div>
            </template>
            <el-empty v-else :description="t('certificates.noUsageRecords')" />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Refresh, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getAllCertificates, retryCertificate, getCertificateUsageHistory } from '@/api/certificates'
import type { CertificateRecord, CertificateUsageEvent } from '@/types'

const { t } = useI18n()
const certificates = ref<CertificateRecord[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchUserId = ref('')
const searchSerial = ref('')
const statusFilter = ref('')
const validityFilter = ref('')
const certTypeFilter = ref<number | null>(null)
const detailVisible = ref(false)
const selectedCert = ref<CertificateRecord | null>(null)
const drawerTab = ref('details')
const usageHistory = ref<CertificateUsageEvent[]>([])
const usageLoading = ref(false)
const usagePage = ref(1)
const usagePageSize = ref(20)
const usageTotal = ref(0)

const statuses = ['PENDING', 'COMPLETED', 'FAILED', 'FAILED_PERMANENT']

const statusType = (s: string) => ({
  COMPLETED: 'success', PENDING: 'warning', FAILED: 'danger', FAILED_PERMANENT: 'danger'
}[s] || 'info') as any

function isValid(cert: CertificateRecord): boolean {
  return !!(cert.expiresAt && new Date(cert.expiresAt) > new Date())
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleString('vi-VN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

const displayedCertificates = computed(() => {
  const serial = searchSerial.value.trim().toLowerCase()
  return certificates.value.filter(cert => {
    if (validityFilter.value === 'valid' && !isValid(cert)) return false
    if (validityFilter.value === 'expired' && isValid(cert)) return false
    if (certTypeFilter.value !== null && cert.certType !== certTypeFilter.value) return false
    if (serial && !(cert.certificateId ?? '').toLowerCase().includes(serial)) return false
    return true
  })
})

const certTypeLabel = (type?: number) => ({
  1: t('certificates.certTypeIndividual'),
  2: t('certificates.certTypeIndividualOfOrg'),
  3: t('certificates.certTypeOrganization'),
}[type as 1 | 2 | 3] ?? '—')

const certTypeTagType = (type?: number): 'primary' | 'success' | 'warning' => (({
  1: 'primary',
  2: 'warning',
  3: 'success',
} as Record<number, 'primary' | 'success' | 'warning'>)[type ?? 0]) ?? 'primary'

function openDetail(row: CertificateRecord) {
  selectedCert.value = row
  drawerTab.value = 'details'
  usageHistory.value = []
  usageTotal.value = 0
  usagePage.value = 1
  detailVisible.value = true
}

function onDrawerOpen() {
  // history tab loads lazily on tab switch via @change; nothing needed here
}

async function loadUsageHistory() {
  if (!selectedCert.value?.certificateId) return
  usageLoading.value = true
  try {
    const res = await getCertificateUsageHistory(selectedCert.value.certificateId, {
      page: usagePage.value - 1,
      size: usagePageSize.value
    })
    if (res.success && res.data) {
      usageHistory.value = res.data.content
      usageTotal.value = res.data.totalElements
    }
  } finally {
    usageLoading.value = false
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; load() }, 400)
}
let serialTimer: ReturnType<typeof setTimeout> | null = null
function onSerialInput() {
  if (serialTimer) clearTimeout(serialTimer)
  serialTimer = setTimeout(() => { currentPage.value = 1 }, 400)
}
function resetAndLoad() { currentPage.value = 1; load() }
function applyClientFilter() { currentPage.value = 1 }

async function load() {
  loading.value = true
  try {
    const res = await getAllCertificates({
      status: statusFilter.value || undefined,
      userId: searchUserId.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    if (res.success && res.data) {
      certificates.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally { loading.value = false }
}

async function doRetry(record: CertificateRecord) {
  try {
    await retryCertificate(record.provisioningRecordId)
    ElMessage.success(t('certificates.retryInitiated'))
    load()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('certificates.retry'))
  }
}

watch(drawerTab, (tab) => {
  if (tab === 'history') loadUsageHistory()
})

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { display: flex; gap: 8px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.text-muted { color: #bbb; }
</style>
