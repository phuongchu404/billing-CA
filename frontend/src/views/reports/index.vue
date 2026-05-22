<template>
  <div class="reports-page">
    <!-- Header -->
    <div class="dashbboard-header">
      <h2>{{ t('reports.dashboardTitle') }}</h2>
      <div class="type-switcher">
        <span class="switcher-label">{{ t('reports.customer') }}</span>
        <button v-if="canViewGroup"      class="type-btn" :class="{ active: selectedType === 'GROUP' }"      @click="setType('GROUP')">{{ t('reports.groupCustomer') }}</button>
        <button v-if="canViewIndividual" class="type-btn" :class="{ active: selectedType === 'INDIVIDUAL' }" @click="setType('INDIVIDUAL')">{{ t('reports.individualCustomer') }}</button>
      </div>
    </div>

    <!-- Individual customer dashboard -->
    <IndividualReport v-if="selectedType === 'INDIVIDUAL'" />

    <!-- Không có quyền xem tab nào -->
    <el-empty v-if="!canViewGroup && !canViewIndividual" :description="t('reports.noPermission')" />

    <!-- Stat cards -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16" style="margin-bottom: 20px" v-loading="loading">
      <!-- Card 1: Active partners -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon">
              <el-icon size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ report?.stats.activePartners ?? 0 }}</div>
            </div>
          </div>
          <div class="stat-label">{{ t('reports.activePartners') }}</div>
        </el-card>
      </el-col>

      <!-- Card 2: New CTS this month -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon">
              <el-icon size="28"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (report?.stats.newCts ?? 0).toLocaleString('vi-VN') }}</div>
            </div>
          </div>
          <div>
            <div class="stat-label">{{ t('reports.newCtsThisMonth') }}</div>
            <div class="stat-growth" :class="(report?.stats.newCtsPct ?? 0) >= 0 ? 'positive' : 'negative'">
              {{ (report?.stats.newCtsPct ?? 0) >= 0 ? '+' : '' }}{{ report?.stats.newCtsPct ?? 0 }}%
              <span class="growth-note">{{ t('reports.comparedPreviousMonth') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 3: Signings this month -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon">
              <el-icon size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (report?.stats.signings ?? 0).toLocaleString('vi-VN') }}</div>
            </div>
          </div>
          <div>
            <div class="stat-label">{{ t('reports.signingsThisMonth') }}</div>
            <div class="stat-growth" :class="(report?.stats.signingsPct ?? 0) >= 0 ? 'positive' : 'negative'">
              {{ (report?.stats.signingsPct ?? 0) >= 0 ? '+' : '' }}{{ report?.stats.signingsPct ?? 0 }}%
              <span class="growth-note">{{ t('reports.comparedPreviousMonth') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 4: Expiring soon warning -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background: #fff3e0; color: #F56C6C">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ report?.stats.expiringSoon ?? 0 }}</div>
            </div>
          </div>
          <div>
            <div class="stat-label">
              {{ t('reports.expiringSoonSummary') }}
              <el-button link type="primary" style="padding: 0; font-size: 15px; text-decoration: underline; font-style: italic;" @click="openExpiringDialog">{{ t('common.detail') }}</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Month selector -->
    <div v-if="selectedType === 'GROUP'" class="month-bar">
      <el-select v-model="selectedMonth" size="small" style="width: 180px;" @change="loadReport">
        <el-option v-for="m in monthOptions" :key="m.value" :label="m.label" :value="m.value" />
      </el-select>
    </div>

    <!-- Charts row 1 -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16" style="margin-bottom: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">{{ t('reports.certCreatedChart') }}</span></template>
          <div ref="certChartRef" style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">{{ t('reports.signingsByMonthChart') }}</span></template>
          <div ref="signingChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Expiring soon dialog -->
    <el-dialog
      v-model="expiringDialogVisible"
      :title="t('reports.expiringPlanTitle')"
      width="680px"
      :close-on-click-modal="false"
    >
      <p class="dlg-desc">
        {{ t('reports.expiringPlanDesc') }}
      </p>
      <el-table :data="expiringRows" border style="width: 100%" size="small">
        <el-table-column type="index" label="#" width="48" align="center" />
        <el-table-column prop="code" :label="t('agency.colCode')" sortable min-width="110" />
        <el-table-column prop="name" :label="t('agency.colName')" sortable min-width="160" />
        <el-table-column prop="plan" :label="t('reports.currentPlan')" min-width="150" />
        <el-table-column prop="expiry" :label="t('agency.colApplyTo')" sortable width="130" align="center" />
        <el-table-column :label="t('common.actions')" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" plain round @click="goExpiringGroupDetail(row)">
              <el-icon><View /></el-icon> {{ t('common.detail') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="dlg-updated">{{ t('reports.updatedAt', { time: report?.lastUpdated ?? '' }) }}</div>
      <template #footer>
        <el-button @click="expiringDialogVisible = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Charts row 2 -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">{{ t('reports.signingPerCertRatio') }}</span></template>
          <div class="ratio-table-wrap">
            <table class="ratio-table">
              <thead>
                <tr>
                  <th class="col-agency">{{ t('reports.groupCustomer').toUpperCase() }}</th>
                  <th class="col-metric">{{ t('individualReport.filterIndividual').toUpperCase() }}</th>
                  <th class="col-metric">{{ t('individualReport.filterOrganization').toUpperCase() }}</th>
                  <th class="col-metric">{{ t('reports.individualOfOrgShort').toUpperCase() }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in report?.ratioData ?? []" :key="row.name">
                  <td>{{ row.name }}</td>
                  <td class="text-right">{{ row.individual }}</td>
                  <td class="text-right">{{ row.organization }}</td>
                  <td class="text-right">{{ row.individualOfOrg }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">{{ t('reports.signingGrowthChart') }}</span></template>
          <div ref="growthChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, DocumentChecked, DataAnalysis, Warning, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import IndividualReport from './IndividualReport.vue'
import { getGroupReport } from '@/api/reports'
import type { ExpiringGroupRow, GroupReportResponse } from '@/api/reports'
import { usePermission } from '@/composables/usePermission'
import { useI18n } from 'vue-i18n'

type CustomerType = 'GROUP' | 'INDIVIDUAL'

const { can } = usePermission()
const { t, locale } = useI18n()
const router = useRouter()
const canViewGroup      = computed(() => can('report:group:view'))
const canViewIndividual = computed(() => can('report:individual:view'))

// Auto-select the first tab the current user can view.
const defaultTab: CustomerType = (() => {
  if (can('report:group:view')) return 'GROUP'
  if (can('report:individual:view')) return 'INDIVIDUAL'
  return 'GROUP'
})()

const selectedType = ref<CustomerType>(defaultTab)
const selectedMonth = ref(currentMonthKey())
const loading = ref(false)
const expiringDialogVisible = ref(false)

const report = ref<GroupReportResponse | null>(null)

const expiringRows = computed(() => report.value?.expiringRows ?? [])

const certChartRef = ref<HTMLElement>()
const signingChartRef = ref<HTMLElement>()
const growthChartRef = ref<HTMLElement>()

let certChart: echarts.ECharts | null = null
let signingChart: echarts.ECharts | null = null
let growthChart: echarts.ECharts | null = null

const monthOptions = computed(() => buildMonthOptions());

function currentMonthKey() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function buildMonthOptions(count = 12) {
  const now = new Date()
  return Array.from({ length: count }, (_, idx) => {
    const d = new Date(now.getFullYear(), now.getMonth() - idx, 1)
    const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    return {
      label: t('individualReport.month', { m: d.getMonth() + 1, y: d.getFullYear() }),
      value,
    }
  })
}

async function loadReport() {
  loading.value = true
  try {
    const res = await getGroupReport(selectedMonth.value)
    report.value = res.data ?? null
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function openExpiringDialog() {
  expiringDialogVisible.value = true
}

function goExpiringGroupDetail(row: ExpiringGroupRow) {
  expiringDialogVisible.value = false
  router.push(`/plans/${row.groupId}`)
}

function setType(type: CustomerType) {
  if (type === 'GROUP' && !canViewGroup.value) return
  if (type === 'INDIVIDUAL' && !canViewIndividual.value) return
  selectedType.value = type
  if (type === 'GROUP') {
    // Dispose stale instances — their DOM containers were removed by v-if when tab was hidden
    certChart?.dispose(); certChart = null
    signingChart?.dispose(); signingChart = null
    growthChart?.dispose(); growthChart = null
    if (report.value) nextTick(renderCharts)
    else loadReport()
  }
}

function renderCharts() {
  if (!report.value) return
  renderCertChart()
  renderSigningChart()
  renderGrowthChart()
}

const CHART_PAGE_SIZE = 8

function renderCertChart() {
  if (!certChartRef.value || !report.value) return
  if (!certChart) certChart = echarts.init(certChartRef.value)
  const { agencies, certData } = report.value
  const end = Math.min(CHART_PAGE_SIZE, agencies.length)
  const endPct = agencies.length > 0 ? Math.round((end / agencies.length) * 100) : 100
  certChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      bottom: 0,
      data: [t('individualReport.filterIndividual'), t('individualReport.filterOrganization'), t('individualReport.filterIndividualOfOrg')],
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12 },
    },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: agencies, inverse: true, axisLabel: { fontSize: 12 } },
    dataZoom: agencies.length > CHART_PAGE_SIZE ? [
      { type: 'slider', yAxisIndex: 0, orient: 'vertical', width: 6, right: 4, start: 0, end: endPct, brushSelect: false, handleSize: 0, showDetail: false, showDataShadow: false, borderColor: 'transparent', backgroundColor: 'transparent', fillerColor: '#dcdfe6'},
      { type: 'inside', yAxisIndex: 0, start: 0, end: endPct },
    ] : [],
    series: [
      { name: t('individualReport.filterIndividual'), type: 'bar', stack: 'total', data: certData.individual, itemStyle: { color: '#1B60CB' } },
      { name: t('individualReport.filterOrganization'), type: 'bar', stack: 'total', data: certData.organization, itemStyle: { color: '#5B9BD5' } },
      { name: t('individualReport.filterIndividualOfOrg'), type: 'bar', stack: 'total', data: certData.individualOfOrg, itemStyle: { color: '#D4E6F1' } },
    ],
  }, true)
}

function renderSigningChart() {
  if (!signingChartRef.value || !report.value) return
  if (!signingChart) signingChart = echarts.init(signingChartRef.value)
  const { agencies, signingData } = report.value
  const end = Math.min(CHART_PAGE_SIZE, agencies.length)
  const endPct = agencies.length > 0 ? Math.round((end / agencies.length) * 100) : 100
  signingChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 16, right: 32, bottom: 16, top: 8, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: agencies, inverse: true, axisLabel: { fontSize: 12 } },
    dataZoom: agencies.length > CHART_PAGE_SIZE ? [
      { type: 'slider', yAxisIndex: 0, orient: 'vertical', width: 6, right: 4, start: 0, end: endPct, brushSelect: false, handleSize: 0, showDetail: false, showDataShadow: false, borderColor: 'transparent', backgroundColor: 'transparent', fillerColor: '#dcdfe6'},
      { type: 'inside', yAxisIndex: 0, start: 0, end: endPct },
    ] : [],
    series: [
      {
        type: 'bar',
        data: signingData,
        itemStyle: { color: '#1B60CB' },
        label: {
          show: true,
          position: 'insideRight',
          color: '#fff',
          fontSize: 11,
          formatter: (p: any) => (p.value > 0 ? p.value.toLocaleString(locale.value === 'vi' ? 'vi-VN' : 'en-US') : ''),
        },
      },
    ],
  }, true)
}

