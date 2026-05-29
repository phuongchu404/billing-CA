<template>
  <div>
    <div class="page-header">
      <h2>{{ $t('agency.addPlanTitle') }}</h2>
      <span class="breadcrumb">{{ $t('agency.breadcrumb') }}</span>
    </div>

    <el-button :icon="Document" style="margin-bottom:20px" @click="templateVisible = true" class="btn">
      {{ $t('agency.chooseTemplate') }}
    </el-button>

    <!-- THÔNG TIN ĐẠI LÝ (read-only) -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.infoSection') }}</div>
      <div class="info-grid">
        <el-row class="text-regular">
          <el-col :span="12">
            <span>{{ $t('agency.agencyNameField') }} {{ agency.groupName }}</span>
          </el-col>
          <el-col :span="12">
            <span>{{ $t('agency.agencyCodeField') }} {{ agency.groupCode }}</span>
          </el-col>
        </el-row>
        <div class="info-row text-regular">
          <span>{{ $t('agency.picEmailLabel') }}: {{ agency.picEmails.join(', ') }}</span>
        </div>
        <div class="info-row text-regular">
          <span>{{ $t('agency.contactEmailLabel') }}: {{ agency.contactEmails.join(', ') }}</span>
        </div>
        <div class="info-row text-regular">
          <span>{{ $t('agency.refContractField') }} {{ agency.refContractNo }}</span>
        </div>
      </div>
    </el-card>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.planSection') }}</div>
      <el-form :model="form" label-width="280" label-position="left" class="custom-form">
        <el-form-item :label="$t('agency.planNameLabel')">
          <el-input v-model="form.planName" :placeholder="$t('agency.planNamePlaceholder')" :maxlength="150" show-word-limit />
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
      <!--  
        <div class="plan-notes">
          <p>
            {{ $t('agency.addPlanAvailablePrefix') }} <b>{{ $t('agency.applyNoteAvailableStatus') }}</b>
            {{ $t('agency.addPlanAvailableSuffix') }}
          </p>
        </div>
      -->
      <div class="plan-notes">
        <p>
          {{ $t("agency.applyNotePendingPrefix") }}
          {{ $t("agency.applyNotePendingStatus") }}
          {{ $t("agency.applyNotePendingSuffix") }}
        </p>
        <p>
          {{ $t("agency.applyNoteAvailablePrefix") }}
          {{ $t("agency.applyNoteAvailableStatus") }}
          {{ $t("agency.applyNoteAvailableSuffix") }}
        </p>
      </div>
    </el-card>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.configSection') }}</div>
      <el-table :data="configRows" border>
        <el-table-column type="index" width="50" align="center"/>
        <el-table-column :label="$t('agency.colSubject')" width="200" sortable>
          <template #default="{ row }">
            <span class="subject-label">{{ row.subject }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colCtsDuration')" width="180" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.duration" :min="0" :max="48" controls-position="right" style="width:80px" size="small" />
              <span>{{ $t('agency.monthUnit') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colCondition')" width="180" sortable>
          <template #default="{ row }">
            <el-select v-model="row.condition" size="small" style="width:100%">
              <el-option :label="$t('agency.conditionSigning')" value="signing" />
              <el-option :label="$t('agency.conditionCertificate')" value="certificate" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMinValue')" width="180" sortable>
          <template #default="{ row }">
            <el-input-number v-model="row.minValue" :min="1" controls-position="right" style="width:100%" size="small" />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMaxValue')" width="180" sortable>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :value-on-clear="null"
              controls-position="right"
              style="width:100%"
              size="small"
              :placeholder="$t('agency.maxValuePlaceholder')"
              @change="autoCalcTotalPrice(row)"
            />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colFeePerCondition')" width="200" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.fee" :min="0" controls-position="right" style="flex:1" size="small" @change="autoCalcTotalPrice(row)" />
              <span>{{ $t('agency.vnd') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colTotalPrice')" width="200" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number
                v-model="row.totalPrice"
                :min="0"
                :controls="false"
                style="flex:1"
                size="small"
                :placeholder="$t('agency.totalPricePlaceholder')"
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
          <li class="text-primary"><b>{{ $t("agency.guideIntro") }}</b></li>
          <li>
            <b class="text-primary">{{ $t("agency.guideDurationLabel") }}</b>
            {{ $t("agency.guideDurationText") }}
          </li>
          <li>
            <b class="text-primary">{{ $t("agency.guideConditionLabel") }}</b>
            {{ $t("agency.guideConditionText") }}
          </li>
          <li>
            <span>
              <b class="text-primary">{{ $t("agency.guideMinLabel") }}</b> 
              {{ $t("agency.guideMinText") }}
            </span>
            <span>
              <b class="text-primary">{{ $t("agency.guideMaxLabel") }}</b> 
              {{ $t("agency.guideMaxText") }}
            </span>
            <br />
            <span>
              <u>{{ $t("agency.guideNoteLabel") }}</u>
              {{ $t("agency.guideNoteText") }}
            </span>
          </li>
          <li>
            <b class="text-primary">{{ $t("agency.guideFeeLabel") }}</b>
            {{ $t("agency.guideFeeText") }}
          </li>
        </ul>
      </div>
    </el-card>

    <div class="footer-btns">
      <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      <el-button @click="handleCancel" class="btn-cancel">{{ $t('roles.cancelEdit') }}</el-button>
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
    <el-dialog v-model="templateVisible" :title="$t('agency.chooseTemplateTitle')" width="660" height="280" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="text-regular">
          {{ $t('agency.chooseTemplateDesc') }}
        </p>
        <div class="dlg-select-row custom-form">
          <span class="text-primary">{{ $t('agency.dialogPlanName') }}</span>
          <el-select
            v-model="selectedTemplate"
            :placeholder="$t('agency.chooseTemplatePlaceholder')"
            style="flex:1"
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
        <el-button @click="templateVisible = false" class="btn-cancel">{{ $t('roles.cancelEdit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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

function autoCalcTotalPrice(row: ConfigRow) {
  if (row.maxValue != null) {
    row.totalPrice = row.fee * row.maxValue
  }
}

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
    const res = await listPlanTemplates('GROUP')
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
      const suffix = t('individualPlan.copySuffix')
      const proposed = tmpl.planName + suffix
      form.planName = proposed.length <= 150 ? proposed : tmpl.planName
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
  if (form.planName.length > 150) {
    ElMessage.warning(t('agency.warningPlanNameMaxLength'))
    return
  }
  if (configRows.some((row) => row.totalPrice == null || row.totalPrice < 0)) {
    ElMessage.warning(t('agency.warningTotalPrice'))
    return
  }
  if (configRows.some((row) => row.maxValue != null && row.maxValue < row.minValue)) {
    ElMessage.warning('Giá trị max phải lớn hơn hoặc bằng giá trị min')
    return
  }
  if (configRows.some((row) => !row.duration)) {
    ElMessage.warning('Vui lòng nhập số tháng thời hạn chứng thư cho tất cả các dòng')
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

.info-grid { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #303133; }
.info-row { line-height: 1.6; }
.info-row.two-col { display: flex; gap: 60px; }

.cell-row { display: flex; align-items: center; gap: 6px; }
.subject-label { color: #909399; }

.guide-toggle {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  cursor: pointer;
  color: var(--el-color-primary);
  font-size: 18px;
  font-weight: 500;
  font-style: italic;
  user-select: none;
}

.guide-content {
  font-size: 18px;
  color: var(--el-text-color-regular);
  margin-top: 8px;
  line-height: 1.6;
  font-style: italic;
} 

.guide-content ul { padding-left: 16px; margin: 0; list-style: disc; }
.guide-content li { margin-bottom: 6px; }
.guide-content li b {
  font-weight: 600!important;
}

.footer-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 8px;
}

/* form */
.custom-form :deep(.el-input),
.custom-form :deep(.el-select),
.custom-form :deep(.el-select__wrapper) {
  height: 3rem;
}

.custom-form :deep(.el-date-editor.el-input__wrapper) {
  height: 1.5rem;
}

.custom-form :deep(.el-input__wrapper),
.custom-form :deep(.el-select__wrapper) {
  padding: 0.75rem 1rem;
}

/* Dialog */
.dlg-body { font-size: 13px; color: #303133; line-height: 1.7; }
.dlg-desc { margin: 0 0 0 1rem; }
.dlg-select-row { display: flex; align-items: center; gap: 12px; margin-top: 1rem; }
.dlg-select-label { white-space: nowrap; font-size: 13px; }

</style>
