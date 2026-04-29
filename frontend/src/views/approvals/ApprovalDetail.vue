<template>
  <div class="approval-detail-page">
    <div class="page-header">
      <el-button class="back-btn" :icon="ArrowLeft" @click="router.back()" link>Quay lại</el-button>
      <h2>Chi tiết yêu cầu phê duyệt <span class="req-id">#{{ requestId }}</span></h2>
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
        >Gửi yêu cầu duyệt</el-button>

        <!-- Sale: Resubmit sau revision -->
        <el-button
          v-if="data.status === 'NEED_REVISION'"
          type="warning"
          :icon="Refresh"
          @click="showResubmitDialog = true"
        >Gửi lại sau chỉnh sửa</el-button>

        <!-- Approver: Approve / Reject / Revision (khi IN_APPROVAL) -->
        <template v-if="data.status === 'IN_APPROVAL'">
          <el-button type="success" :icon="CircleCheck" @click="showApproveDialog = true">Phê duyệt</el-button>
          <el-button type="danger" :icon="CircleClose" @click="showRejectDialog = true">Từ chối</el-button>
          <el-button type="warning" :icon="Edit" @click="showRevisionDialog = true">Yêu cầu chỉnh sửa</el-button>
        </template>
      </div>

      <!-- Thông tin chung -->
      <div class="section-card" v-if="data">
        <div class="section-header">
          <span class="section-title">THÔNG TIN YÊU CẦU</span>
        </div>
        <div class="info-grid">
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">Loại yêu cầu:</span>
              <span class="info-value">{{ data.requestType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Loại khách hàng:</span>
              <span class="info-value">{{ segmentLabel(data.customerSegment) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">Người tạo:</span>
              <span class="info-value">{{ data.requestedBy }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Ngày tạo:</span>
              <span class="info-value">{{ fmtDate(data.createdAt) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">Giá trị hợp đồng:</span>
              <span class="info-value">{{ data.contractValue ? formatAmount(data.contractValue) : '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Số cấp duyệt:</span>
              <span class="info-value">{{ data.totalLevels }} cấp</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item info-item--full">
              <span class="info-label">Mô tả:</span>
              <span class="info-value">{{ data.description }}</span>
            </div>
          </div>
          <div class="info-row" v-if="data.status === 'NEED_REVISION' && data.reviewNote">
            <div class="info-item info-item--full">
              <span class="info-label revision-label">Lý do chỉnh sửa:</span>
              <span class="info-value revision-note">{{ data.reviewNote }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Chi tiết gói cước -->
      <div class="section-card" v-if="entityDetail">
        <div class="section-header">
          <span class="section-title">CHI TIẾT GÓI CƯỚC</span>
        </div>
        <div class="info-grid">
          <div class="info-row" v-if="entityDetail.groupName">
            <div class="info-item">
              <span class="info-label">Tên đại lý:</span>
              <span class="info-value">{{ entityDetail.groupName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Mã đại lý:</span>
              <span class="info-value">{{ entityDetail.groupCode }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">Tên gói cước:</span>
              <span class="info-value plan-name">{{ entityDetail.planName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Mã gói cước:</span>
              <span class="info-value">{{ entityDetail.planCode }}</span>
            </div>
          </div>
          <div class="info-row" v-if="entityDetail.applyFrom || entityDetail.applyTo">
            <div class="info-item">
              <span class="info-label">Áp dụng từ:</span>
              <span class="info-value">{{ entityDetail.applyFrom ?? '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Áp dụng đến:</span>
              <span class="info-value">{{ entityDetail.applyTo ?? '—' }}</span>
            </div>
          </div>
        </div>

        <!-- Bảng cấu hình giá -->
        <div class="pricing-section" v-if="pricingRules.length">
          <div class="pricing-title">Cấu hình bảng giá</div>
          <el-table :data="pricingRules" border size="small" class="pricing-table">
            <el-table-column label="Đối tượng" width="160">
              <template #default="{ row }">{{ subjectLabel(row.subjectType) }}</template>
            </el-table-column>
            <el-table-column label="Hiệu lực chứng thư" width="160">
              <template #default="{ row }">
                {{ row.certificateValidityValue }} {{ validityUnitLabel(row.certificateValidityUnit) }}
              </template>
            </el-table-column>
            <el-table-column label="Tính phí theo" width="140">
              <template #default="{ row }">{{ metricLabel(row.pricingMetric) }}</template>
            </el-table-column>
            <el-table-column label="Khoảng" width="130">
              <template #default="{ row }">
                {{ row.rangeMin }} — {{ row.rangeMax ?? '∞' }}
              </template>
            </el-table-column>
            <el-table-column label="Đơn giá" min-width="130" align="right">
              <template #default="{ row }">
                <span class="price-value">{{ formatAmount(row.unitPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Quota tổng" width="110" align="right">
              <template #default="{ row }">{{ row.quotaTotal ?? '—' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Timeline duyệt -->
      <div class="section-card" v-if="data?.steps?.length">
        <div class="section-header">
          <span class="section-title">TIẾN TRÌNH PHÊ DUYỆT</span>
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
                Đang chờ phê duyệt
              </div>
              <div v-if="step.comment" class="step-comment">"{{ step.comment }}"</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog: Submit -->
    <el-dialog v-model="showSubmitDialog" title="GỬI YÊU CẦU PHÊ DUYỆT" width="460px" align-center>
      <p class="dlg-body">
        Request sẽ được gửi cho người phê duyệt cấp 1 (Trưởng phòng).
        Số cấp duyệt sẽ được tính tự động dựa trên giá trị hợp đồng.
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item label="Giá trị HĐ (VND)">
          <el-input-number v-model="submitForm.contractValue" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <el-form label-width="150px" style="margin-top: 8px">
        <el-form-item label="Cấp phê duyệt">
          <el-select v-model="submitForm.approvalLevel" style="width: 100%">
            <el-option label="Trưởng phòng kinh doanh" :value="1" />
            <el-option label="CFO (Finance Manager)" :value="2" />
            <el-option label="CEO" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="actionLoading" @click="doSubmit">Xác nhận gửi</el-button>
        <el-button @click="showSubmitDialog = false">Huỷ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Approve -->
    <el-dialog v-model="showApproveDialog" title="PHÊ DUYỆT" width="460px" align-center>
      <p class="dlg-body">
        Bạn đang phê duyệt cấp <b>{{ data?.currentLevel }}</b>/{{ data?.totalLevels }}
        ({{ data ? levelLabel(currentStepRole) : '' }}).
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item label="Ghi chú">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="Ghi chú (tùy chọn)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="success" :loading="actionLoading" @click="doApprove">Phê duyệt</el-button>
        <el-button @click="showApproveDialog = false">Huỷ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Reject -->
    <el-dialog v-model="showRejectDialog" title="TỪ CHỐI YÊU CẦU" width="460px" align-center>
      <p class="dlg-body dlg-danger">
        Request sẽ bị từ chối hoàn toàn. Sale sẽ nhận được thông báo qua email.
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item label="Lý do" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="Bắt buộc nhập lý do" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="actionLoading" @click="doReject">Xác nhận từ chối</el-button>
        <el-button @click="showRejectDialog = false">Huỷ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Revision -->
    <el-dialog v-model="showRevisionDialog" title="YÊU CẦU CHỈNH SỬA" width="460px" align-center>
      <p class="dlg-body">
        Toàn bộ tiến trình duyệt sẽ reset. Sale sẽ nhận email và cần gửi lại sau khi chỉnh sửa.
      </p>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item label="Lý do" required>
          <el-input v-model="revisionForm.reason" type="textarea" :rows="3" placeholder="Mô tả nội dung cần chỉnh sửa" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" :loading="actionLoading" @click="doRevision">Gửi yêu cầu</el-button>
        <el-button @click="showRevisionDialog = false">Huỷ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Resubmit -->
    <el-dialog v-model="showResubmitDialog" title="GỬI LẠI SAU CHỈNH SỬA" width="460px" align-center>
      <p class="dlg-body">
        Request sẽ được reset và gửi lại từ cấp 1. Hãy đảm bảo bạn đã chỉnh sửa nội dung cần thiết.
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item label="Giá trị HĐ (VND)">
          <el-input-number v-model="resubmitForm.contractValue" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Cấp phê duyệt">
          <el-select v-model="resubmitForm.approvalLevel" style="width: 100%">
            <el-option label="Trưởng phòng kinh doanh" :value="1" />
            <el-option label="CFO (Finance Manager)" :value="2" />
            <el-option label="CEO" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" :loading="actionLoading" @click="doResubmit">Gửi lại</el-button>
        <el-button @click="showResubmitDialog = false">Huỷ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
    ElMessage.error('Không thể tải thông tin yêu cầu')
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
    // không bắt buộc — nếu không fetch được thì ẩn section
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
    ElMessage.success('Đã gửi yêu cầu phê duyệt. Email đã được gửi cho người duyệt cấp 1.')
  } catch {
    ElMessage.error('Gửi yêu cầu thất bại')
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
    ElMessage.success('Đã phê duyệt thành công')
  } catch {
    ElMessage.error('Phê duyệt thất bại')
  } finally {
    actionLoading.value = false
  }
}

async function doReject() {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('Vui lòng nhập lý do từ chối')
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
    ElMessage.success('Đã từ chối yêu cầu')
  } catch {
    ElMessage.error('Từ chối thất bại')
  } finally {
    actionLoading.value = false
  }
}

async function doRevision() {
  if (!revisionForm.value.reason.trim()) {
    ElMessage.warning('Vui lòng nhập lý do chỉnh sửa')
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
    ElMessage.success('Đã gửi yêu cầu chỉnh sửa. Email thông báo đã được gửi cho người tạo.')
  } catch {
    ElMessage.error('Gửi yêu cầu chỉnh sửa thất bại')
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
    ElMessage.success('Đã gửi lại yêu cầu. Email đã được gửi cho người duyệt cấp 1.')
  } catch {
    ElMessage.error('Gửi lại thất bại')
  } finally {
    actionLoading.value = false
  }
}

function segmentLabel(segment?: CustomerSegment): string {
  if (segment === 'GROUP') return 'Khách hàng đại lý'
  return 'Khách hàng phổ thông'
}

function statusLabel(status?: MultiApprovalStatus): string {
  const map: Record<string, string> = {
    DRAFT: 'Bản nháp', IN_APPROVAL: 'Đang duyệt',
    NEED_REVISION: 'Cần chỉnh sửa', APPROVED: 'Đã duyệt', REJECTED: 'Bị từ chối',
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
    LEVEL_1: 'Trưởng phòng kinh doanh', LEVEL_2: 'CFO (Finance Manager)', LEVEL_3: 'CEO',
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
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối',
    SKIPPED: 'Bỏ qua',
    PENDING: 'Chờ duyệt',
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
  return new Intl.NumberFormat('vi-VN').format(value) + ' VND'
}

function fmtDate(dt?: string): string {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

function subjectLabel(type: string): string {
  const map: Record<string, string> = {
    INDIVIDUAL: 'Cá nhân',
    ORGANIZATION: 'Tổ chức',
    INDIVIDUAL_OF_ORG: 'Cá nhân thuộc tổ chức',
  }
  return map[type] ?? type
}

function validityUnitLabel(unit: string): string {
  const map: Record<string, string> = { DAY: 'ngày', MONTH: 'tháng', YEAR: 'năm' }
  return map[unit] ?? unit
}

function metricLabel(metric: string): string {
  const map: Record<string, string> = {
    SIGNING_COUNT: 'Lượt ký',
    CERTIFICATE_COUNT: 'Chứng thư',
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
