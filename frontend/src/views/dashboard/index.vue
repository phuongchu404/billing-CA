<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="left-controls">
        <h2>{{ t('dashboard.title') }}</h2>
        <div class="customer-switcher">
          <span class="switcher-label">{{ t('dashboard.customer') }}</span>
          <button
            class="type-btn"
            :class="{ active: selectedType === 'GROUP' }"
            @click="setType('GROUP')"
          >{{ t('dashboard.partner') }}</button>
          <button
            class="type-btn"
            :class="{ active: selectedType === 'INDIVIDUAL' }"
            @click="setType('INDIVIDUAL')"
          >{{ t('dashboard.individual') }}</button>
        </div>
      </div>
      <div class="refresh-controls">
        <div class="refresh-row">
          <el-button :icon="Refresh" circle size="small" @click="manualRefresh" :loading="refreshing" />
          <el-select v-model="autoRefreshInterval" size="small" style="width: 126px" @change="onIntervalChange">
            <el-option :label="t('dashboard.refreshOff')" :value="0" />
            <el-option :label="t('dashboard.refresh30s')" :value="30000" />
            <el-option :label="t('dashboard.refresh1m')"  :value="60000" />
            <el-option :label="t('dashboard.refresh5m')"  :value="300000" />
          </el-select>
        </div>
        <div v-if="lastRefreshed" class="last-refreshed">
          {{ t('dashboard.lastRefreshed', { time: lastRefreshed }) }}
        </div>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="stat in stats" :key="stat.key">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color + '20', color: stat.color }">
              <el-icon size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ t(`dashboard.${stat.key}`) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" :header="t('dashboard.subscriptionsByStatus')">
          <div ref="pieChartRef" style="height: 280px;" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never" :header="t('dashboard.expiringSoon')">
          <el-table :data="expiringSoon" size="small" v-loading="loadingExpiring" :empty-text="t('dashboard.noExpiring')">
            <el-table-column prop="planName" :label="t('plans.name')" min-width="130" show-overflow-tooltip />
            <el-table-column prop="userId" :label="t('individual.userId')" min-width="110" show-overflow-tooltip>
              <template #default="{ row }">{{ row.userId ?? ('Partner #' + row.groupId) }}</template>
            </el-table-column>
            <el-table-column prop="endDate" :label="t('subscriptions.endDate')" width="110" />
            <el-table-column :label="t('common.status')" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" :header="t('dashboard.recentSubscriptions')">
          <el-table :data="recentSubscriptions" size="small" v-loading="loadingRecent" :empty-text="t('dashboard.noRecent')">
            <el-table-column prop="planName" :label="t('plans.name')" min-width="130" show-overflow-tooltip />
            <el-table-column :label="t('common.status')" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="t('certificates.createdAt')" width="110">
              <template #default="{ row }">{{ row.createdAt?.slice(0, 10) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getTypedSubscriptionSummary, getExpiringSoon, getAllSubscriptions } from '@/api/reports'
import type { TypedSubscriptionSummary, Subscription } from '@/types'
import * as echarts from 'echarts'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const selectedType = ref<'INDIVIDUAL' | 'GROUP'>('GROUP')

const pieChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts | null = null

const expiringSoon = ref<Subscription[]>([])
const allSubscriptions = ref<Subscription[]>([])
const recentSubscriptions = ref<Subscription[]>([])
const loadingExpiring = ref(false)
const loadingRecent = ref(false)
const refreshing = ref(false)
const autoRefreshInterval = ref(0)
const lastRefreshed = ref('')
let refreshTimer: ReturnType<typeof setInterval> | null = null

function statusTagType(status: string): 'success' | 'primary' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
    ACTIVE: 'success', PENDING: 'warning', EXPIRED: 'info', CANCELLED: 'danger', SUSPENDED: 'warning',
  }
  return map[status]
}

const stats = ref([
  { key: 'active',               value: 0, color: '#67C23A', icon: 'CircleCheck' },
  { key: 'newCertsThisMonth',    value: 0, color: '#409EFF', icon: 'Document' },
  { key: 'usageThisMonth',       value: 0, color: '#9C27B0', icon: 'DataAnalysis' },
  { key: 'expiringSoonStat',     value: 0, color: '#F56C6C', icon: 'Warning' },
])

function setType(type: 'INDIVIDUAL' | 'GROUP') {
  selectedType.value = type
  loadDashboardData()
}

async function manualRefresh() {
  refreshing.value = true
  try { await loadDashboardData() } finally { refreshing.value = false }
}

function onIntervalChange(ms: number) {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (ms > 0) refreshTimer = setInterval(loadDashboardData, ms)
}

async function loadDashboardData() {
  const type = selectedType.value

  // Summary stats + charts
  try {
    const res = await getTypedSubscriptionSummary(type)
    if (res.success && res.data) {
      const d = res.data
      stats.value[0].value = d.active
      stats.value[1].value = d.newCertsThisMonth ?? 0
      stats.value[2].value = d.usageThisMonth ?? 0
      stats.value[3].value = d.expiringSoon ?? 0
      renderPieChart(d)
    }
  } catch {}

  // Expiring soon (server-filtered)
  loadingExpiring.value = true
  try {
    const res = await getExpiringSoon(30, type)
    if (res.success) expiringSoon.value = res.data || []
  } catch {} finally { loadingExpiring.value = false }

  // Recent subscriptions (client-filtered from cached list)
  recentSubscriptions.value = allSubscriptions.value
    .filter(s => s.subscriberType === type)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 8)

  lastRefreshed.value = new Date().toLocaleTimeString()
}

function renderPieChart(data: TypedSubscriptionSummary) {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: data.active,    name: t('dashboard.active'),    itemStyle: { color: '#67C23A' } },
        { value: data.pending,   name: t('dashboard.pending'),   itemStyle: { color: '#E6A23C' } },
        { value: data.expired,   name: t('dashboard.expired'),   itemStyle: { color: '#909399' } },
        { value: data.cancelled, name: t('dashboard.cancelled'), itemStyle: { color: '#F56C6C' } },
        { value: data.suspended, name: t('dashboard.suspended'), itemStyle: { color: '#409EFF' } },
      ],
    }],
  }, true)
}


function handleResize() {
  pieChart?.resize()
}

onMounted(async () => {
  // Fetch all subscriptions once; used for client-side recent filter
  loadingRecent.value = true
  try {
    const res = await getAllSubscriptions()
    if (res.success) allSubscriptions.value = res.data || []
  } catch {} finally { loadingRecent.value = false }

  await loadDashboardData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard { padding: 4px; }
.page-header { display: flex; flex-direction: row; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.left-controls { display: flex; flex-direction: column; align-items: flex-start; gap: 8px; }
.left-controls h2 { margin: 0; font-size: 20px; color: #1d2b45; }
.refresh-controls { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.refresh-row { display: flex; align-items: center; gap: 8px; }
.last-refreshed { font-size: 11px; color: #909399; }

.customer-switcher { display: flex; align-items: center; gap: 8px; }
.switcher-label { font-size: 13px; color: #666; }
.type-btn {
  padding: 5px 16px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.4;
}
.type-btn:hover { border-color: #409eff; color: #409eff; }
.type-btn.active { background: #409eff; border-color: #409eff; color: #fff; font-weight: 500; }

.stat-card { border-radius: 8px; }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #1d2b45; line-height: 1; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }
</style>
