<template>
  <div class="reports-page">
    <!-- Header -->
    <div class="page-header">
      <h2>DASHBOARD - BÁO CÁO THỐNG KÊ</h2>
      <div class="type-switcher">
        <span class="switcher-label">Khách hàng</span>
        <button v-if="canViewGroup"      class="type-btn" :class="{ active: selectedType === 'GROUP' }"      @click="setType('GROUP')">Đại lý</button>
        <button v-if="canViewIndividual" class="type-btn" :class="{ active: selectedType === 'INDIVIDUAL' }" @click="setType('INDIVIDUAL')">Phổ thông</button>
      </div>
    </div>

    <!-- Individual customer dashboard -->
    <IndividualReport v-if="selectedType === 'INDIVIDUAL'" />

    <!-- Không có quyền xem tab nào -->
    <el-empty v-if="!canViewGroup && !canViewIndividual" description="Bạn không có quyền xem báo cáo" />

    <!-- Stat cards -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16" style="margin-bottom: 20px" v-loading="loading">
      <!-- Card 1: Active partners -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background: #e8f0fd; color: #1B60CB">
              <el-icon size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ report?.stats.activePartners ?? 0 }}</div>
              <div class="stat-label">SL đại lý đang hoạt động</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 2: New CTS this month -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background: #e8f0fd; color: #1B60CB">
              <el-icon size="28"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (report?.stats.newCts ?? 0).toLocaleString('vi-VN') }}</div>
              <div class="stat-label">SL CTS mới được tạo trong tháng này</div>
              <div class="stat-growth" :class="(report?.stats.newCtsPct ?? 0) >= 0 ? 'positive' : 'negative'">
                {{ (report?.stats.newCtsPct ?? 0) >= 0 ? '+' : '' }}{{ report?.stats.newCtsPct ?? 0 }}%
                <span class="growth-note">so với tháng trước</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 3: Signings this month -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background: #f0ebfd; color: #7c3aed">
              <el-icon size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (report?.stats.signings ?? 0).toLocaleString('vi-VN') }}</div>
              <div class="stat-label">SL lượt ký trong tháng này</div>
              <div class="stat-growth" :class="(report?.stats.signingsPct ?? 0) >= 0 ? 'positive' : 'negative'">
                {{ (report?.stats.signingsPct ?? 0) >= 0 ? '+' : '' }}{{ report?.stats.signingsPct ?? 0 }}%
                <span class="growth-note">so với tháng trước</span>
              </div>
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
              <div class="stat-value" style="color: #F56C6C">{{ report?.stats.expiringSoon ?? 0 }}</div>
              <div class="stat-label">
                đại lý có gói cước hiệu lực dưới 3 tháng (không có gói tiếp).
                <el-button link type="primary" style="padding: 0; font-size: 12px" @click="openExpiringDialog">Xem chi tiết</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Month selector -->
    <div v-if="selectedType === 'GROUP'" class="month-bar">
      <el-select v-model="selectedMonth" size="small" style="width: 140px" @change="loadReport">
        <el-option v-for="m in monthOptions" :key="m.value" :label="m.label" :value="m.value" />
      </el-select>
    </div>

    <!-- Charts row 1 -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16" style="margin-bottom: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">SL chứng thư số được tạo</span></template>
          <div ref="certChartRef" style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">SL lượt ký theo tháng</span></template>
          <div ref="signingChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Expiring soon dialog -->
    <el-dialog
      v-model="expiringDialogVisible"
      title="GÓI CƯỚC SẮP HẾT HẠN"
      width="680px"
      :close-on-click-modal="false"
    >
      <p class="dlg-desc">
        (Các) Đại lý dưới đây có gói cước sẽ hết hiệu lực trong vòng 3 tháng và chưa có gói cước kế tiếp.
        Vui lòng tạo gói cước mới để đảm bảo sử dụng dịch vụ không bị gián đoạn.
      </p>
      <el-table :data="expiringRows" border style="width: 100%" size="small">
        <el-table-column type="index" label="#" width="48" align="center" />
        <el-table-column prop="code" label="MÃ ĐẠI LÝ" sortable min-width="110" />
        <el-table-column prop="name" label="TÊN ĐẠI LÝ" sortable min-width="160" />
        <el-table-column prop="plan" label="GÓI CƯỚC HIỆN TẠI" min-width="150" />
        <el-table-column prop="expiry" label="ÁP DỤNG ĐẾN" sortable width="130" align="center" />
        <el-table-column label="HÀNH ĐỘNG" width="100" align="center">
          <template #default>
            <el-button size="small" plain round>
              <el-icon><View /></el-icon> Chi tiết
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="dlg-updated">Thời gian cập nhật: {{ report?.lastUpdated ?? '' }}</div>
      <template #footer>
        <el-button @click="expiringDialogVisible = false">Đóng</el-button>
      </template>
    </el-dialog>

    <!-- Charts row 2 -->
    <el-row v-if="selectedType === 'GROUP'" :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">Tỉ lệ lượt ký/ 1 CTS</span></template>
          <table class="ratio-table">
            <thead>
              <tr>
                <th class="col-agency">ĐẠI LÝ</th>
                <th class="col-metric">CÁ NHÂN</th>
                <th class="col-metric">TỔ CHỨC</th>
                <th class="col-metric">CN THUỘC TC</th>
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
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="chart-title">Tỉ lệ tăng trưởng SL lượt ký với tháng trước đó</span></template>
          <div ref="growthChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { UserFilled, DocumentChecked, DataAnalysis, Warning, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import IndividualReport from './IndividualReport.vue'
