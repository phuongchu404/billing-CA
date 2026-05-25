<template>
  <div>
    <div class="page-header">
      <h2>{{ $t('agency.addPlanTitle') }}</h2>
      <span class="breadcrumb">{{ $t('agency.breadcrumb') }}</span>
    </div>

    <el-button :icon="Document" style="margin-bottom:20px" @click="templateVisible = true">
      {{ $t('agency.chooseTemplate') }}
    </el-button>

    <!-- THÔNG TIN ĐẠI LÝ (read-only) -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.infoSection') }}</div>
      <div class="info-grid">
        <div class="info-row two-col">
          <span><b>{{ $t('agency.agencyNameField') }}</b> {{ agency.groupName }}</span>
          <span><b>{{ $t('agency.agencyCodeField') }}</b> {{ agency.groupCode }}</span>
        </div>
        <div class="info-row">
          <span><b>{{ $t('agency.picField') }}</b> {{ agency.picEmails.join(', ') }}</span>
        </div>
        <div class="info-row">
          <span><b>{{ $t('agency.representativeField') }}</b> {{ agency.contactEmails.join(', ') }}</span>
        </div>
        <div class="info-row">
          <span><b>{{ $t('agency.refContractField') }}</b> {{ agency.refContractNo }}</span>
        </div>
      </div>
    </el-card>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.planSection') }}</div>
      <el-form :model="form" label-width="220px" label-position="left">
        <el-form-item :label="$t('agency.planNameLabel')">
          <el-input v-model="form.planName" :placeholder="$t('agency.planNamePlaceholder')" />
          <div class="field-hint">{{ $t('agency.planNameHint') }}</div>
        </el-form-item>
        <el-form-item :label="$t('agency.applyDateLabel')">
          <el-date-picker
            v-model="form.applyDateRange"
            type="daterange"
            :start-placeholder="$t('agency.dateFrom')"
            range-separator="-"
            :end-placeholder="$t('agency.dateTo')"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <div class="plan-notes">
        <p>
          {{ $t('agency.addPlanAvailablePrefix') }} <b>{{ $t('agency.applyNoteAvailableStatus') }}</b>
          {{ $t('agency.addPlanAvailableSuffix') }}
        </p>
      </div>
    </el-card>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.configSection') }}</div>
      <el-table :data="configRows" border>
        <el-table-column type="index" width="50" />
        <el-table-column :label="$t('agency.colSubject')" width="160" sortable>
          <template #default="{ row }">
            <span class="subject-label">{{ row.subject }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colCtsDuration')" width="165" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.duration" :min="0" :max="48" controls-position="right" style="width:80px" size="small" />
              <span>{{ $t('agency.monthUnit') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colCondition')" width="150" sortable>
          <template #default="{ row }">
            <el-select v-model="row.condition" size="small" style="width:100%">
              <el-option :label="$t('agency.conditionSigning')" value="signing" />
              <el-option :label="$t('agency.conditionCertificate')" value="certificate" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMinValue')" width="175" sortable>
          <template #default="{ row }">
            <el-input-number v-model="row.minValue" :min="1" controls-position="right" style="width:100%" size="small" />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMaxValue')" width="175" sortable>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :value-on-clear="null"
              controls-position="right"
              style="width:100%"
              size="small"
              :placeholder="$t('agency.maxValuePlaceholder')"
            />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colFeePerCondition')" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.fee" :min="0" controls-position="right" style="flex:1" size="small" />
              <span>{{ $t('agency.vnd') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colTotalPrice')" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number
                v-model="row.totalPrice"
                :min="0"
                controls-position="right"
                style="flex:1"
                size="small"
                :placeholder="row.maxValue == null ? $t('agency.maxValuePlaceholder') : ''"
                :disabled="row.maxValue != null"
              />
              <span>{{ $t('agency.vnd') }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="guide-toggle" @click="showGuide = !showGuide">
        {{ $t('agency.guideToggle') }}
        <el-icon><ArrowUp v-if="showGuide" /><ArrowDown v-else /></el-icon>
      </div>

      <div v-show="showGuide" class="guide-content">
        <ul>
          <li>{{ $t('agency.guideIntro') }}</li>
          <li>
            <b>{{ $t('agency.guideDurationLabel') }}</b> {{ $t('agency.guideDurationText') }}
          </li>
          <li>
            <b>{{ $t('agency.guideConditionLabel') }}</b> {{ $t('agency.guideConditionText') }}
          </li>
          <li>
            <b>{{ $t('agency.guideMinLabel') }}</b> {{ $t('agency.guideMinText') }}
            <b>{{ $t('agency.guideMaxLabel') }}</b> {{ $t('agency.guideMaxText') }}
            <br />
            <u>{{ $t('agency.guideNoteLabel') }}</u> {{ $t('agency.guideNoteText') }}
          </li>
          <li>
            <b>{{ $t('agency.guideFeeLabel') }}</b> {{ $t('agency.guideFeeText') }}
          </li>
        </ul>
      </div>
    </el-card>

    <div class="footer-btns">
      <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      <el-button @click="handleCancel">{{ $t('common.cancel') }}</el-button>
    </div>

    <!-- Dialog: Approval success -->
    <el-dialog v-model="approvalSuccessVisible" :title="$t('agency.approvalSuccessTitle')" width="440px" align-center :close-on-click-modal="false">
      <div style="text-align:center; padding: 8px 0 16px">
        <el-icon style="font-size:48px; color:#67c23a"><CircleCheck /></el-icon>
        <p style="margin:12px 0 4px; font-size:15px; font-weight:600; color:#303133">
          {{ $t('agency.addPlanApprovalSuccess') }}
        </p>
        <p style="font-size:13px; color:#606266; margin:0">
          {{ $t('agency.approvalSuccessDesc1') }}<br />
          {{ $t('agency.approvalSuccessDesc2') }}
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToApproval">{{ $t('agency.viewApprovalProgress') }}</el-button>
        <el-button @click="router.push('/plans/' + agencyId)">{{ $t('agency.backToPlanList') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: CHỌN GÓI CƯỚC MẪU ===== -->
    <el-dialog v-model="templateVisible" :title="$t('agency.chooseTemplateTitle')" width="460px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          {{ $t('agency.chooseTemplateDesc') }}
        </p>
        <div class="dlg-select-row">
          <span class="dlg-select-label">{{ $t('agency.dialogPlanName') }}</span>
          <el-select
            v-model="selectedTemplate"
            :placeholder="$t('agency.chooseTemplatePlaceholder')"
            style="flex:1"
            clearable
          >
            <el-option
              v-for="t in templateOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!selectedTemplate" @click="confirmTemplate">{{ $t('common.confirm') }}</el-button>
        <el-button @click="templateVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, ArrowUp, ArrowDown, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getGroup, addGroupPlan } from '@/api/groups'
import { listPlanTemplates, getPlanTemplate } from '@/api/planTemplates'
import type { GroupDetail, AddGroupPlanRequest } from '@/types/group'
import type { PlanTemplate, PlanPricingRuleRequest } from '@/types/planTemplate'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const agencyId = Number(route.params.id)

interface ConfigRow {
  subject: string
  subjectType: 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
  duration: number
  condition: 'signing' | 'certificate'
  minValue: number
  maxValue: number | null
  fee: number
  totalPrice: number | null
}

// Dữ liệu đại lý (read-only) — load từ API
const agency = reactive<GroupDetail>({
  groupId: 0,
  groupCode: '',
  groupName: '',
  status: 'ACTIVE',
  picEmails: [],
  contactEmails: [],
  refContractNo: null,
  createdBy: null,
  createdAt: null,
  updatedAt: null,
  ownerUserId: null,
  ownerName: null,
})

const form = reactive({
  planName: '',
  applyDateRange: null as [string, string] | null,
})

const configRows = reactive<ConfigRow[]>([
  { subject: t('agency.subjectIndividual'), subjectType: 'INDIVIDUAL', duration: 1, condition: 'signing', minValue: 1, maxValue: null, fee: 0, totalPrice: null },
  { subject: t('agency.subjectOrganization'), subjectType: 'ORGANIZATION', duration: 24, condition: 'certificate', minValue: 1, maxValue: null, fee: 0, totalPrice: null },
  { subject: t('agency.subjectIndividualOfOrg'), subjectType: 'INDIVIDUAL_OF_ORG', duration: 12, condition: 'certificate', minValue: 1, maxValue: null, fee: 0, totalPrice: null },
])

// Auto-calculate totalPrice = fee * (maxValue - minValue) when maxValue is set
watch(configRows, (rows) => {
  rows.forEach((row) => {
    if (row.maxValue != null) {
      row.totalPrice = row.fee * row.maxValue
    }
  })
}, { deep: true })

const showGuide = ref(true)
const submitting = ref(false)
const loadingAgency = ref(false)
const approvalSuccessVisible = ref(false)
const lastApprovalId = ref<number | null>(null)

// --- Template dialog ---
const templateVisible = ref(false)
const selectedTemplate = ref<number | null>(null)
const templateOptions = ref<{ label: string; value: number }[]>([])

async function loadAgency() {
  if (!agencyId) return
  loadingAgency.value = true
  try {
    const res = await getGroup(agencyId)
    if (res.success && res.data) {
      Object.assign(agency, res.data)
    } else {
      ElMessage.error(res.message || t('agency.errorAgencyNotFound'))
    }
  } catch {
    ElMessage.error(t('agency.errorServer'))
  } finally {
    loadingAgency.value = false
  }
}

async function loadTemplateOptions() {
  try {
    const res = await listPlanTemplates()
    if (res.success && res.data) {
      templateOptions.value = res.data
        .filter(t => t.status === 'AVAILABLE' || t.status === 'DRAFT')
        .map(t => ({ label: t.planName, value: t.planTemplateId }))
    }
  } catch {
    // Optional template suggestions; ignore failures here.
  }
}

async function confirmTemplate() {
  if (!selectedTemplate.value) return
  try {
    const res = await getPlanTemplate(selectedTemplate.value)
    if (res.success && res.data) {
      const tmpl: PlanTemplate = res.data
      // Copy pricing rules into config rows.
      tmpl.pricingRules.forEach((rule, i) => {
        if (i < configRows.length) {
          configRows[i].duration = rule.certificateValidityValue
          configRows[i].condition = rule.pricingMetric === 'SIGNING_COUNT' ? 'signing' : 'certificate'
          configRows[i].minValue = rule.rangeMin
          configRows[i].maxValue = rule.rangeMax
          configRows[i].fee = Number(rule.unitPrice)
          configRows[i].totalPrice = rule.totalPrice != null ? Number(rule.totalPrice) : null
        }
      })
    }
  } catch {
    ElMessage.error(t('agency.errorLoadTemplate'))
  }
  templateVisible.value = false
}

async function handleSubmit() {
  if (!form.planName.trim()) {
    ElMessage.warning(t('agency.warningPlanName'))
    return
  }

  submitting.value = true
  try {
    const pricingRules: PlanPricingRuleRequest[] = configRows.map((row, i) => ({
      subjectType: row.subjectType,
      certificateValidityValue: row.duration,
      certificateValidityUnit: 'MONTH',
      pricingMetric: row.condition === 'signing' ? 'SIGNING_COUNT' : 'CERTIFICATE_COUNT',
      rangeMin: row.minValue,
      rangeMax: row.maxValue,
      unitPrice: row.fee,
      totalPrice: row.totalPrice,
      currency: 'VND',
      quotaTotal: null,
      sortOrder: i + 1,
      isActive: true,
    }))

    const req: AddGroupPlanRequest = {
      planName: form.planName,
      applyFrom: form.applyDateRange ? form.applyDateRange[0] : null,
      applyTo: form.applyDateRange ? form.applyDateRange[1] : null,
      requestedBy: 'system',
      pricingRules,
    }

    const res = await addGroupPlan(agencyId, req)
    if (res.success) {
      ElMessage.success(t('agency.successCreatePlan'))
      router.push('/plans/' + agencyId)
    } else {
      ElMessage.error(res.message || t('agency.errorCreatePlan'))
    }
  } catch {
    ElMessage.error(t('agency.errorServer'))
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/plans/' + agencyId)
}

function goToApproval() {
  approvalSuccessVisible.value = false
  router.push(lastApprovalId.value ? `/approvals/${lastApprovalId.value}` : '/approvals')
}

onMounted(() => {
  loadAgency()
  loadTemplateOptions()
})
</script>



<style scoped>
.page-header { margin-bottom: 12px; }
.page-header h2 { margin: 0; font-size: 20px; }
.breadcrumb { font-size: 13px; color: #909399; }

.section-card { margin-bottom: 16px; }
.section-title {
  color: #1B60CB;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
}

.info-grid { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #303133; }
.info-row { line-height: 1.6; }
.info-row.two-col { display: flex; gap: 60px; }

.field-hint { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.4; }

.plan-notes {
  padding-left: 220px;
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.plan-notes p { margin: 4px 0; }

.cell-row { display: flex; align-items: center; gap: 6px; }
.subject-label { color: #909399; }

.guide-toggle {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  cursor: pointer;
  color: #1B60CB;
  font-size: 13px;
  user-select: none;
}

.guide-content {
  font-size: 13px;
  color: #606266;
  margin-top: 8px;
  line-height: 1.6;
}
.guide-content ul { padding-left: 16px; margin: 0; list-style: disc; }
.guide-content li { margin-bottom: 6px; }

.footer-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 8px;
}

/* Dialog */
.dlg-body { font-size: 13px; color: #303133; line-height: 1.7; }
.dlg-desc { margin: 0 0 16px; }
.dlg-select-row { display: flex; align-items: center; gap: 12px; }
.dlg-select-label { white-space: nowrap; font-size: 13px; }
</style>