function renderGrowthChart() {
  if (!growthChartRef.value || !report.value) return
  if (!growthChart) growthChart = echarts.init(growthChartRef.value)
  const { agencies, growthData } = report.value
  const growthValues = growthData.map(d => d.growth)
  const end = Math.min(CHART_PAGE_SIZE, agencies.length)
  const endPct = agencies.length > 0 ? Math.round((end / agencies.length) * 100) : 100

  growthChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const idx = params[0].dataIndex
        const meta = growthData[idx]
        return [
          `<b>${agencies[idx]}</b>`,
          t('reports.currentMonthSignings', { value: meta.current.toLocaleString(locale.value === 'vi' ? 'vi-VN' : 'en-US') }),
          t('reports.previousMonthSignings', { value: meta.prev.toLocaleString(locale.value === 'vi' ? 'vi-VN' : 'en-US') }),
          t('reports.growthRate', { value: meta.growth }),
        ].join('<br/>')
      },
    },
    legend: {
      bottom: 0,
      data: [t('reports.growthPercent')],
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 11 },
    },
    grid: { left: 16, right: 16, bottom: agencies.length > CHART_PAGE_SIZE ? 72 : 48, top: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: agencies,
      axisLabel: { fontSize: 12, interval: 0, rotate: 45, overflow: 'none' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { formatter: (v: number) => v + '' },
    },
    dataZoom: agencies.length > CHART_PAGE_SIZE ? [
      { type: 'slider',  xAxisIndex: 0, bottom: 28, height: 6, start: 0, end: endPct, brushSelect: false, handleSize: 0, showDetail: false, showDataShadow: false, borderColor: 'transparent', backgroundColor: 'transparent', fillerColor: '#dcdfe6'},
      { type: 'inside', xAxisIndex: 0, start: 0, end: endPct },
    ] : [],
    series: [
      {
        name: t('reports.growthFormula'),
        type: 'bar',
        data: growthValues.map(v => ({ value: v, itemStyle: { color: '#1B60CB' } })),
        label: { show: false },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#999', type: 'solid', width: 1 },
          label: { show: false },
          data: [{ yAxis: 0 }],
        },
      },
    ],
  }, true)
}

