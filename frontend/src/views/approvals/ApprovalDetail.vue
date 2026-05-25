<template>
  <div class="approval-detail-page">
    <div class="page-header">
      <el-button class="back-btn" :icon="ArrowLeft" @click="router.back()" link>{{ t('common.back') }}</el-button>
      <h2>{{ t('approvals.detailTitle') }} <span class="req-id">#{{ requestId }}</span></h2>
      <p class="page-subtitle">
        {{ segmentLabel(data?.customerSegment) }} —
        <el-tag :type="statusType(data?.status)" size="small">{{ statusLabel(data?.status) }}</el-tag>
      </p>
    </div>

    <div v-loading="loading">
      <!-- Action bar -->
      <div class="action-bar" v-if="data">
        <!-- Sale: Submit DRAFT -->
        <el-button
          v-if="data.status === 'DRAFT'"
          type="primary"
          :icon="Promotion"
          @click="showSubmitDialog = true"
        >{{ t('approvals.submitForApproval') }}</el-button>

        <!-- Sale: Resubmit sau revision -->
        <el-button
          v-if="data.status === 'NEED_REVISION'"
          type="warning"
          :icon="Refresh"
          @click="showResubmitDialog = true"
        >{{ t('approvals.resubmitAfterRevision') }}</el-button>

        <!-- Approver: Approve / Reject / Revision (khi IN_APPROVAL) -->
        <template v-if="data.status === 'IN_APPROVAL'">
          <el-button type="success" :icon="CircleCheck" @click="showApproveDialog = true">{{ t('approvals.approve') }}</el-button>
          <el-button type="danger" :icon="CircleClose" @click="showRejectDialog = true">{{ t('approvals.reject') }}</el-button>
          <el-button type="warning" :icon="Edit" @click="showRevisionDialog = true">{{ t('approvals.requestRevision') }}</el-button>
        </template>
      </div>

      <!-- Thông tin chung -->
      <div class="section-card" v-if="data">
        <div class="section-header">
          <span class="section-title">{{ t('approvals.requestInfoSection') }}</span>
        </div>
        <div class="info-grid">
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.requestType') }}</span>
              <span class="info-value">{{ data.requestType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.customerTypeLabel') }}</span>
              <span class="info-value">{{ segmentLabel(data.customerSegment) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.createdByLabel') }}</span>
              <span class="info-value">{{ data.requestedBy }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.createdAtLabel') }}</span>
              <span class="info-value">{{ fmtDate(data.createdAt) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.contractValueLabel') }}</span>
              <span class="info-value">{{ data.contractValue ? formatAmount(data.contractValue) : '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.totalLevelsLabel') }}</span>
              <span class="info-value">{{ t('approvals.levelCount', { count: data.totalLevels }) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item info-item--full">
              <span class="info-label">{{ t('approvals.descriptionLabel') }}</span>
              <span class="info-value">{{ data.description }}</span>
            </div>
          </div>
          <div class="info-row" v-if="data.status === 'NEED_REVISION' && data.reviewNote">
            <div class="info-item info-item--full">
              <span class="info-label revision-label">{{ t('approvals.revisionReasonLabel') }}</span>
              <span class="info-value revision-note">{{ data.reviewNote }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Chi tiết gói cước -->
      <div class="section-card" v-if="entityDetail">
        <div class="section-header">
          <span class="section-title">{{ t('approvals.planDetailsSection') }}</span>
        </div>
        <div class="info-grid">
          <div class="info-row" v-if="entityDetail.groupName">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.agencyNameLabel') }}</span>
              <span class="info-value">{{ entityDetail.groupName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.agencyCodeLabel') }}</span>
              <span class="info-value">{{ entityDetail.groupCode }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.planNameLabel') }}</span>
              <span class="info-value plan-name">{{ entityDetail.planName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.planCodeLabel') }}</span>
              <span class="info-value">{{ entityDetail.planCode }}</span>
            </div>
          </div>
          <div class="info-row" v-if="entityDetail.applyFrom || entityDetail.applyTo">
            <div class="info-item">
              <span class="info-label">{{ t('approvals.applyFromLabel') }}</span>
              <span class="info-value">{{ entityDetail.applyFrom ?? '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ t('approvals.applyToLabel') }}</span>
              <span class="info-value">{{ entityDetail.applyTo ?? '—' }}</span>
            </div>
          </div>
        </div>

        <!-- Bảng cấu hình giá -->
        <div class="pricing-section" v-if="pricingRules.length">
          <div class="pricing-title">{{ t('approvals.pricingConfig') }}</div>
          <el-table :data="pricingRules" border size="small" class="pricing-table">
            <el-table-column :label="t('approvals.subject')" width="160">
              <template #default="{ row }">{{ subjectLabel(row.subjectType) }}</template>
            </el-table-column>
            <el-table-column :label="t('approvals.certValidity')" width="160">
              <template #default="{ row }">
                {{ row.certificateValidityValue }} {{ validityUnitLabel(row.certificateValidityUnit) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('approvals.pricingMetric')" width="140">
              <template #default="{ row }">{{ metricLabel(row.pricingMetric) }}</template>
            </el-table-column>
            <el-table-column :label="t('approvals.range')" width="130">
              <template #default="{ row }">
                {{ row.rangeMin }} — {{ row.rangeMax ?? '∞' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('approvals.unitPrice')" min-width="130" align="right">
              <template #default="{ row }">
                <span class="price-value">{{ formatAmount(row.unitPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('approvals.totalPrice')" min-width="140" align="right">
              <template #default="{ row }">
                <span v-if="row.totalPrice != null" class="price-value">{{ formatAmount(row.totalPrice) }}</span>
                <span v-else class="price-empty">—</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('approvals.quotaTotal')" width="110" align="right">
              <template #default="{ row }">{{ row.quotaTotal ?? '—' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Timeline duyệt -->
      <div class="section-card" v-if="data?.steps?.length">
        <div class="section-header">
          <span class="section-title">{{ t('approvals.approvalProgressSection') }}</span>
        </div>
        <div class="steps-container">
          <div
            v-for="step in data.steps"
            :key="step.id"
            class="step-row"
            :class="'step-row--' + step.status.toLowerCase()"
          >
            <div class="step-indicator">
              <el-icon v-if="step.status === 'APPROVED'" class="icon-approved"><CircleCheck /></el-icon>
              <el-icon v-else-if="step.status === 'REJECTED'" class="icon-rejected"><CircleClose /></el-icon>
              <el-icon v-else-if="step.status === 'SKIPPED'" class="icon-skipped"><Clock /></el-icon>
              <span v-else class="step-level-badge">{{ step.stepLevel }}</span>
            </div>
            <div class="step-body">
              <div class="step-header-row">
                <span class="step-level-name">{{ levelLabel(step.requiredApprovalLevel) }}</span>
                <el-tag :type="stepTagType(step.status)" size="small">{{ stepStatusLabel(step.status) }}</el-tag>
              </div>
              <div v-if="step.status === 'APPROVED' || step.status === 'REJECTED'" class="step-decision">
                <span class="step-actor">{{ step.decidedBy }}</span>
                <span class="step-dot">·</span>
                <span class="step-time">{{ fmtDate(step.decidedAt) }}</span>
              </div>
              <div v-else-if="step.status === 'PENDING'" class="step-decision step-decision--pending">
                {{ t('approvals.waitingApproval') }}
              </div>
              <div v-if="step.comment" class="step-comment">"{{ step.comment }}"</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog: Submit -->
    <el-dialog v-model="showSubmitDialog" :title="t('approvals.submitDialogTitle')" width="460px" align-center>
      <p class="dlg-body">
        {{ t('approvals.submitDialogDesc') }}
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item :label="t('approvals.contractValueVnd')">
          <el-input-number v-model="submitForm.contractValue" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <el-form label-width="150px" style="margin-top: 8px">
        <el-form-item :label="t('approvals.approvalLevel')">
          <el-select v-model="submitForm.approvalLevel" style="width: 100%">
            <el-option :label="t('approvals.level1')" :value="1" />
            <el-option :label="t('approvals.level2')" :value="2" />
            <el-option :label="t('approvals.level3')" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="actionLoading" @click="doSubmit">{{ t('approvals.confirmSubmit') }}</el-button>
        <el-button @click="showSubmitDialog = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Approve -->
    <el-dialog v-model="showApproveDialog" :title="t('approvals.approveDialogTitle')" width="460px" align-center>
      <p class="dlg-body">
        {{ t('approvals.approvingLevelPrefix') }} <b>{{ data?.currentLevel }}</b>/{{ data?.totalLevels }}
        ({{ data ? levelLabel(currentStepRole) : '' }}).
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item :label="t('approvals.note')">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" :placeholder="t('approvals.noteOptional')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="success" :loading="actionLoading" @click="doApprove">{{ t('approvals.approve') }}</el-button>
        <el-button @click="showApproveDialog = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Reject -->
    <el-dialog v-model="showRejectDialog" :title="t('approvals.rejectDialogTitle')" width="460px" align-center>
      <p class="dlg-body dlg-danger">
        {{ t('approvals.rejectDialogDesc') }}
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item :label="t('approvals.reason')" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" :placeholder="t('approvals.reasonRequired')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="actionLoading" @click="doReject">{{ t('approvals.confirmReject') }}</el-button>
        <el-button @click="showRejectDialog = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Revision -->
    <el-dialog v-model="showRevisionDialog" :title="t('approvals.revisionDialogTitle')" width="460px" align-center>
      <p class="dlg-body">
        {{ t('approvals.revisionDialogDesc') }}
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item :label="t('approvals.reason')" required>
          <el-input v-model="revisionForm.reason" type="textarea" :rows="3" :placeholder="t('approvals.revisionReasonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" :loading="actionLoading" @click="doRevision">{{ t('approvals.sendRequest') }}</el-button>
        <el-button @click="showRevisionDialog = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Resubmit -->
    <el-dialog v-model="showResubmitDialog" :title="t('approvals.resubmitDialogTitle')" width="460px" align-center>
      <p class="dlg-body">
        {{ t('approvals.resubmitDialogDesc') }}
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item :label="t('approvals.contractValueVnd')">
          <el-input-number v-model="resubmitForm.contractValue" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('approvals.approvalLevel')">
          <el-select v-model="resubmitForm.approvalLevel" style="width: 100%">
            <el-option :label="t('approvals.level1')" :value="1" />
            <el-option :label="t('approvals.level2')" :value="2" />
            <el-option :label="t('approvals.level3')" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" :loading="actionLoading" @click="doResubmit">{{ t('approvals.resubmit') }}</el-button>
        <el-button @click="showResubmitDialog = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeft, ArrowDown, Promotion, Refresh,
  CircleCheck, CircleClose, Edit, Clock,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getApproval,
  submitApproval,
  approveStep,
  rejectRequest,
  requestRevision,
  resubmitApproval,
  getRetailPlanSchedule,
} from '@/api/approvals'
import { getGroupAssignment } from '@/api/groups'
import { getPlanTemplate } from '@/api/planTemplates'
import type { PlanPricingRule } from '@/types/planTemplate'
import type {
  MultiLevelApprovalResponse,
  MultiApprovalStatus,
  CustomerSegment,
  ApprovalStepResponse,
} from '@/api/approvals'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const requestId = Number(route.params.id)

const loading = ref(false)
const actionLoading = ref(false)
const data = ref<MultiLevelApprovalResponse | null>(null)
const entityDetail = ref<{ planName: string; planCode: string; planTemplateId?: number; groupName?: string; groupCode?: string; applyFrom?: string; applyTo?: string } | null>(null)
const pricingRules = ref<PlanPricingRule[]>([])

// Dialogs
const showSubmitDialog = ref(false)
const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const showRevisionDialog = ref(false)
const showResubmitDialog = ref(false)

// Forms
const submitForm = ref({ contractValue: undefined as number | undefined, approvalLevel: 1 as 1 | 2 | 3 })
const approveForm = ref({ comment: '' })
const rejectForm = ref({ reason: '' })
const revisionForm = ref({ reason: '' })
const resubmitForm = ref({ contractValue: undefined as number | undefined, approvalLevel: 1 as 1 | 2 | 3 })

const activeStepIndex = computed(() => {
  if (!data.value) return 0
  if (data.value.status === 'APPROVED') return data.value.totalLevels
  if (data.value.status === 'DRAFT') return 0
  return (data.value.currentLevel ?? 1) - 1
})

const currentStepRole = computed(() => {
  if (!data.value?.steps) return 'LEVEL_1'
  const step = data.value.steps.find(s => s.stepLevel === data.value!.currentLevel)
  return step?.requiredApprovalLevel ?? 'LEVEL_1'
})

async function load() {
  loading.value = true
  try {
    const res = await getApproval(requestId)
    data.value = (res as any).data ?? res
    if (data.value?.contractValue) {
      submitForm.value.contractValue = data.value.contractValue
      resubmitForm.value.contractValue = data.value.contractValue
    }
    await loadEntityDetail(data.value)
  } catch {
    ElMessage.error(t('approvals.loadDetailError'))
  } finally {
    loading.value = false
  }
}

async function loadEntityDetail(approval: MultiLevelApprovalResponse | null) {
  if (!approval?.entityId) return
  const id = Number(approval.entityId)
  try {
    let planTemplateId: number | undefined
    if (approval.entityType === 'GROUP_PLAN_ASSIGNMENT') {
      const res = await getGroupAssignment(id)
      const d = (res as any).data ?? res
      planTemplateId = d.planTemplateId
      entityDetail.value = {
        planName: d.planName,
        planCode: d.planCode,
        planTemplateId: d.planTemplateId,
        groupName: d.groupName,
        groupCode: d.groupCode,
        applyFrom: d.applyFrom,
        applyTo: d.applyTo,
      }
    } else if (approval.entityType === 'RETAIL_PLAN_SCHEDULE') {
      const res = await getRetailPlanSchedule(id)
      const d = (res as any).data ?? res
      planTemplateId = d.planTemplateId
      entityDetail.value = {
        planName: d.planName,
        planCode: d.planCode,
        planTemplateId: d.planTemplateId,
        applyFrom: d.applyFrom,
        applyTo: d.applyTo,
      }
    }

    if (planTemplateId) {
      const tmplRes = await getPlanTemplate(planTemplateId)
      const tmpl = (tmplRes as any).data ?? tmplRes
      pricingRules.value = (tmpl.pricingRules ?? []).filter((r: PlanPricingRule) => r.isActive !== false)
    }
  } catch {
    // Optional details; keep the section hidden if this fetch fails.
  }
}

async function doSubmit() {
  actionLoading.value = true
  try {
    const res = await submitApproval(requestId, {
      submittedBy: '',
      contractValue: submitForm.value.contractValue,
      approvalLevel: submitForm.value.approvalLevel,
    })
    data.value = (res as any).data ?? res
    showSubmitDialog.value = false
    ElMessage.success(t('approvals.submitSuccess'))
  } catch {
    ElMessage.error(t('approvals.submitFailed'))
  } finally {
    actionLoading.value = false
  }
}

async function doApprove() {
  actionLoading.value = true
  try {
    const res = await approveStep(requestId, {
      approvedBy: '',
      comment: approveForm.value.comment,
    })
    data.value = (res as any).data ?? res
    showApproveDialog.value = false
    ElMessage.success(t('approvals.approveSuccess'))
  } catch {
    ElMessage.error(t('approvals.approveFailed'))
  } finally {
    actionLoading.value = false
  }
}

async function doReject() {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning(t('approvals.rejectReasonRequired'))
    return
  }
  actionLoading.value = true
  try {
    const res = await rejectRequest(requestId, {
      rejectedBy: '',
      reason: rejectForm.value.reason,
    })
    data.value = (res as any).data ?? res
    showRejectDialog.value = false
    ElMessage.success(t('approvals.rejectSuccess'))
  } catch {
    ElMessage.error(t('approvals.rejectFailed'))
  } finally {
    actionLoading.value = false
  }
}

async function doRevision() {
  if (!revisionForm.value.reason.trim()) {
    ElMessage.warning(t('approvals.revisionReasonRequired'))
    return
  }
  actionLoading.value = true
  try {
    const res = await requestRevision(requestId, {
      requestedBy: '',
      reason: revisionForm.value.reason,
    })
    data.value = (res as any).data ?? res
    showRevisionDialog.value = false
    ElMessage.success(t('approvals.revisionSuccess'))
  } catch {
    ElMessage.error(t('approvals.revisionFailed'))
  } finally {
    actionLoading.value = false
  }
}

async function doResubmit() {
  actionLoading.value = true
  try {
    const res = await resubmitApproval(requestId, {
      submittedBy: '',
      contractValue: resubmitForm.value.contractValue,
      approvalLevel: resubmitForm.value.approvalLevel,
    })
    data.value = (res as any).data ?? res
    showResubmitDialog.value = false
    ElMessage.success(t('approvals.resubmitSuccess'))
  } catch {
    ElMessage.error(t('approvals.resubmitFailed'))
  } finally {
    actionLoading.value = false
  }
}

function segmentLabel(segment?: CustomerSegment): string {
  if (segment === 'GROUP') return t('approvals.segmentGroup')
  return t('approvals.segmentIndividual')
}

function statusLabel(status?: MultiApprovalStatus): string {
  const map: Record<string, string> = {
    DRAFT: t('approvals.statusDraft'), IN_APPROVAL: t('approvals.statusInApproval'),
    NEED_REVISION: t('approvals.statusNeedRevision'), APPROVED: t('approvals.statusApproved'), REJECTED: t('approvals.statusRejected'),
  }
  return status ? (map[status] ?? status) : ''
}

function statusType(status?: MultiApprovalStatus): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    DRAFT: 'info', IN_APPROVAL: 'primary', NEED_REVISION: 'warning', APPROVED: 'success', REJECTED: 'danger',
  }
  return status ? map[status] : undefined
}

function levelLabel(level?: string): string {
  const map: Record<string, string> = {
    LEVEL_1: t('approvals.level1'), LEVEL_2: t('approvals.level2'), LEVEL_3: t('approvals.level3'),
  }
  return level ? (map[level] ?? level) : ''
}

function stepStatus(step: ApprovalStepResponse): 'process' | 'finish' | 'error' | 'wait' {
  if (step.status === 'APPROVED') return 'finish'
  if (step.status === 'REJECTED') return 'error'
  if (step.status === 'SKIPPED') return 'wait'
  return 'process'
}

function stepStatusLabel(status: string): string {
  const map: Record<string, string> = {
    APPROVED: t('approvals.statusApproved'),
    REJECTED: t('approvals.statusRejectedShort'),
    SKIPPED: t('approvals.statusSkipped'),
    PENDING: t('approvals.statusPending'),
  }
  return map[status] ?? status
}

function stepTagType(status: string): 'success' | 'danger' | 'info' | 'warning' {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'SKIPPED') return 'info'
  return 'warning'
}

function formatAmount(value: number): string {
  return new Intl.NumberFormat(locale.value === 'vi' ? 'vi-VN' : 'en-US').format(value) + ' VND'
}

function fmtDate(dt?: string): string {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString(locale.value === 'vi' ? 'vi-VN' : 'en-US', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

function subjectLabel(type: string): string {
  const map: Record<string, string> = {
    INDIVIDUAL: t('approvals.subjectIndividual'),
    ORGANIZATION: t('approvals.subjectOrganization'),
    INDIVIDUAL_OF_ORG: t('approvals.subjectIndividualOfOrg'),
  }
  return map[type] ?? type
}

function validityUnitLabel(unit: string): string {
  const map: Record<string, string> = { DAY: t('approvals.unitDay'), MONTH: t('approvals.unitMonth'), YEAR: t('approvals.unitYear') }
  return map[unit] ?? unit
}

function metricLabel(metric: string): string {
  const map: Record<string, string> = {
    SIGNING_COUNT: t('approvals.metricSigning'),
    CERTIFICATE_COUNT: t('approvals.metricCertificate'),
  }
  return map[metric] ?? metric
}

onMounted(load)
</script>

<style scoped>
/* ── Page header ── */
.page-header { margin-bottom: 20px; }
.back-btn { padding: 0; margin-bottom: 8px; font-size: 13px; }
.page-header h2 { margin: 0 0 4px; font-size: 20px; }
.req-id { color: #409eff; }
.page-subtitle { margin: 0; color: #909399; font-size: 13px; display: flex; align-items: center; gap: 8px; }

.action-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }

/* ── Section card ── */
.section-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 16px;
  overflow: hidden;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.section-title { font-weight: 700; font-size: 14px; color: #1B60CB; letter-spacing: 0.3px; }

/* ── Info grid ── */
.info-grid { padding: 4px 20px 20px; }
.info-row { display: flex; gap: 24px; margin-top: 12px; }
.info-item { flex: 1; display: flex; gap: 4px; font-size: 14px; color: #303133; line-height: 1.6; }
.info-item--full { flex: 2; }
.info-label { color: #303133; white-space: nowrap; font-weight: 500; }
.info-value { color: #303133; }
.revision-label { color: #e6a23c; font-weight: 600; }
.revision-note { color: #e6a23c; }
.plan-name { font-weight: 600; color: #1B60CB; }

/* ── Pricing table ── */
.pricing-section { padding: 0 20px 20px; }
.pricing-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  padding-top: 4px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.pricing-table { width: 100%; }
.price-value { font-weight: 600; color: #303133; }

/* ── Steps timeline ── */
.steps-container { padding: 8px 20px 20px; }

.step-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.step-row:last-child { border-bottom: none; }

.step-indicator {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: #f5f7fa;
}
.step-row--approved .step-indicator { background: #f0f9eb; }
.step-row--rejected .step-indicator { background: #fef0f0; }
.step-row--pending  .step-indicator { background: #fdf6ec; }
.step-row--skipped  .step-indicator { background: #f4f4f5; }

.icon-approved { color: #67c23a; }
.icon-rejected { color: #f56c6c; }
.icon-skipped  { color: #909399; }

.step-level-badge {
  font-size: 13px;
  font-weight: 700;
  color: #e6a23c;
}

.step-body { flex: 1; }

.step-header-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.step-level-name { font-size: 14px; font-weight: 600; color: #303133; }

.step-decision {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}
.step-decision--pending { color: #e6a23c; font-style: italic; }
.step-actor { font-weight: 500; color: #303133; }
.step-dot { color: #c0c4cc; }
.step-time { color: #909399; }
.step-comment { font-style: italic; color: #909399; font-size: 12px; margin-top: 4px; }

/* ── Dialogs ── */
.dlg-body { font-size: 14px; color: #303133; line-height: 1.6; margin: 0; }
.dlg-danger { color: #f56c6c; }
</style>
