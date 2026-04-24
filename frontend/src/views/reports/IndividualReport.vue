<template>
  <div>
    <!-- Stat cards -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <!-- Card 1 -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background:#e8f0fd;color:#1B60CB">
              <el-icon size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ data.activeCustomers }}</div>
              <div class="stat-label">SL khách hàng có CTS đang hoạt động</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 2 -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background:#e8f0fd;color:#1B60CB">
              <el-icon size="28"><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ data.newCts }}</div>
              <div class="stat-label">SL CTS mới được tạo trong tháng này</div>
              <div class="stat-growth positive">+18% <span class="growth-note">so với tháng trước</span></div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 3 -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" style="background:#f0ebfd;color:#7c3aed">
              <el-icon size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ data.signings.toLocaleString('vi-VN') }}</div>
              <div class="stat-label">SL lượt ký trong tháng này</div>
              <div class="stat-growth negative">-3% <span class="growth-note">so với tháng trước</span></div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Card 4: uploads + donut -->
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner" style="align-items:center">
            <div class="stat-info">
              <div class="stat-value">{{ data.uploads.toLocaleString('vi-VN') }}</div>
              <div class="stat-label">Tài liệu đã được tải lên trong tháng này</div>
            </div>
            <div class="donut-wrap">
              <div ref="donutRef" style="width:72px;height:72px" />
              <div class="donut-label">{{ data.uploadPct }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Month selector -->
    <div class="month-bar">
      <el-select v-model="selectedMonth" size="small" style="width:140px">
        <el-option v-for="m in monthOptions" :key="m.value" :label="m.label" :value="m.value" />
      </el-select>
    </div>

    <!-- Charts row 1 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <!-- SL khách hàng mới -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">SL khách hàng mới trong tháng</span>
          </template>
          <div ref="newCustChartRef" style="height:260px" />
        </el-card>
      </el-col>

      <!-- SL CTS được tạo -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">SL chứng thư số được tạo</span>
              <el-select v-model="filterCts" size="small" style="width:100px" @change="renderCtsChart">
                <el-option label="Tất Cả" value="" />
                <el-option label="Cá nhân" value="INDIVIDUAL" />
                <el-option label="Tổ chức" value="ORGANIZATION" />
                <el-option label="CN thuộc TC" value="INDIVIDUAL_OF_ORG" />
              </el-select>
            </div>
          </template>
          <div ref="ctsChartRef" style="height:260px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts row 2 -->
    <el-row :gutter="16">
      <!-- SL lượt ký -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">SL lượt ký trong tháng</span>
              <el-select v-model="filterSigning" size="small" style="width:100px" @change="renderSigningChart">
                <el-option label="Tất Cả" value="" />
                <el-option label="Cá nhân" value="INDIVIDUAL" />
                <el-option label="Tổ chức" value="ORGANIZATION" />
                <el-option label="CN thuộc TC" value="INDIVIDUAL_OF_ORG" />
              </el-select>
            </div>
          </template>
          <div ref="signingChartRef" style="height:260px" />
        </el-card>
      </el-col>

      <!-- Tỉ lệ xác thực thất bại -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">Tỉ lệ (%) xác thực thất bại khi ký</span>
              <el-select v-model="filterFailure" size="small" style="width:100px">
                <el-option label="Tất Cả" value="" />
                <el-option label="Cá nhân" value="INDIVIDUAL" />
                <el-option label="Tổ chức" value="ORGANIZATION" />
              </el-select>
            </div>
          </template>
          <div ref="failureChartRef" style="height:260px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { UserFilled, DocumentChecked, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const selectedMonth = ref('2026-03')
const filterCts = ref('')
const filterSigning = ref('')
const filterFailure = ref('')

const donutRef = ref<HTMLElement>()
const newCustChartRef = ref<HTMLElement>()
const ctsChartRef = ref<HTMLElement>()
const signingChartRef = ref<HTMLElement>()
const failureChartRef = ref<HTMLElement>()

let donutChart: echarts.ECharts | null = null
let newCustChart: echarts.ECharts | null = null
let ctsChart: echarts.ECharts | null = null
let signingChart: echarts.ECharts | null = null
let failureChart: echarts.ECharts | null = null

const monthOptions = [
  { label: 'Tháng 1/2026', value: '2026-01' },
  { label: 'Tháng 2/2026', value: '2026-02' },
  { label: 'Tháng 3/2026', value: '2026-03' },
  { label: 'Tháng 4/2026', value: '2026-04' },
]

const data = ref({
  activeCustomers: 16,
  newCts: 8,
  signings: 1200,
  uploads: 1250,
  uploadPct: 96,
})

const weeks = ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4']

const COLORS = {
  individual:      '#1B60CB',
  organization:    '#5B9BD5',
  individualOfOrg: '#D4E6F1',
  pin:  '#1B60CB',
  otp:  '#F59E0B',
  moc:  '#FDBA74',
}

function renderDonut() {
  if (!donutRef.value) return
  if (!donutChart) donutChart = echarts.init(donutRef.value)
  donutChart.setOption({
    series: [{
      type: 'pie',
      radius: ['68%', '100%'],
      startAngle: 90,
      data: [
        { value: data.value.uploadPct, itemStyle: { color: '#1B60CB' } },
        { value: 100 - data.value.uploadPct, itemStyle: { color: '#E4E7ED' } },
      ],
      label: { show: false },
      emphasis: { scale: false },
    }],
  }, true)
}

function renderNewCustChart() {
  if (!newCustChartRef.value) return
  if (!newCustChart) newCustChart = echarts.init(newCustChartRef.value)
  const values = [12, 3, 5, 4]
  newCustChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 16, right: 16, bottom: 16, top: 24, containLabel: true },
    xAxis: { type: 'category', data: weeks, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: { color: '#1B60CB' },
      barMaxWidth: 48,
      label: { show: true, position: 'top', fontSize: 11, color: '#606266' },
    }],
  }, true)
}

