<template>
  <div>
    <div class="page-header">
      <h2>{{ t('individual.plansTitle') }}</h2>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchQuery" :placeholder="t('plans.searchPlaceholder')" clearable style="width:240px" />
        <el-select v-model="filterType" clearable :placeholder="t('plans.allTypes')" style="width:140px">
          <el-option :label="t('plans.byDate')" value="VALIDITY_PERIOD" />
          <el-option :label="t('plans.perUsage')" value="COMBINED" />
        </el-select>
        <el-select v-model="filterStatus" clearable :placeholder="t('plans.allStatuses')" style="width:140px">
          <el-option :label="t('common.active')" value="active" />
          <el-option :label="t('common.inactive')" value="inactive" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="openCreate" style="margin-left:auto">{{ t('plans.newPlan') }}</el-button>
      </div>
      <el-table :data="pagedPlans" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="55" :index="(i: number) => (page - 1) * pageSize + i + 1" />
        <el-table-column prop="planCode" :label="t('plans.code')" width="140" />
        <el-table-column prop="planName" :label="t('plans.name')" min-width="160" />
        <el-table-column prop="price" :label="t('plans.price')" width="120">
          <template #default="{ row }">{{ row.price.toLocaleString() }} {{ row.currency }}</template>
        </el-table-column>
        <el-table-column :label="t('plans.type')" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.planType === 'VALIDITY_PERIOD'" size="small" type="info">{{ t('plans.byDate') }}</el-tag>
            <el-tag v-else-if="row.planType === 'COMBINED'" size="small" type="primary">{{ t('plans.perUsage') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.days')" width="100">
          <template #default="{ row }">{{ formatValidity(row) }}</template>
        </el-table-column>
        <el-table-column :label="t('plans.limits')" width="130">
          <template #default="{ row }">
            <span v-if="row.planType === 'COMBINED'">{{ row.maxSigningQuota }} {{ t('plans.signs') }}</span>
            <span v-else style="color:#bbb">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.effectiveFrom')" width="120">
          <template #default="{ row }">{{ row.effectiveFrom ?? '—' }}</template>
        </el-table-column>
        <el-table-column :label="t('plans.effectiveTo')" width="120">
          <template #default="{ row }">{{ row.effectiveTo ?? '—' }}</template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? t('common.active') : t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
              <el-popconfirm :title="t('plans.deactivateConfirm')" @confirm="handleDeactivate(row)" v-if="row.isActive">
                <template #reference>
                  <el-button size="small" type="danger">{{ t('plans.deactivate') }}</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="filteredPlans.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingPlan ? t('plans.editPlan') : t('plans.newPlan')" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="t('plans.code')" prop="planCode">
          <el-input v-model="form.planCode" :disabled="!!editingPlan" placeholder="e.g. RS-IND-365" />
        </el-form-item>
        <el-form-item :label="t('plans.name')" prop="planName">
          <el-input v-model="form.planName" />
        </el-form-item>
        <el-form-item :label="t('plans.expiryType')" prop="planType">
          <el-radio-group v-model="form.planType">
            <el-radio value="VALIDITY_PERIOD">{{ t('plans.byDate') }}</el-radio>
            <el-radio value="COMBINED">{{ t('plans.perUsage') }}</el-radio>
          </el-radio-group>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            <template v-if="form.planType === 'VALIDITY_PERIOD'">{{ t('plans.expiryDescByDate') }}</template>
            <template v-else>{{ t('plans.expiryDescCombined') }}</template>
          </div>
        </el-form-item>
        <el-form-item :label="t('plans.price')" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.currency')" prop="currency">
          <el-select v-model="form.currency" style="width:100%">
            <el-option label="VND" value="VND" />
            <el-option label="USD" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('plans.validityDays')" prop="validityDays">
          <div style="display:flex;flex-direction:column;gap:6px;width:100%">
            <div style="display:flex;gap:6px">
              <el-button size="small" round :type="validityUnit === 'DAYS' ? 'primary' : ''" @click="setValidityUnit('DAYS')">{{ t('plans.unitDays') }}</el-button>
              <el-button size="small" round :type="validityUnit === 'MONTHS' ? 'primary' : ''" @click="setValidityUnit('MONTHS')">{{ t('plans.unitMonths') }}</el-button>
              <el-button size="small" round :type="validityUnit === 'YEARS' ? 'primary' : ''" @click="setValidityUnit('YEARS')">{{ t('plans.unitYears') }}</el-button>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
              <el-input-number v-model="validityAmount" :min="1" style="width:140px" />
              <span v-if="validityUnit !== 'DAYS'" style="font-size:12px;color:#909399;white-space:nowrap">= {{ form.validityDays }} {{ t('plans.unitDays').toLowerCase() }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="form.planType === 'COMBINED'" :label="t('plans.maxUsageCount')" prop="maxSigningQuota">
          <el-input-number v-model="form.maxSigningQuota" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveFrom')">
          <el-date-picker v-model="form.effectiveFrom" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveTo')">
          <el-date-picker v-model="form.effectiveTo" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.isVisible')">
          <el-switch v-model="form.isVisible" />
        </el-form-item>
        <el-form-item :label="t('plans.partnerPlan')">
          <el-switch v-model="form.isGroupPlan" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, watchEffect } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getPlans, createPlan, updatePlan, deactivatePlan } from '@/api/plans'
import type { Plan, PlanType } from '@/types'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()
const plans = ref<Plan[]>([])
const loading = ref(false)
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)

const page = ref(1)
const pageSize = ref(10)

