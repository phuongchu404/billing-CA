<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">{{ t('approvals.title') }}</h1>
      <el-button type="primary" :icon="Plus" @click="openSubmit">
        {{ t('approvals.newRequest') }}
      </el-button>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="load">
        <el-radio-button value="">{{ t('approvals.all') }}</el-radio-button>
        <el-radio-button value="PENDING">{{ t('approvals.pending') }}</el-radio-button>
        <el-radio-button value="APPROVED">{{ t('approvals.approved') }}</el-radio-button>
        <el-radio-button value="DENIED">{{ t('approvals.denied') }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <el-table :data="rows" v-loading="loading" row-key="id" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="requestType" :label="t('approvals.type')" width="200">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ typeLabel(row.requestType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('approvals.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestedBy" :label="t('approvals.submittedBy')" width="140" />
        <el-table-column prop="status" :label="t('common.status')" width="110">
          <template #default="{ row }">
            <el-tag :type="(statusTag[row.status as keyof typeof statusTag] as any)" size="small">
              {{ t('approvals.' + row.status.toLowerCase()) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('approvals.submittedAt')" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="reviewedBy" :label="t('approvals.reviewedBy')" width="130" />
        <el-table-column :label="t('common.actions')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">
              {{ t('common.detail') }}
            </el-button>
            <template v-if="row.status === 'PENDING' && authStore.hasPermission('approval:review')">
              <el-button link type="success" size="small" @click="openReview(row, 'approve')">
                {{ t('approvals.approve') }}
              </el-button>
              <el-button link type="danger" size="small" @click="openReview(row, 'deny')">
                {{ t('approvals.deny') }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="load"
        />
      </div>
    </el-card>

    <!-- ── Submit dialog ──────────────────────────────────────────── -->
    <el-dialog v-model="submitVisible" :title="t('approvals.newRequest')" width="600px" @close="resetSubmit">
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="150px">
        <el-form-item :label="t('approvals.type')" prop="requestType">
          <el-select v-model="submitForm.requestType" @change="onTypeChange">
            <el-option v-for="opt in typeOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('approvals.description')" prop="description">
          <el-input v-model="submitForm.description" type="textarea" :rows="2" />
        </el-form-item>

        <!-- CREATE_PLAN fields -->
        <template v-if="submitForm.requestType === 'CREATE_PLAN'">
          <el-divider>{{ t('approvals.planDetails') }}</el-divider>
          <el-form-item :label="t('plans.code')" prop="payload.planCode">
            <el-input v-model="(submitForm.payload as any).planCode" />
          </el-form-item>
          <el-form-item :label="t('plans.name')" prop="payload.planName">
            <el-input v-model="(submitForm.payload as any).planName" />
          </el-form-item>
          <el-form-item :label="t('plans.price')" prop="payload.price">
            <el-input-number v-model="(submitForm.payload as any).price" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item :label="t('plans.currency')">
            <el-input v-model="(submitForm.payload as any).currency" style="width:90px" maxlength="3" />
          </el-form-item>
          <el-form-item :label="t('plans.type')">
            <el-select v-model="(submitForm.payload as any).planType">
              <el-option value="VALIDITY_PERIOD" label="By Date" />
              <el-option value="COMBINED" label="Date + Quota" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('plans.validityDays')" prop="payload.validityDays">
            <el-input-number v-model="(submitForm.payload as any).validityDays" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item v-if="(submitForm.payload as any).planType === 'COMBINED'" :label="t('plans.maxUsageCount')">
            <el-input-number v-model="(submitForm.payload as any).maxSigningQuota" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item :label="t('plans.partnerPlan')">
            <el-switch v-model="(submitForm.payload as any).isGroupPlan" @change="onGroupPlanChange" />
          </el-form-item>
          <el-form-item v-if="(submitForm.payload as any).isGroupPlan" :label="t('plans.maxUsers')">
            <el-input-number v-model="(submitForm.payload as any).maxMembers" :min="1" style="width:100%" />
          </el-form-item>
        </template>

        <!-- ASSIGN_GROUP_PLAN fields -->
        <template v-else-if="submitForm.requestType === 'ASSIGN_GROUP_PLAN'">
          <el-divider>{{ t('approvals.assignDetails') }}</el-divider>
          <el-form-item label="Partner ID" prop="payload.groupId">
            <el-input-number v-model="(submitForm.payload as any).groupId" :min="1" style="width:100%" />
          </el-form-item>
          <el-form-item :label="t('plans.code')" prop="payload.planCode">
            <el-input v-model="(submitForm.payload as any).planCode" />
          </el-form-item>
        </template>

        <!-- CANCEL_SUBSCRIPTION / SUSPEND_SUBSCRIPTION fields -->
        <template v-else-if="submitForm.requestType === 'CANCEL_SUBSCRIPTION' || submitForm.requestType === 'SUSPEND_SUBSCRIPTION'">
          <el-divider>{{ t('approvals.subscriptionDetails') }}</el-divider>
          <el-form-item label="Subscription ID" prop="payload.subscriptionId">
            <el-input-number v-model="(submitForm.payload as any).subscriptionId" :min="1" style="width:100%" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="doSubmit">{{ t('approvals.submitRequest') }}</el-button>
      </template>
    </el-dialog>

    <!-- ── Detail dialog ──────────────────────────────────────────── -->
    <el-dialog v-model="detailVisible" :title="t('common.detail')" width="560px">
      <template v-if="selected">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="ID">{{ selected.id }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.type')">{{ typeLabel(selected.requestType) }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.status')">
            <el-tag :type="(statusTag[selected.status as keyof typeof statusTag] as any)" size="small">
              {{ t('approvals.' + selected.status.toLowerCase()) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('approvals.description')">{{ selected.description }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.submittedBy')">{{ selected.requestedBy }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.submittedAt')">{{ fmtDateTime(selected.createdAt) }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.reviewedBy')" v-if="selected.reviewedBy">{{ selected.reviewedBy }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.reviewNote')" v-if="selected.reviewNote">{{ selected.reviewNote }}</el-descriptions-item>
          <el-descriptions-item :label="t('approvals.reviewedAt')" v-if="selected.reviewedAt">{{ fmtDateTime(selected.reviewedAt!) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="selected.payload" style="margin-top:16px">
          <div class="payload-label">{{ t('approvals.payload') }}</div>
          <pre class="payload-pre">{{ JSON.stringify(selected.payload, null, 2) }}</pre>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">{{ t('common.cancel') }}</el-button>
        <template v-if="selected?.status === 'PENDING' && authStore.hasPermission('approval:review')">
          <el-button type="success" @click="detailVisible = false; openReview(selected!, 'approve')">
            {{ t('approvals.approve') }}
          </el-button>
          <el-button type="danger" @click="detailVisible = false; openReview(selected!, 'deny')">
            {{ t('approvals.deny') }}
          </el-button>
        </template>
      </template>
    </el-dialog>

    <!-- ── Review dialog ──────────────────────────────────────────── -->
    <el-dialog v-model="reviewVisible" :title="reviewAction === 'approve' ? t('approvals.approveTitle') : t('approvals.denyTitle')" width="420px">
      <el-form :model="reviewForm" label-width="110px">
        <el-form-item :label="t('approvals.reviewNote')">
          <el-input v-model="reviewForm.note" type="textarea" :rows="3" :placeholder="t('approvals.noteOptional')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button
          :type="reviewAction === 'approve' ? 'success' : 'danger'"
          :loading="saving"
          @click="doReview"
        >
          {{ reviewAction === 'approve' ? t('approvals.approve') : t('approvals.deny') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store'
import {
  listApprovals, submitApproval, approveRequest, denyRequest,
  type ApprovalResponse,
} from '@/api/approvals'

const { t } = useI18n()
const authStore = useAuthStore()

// ── list state ────────────────────────────────────────────────────
const rows = ref<ApprovalResponse[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const filterStatus = ref('')

async function load() {
  loading.value = true
  try {
    const res = await listApprovals({ status: filterStatus.value || undefined, page: page.value - 1, size: pageSize.value })
    rows.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

onMounted(load)

// ── detail ────────────────────────────────────────────────────────
const detailVisible = ref(false)
const selected = ref<ApprovalResponse | null>(null)

function openDetail(row: ApprovalResponse) {
  selected.value = row
  detailVisible.value = true
}

// ── submit ────────────────────────────────────────────────────────
const submitVisible = ref(false)
const saving = ref(false)
const submitFormRef = ref()

const BLANK_PLAN_PAYLOAD = () => ({
  planCode: '',
  planName: '',
  price: 0,
  currency: 'VND',
  planType: 'VALIDITY_PERIOD',
  validityDays: 30,
  maxSigningQuota: null as number | null,
  isGroupPlan: false,
  maxMembers: null as number | null,
})

const submitForm = reactive({
  requestType: 'CREATE_PLAN',
  description: '',
  payload: BLANK_PLAN_PAYLOAD() as Record<string, unknown>,
})

const submitRules = {
  requestType: [{ required: true }],
  description: [{ required: true, message: 'Description is required' }],
  'payload.planCode': [{ required: true, message: 'Plan code is required' }],
  'payload.planName': [{ required: true, message: 'Plan name is required' }],
  'payload.price': [{ required: true }],
  'payload.validityDays': [{ required: true }],
  'payload.planCode2': [{ required: true }],
  'payload.groupId': [{ required: true }],
  'payload.subscriptionId': [{ required: true }],
}

const typeOptions = [
  { value: 'CREATE_PLAN', label: t('approvals.typeCREATE_PLAN') },
  { value: 'ASSIGN_GROUP_PLAN', label: t('approvals.typeASSIGN_GROUP_PLAN') },
  { value: 'CANCEL_SUBSCRIPTION', label: t('approvals.typeCANCEL_SUBSCRIPTION') },
  { value: 'SUSPEND_SUBSCRIPTION', label: t('approvals.typeSUSPEND_SUBSCRIPTION') },
]

function onTypeChange() {
  if (submitForm.requestType === 'CREATE_PLAN') {
    submitForm.payload = BLANK_PLAN_PAYLOAD()
  } else if (submitForm.requestType === 'ASSIGN_GROUP_PLAN') {
    submitForm.payload = { groupId: null, planCode: '' }
  } else {
    submitForm.payload = { subscriptionId: null }
  }
}

function onGroupPlanChange(v: string | number | boolean) {
  if (!v) (submitForm.payload as any).maxMembers = null
}

function openSubmit() {
  submitForm.requestType = 'CREATE_PLAN'
  submitForm.description = ''
  submitForm.payload = BLANK_PLAN_PAYLOAD()
  submitVisible.value = true
}

function resetSubmit() {
  submitFormRef.value?.clearValidate()
}

async function doSubmit() {
  await submitFormRef.value?.validate()
  saving.value = true
  try {
    await submitApproval({
      requestType: submitForm.requestType,
      description: submitForm.description,
      payload: { ...submitForm.payload },
    })
    ElMessage.success(t('approvals.submitted'))
    submitVisible.value = false
    load()
  } catch {
    ElMessage.error(t('approvals.submitFailed'))
  } finally {
    saving.value = false
  }
}

// ── review ────────────────────────────────────────────────────────
const reviewVisible = ref(false)
const reviewAction = ref<'approve' | 'deny'>('approve')
const reviewTarget = ref<ApprovalResponse | null>(null)
const reviewForm = reactive({ note: '' })

function openReview(row: ApprovalResponse, action: 'approve' | 'deny') {
  reviewTarget.value = row
  reviewAction.value = action
  reviewForm.note = ''
  reviewVisible.value = true
}

async function doReview() {
  if (!reviewTarget.value) return
  saving.value = true
  try {
    if (reviewAction.value === 'approve') {
      await approveRequest(reviewTarget.value.id, { note: reviewForm.note || undefined })
      ElMessage.success(t('approvals.approvedMsg'))
    } else {
      await denyRequest(reviewTarget.value.id, { note: reviewForm.note || undefined })
      ElMessage.success(t('approvals.deniedMsg'))
    }
    reviewVisible.value = false
    load()
  } catch {
    ElMessage.error(t('approvals.reviewFailed'))
  } finally {
    saving.value = false
  }
}

// ── helpers ───────────────────────────────────────────────────────
const statusTag = {
  PENDING: 'warning',
  APPROVED: 'success',
  DENIED: 'danger',
} as const

function typeLabel(type: string) {
  const key = `approvals.type${type}` as const
  return t(key as any)
}

function fmtDateTime(v: string) {
  if (!v) return ''
  return new Date(v).toLocaleString('sv-SE').replace('T', ' ')
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-card { flex: 1; }
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.payload-label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 6px;
}
.payload-pre {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 12px;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
  max-height: 260px;
}
</style>