function renderCtsChart() {
  if (!ctsChartRef.value) return
  if (!ctsChart) ctsChart = echarts.init(ctsChartRef.value)

  const individual =      [23, 1, 3, 7]
  const organization =    [0,  0, 0, 2]
  const individualOfOrg = [0,  0, 0, 0]

  const visibleSeries = []
  if (!filterCts.value || filterCts.value === 'INDIVIDUAL')
    visibleSeries.push({ name: 'Cá nhân', type: 'bar', stack: 'total', data: individual, itemStyle: { color: COLORS.individual }, barMaxWidth: 48, label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: (p: any) => p.value > 0 ? p.value : '' } })
  if (!filterCts.value || filterCts.value === 'ORGANIZATION')
    visibleSeries.push({ name: 'Tổ chức', type: 'bar', stack: 'total', data: organization, itemStyle: { color: COLORS.organization }, barMaxWidth: 48, label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: (p: any) => p.value > 0 ? p.value : '' } })
  if (!filterCts.value || filterCts.value === 'INDIVIDUAL_OF_ORG')
    visibleSeries.push({ name: 'Cá nhân thuộc tổ chức', type: 'bar', stack: 'total', data: individualOfOrg, itemStyle: { color: COLORS.individualOfOrg }, barMaxWidth: 48 })

  ctsChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 11 } },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'category', data: weeks, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: visibleSeries,
  }, true)
}