const filteredPlans = computed(() => {
  let result = plans.value.filter(p => !p.isGroupPlan)
  const q = searchQuery.value.toLowerCase()
  if (q) result = result.filter(p => p.planCode.toLowerCase().includes(q) || p.planName.toLowerCase().includes(q))
  if (filterType.value) result = result.filter(p => p.planType === filterType.value)
  if (filterStatus.value === 'active') result = result.filter(p => p.isActive)
  if (filterStatus.value === 'inactive') result = result.filter(p => !p.isActive)
  return result
})

const pagedPlans = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredPlans.value.slice(start, start + pageSize.value)
})

watch([searchQuery, filterType, filterStatus], () => { page.value = 1 })

const saving = ref(false)
const editingPlan = ref<Plan | null>(null)
const formRef = ref<FormInstance>()

const validityAmount = ref(15)
const validityUnit = ref<'DAYS' | 'MONTHS' | 'YEARS'>('DAYS')
const unitValues = reactive<Record<string, number>>({ DAYS: 15, MONTHS: 3, YEARS: 1 })
const UNIT_MULTIPLIER: Record<string, number> = { DAYS: 1, MONTHS: 30, YEARS: 365 }

const form = reactive({
  planCode: '', planName: '', price: 0, currency: 'VND',
  planType: 'VALIDITY_PERIOD' as PlanType,
  validityDays: 15, validityAmount: 15 as number | undefined, validityUnit: 'DAYS' as 'DAYS' | 'MONTHS' | 'YEARS' | undefined,
  maxSigningQuota: null as number | null, maxMembers: null as number | null,
  isGroupPlan: false,
  effectiveFrom: null as string | null, effectiveTo: null as string | null,
  isVisible: true,
})

watchEffect(() => {
  form.validityDays = (validityAmount.value || 1) * UNIT_MULTIPLIER[validityUnit.value]
  form.validityAmount = validityAmount.value
  form.validityUnit = validityUnit.value
})

function setValidityUnit(unit: 'DAYS' | 'MONTHS' | 'YEARS') {
  unitValues[validityUnit.value] = validityAmount.value
  validityUnit.value = unit
  validityAmount.value = unitValues[unit]
}

function reverseMapValidity(days: number, unit?: string, amount?: number) {
  if (unit && amount) {
    validityUnit.value = unit as any
    validityAmount.value = amount
    unitValues[unit] = amount
    return
  }
  if (days % 365 === 0) { validityUnit.value = 'YEARS'; validityAmount.value = days / 365; unitValues.YEARS = days / 365 }
  else if (days % 30 === 0) { validityUnit.value = 'MONTHS'; validityAmount.value = days / 30; unitValues.MONTHS = days / 30 }
  else { validityUnit.value = 'DAYS'; validityAmount.value = days; unitValues.DAYS = days }
}

function formatValidity(plan: Plan): string {
  if (!plan.validityDays) return '—'
  if (plan.validityUnit && plan.validityAmount) {
    const label = plan.validityUnit === 'DAYS' ? t('plans.unitDays')
      : plan.validityUnit === 'MONTHS' ? t('plans.unitMonths')
      : t('plans.unitYears')
    return `${plan.validityAmount} ${label}`
  }
  return `${plan.validityDays} ${t('plans.unitDays')}`
}

const rules = computed(() => ({
  planCode: [{ required: true, message: t('common.required') }],
  planName: [{ required: true, message: t('common.required') }],
  price: [{ required: true, message: t('common.required') }],
  currency: [{ required: true, message: t('common.required') }],
  validityDays: [{ required: true, message: t('common.required') }],
  maxSigningQuota: [{ required: false, validator: (_: any, value: any, cb: any) => {
    if (form.planType === 'COMBINED' && (!value || value < 1)) cb(new Error(t('common.required')))
    else cb()
  }}],
}))

async function loadPlans() {
  loading.value = true
  try {
    const res = await getPlans()
    if (res.success) plans.value = res.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editingPlan.value = null
  Object.assign(unitValues, { DAYS: 15, MONTHS: 3, YEARS: 1 })
  validityUnit.value = 'DAYS'
  validityAmount.value = 15
  Object.assign(form, { planCode: '', planName: '', price: 0, currency: 'VND', planType: 'VALIDITY_PERIOD', validityDays: 15, maxSigningQuota: null, maxMembers: null, isGroupPlan: false, effectiveFrom: null, effectiveTo: null, isVisible: true })
  dialogVisible.value = true
}

function openEdit(plan: Plan) {
  editingPlan.value = plan
  reverseMapValidity(plan.validityDays, plan.validityUnit, plan.validityAmount)
  Object.assign(form, { planCode: plan.planCode, planName: plan.planName, price: plan.price, currency: plan.currency, planType: plan.planType || 'VALIDITY_PERIOD', validityDays: plan.validityDays, maxSigningQuota: plan.planType === 'COMBINED' ? plan.maxSigningQuota : null, maxMembers: null, isGroupPlan: false, effectiveFrom: plan.effectiveFrom ?? null, effectiveTo: plan.effectiveTo ?? null, isVisible: plan.isVisible ?? true })
  dialogVisible.value = true
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    if (editingPlan.value) {
      await updatePlan(editingPlan.value.planCode, form)
      ElMessage.success(t('plans.updated'))
    } else {
      await createPlan(form)
      ElMessage.success(t('plans.created'))
    }
    dialogVisible.value = false
    loadPlans()
  } finally { saving.value = false }
}

async function handleDeactivate(plan: Plan) {
  await deactivatePlan(plan.planCode)
  ElMessage.success(t('plans.deactivated'))
  loadPlans()
}

onMounted(loadPlans)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-bar { margin-top: 16px; display: flex; justify-content: flex-end; }
.action-btns { display: flex; gap: 6px; align-items: center; }
.action-btns :deep(.el-button) { margin: 0; }
</style>
