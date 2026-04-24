<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text :icon="ArrowLeft" @click="router.back()">{{ t('common.back') }}</el-button>
      <h2>{{ t('partners.newPartner') }}</h2>
    </div>

    <!-- Partner Info -->
    <el-card shadow="never" :header="t('partners.info')">
      <el-form ref="partnerFormRef" :model="partnerForm" :rules="partnerRules" label-width="140px">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item :label="t('partners.partnerCode')" prop="groupCode">
              <el-input v-model="partnerForm.groupCode" placeholder="e.g. PTN-HN-001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.partnerName')" prop="groupName">
              <el-input v-model="partnerForm.groupName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.username')" prop="username">
              <el-input v-model="partnerForm.username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.password')" prop="password">
              <el-input v-model="partnerForm.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.contactEmail')" prop="contactEmail">
              <el-input v-model="partnerForm.contactEmail" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.contactPhone')" prop="contactPhone">
              <el-input v-model="partnerForm.contactPhone" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.refContractNo')" prop="refContractNo">
              <el-input v-model="partnerForm.refContractNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.picEmails')" prop="picEmails">
              <el-input v-model="partnerForm.picEmails" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Staging Plans -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>{{ t('partners.plans') }}</span>
          <el-button type="primary" size="small" :icon="Plus" @click="openPlanDialog">
            {{ t('partners.addPlan') }}
          </el-button>
        </div>
      </template>

      <el-empty v-if="stagingPlans.length === 0" :description="t('partners.noPlansAdded')" :image-size="60" />

      <el-table v-else :data="stagingPlans" stripe style="width:100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="planCode" :label="t('plans.code')" width="140" />
        <el-table-column prop="planName" :label="t('plans.name')" min-width="160" />
        <el-table-column :label="t('plans.type')" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.planType === 'VALIDITY_PERIOD' ? 'info' : 'primary'">
              {{ row.planType === 'VALIDITY_PERIOD' ? t('plans.byDate') : t('plans.perUsage') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.price')" width="120">
          <template #default="{ row }">{{ row.price.toLocaleString() }} {{ row.currency }}</template>
        </el-table-column>
        <el-table-column :label="t('plans.validityDays')" width="100">
          <template #default="{ row }">{{ row.validityDays }}</template>
        </el-table-column>
        <el-table-column :label="t('plans.limits')" width="140">
          <template #default="{ row }">
            <span style="font-size:12px">
              <span v-if="row.maxMembers">{{ t('plans.members') }}: {{ row.maxMembers }}</span>
              <span v-if="row.maxMembers && row.maxSigningQuota"> · </span>
              <span v-if="row.maxSigningQuota">{{ t('plans.quota') }}: {{ row.maxSigningQuota }}</span>
              <span v-if="!row.maxMembers && !row.maxSigningQuota" style="color:#bbb">—</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="110" fixed="right">
          <template #default="{ row, $index }">
            <el-tooltip :content="t('common.edit')" placement="top">
              <el-button size="small" type="primary" :icon="EditIcon" @click="openEditPlan($index)" />
            </el-tooltip>
            <el-tooltip :content="t('common.remove')" placement="top">
              <el-button size="small" type="danger" :icon="Delete" @click="removePlan($index)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Footer -->
    <div class="form-footer">
      <el-button @click="router.back()">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="saving" @click="handleCreate">
        {{ t('common.create') }}
      </el-button>
    </div>

    <!-- Add / Edit Plan Dialog -->
    <el-dialog v-model="planDialogVisible" :title="editingPlanIndex === -1 ? t('partners.addPlan') : t('plans.editPlan')" width="540px" :close-on-click-modal="false">
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="150px">
        <el-form-item :label="t('plans.code')" prop="planCode">
          <el-input v-model="planForm.planCode" placeholder="e.g. RS-PTN-365" :disabled="editingPlanIndex !== -1" />
        </el-form-item>
        <el-form-item :label="t('plans.name')" prop="planName">
          <el-input v-model="planForm.planName" />
        </el-form-item>
        <el-form-item :label="t('plans.expiryType')" prop="planType">
          <el-radio-group v-model="planForm.planType">
            <el-radio value="VALIDITY_PERIOD">{{ t('plans.byDate') }}</el-radio>
            <el-radio value="COMBINED">{{ t('plans.perUsage') }}</el-radio>
          </el-radio-group>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            <template v-if="planForm.planType === 'VALIDITY_PERIOD'">{{ t('plans.expiryDescByDate') }}</template>
            <template v-else>{{ t('plans.expiryDescPartnerCombined') }}</template>
          </div>
        </el-form-item>
        <el-form-item :label="t('plans.price')" prop="price">
          <el-input-number v-model="planForm.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.currency')" prop="currency">
          <el-select v-model="planForm.currency" style="width:100%">
            <el-option label="VND" value="VND" />
            <el-option label="USD" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('plans.validityDays')" prop="validityDays">
          <div style="display:flex;flex-direction:column;gap:6px;width:100%">
            <div style="display:flex;gap:6px">
              <el-button size="small" round :type="planValidityUnit === 'DAYS' ? 'primary' : ''" @click="setPlanValidityUnit('DAYS')">{{ t('plans.unitDays') }}</el-button>
              <el-button size="small" round :type="planValidityUnit === 'MONTHS' ? 'primary' : ''" @click="setPlanValidityUnit('MONTHS')">{{ t('plans.unitMonths') }}</el-button>
              <el-button size="small" round :type="planValidityUnit === 'YEARS' ? 'primary' : ''" @click="setPlanValidityUnit('YEARS')">{{ t('plans.unitYears') }}</el-button>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
              <el-input-number v-model="planValidityAmount" :min="1" style="width:140px" />
              <span v-if="planValidityUnit !== 'DAYS'" style="font-size:12px;color:#909399;white-space:nowrap">= {{ planForm.validityDays }} {{ t('plans.unitDays').toLowerCase() }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="t('plans.maxUsers')" prop="maxMembers">
          <el-input-number v-model="planForm.maxMembers" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="planForm.planType === 'COMBINED'" :label="t('plans.maxUsageCount')" prop="maxSigningQuota">
          <el-input-number v-model="planForm.maxSigningQuota" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveFrom')">
          <el-date-picker v-model="planForm.effectiveFrom" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveTo')">
          <el-date-picker v-model="planForm.effectiveTo" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.partnerPlan')">
          <el-switch v-model="planForm.isGroupPlan" disabled />
        </el-form-item>
        <el-form-item :label="t('plans.isVisible')">
          <el-switch v-model="planForm.isVisible" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddPlan">{{ editingPlanIndex === -1 ? t('common.add') : t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Plus, Delete, Edit as EditIcon } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { createPartner } from '@/api/groups'
import { createPlan } from '@/api/plans'
import { assignGroupPlan } from '@/api/subscriptions'
import type { PlanType, CreatePlanRequest } from '@/types'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const saving = ref(false)
const partnerFormRef = ref<FormInstance>()
const planFormRef = ref<FormInstance>()
const planDialogVisible = ref(false)
const editingPlanIndex = ref(-1) // -1 = adding new, >=0 = editing existing

// ── Partner form ──────────────────────────────────────────────
const partnerForm = reactive({
  groupCode: '', groupName: '', username: '',
  password: '', contactEmail: '', contactPhone: '',
  refContractNo: '', picEmails: '',
})

const partnerRules = computed(() => ({
  groupCode:    [{ required: true, message: t('common.required') }],
  groupName:    [{ required: true, message: t('common.required') }],
  username:     [{ required: true, message: t('common.required') }],
  password:     [{ required: true, message: t('common.required') }, { min: 6, message: t('partners.minPassword') }],
  contactEmail: [{ required: true, message: t('common.required') }],
}))

// ── Staging plans ─────────────────────────────────────────────
const stagingPlans = ref<CreatePlanRequest[]>([])

function removePlan(idx: number) {
  stagingPlans.value.splice(idx, 1)
}

// ── Plan dialog form ──────────────────────────────────────────
const planValidityAmount = ref(15)
const planValidityUnit = ref<'DAYS' | 'MONTHS' | 'YEARS'>('DAYS')
const planUnitValues = reactive<Record<string, number>>({ DAYS: 15, MONTHS: 3, YEARS: 1 })
const PLAN_UNIT_MULTIPLIER: Record<string, number> = { DAYS: 1, MONTHS: 30, YEARS: 365 }

function setPlanValidityUnit(unit: 'DAYS' | 'MONTHS' | 'YEARS') {
  planUnitValues[planValidityUnit.value] = planValidityAmount.value
  planValidityUnit.value = unit
  planValidityAmount.value = planUnitValues[unit]
}

const BLANK_PLAN = (): CreatePlanRequest => ({
  planCode: '', planName: '', price: 0, currency: 'VND',
  planType: 'VALIDITY_PERIOD' as PlanType,
  validityDays: 15,
  validityAmount: 15,
  validityUnit: 'DAYS',
  maxSigningQuota: null,
  maxMembers: null,
  isGroupPlan: true,
  effectiveFrom: null, effectiveTo: null,
  isVisible: false,
})

const planForm = reactive<CreatePlanRequest>(BLANK_PLAN())

watchEffect(() => {
  planForm.validityDays = (planValidityAmount.value || 1) * PLAN_UNIT_MULTIPLIER[planValidityUnit.value]
  planForm.validityAmount = planValidityAmount.value
  planForm.validityUnit = planValidityUnit.value
})

const planRules = computed(() => ({
  planCode:  [{ required: true, message: t('common.required') }],
  planName:  [{ required: true, message: t('common.required') }],
  price:     [{ required: true, message: t('common.required') }],
  currency:  [{ required: true, message: t('common.required') }],
  validityDays: [{ required: true, message: t('common.required') }],
  maxMembers: [{ validator: (_: any, v: any, cb: any) => {
    if (!v || v < 1) cb(new Error(t('common.required')))
    else cb()
  }}],
  maxSigningQuota: [{ validator: (_: any, v: any, cb: any) => {
    if (planForm.planType === 'COMBINED' && (!v || v < 1)) cb(new Error(t('common.required')))
    else cb()
  }}],
}))

function openPlanDialog() {
  editingPlanIndex.value = -1
  Object.assign(planUnitValues, { DAYS: 15, MONTHS: 3, YEARS: 1 })
  planValidityUnit.value = 'DAYS'
  planValidityAmount.value = 15
  Object.assign(planForm, BLANK_PLAN())
  planFormRef.value?.clearValidate()
  planDialogVisible.value = true
}

function openEditPlan(idx: number) {
  editingPlanIndex.value = idx
  const p = stagingPlans.value[idx]
  const unit = (p.validityUnit as 'DAYS' | 'MONTHS' | 'YEARS') || 'DAYS'
  const amount = p.validityAmount || p.validityDays
  Object.assign(planUnitValues, { DAYS: 15, MONTHS: 3, YEARS: 1, [unit]: amount })
  planValidityUnit.value = unit
  planValidityAmount.value = amount
  Object.assign(planForm, { ...p })
  planFormRef.value?.clearValidate()
  planDialogVisible.value = true
}

async function handleAddPlan() {
  if (!await planFormRef.value?.validate().catch(() => false)) return

  if (editingPlanIndex.value === -1) {
    // Adding new
    const isDuplicate = stagingPlans.value.some(p => p.planCode === planForm.planCode)
    if (isDuplicate) {
      ElMessage.warning(t('partners.planCodeDuplicate'))
      return
    }
    stagingPlans.value.push({ ...planForm })
  } else {
    // Updating existing
    stagingPlans.value[editingPlanIndex.value] = { ...planForm }
  }

  planDialogVisible.value = false
}

// ── Submit ────────────────────────────────────────────────────
async function handleCreate() {
  if (!await partnerFormRef.value?.validate().catch(() => false)) return

  saving.value = true
  try {
    const partnerRes = await createPartner(partnerForm)
    if (!partnerRes.success || !partnerRes.data) throw new Error(partnerRes.message)
    const partnerId = partnerRes.data.groupId

    for (const plan of stagingPlans.value) {
      await createPlan(plan)
      await assignGroupPlan(partnerId, { planCode: plan.planCode })
    }

    ElMessage.success(t('partners.createdMsg'))
    router.push('/partners/' + partnerId)
  } catch (e: any) {
    ElMessage.error(e?.message || t('partners.createFailed'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-header h2 { margin: 0; }
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