import { getGroupReport } from '@/api/reports'
import type { GroupReportResponse } from '@/api/reports'
import { usePermission } from '@/composables/usePermission'

type CustomerType = 'GROUP' | 'INDIVIDUAL'

const { can } = usePermission()
const canViewGroup      = computed(() => can('report:group:view'))
const canViewIndividual = computed(() => can('report:individual:view'))

// Auto-select tab đầu tiên có quyền
const defaultTab: CustomerType = (() => {
  if (can('report:group:view')) return 'GROUP'
  if (can('report:individual:view')) return 'INDIVIDUAL'
  return 'GROUP'
})()

const selectedType = ref<CustomerType>(defaultTab)
const selectedMonth = ref('2026-03')
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

const monthOptions = [
  { label: 'Tháng 1/2026', value: '2026-01' },
  { label: 'Tháng 2/2026', value: '2026-02' },
  { label: 'Tháng 3/2026', value: '2026-03' },
  { label: 'Tháng 4/2026', value: '2026-04' },
]

async function loadReport() {
  loading.value = true
  try {
    const res = await getGroupReport(selectedMonth.value)
    report.value = res.data
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function openExpiringDialog() {
  expiringDialogVisible.value = true
}

function setType(type: CustomerType) {
  if (type === 'GROUP' && !canViewGroup.value) return
  if (type === 'INDIVIDUAL' && !canViewIndividual.value) return
  selectedType.value = type
  if (type === 'GROUP') {
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

function renderCertChart() {
  if (!certChartRef.value || !report.value) return
  if (!certChart) certChart = echarts.init(certChartRef.value)
  const { agencies, certData } = report.value
  certChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      bottom: 0,
      data: ['Cá nhân', 'Tổ chức', 'Cá nhân thuộc tổ chức'],
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12 },
    },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: agencies, inverse: true, axisLabel: { fontSize: 12 } },
    series: [
      { name: 'Cá nhân', type: 'bar', stack: 'total', data: certData.individual, itemStyle: { color: '#1B60CB' } },
      { name: 'Tổ chức', type: 'bar', stack: 'total', data: certData.organization, itemStyle: { color: '#5B9BD5' } },
      { name: 'Cá nhân thuộc tổ chức', type: 'bar', stack: 'total', data: certData.individualOfOrg, itemStyle: { color: '#D4E6F1' } },
    ],
  }, true)
}

function renderSigningChart() {
  if (!signingChartRef.value || !report.value) return
  if (!signingChart) signingChart = echarts.init(signingChartRef.value)
  const { agencies, signingData } = report.value
  signingChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 16, right: 32, bottom: 16, top: 8, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: { type: 'category', data: agencies, inverse: true, axisLabel: { fontSize: 12 } },
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
          formatter: (p: any) => (p.value > 0 ? p.value.toLocaleString('vi-VN') : ''),
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

  growthChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const idx = params[0].dataIndex
        const meta = growthData[idx]
        return [
          `SL lượt ký tháng này: ${meta.current.toLocaleString('vi-VN')}`,
          `SL lượt ký tháng trước: ${meta.prev.toLocaleString('vi-VN')}`,
          `Tỉ lệ tăng: ${meta.growth}%`,
        ].join('<br/>')
      },
    },
    legend: {
      bottom: 0,
      data: ['% Tăng trưởng'],
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 11 },
    },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'category', data: agencies, axisLabel: { fontSize: 12, interval: 0 } },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { formatter: (v: number) => v + '' },
    },
    series: [
      {
        name: '% Tăng trưởng = (Tháng hiện tại - Tháng trước) / Tháng trước × 100%',
        type: 'bar',
        data: growthValues.map(v => ({ value: v, itemStyle: { color: '#1B60CB' } })),
        label: { show: false },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#999', type: 'solid', width: 1 },
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

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #303133; }

.type-switcher { display: flex; align-items: center; gap: 8px; }
.switcher-label { font-size: 14px; color: #606266; }
.type-btn {
  padding: 4px 14px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  background: #fff;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}
.type-btn.active {
  background: #1B60CB;
  border-color: #1B60CB;
  color: #fff;
}

/* Stat cards */
.stat-card { height: 100%; }
.stat-inner { display: flex; align-items: flex-start; gap: 12px; }
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.4; }
.stat-growth { font-size: 12px; margin-top: 4px; font-weight: 500; }
.stat-growth.positive { color: #67c23a; }
.stat-growth.negative { color: #f56c6c; }
.growth-note { color: #909399; font-weight: 400; }

/* Month selector */
.month-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

/* Chart title */
.chart-title { font-size: 14px; font-weight: 600; color: #303133; }

/* Ratio table */
.ratio-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
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