function handleResize() {
  certChart?.resize()
  signingChart?.resize()
  growthChart?.resize()
}

onMounted(async () => {
  if (canViewGroup.value && selectedType.value === 'GROUP') {
    await loadReport()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  certChart?.dispose()
  signingChart?.dispose()
  growthChart?.dispose()
})
</script>

<style scoped>
.reports-page { padding-bottom: 24px; }

.dashbboard-header {
  margin-bottom: 1.25rem;
}

.page-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #303133; }

.type-switcher { display: flex; align-items: center; gap: 1.5rem; }
.switcher-label { font-size: 1.125rem; color: #606266; }
.type-btn {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  border: 1px solid var(--el-color-primary);
  background: transparent;
  font-size: 15px;
  color: var(--el-color-primary);
  cursor: pointer;
  transition: all 0.2s;   
  width: 110px;
  height: 38px;
}
.type-btn.active {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}

/* Stat cards */
.stat-card { height: 100%; }
.stat-inner { display: flex; align-items: center; gap: 1rem; }
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #e8f0fd; 
  color: var(--el-color-primary)
}
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #2F2B3DE5; line-height: 42px; }
.stat-label { font-size: 15px; color: var(--el-text-color-regular); margin-top: 8px; line-height: 22px; }
.stat-growth { font-size: 15px; margin-top: 4px; font-weight: 500; color: #2F2B3DE5;}
/*
.stat-growth.positive { color: #67c23a; }
.stat-growth.negative { color: #f56c6c; }
*/
.growth-note { color: #2F2B3D66; font-weight: 400; font-size: 13px;}
:deep(.stat-card .el-card__body) {
  padding: 1rem;
}

/* Month selector */
.month-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.month-bar :deep(.el-input__wrapper),
.month-bar :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
  padding-right: 0 !important;
  border-radius: 4px;
  overflow: hidden; 
  height: 38px;
}

.month-bar :deep(.el-input__wrapper.is-focus),
.month-bar :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.month-bar :deep(.el-input__inner),
.month-bar :deep(.el-select__placeholder) {
  color: var(--el-color-primary) !important;
  font-size: 15px; 
}

.month-bar :deep(.el-input__suffix),
.month-bar :deep(.el-select__suffix) {
  border-left: 1px solid var(--el-color-primary);
  width: 32px; 
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  right: 0;
  height: 100%;
  top: 0;
  background-color: transparent;
}

.month-bar :deep(.el-select__caret),
.month-bar :deep(.el-icon) {
  color: var(--el-color-primary) !important;
  font-weight: bold;
}

/* Chart title */
.chart-title { font-size: 14px; font-weight: 600; color: #303133; }

/* Ratio table */
.ratio-table-wrap {
  max-height: 300px;
  overflow-y: auto;
}
.ratio-table-wrap::-webkit-scrollbar { width: 6px; }
.ratio-table-wrap::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
.ratio-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.ratio-table thead th {
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}
.ratio-table th {
  padding: 8px 12px;
  font-weight: 600;
  font-size: 12px;
  color: #1B60CB;
  border-bottom: 1px solid #e4e7ed;
  text-align: left;
}
.ratio-table th.col-agency { color: #303133; }
.ratio-table th.col-metric { text-align: right; }
.ratio-table td {
  padding: 9px 12px;
  border-bottom: 1px solid #f4f4f5;
  color: #303133;
}
.ratio-table tbody tr:last-child td { border-bottom: none; }
.text-right { text-align: right; }

/* Expiring dialog */
.dlg-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 14px;
  line-height: 1.6;
}
.dlg-updated {
  font-size: 12px;
  color: #909399;
  text-align: right;
  margin-top: 8px;
}
</style>
