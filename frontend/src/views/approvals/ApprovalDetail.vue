<template>
  <div class="approval-detail-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.back()" link>Quay láº¡i</el-button>
      <div>
        <h2>Chi tiáº¿t yÃªu cáº§u phÃª duyá»‡t <span class="req-id">#{{ requestId }}</span></h2>
        <p class="page-subtitle">
          {{ segmentLabel(data?.customerSegment) }} â€”
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
        >Gá»­i yÃªu cáº§u duyá»‡t</el-button>

        <!-- Sale: Resubmit sau revision -->
        <el-button
          v-if="data.status === 'NEED_REVISION'"
          type="warning"
          :icon="Refresh"
          @click="showResubmitDialog = true"
        >Gá»­i láº¡i sau chá»‰nh sá»­a</el-button>

        <!-- Approver: Approve / Reject / Revision (khi IN_APPROVAL) -->
        <template v-if="data.status === 'IN_APPROVAL'">
          <el-button type="success" :icon="CircleCheck" @click="showApproveDialog = true">PhÃª duyá»‡t</el-button>
          <el-button type="danger" :icon="CircleClose" @click="showRejectDialog = true">Tá»« chá»‘i</el-button>
          <el-button type="warning" :icon="Edit" @click="showRevisionDialog = true">YÃªu cáº§u chá»‰nh sá»­a</el-button>
        </template>
      </div>

      <!-- ThÃ´ng tin chung -->
      <div class="section-card" v-if="data">
        <div class="section-header">
          <span class="section-title">THÃ”NG TIN YÃŠU Cáº¦U</span>
        </div>
        <div class="info-grid">
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">Loáº¡i yÃªu cáº§u:</span>
              <span class="info-value">{{ data.requestType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Loáº¡i khÃ¡ch hÃ ng:</span>
              <span class="info-value">{{ segmentLabel(data.customerSegment) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">NgÆ°á»i táº¡o:</span>
              <span class="info-value">{{ data.requestedBy }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">NgÃ y táº¡o:</span>
              <span class="info-value">{{ fmtDate(data.createdAt) }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">GiÃ¡ trá»‹ há»£p Ä‘á»“ng:</span>
              <span class="info-value">{{ data.contractValue ? formatAmount(data.contractValue) : 'â€”' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Sá»‘ cáº¥p duyá»‡t:</span>
              <span class="info-value">{{ data.totalLevels }} cáº¥p</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item info-item--full">
              <span class="info-label">MÃ´ táº£:</span>
              <span class="info-value">{{ data.description }}</span>
            </div>
          </div>
          <div class="info-row" v-if="data.status === 'NEED_REVISION' && data.reviewNote">
            <div class="info-item info-item--full">
              <span class="info-label revision-label">LÃ½ do chá»‰nh sá»­a:</span>
              <span class="info-value revision-note">{{ data.reviewNote }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Timeline duyá»‡t -->
      <div class="section-card" v-if="data?.steps?.length">
        <div class="section-header">
          <span class="section-title">TIáº¾N TRÃŒNH PHÃŠ DUYá»†T</span>
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
                    <span>{{ step.decidedBy }} â€” {{ fmtDate(step.decidedAt) }}</span>
                  </div>
                  <div v-else-if="step.status === 'REJECTED'" class="step-meta danger">
                    <el-icon><CircleClose /></el-icon>
                    <span>{{ step.decidedBy }} â€” {{ fmtDate(step.decidedAt) }}</span>
                  </div>
                  <div v-else-if="step.status === 'SKIPPED'" class="step-meta skipped">
                    <span>Bá» qua</span>
                  </div>
                  <div v-else class="step-meta pending">
                    <el-icon><Clock /></el-icon>
                    <span>Chá» duyá»‡t</span>
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
          <span class="section-title">Dá»® LIá»†U YÃŠU Cáº¦U</span>
          <el-icon :class="{ rotated: !showPayload }"><ArrowDown /></el-icon>
        </div>
        <div v-show="showPayload" class="payload-block">
          <pre>{{ JSON.stringify(data.payload, null, 2) }}</pre>
        </div>
      </div>
    </div>

    <!-- Dialog: Submit -->
    <el-dialog v-model="showSubmitDialog" title="Gá»¬I YÃŠU Cáº¦U PHÃŠ DUYá»†T" width="460px" align-center>
      <p class="dlg-body">
        Request sáº½ Ä‘Æ°á»£c gá»­i cho ngÆ°á»i phÃª duyá»‡t cáº¥p 1 (TrÆ°á»Ÿng phÃ²ng).
        Sá»‘ cáº¥p duyá»‡t sáº½ Ä‘Æ°á»£c tÃ­nh tá»± Ä‘á»™ng dá»±a trÃªn giÃ¡ trá»‹ há»£p Ä‘á»“ng.
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item label="GiÃ¡ trá»‹ HÄ (VND)">
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
        <el-button type="primary" :loading="actionLoading" @click="doSubmit">XÃ¡c nháº­n gá»­i</el-button>
        <el-button @click="showSubmitDialog = false">Huá»·</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Approve -->
    <el-dialog v-model="showApproveDialog" title="PHÃŠ DUYá»†T" width="460px" align-center>
      <p class="dlg-body">
        Báº¡n Ä‘ang phÃª duyá»‡t cáº¥p <b>{{ data?.currentLevel }}</b>/{{ data?.totalLevels }}
        ({{ data ? levelLabel(currentStepRole) : '' }}).
      </p>
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="NgÆ°á»i duyá»‡t">
          <el-input v-model="approveForm.approvedBy" placeholder="Username cá»§a báº¡n" />
        </el-form-item>
        <el-form-item label="Ghi chÃº">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="Ghi chÃº (tuá»³ chá»n)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="success" :loading="actionLoading" @click="doApprove">PhÃª duyá»‡t</el-button>
        <el-button @click="showApproveDialog = false">Huá»·</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Reject -->
    <el-dialog v-model="showRejectDialog" title="Tá»ª CHá»I YÃŠU Cáº¦U" width="460px" align-center>
      <p class="dlg-body dlg-danger">
        Request sáº½ bá»‹ tá»« chá»‘i hoÃ n toÃ n. Sale sáº½ nháº­n Ä‘Æ°á»£c thÃ´ng bÃ¡o qua email.
      </p>
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="NgÆ°á»i tá»« chá»‘i" required>
          <el-input v-model="rejectForm.rejectedBy" placeholder="Username cá»§a báº¡n" />
        </el-form-item>
        <el-form-item label="LÃ½ do" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="Báº¯t buá»™c nháº­p lÃ½ do" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="actionLoading" @click="doReject">XÃ¡c nháº­n tá»« chá»‘i</el-button>
        <el-button @click="showRejectDialog = false">Huá»·</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Revision -->
    <el-dialog v-model="showRevisionDialog" title="YÃŠU Cáº¦U CHá»ˆNH Sá»¬A" width="460px" align-center>
      <p class="dlg-body">
        ToÃ n bá»™ tiáº¿n trÃ¬nh duyá»‡t sáº½ reset. Sale sáº½ nháº­n email vÃ  cáº§n gá»­i láº¡i sau khi chá»‰nh sá»­a.
      </p>
      <el-form label-width="120px" style="margin-top: 12px">
        <el-form-item label="NgÆ°á»i yÃªu cáº§u">
          <el-input v-model="revisionForm.requestedBy" placeholder="Username cá»§a báº¡n" />
        </el-form-item>
        <el-form-item label="LÃ½ do" required>
          <el-input v-model="revisionForm.reason" type="textarea" :rows="3" placeholder="MÃ´ táº£ ná»™i dung cáº§n chá»‰nh sá»­a" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" :loading="actionLoading" @click="doRevision">Gá»­i yÃªu cáº§u</el-button>
        <el-button @click="showRevisionDialog = false">Huá»·</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Resubmit -->
    <el-dialog v-model="showResubmitDialog" title="Gá»¬I Láº I SAU CHá»ˆNH Sá»¬A" width="460px" align-center>
      <p class="dlg-body">
        Request sáº½ Ä‘Æ°á»£c reset vÃ  gá»­i láº¡i tá»« cáº¥p 1. HÃ£y Ä‘áº£m báº£o báº¡n Ä‘Ã£ chá»‰nh sá»­a ná»™i dung cáº§n thiáº¿t.
      </p>
      <el-form label-width="150px" style="margin-top: 12px">
        <el-form-item label="GiÃ¡ trá»‹ HÄ (VND)">
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
        <el-button type="warning" :loading="actionLoading" @click="doResubmit">Gá»­i láº¡i</el-button>
        <el-button @click="showResubmitDialog = false">Huá»·</el-button>
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
const submitForm = ref({ contractValue: undefined as number | undefined, approvalLevel: 1 as 1 | 2 | 3 })
const approveForm = ref({ approvedBy: '', comment: '' })
const rejectForm = ref({ rejectedBy: '', reason: '' })
const revisionForm = ref({ requestedBy: '', reason: '' })
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
  } catch {
    ElMessage.error('KhÃ´ng thá»ƒ táº£i thÃ´ng tin yÃªu cáº§u')
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
      approvalLevel: submitForm.value.approvalLevel,
    })
    data.value = (res as any).data ?? res
    showSubmitDialog.value = false
    ElMessage.success('ÄÃ£ gá»­i yÃªu cáº§u phÃª duyá»‡t. Email Ä‘Ã£ Ä‘Æ°á»£c gá»­i cho ngÆ°á»i duyá»‡t cáº¥p 1.')
  } catch {
    ElMessage.error('Gá»­i yÃªu cáº§u tháº¥t báº¡i')
  } finally {
    actionLoading.value = false
  }
}

async function doApprove() {
  if (!approveForm.value.approvedBy.trim()) {
    ElMessage.warning('Vui lÃ²ng nháº­p username ngÆ°á»i duyá»‡t')
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
    ElMessage.success('ÄÃ£ phÃª duyá»‡t thÃ nh cÃ´ng')
  } catch {
    ElMessage.error('PhÃª duyá»‡t tháº¥t báº¡i')
  } finally {
    actionLoading.value = false
  }
}

async function doReject() {
  if (!rejectForm.value.rejectedBy.trim() || !rejectForm.value.reason.trim()) {
    ElMessage.warning('Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ thÃ´ng tin')
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
    ElMessage.success('ÄÃ£ tá»« chá»‘i yÃªu cáº§u')
  } catch {
    ElMessage.error('Tá»« chá»‘i tháº¥t báº¡i')
  } finally {
    actionLoading.value = false
  }
}

async function doRevision() {
  if (!revisionForm.value.reason.trim()) {
    ElMessage.warning('Vui lÃ²ng nháº­p lÃ½ do chá»‰nh sá»­a')
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
    ElMessage.success('ÄÃ£ gá»­i yÃªu cáº§u chá»‰nh sá»­a. Email thÃ´ng bÃ¡o Ä‘Ã£ Ä‘Æ°á»£c gá»­i cho ngÆ°á»i táº¡o.')
  } catch {
    ElMessage.error('Gá»­i yÃªu cáº§u chá»‰nh sá»­a tháº¥t báº¡i')
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
    ElMessage.success('ÄÃ£ gá»­i láº¡i yÃªu cáº§u. Email Ä‘Ã£ Ä‘Æ°á»£c gá»­i cho ngÆ°á»i duyá»‡t cáº¥p 1.')
  } catch {
    ElMessage.error('Gá»­i láº¡i tháº¥t báº¡i')
  } finally {
    actionLoading.value = false
  }
}

function segmentLabel(segment?: CustomerSegment): string {
  if (segment === 'GROUP') return 'KhÃ¡ch hÃ ng Ä‘áº¡i lÃ½'
  return 'KhÃ¡ch hÃ ng phá»• thÃ´ng'
}

function statusLabel(status?: MultiApprovalStatus): string {
  const map: Record<string, string> = {
    DRAFT: 'Báº£n nhÃ¡p', IN_APPROVAL: 'Äang duyá»‡t',
    NEED_REVISION: 'Cáº§n chá»‰nh sá»­a', APPROVED: 'ÄÃ£ duyá»‡t', REJECTED: 'Bá»‹ tá»« chá»‘i',
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

function formatAmount(value: number): string {
  return new Intl.NumberFormat('vi-VN').format(value) + ' VND'
}

function fmtDate(dt?: string): string {
  if (!dt) return 'â€”'
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

/* Section card style â€” match dá»± Ã¡n */
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


