<template>
  <div class="approval-detail-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()" link>Quay lại</el-button>
      <div>
        <h2>Chi tiết yêu cầu phê duyệt <span class="req-id">#{{ requestId }}</span></h2>
        <p class="page-subtitle">
          {{ segmentLabel(data?.customerSegment) }} —
          <el-tag :type="statusType(data?.status)" size="small">{{ statusLabel(data?.status) }}</el-tag>
        </p>
      </div>
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

      <!-- Timeline duyệt -->
      <div class="section-card" v-if="data?.steps?.length">
        <div class="section-header">
          <span class="section-title">TIẾN TRÌNH PHÊ DUYỆT</span>
        </div>
        <div class="steps-container">
          <el-steps :active="activeStepIndex" finish-status="success" align-center>
            <el-step
              v-for="step in data.steps"
              :key="step.id"
              :title="levelLabel(step.requiredApprovalLevel)"
              :status="stepStatus(step)"
            >
              <template #description>
                <div class="step-desc">
                  <div v-if="step.status === 'APPROVED'" class="step-meta success">
                    <el-icon><CircleCheck /></el-icon>
                    <span>{{ step.decidedBy }} — {{ fmtDate(step.decidedAt) }}</span>
                  </div>
                  <div v-else-if="step.status === 'REJECTED'" class="step-meta danger">
                    <el-icon><CircleClose /></el-icon>
                    <span>{{ step.decidedBy }} — {{ fmtDate(step.decidedAt) }}</span>
                  </div>
                  <div v-else-if="step.status === 'SKIPPED'" class="step-meta skipped">
                    <span>Bỏ qua</span>
                  </div>
                  <div v-else class="step-meta pending">
                    <el-icon><Clock /></el-icon>
                    <span>Chờ duyệt</span>
                  </div>
                  <div v-if="step.comment" class="step-comment">"{{ step.comment }}"</div>
                </div>
              </template>
            </el-step>
          </el-steps>
        </div>
      </div>

      <!-- Payload -->
      <div class="section-card" v-if="data?.payload">
        <div class="section-header" @click="showPayload = !showPayload" style="cursor:pointer">
          <span class="section-title">DỮ LIỆU YÊU CẦU</span>
          <el-icon :class="{ rotated: !showPayload }"><ArrowDown /></el-icon>
        </div>
        <div v-show="showPayload" class="payload-block">
          <pre>{{ JSON.stringify(data.payload, null, 2) }}</pre>
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
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="Người duyệt">
          <el-input v-model="approveForm.approvedBy" placeholder="Username của bạn" />
        </el-form-item>
        <el-form-item label="Ghi chú">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="Ghi chú (tuỳ chọn)" />
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
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="Người từ chối" required>
          <el-input v-model="rejectForm.rejectedBy" placeholder="Username của bạn" />
        </el-form-item>
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
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="Người yêu cầu">
          <el-input v-model="revisionForm.requestedBy" placeholder="Username của bạn" />
        </el-form-item>
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
} from '@/api/approvals'
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
const showPayload = ref(false)

// Dialogs
const showSubmitDialog = ref(false)
const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const showRevisionDialog = ref(false)
const showResubmitDialog = ref(false)

// Forms
const submitForm = ref({ contractValue: undefined as number | undefined })
const approveForm = ref({ approvedBy: '', comment: '' })
const rejectForm = ref({ rejectedBy: '', reason: '' })
const revisionForm = ref({ requestedBy: '', reason: '' })
const resubmitForm = ref({ contractValue: undefined as number | undefined })

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
  } catch {
    ElMessage.error('Không thể tải thông tin yêu cầu')
  } finally {
    loading.value = false
  }
}

async function doSubmit() {
  actionLoading.value = true
  try {
    const res = await submitApproval(requestId, {
      submittedBy: '',
      contractValue: submitForm.value.contractValue,
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
  if (!approveForm.value.approvedBy.trim()) {
    ElMessage.warning('Vui lòng nhập username người duyệt')
    return
  }
  actionLoading.value = true
  try {
    const res = await approveStep(requestId, {
      approvedBy: approveForm.value.approvedBy,
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
  if (!rejectForm.value.rejectedBy.trim() || !rejectForm.value.reason.trim()) {
    ElMessage.warning('Vui lòng nhập đầy đủ thông tin')
    return
  }
  actionLoading.value = true
  try {
    const res = await rejectRequest(requestId, {
      rejectedBy: rejectForm.value.rejectedBy,
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
      requestedBy: revisionForm.value.requestedBy,
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

function statusType(status?: MultiApprovalStatus): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    DRAFT: 'info', IN_APPROVAL: '', NEED_REVISION: 'warning', APPROVED: 'success', REJECTED: 'danger',
  }
  return status ? (map[status] ?? '') : ''
}

function levelLabel(level?: string): string {
  const map: Record<string, string> = {
    LEVEL_1: 'Trưởng phòng', LEVEL_2: 'Giám đốc', LEVEL_3: 'CFO',
  }
  return level ? (map[level] ?? level) : ''
}

function stepStatus(step: ApprovalStepResponse): 'process' | 'finish' | 'error' | 'wait' {
  if (step.status === 'APPROVED') return 'finish'
  if (step.status === 'REJECTED') return 'error'
  if (step.status === 'SKIPPED') return 'wait'
  return 'process'
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

onMounted(load)
</script>

<style scoped>
.page-header { margin-bottom: 16px; display: flex; flex-direction: column; gap: 4px; }
.page-header h2 { margin: 0; }
.req-id { color: #409eff; }
.page-subtitle { margin: 4px 0 0; color: #909399; font-size: 13px; display: flex; align-items: center; gap: 8px; }

.action-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }

/* Section card style — match dự án */
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
}
.section-title { font-weight: 700; font-size: 14px; color: #1B60CB; letter-spacing: 0.3px; }

.info-grid { padding: 4px 20px 20px; }
.info-row { display: flex; gap: 24px; margin-top: 12px; }
.info-item { flex: 1; display: flex; gap: 4px; font-size: 14px; color: #303133; line-height: 1.6; }
.info-item--full { flex: 2; }
.info-label { color: #303133; white-space: nowrap; font-weight: 500; }
.info-value { color: #303133; }
.revision-label { color: #e6a23c; font-weight: 600; }
.revision-note { color: #e6a23c; }

/* Steps timeline */
.steps-container { padding: 20px 32px 28px; }
.step-desc { margin-top: 6px; font-size: 12px; }
.step-meta { display: flex; align-items: center; gap: 4px; }
.step-meta.success { color: #67c23a; }
.step-meta.danger { color: #f56c6c; }
.step-meta.skipped { color: #909399; }
.step-meta.pending { color: #909399; }
.step-comment { font-style: italic; color: #606266; margin-top: 4px; }

/* Payload */
.payload-block { padding: 12px 20px 20px; }
.payload-block pre {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  overflow-x: auto;
  margin: 0;
}
.rotated { transform: rotate(-90deg); transition: transform 0.2s; }

/* Dialogs */
.dlg-body { font-size: 14px; color: #303133; line-height: 1.6; margin: 0; }
.dlg-danger { color: #f56c6c; }
</style>
