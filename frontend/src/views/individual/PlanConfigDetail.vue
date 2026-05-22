<template>
  <div class="plan-config-detail">
    <div class="page-header">
      <div>
        <h2>{{ t('individualPlan.detailTitle') }}</h2>
        <p class="page-subtitle">{{ t('individualPlan.subtitle') }}</p>
      </div>
    </div>

    <div class="top-actions" v-if="info">
      <el-button
        v-if="info.status === 'AVAILABLE'"
        :icon="Promotion"
        type="primary"
        plain
        @click="requestApplyVisible = true"
      >
        {{ t('agency.btnRequestApply') }}
      </el-button>
      <el-button :icon="Remove" @click="deactivateVisible = true">{{ t('common.deactivate') }}</el-button>
    </div>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <div class="section-card" v-loading="loading">
      <div class="section-header">
        <span class="section-title">{{ t('agency.planSection') }}</span>
        <!--
          <el-icon class="chevron" :class="{ rotated: !openSections.has('info') }"><ArrowDown /></el-icon>
        -->
      </div>
      <div v-show="openSections.has('info')" class="info-grid" v-if="info">
        <div class="info-row">
          <div class="info-item">
            <span class="text-regular">{{ t('approvals.planNameLabel') }}</span>
            <span class="text-regular ">{{ info.name }}</span>
          </div>
          <div class="info-item">
            <span class="text-regular">{{ t('agency.statusField') }}</span>
            <span class="text-regular">{{ statusLabel(info.status as any) }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="text-regular">{{ t('approvals.applyFromLabel') }}</span>
            <span class="text-regular">{{ info.applyFrom ?? '' }}</span>
          </div>
          <div class="info-item">
            <span class="text-regular">{{ t('approvals.applyToLabel') }}</span>
            <span class="text-regular">{{ info.applyUntil ?? '' }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item info-item--full">
            <span class="text-regular">{{ t('individualPlan.applyHistory') }}</span>
            <span class="text-primary">{{ info.applyHistory }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="text-regular">{{ t('agency.createdByField') }}</span>
            <span class="text-regular">{{ info.createdBy }}</span>
          </div>
          <div class="info-item">
            <span class="text-regular">{{ t('agency.createdAtField') }}</span>
            <span class="text-regular">{{ info.createdAt }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="text-regular">{{ t('agency.updatedAtField') }}</span>
            <span class="text-regular">{{ info.updatedAt }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <div class="section-card">
      <div class="section-header" @click="toggleSection('config')">
        <span class="section-title">{{ t('agency.configSection') }}</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('config') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('config')">
        <div class="category-tabs">
          <span class="tab-prefix-label">{{ t('individualPlan.category') }}</span>
          <div class="cat-tab-group">
            <button
              v-for="tab in TABS"
              :key="tab.key"
              :class="['cat-tab', { 'is-active': activeTab === tab.key, 'is-done': isTabCompleted(tab.key) }]"
              @click="activeTab = tab.key"
              type="button"
            >
              <el-icon v-if="isTabCompleted(tab.key)" class="tab-done-icon"><CircleCheck /></el-icon>
              {{ tab.label }}
            </button>
          </div>
        </div>

        <el-table :data="currentConfigRows" border style="margin-top: 12px" table-layout="fixed">
          <el-table-column type="index" width="100" :index="(i: number) => i + 1" header-align="center" align="center">
            <template #header>
              <span>#</span>
            </template>
          </el-table-column>

          <el-table-column prop="subject" sortable width="200" header-align="left">
            <template #header>
              <span>{{ t('individualPlan.colSubjectType') }}</span>
            </template>
            <template #default>{{ subjectLabel(activeTab) }}</template>
          </el-table-column>

          <el-table-column prop="durationMonths" sortable width="180" align="right" header-align="left">
            <template #header>
              <span>{{ t('individualPlan.durationHeader1') }}</span><br /><span>{{ t('individualPlan.durationHeader2') }}</span>
            </template>
            <template #default="{ row }">{{ row.durationMonths }} {{ t('agency.monthUnit') }}</template>
          </el-table-column>

          <el-table-column prop="condition" sortable width="180" align="center" header-align="left">
            <template #header>
              <span>{{ t('agency.colCondition') }}</span>
            </template>
            <template #default="{ row }">{{ conditionLabel(row.condition) }}</template>
          </el-table-column>

          <el-table-column prop="minValue" sortable width="180" align="right" header-align="left">
            <template #header>
              <span>{{ t('individualPlan.minHeader') }}</span><br /><span>{{ t('individualPlan.conditionHeader') }}</span>
            </template>
            <template #default="{ row }">{{ row.minValue }}</template>
          </el-table-column>

          <el-table-column prop="maxValue" sortable width="180" align="right" header-align="left">
            <template #header>
              <span>{{ t('individualPlan.maxHeader') }}</span><br /><span>{{ t('individualPlan.conditionHeader') }}</span>
            </template>
            <template #default="{ row }">
              {{ row.maxValue != null ? row.maxValue : t('agency.unlimited') }}
            </template>
          </el-table-column>

          <el-table-column prop="fee" sortable width="200" align="right" header-align="left">
            <template #header>
              <span>{{ t('agency.colFeePerCondition') }}</span>
            </template>
            <template #default="{ row }">{{ formatFee(row.fee) }} {{ t('agency.vnd') }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- LỊCH SỬ CẬP NHẬT TRẠNG THÁI -->
    <div class="section-card">
      <div class="section-header" @click="toggleSection('history')">
        <span class="section-title">{{ t('individualPlan.statusHistory') }}</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('history') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('history')">
        <el-table :data="statusHistory" border>
          <el-table-column type="index" width="60" :index="(i: number) => i + 1" header-align="center" align="center">
            <template #header>
              <div class="col-label">#</div>
              <!--
                <div class="col-filter">
                  <el-button link :icon="Refresh" @click="historyPage = 1" />
                </div>

              -->
            </template>
          </el-table-column>

          <el-table-column prop="status" sortable width="200">
            <template #header>
              <div class="col-label">{{ t('common.status') }}</div>
            </template>
            <template #default="{ row }">{{ statusLabel(row.status) }}</template>
          </el-table-column>

          <el-table-column prop="updatedAt" sortable width="200">
            <template #header>
              <div class="col-label">{{ t('agency.updatedAtField') }}</div>
            </template>
          </el-table-column>

          <el-table-column prop="updatedBy" sortable width="200">
            <template #header>
              <div class="col-label">{{ t('individualPlan.updatedBy') }}</div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Dialog 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC -->
    <el-dialog v-model="requestApplyVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">{{ t('agency.dialogRequestApply') }}</span>
      </template>
      <div class="dlg-name-row">
        <span>{{ t('common.name') }}: {{ info?.name }}</span>
      </div>
      <el-form label-width="140px" style="margin-top: 16px">
        <el-form-item :label="t('agency.dialogApplyPeriod')">
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            range-separator="-"
            :start-placeholder="t('agency.dateFrom')"
            :end-placeholder="t('agency.dateTo')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="t('agency.dialogApprovalLevel')">
          <el-select v-model="requestApprovalLevel" style="width: 100%">
            <el-option :label="t('agency.dialogSalesManager')" :value="1" />
            <el-option :label="t('agency.dialogCFO')" :value="2" />
            <el-option :label="t('agency.dialogCEO')" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="dlg-note">
        {{ t('individualPlan.requestApplyMultiLevelNote') }}
      </p>
      <template #footer>
        <el-button type="primary" :loading="requestApplyLoading" @click="confirmRequestApply">{{ t('common.confirm') }}</el-button>
        <el-button @click="requestApplyVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Approval success -->
    <el-dialog v-model="approvalSuccessVisible" :title="t('agency.approvalSuccessTitle')" width="440px" align-center>
      <div style="text-align:center; padding: 8px 0 16px">
        <el-icon style="font-size:48px; color:#67c23a"><CircleCheck /></el-icon>
        <p style="margin:12px 0 4px; font-size:15px; font-weight:600; color:#303133">
          {{ t('individualPlan.requestApplySent') }}
        </p>
        <p style="font-size:13px; color:#606266; margin:0">
          {{ t('agency.approvalSuccessDesc1') }}<br />
          {{ t('agency.approvalSuccessDesc2') }}
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToApproval">{{ t('agency.viewApprovalProgress') }}</el-button>
        <el-button @click="approvalSuccessVisible = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog 4: VÔ HIỆU HOÁ -->
    <el-dialog v-model="deactivateVisible" width="600px" height="242px" align-center>
      <template #header>
        <span class="dlg-title">{{ t('agency.dialogDisableTitle') }}</span>
      </template>
      <p class="dlg-body" v-html="t('individualPlan.deactivateDesc', { name: info?.name })"></p>
      <template #footer>
        <el-button type="primary" @click="confirmDeactivate">{{ t('common.confirm') }}</el-button>
        <el-button @click="deactivateVisible = false" class="btn-cancel">
          {{ t('individualPlan.cancel') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Promotion, Remove, ArrowDown, Refresh, CircleCheck } from '@element-plus/icons-vue'
// CircleCheck already imported above — used in approval success dialog
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getIndividualPlanConfigDetail,
  requestApplyPlanConfig,
  deactivatePlanConfig,
} from '@/api/individual'
import type { IndividualPlanConfigDetail, IndividualPricingRuleRow, IndividualStatusHistoryRow } from '@/types/individual'

type TabKey = 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
type PlanStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'PENDING' | 'APPROVED' | 'APPLYING'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const planId = Number(route.params.id)
const loading = ref(false)
const openSections = ref<Set<string>>(new Set(['info', 'config', 'history']))
const activeTab = ref<TabKey>('INDIVIDUAL')
const historyPage = ref(1)

const TABS: { key: TabKey; label: string }[] = [
  { key: 'INDIVIDUAL', label: t('approvals.subjectIndividual') },
  { key: 'ORGANIZATION', label: t('approvals.subjectOrganization') },
  { key: 'INDIVIDUAL_OF_ORG', label: t('approvals.subjectIndividualOfOrg') },
]

// Dialog state
const requestApplyVisible = ref(false)
const requestApplyDateRange = ref<[Date, Date] | null>(null)
const requestApprovalLevel = ref<1 | 2 | 3>(1)
const requestApplyLoading = ref(false)
const approvalSuccessVisible = ref(false)
const lastApprovalId = ref<number | null>(null)
const deactivateVisible = ref(false)

const info = ref<IndividualPlanConfigDetail | null>(null)

const tabData = reactive<Record<TabKey, IndividualPricingRuleRow[]>>({
  INDIVIDUAL: [],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
})

const statusHistory = ref<IndividualStatusHistoryRow[]>([])

const currentConfigRows = computed(() => tabData[activeTab.value])

function toggleSection(key: string) {
  const s = new Set(openSections.value)
  s.has(key) ? s.delete(key) : s.add(key)
  openSections.value = s
}

function statusLabel(status: PlanStatus): string {
  const map: Record<PlanStatus, string> = {
    AVAILABLE: t('individualPlan.statusAvailable'),
    UNAVAILABLE: t('individualPlan.statusUnavailable'),
    PENDING: t('individualPlan.statusPending'),
    APPROVED: t('individualPlan.statusApproved'),
    APPLYING: t('individualPlan.statusApplying'),
  }
  return map[status] ?? status
}

function subjectLabel(tab: TabKey): string {
  const map: Record<TabKey, string> = {
    INDIVIDUAL: t('approvals.subjectIndividual'),
    ORGANIZATION: t('approvals.subjectOrganization'),
    INDIVIDUAL_OF_ORG: t('approvals.subjectIndividualOfOrg'),
  }
  return map[tab]
}

function isTabCompleted(tab: TabKey): boolean {
  return tabData[tab].length > 0
}

function conditionLabel(condition: string): string {
  if (condition === 'SIGNING_COUNT') return t('agency.conditionSigning')
  if (condition === 'CERTIFICATE_COUNT') return t('agency.conditionCertificate')
  return condition
}

function formatFee(fee: number): string {
  return fee.toLocaleString(locale.value === 'vi' ? 'vi-VN' : 'en-US')
}

async function load() {
  loading.value = true
  try {
    const res = await getIndividualPlanConfigDetail(planId)
    if (res.success && res.data) {
      info.value = res.data

      tabData.INDIVIDUAL = res.data.pricingRules.filter(r => r.subject === 'INDIVIDUAL')
      tabData.ORGANIZATION = res.data.pricingRules.filter(r => r.subject === 'ORGANIZATION')
      tabData.INDIVIDUAL_OF_ORG = res.data.pricingRules.filter(r => r.subject === 'INDIVIDUAL_OF_ORG')

      statusHistory.value = res.data.statusHistory ?? []
    }
  } catch {
    ElMessage.error(t('individualPlan.loadDetailError'))
  } finally {
    loading.value = false
  }
}

async function confirmRequestApply() {
  if (!requestApplyDateRange.value) {
    ElMessage.warning(t('individualPlan.warningApplyPeriod'))
    return
  }
  const [from, to] = requestApplyDateRange.value
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  requestApplyLoading.value = true
  try {
    const res = await requestApplyPlanConfig(planId, {
      applyFrom: fmt(from),
      applyUntil: fmt(to),
      approvalLevel: requestApprovalLevel.value,
    })
    if (res.data?.approvalRequestId) {
      lastApprovalId.value = res.data.approvalRequestId
    }
    requestApplyVisible.value = false
    approvalSuccessVisible.value = true
    load()
  } catch {
    ElMessage.error(t('individualPlan.requestApplyFailed'))
  } finally {
    requestApplyLoading.value = false
  }
}

function goToApproval() {
  approvalSuccessVisible.value = false
  router.push(lastApprovalId.value ? `/approvals/${lastApprovalId.value}` : '/approvals')
}

async function confirmDeactivate() {
  try {
    await deactivatePlanConfig(planId)
    ElMessage.success(t('individualPlan.deactivateSuccess'))
    deactivateVisible.value = false
    router.push('/individual-plan-config')
  } catch {
    ElMessage.error(t('individualPlan.deactivateFailed'))
  }
}

onMounted(load)
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
.page-header h2 { margin: 0; }
.page-subtitle { margin: 4px 0 0; color: #909399; font-size: 13px; }

.top-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.section-card {
  background: #fff;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0px 3px 12px 0px #2F2B3D24;
  padding: 14px 22px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  margin-bottom: 0.5rem;
}
.section-header:hover { background: #fafafa; }

.section-title {
  font-size: 18px;
  line-height: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.chevron {
  color: var(--el-color-primary);
  font-size: 14px;
  transition: transform 0.2s;
}
.chevron.rotated { transform: rotate(-90deg); }

/* Info grid */
.info-row {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}
.info-item {
  flex: 1;
  display: flex;
  gap: 4px;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}
.info-item--full { flex: 2; }
.info-label { color: #303133; white-space: nowrap; }

/* Tabs */
.category-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 0;
  margin: 0 0 0;
}
.tab-prefix-label {
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
  padding-left: 0;
}
.cat-tab-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 18px;
  font-size: 14px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  user-select: none;
  transition: border-color 0.15s, background 0.15s, color 0.15s;
  line-height: 1.4;
  outline: none;
  white-space: nowrap;
}
.cat-tab.is-active {
  border-color: #409eff;
  color: #409eff;
  font-weight: 600;
  background: #fff;
}
.cat-tab.is-done {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}
.cat-tab.is-done.is-active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}
.tab-done-icon {
  font-size: 14px;
  color: #409eff;
  flex-shrink: 0;
}

/* Column filter row pattern */
:deep(.el-table th.el-table__cell) {
  color: #606266;
  font-size: 12px;
  font-weight: 600;
}
.col-label { font-weight: 500; font-size: 13px; white-space: normal; line-height: 1.3; text-transform: uppercase; min-height: 56px; display: flex; align-items: center; }
.col-filter { min-height: 28px; }

:deep(.el-table th.el-table__cell) { vertical-align: top; padding: 8px 0; }
:deep(.el-table td.el-table__cell) { padding: 8px 0; }

/* Dialog styles */
:deep(.el-dialog__header) { padding-bottom: 1.5rem; }
.dlg-title { font-weight: 500; font-size: 1.125rem; color: var(--el-text-color-primary); text-transform: uppercase;}
.dlg-name-row { font-size: 17px; color: var(--el-text-color-regular); }
.dlg-body { font-size: 18px; color: var(--el-text-color-primary); font-weight: 500; line-height: 28px; margin: 0; }
.dlg-note { font-size: 13px; color: #606266; line-height: 1.6; margin: 12px 0 0; }
</style>