function renderSigningChart() {
  if (!signingChartRef.value) return
  if (!signingChart) signingChart = echarts.init(signingChartRef.value)

  const individual =      [50,  60,  210, 100]
  const organization =    [0,   0,   50,  11]
  const individualOfOrg = [0,   0,   30,  15]

  const visibleSeries = []
  if (!filterSigning.value || filterSigning.value === 'INDIVIDUAL')
    visibleSeries.push({ name: 'Cá nhân', type: 'bar', stack: 'total', data: individual, itemStyle: { color: COLORS.individual }, barMaxWidth: 48, label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: (p: any) => p.value > 0 ? p.value : '' } })
  if (!filterSigning.value || filterSigning.value === 'ORGANIZATION')
    visibleSeries.push({ name: 'Tổ chức', type: 'bar', stack: 'total', data: organization, itemStyle: { color: COLORS.organization }, barMaxWidth: 48, label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: (p: any) => p.value > 0 ? p.value : '' } })
  if (!filterSigning.value || filterSigning.value === 'INDIVIDUAL_OF_ORG')
    visibleSeries.push({ name: 'Cá nhân thuộc tổ chức', type: 'bar', stack: 'total', data: individualOfOrg, itemStyle: { color: COLORS.individualOfOrg }, barMaxWidth: 48, label: { show: true, position: 'inside', color: '#606266', fontSize: 11, formatter: (p: any) => p.value > 0 ? p.value : '' } })

  signingChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 11 } },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'category', data: weeks, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: visibleSeries,
  }, true)
}

function renderFailureChart() {
  if (!failureChartRef.value) return
  if (!failureChart) failureChart = echarts.init(failureChartRef.value)

  const pinData = [21, 14, 23, 5]
  const otpData = [45, 40, 57, 37]
  const mocData = [14, 14, 15, 15]

  failureChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        return params.map((p: any) => `${p.seriesName}: thất bại ${p.value}%`).join('<br/>')
      },
    },
    legend: {
      bottom: 0,
      data: ['Mã PIN', 'OTP', 'MoC'],
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 11 },
    },
    grid: { left: 16, right: 16, bottom: 48, top: 8, containLabel: true },
    xAxis: { type: 'category', data: weeks, axisLabel: { fontSize: 12 } },
    yAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { formatter: (v: number) => v },
    },
    series: [
      {
        name: 'Mã PIN',
        type: 'bar',
        data: pinData,
        itemStyle: { color: COLORS.pin },
        barMaxWidth: 28,
        label: { show: true, position: 'top', fontSize: 11, color: '#606266' },
      },
      {
        name: 'OTP',
        type: 'bar',
        data: otpData,
        itemStyle: { color: COLORS.otp },
        barMaxWidth: 28,
        label: { show: true, position: 'top', fontSize: 11, color: '#606266' },
      },
      {
        name: 'MoC',
        type: 'bar',
        data: mocData,
        itemStyle: { color: COLORS.moc },
        barMaxWidth: 28,
        label: { show: true, position: 'top', fontSize: 11, color: '#606266' },
      },
    ],
  }, true)
}

function handleResize() {
  donutChart?.resize()
  newCustChart?.resize()
  ctsChart?.resize()
  signingChart?.resize()
  failureChart?.resize()
}

onMounted(async () => {
  await nextTick()
  renderDonut()
  renderNewCustChart()
  renderCtsChart()
  renderSigningChart()
  renderFailureChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  donutChart?.dispose()
  newCustChart?.dispose()
  ctsChart?.dispose()
  signingChart?.dispose()
  failureChart?.dispose()
})
</script>

<style scoped>
.stat-card { height: 100%; }
.stat-inner { display: flex; align-items: flex-start; gap: 12px; }
.stat-icon { width: 52px; height: 52px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.4; }
.stat-growth { font-size: 12px; margin-top: 4px; font-weight: 500; }
.stat-growth.positive { color: #67c23a; }
.stat-growth.negative { color: #f56c6c; }
.growth-note { color: #909399; font-weight: 400; }

.donut-wrap { position: relative; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.donut-label { position: absolute; font-size: 12px; font-weight: 700; color: #303133; pointer-events: none; }

.month-bar { display: flex; justify-content: flex-end; margin-bottom: 12px; }

.chart-header { display: flex; justify-content: space-between; align-items: center; }
.chart-title { font-size: 14px; font-weight: 600; color: #303133; }
</style>
