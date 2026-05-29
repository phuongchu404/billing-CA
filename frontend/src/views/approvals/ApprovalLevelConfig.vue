<template>
  <div class="approval-config-page">
    <div class="page-header">
      <div>
        <h2>{{ $t('approvalConfig.title') }}</h2>
        <p class="page-subtitle">{{ $t('approvalConfig.subtitle') }}</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">
        {{ $t('approvalConfig.btnAdd') }}
      </el-button>
    </div>

    <!-- Filter bar -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select
          v-model="filterSegment"
          :placeholder="$t('approvalConfig.colSegment')"
          clearable
          style="width: 200px"
          @change="onFilterChange"
        >
          <el-option :label="$t('approvals.all')" value="" />
          <el-option :label="$t('approvalConfig.segmentGroup')" value="GROUP" />
          <el-option :label="$t('approvalConfig.segmentIndividual')" value="INDIVIDUAL" />
        </el-select>

        <el-select
          v-model="filterActive"
          :placeholder="$t('approvalConfig.colStatus')"
          clearable
          style="width: 180px"
          @change="onFilterChange"
        >
          <el-option :label="$t('approvals.all')" value="" />
          <el-option :label="$t('common.active')" :value="true" />
          <el-option :label="$t('common.inactive')" :value="false" />
        </el-select>

        <el-button :icon="Refresh" :loading="loading" @click="onReload">
          {{ $t('common.refresh') }}
        </el-button>
      </div>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table :data="configs" border stripe v-loading="loading" table-layout="auto" style="width: 100%">
        <el-table-column type="index" label="#" width="55" align="center" />

        <el-table-column prop="customerSegment" :label="$t('approvalConfig.colSegment')" width="200">
          <template #default="{ row }">
            <el-tag :type="row.customerSegment === 'GROUP' ? 'primary' : 'info'" effect="light">
              {{ row.customerSegment === 'GROUP' ? $t('approvalConfig.segmentGroup') : $t('approvalConfig.segmentIndividual') }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="$t('approvalConfig.colMinValue')" width="160" align="right" header-align="left">
          <template #default="{ row }">
            <span class="amount-text">
              {{ row.minValue != null ? Number(row.minValue).toLocaleString('vi-VN') + ' ₫' : $t('approvalConfig.unlimited') }}
            </span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('approvalConfig.colMaxValue')" width="160" align="right" header-align="left">
          <template #default="{ row }">
            <span class="amount-text">
              {{ row.maxValue != null ? Number(row.maxValue).toLocaleString('vi-VN') + ' ₫' : $t('approvalConfig.unlimited') }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="requiredLevels" :label="$t('approvalConfig.colRequiredLevels')" width="130" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="light">
              {{ row.requiredLevels }} {{ $t('approvalConfig.levelUnit') }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" :label="$t('approvalConfig.colDescription')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.description" class="description-text">{{ row.description }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('approvalConfig.colStatus')" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" effect="light">
              {{ row.isActive ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="$t('common.actions')" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" :icon="Edit" @click="openEdit(row)">{{ $t('common.edit') }}</el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && configs.length === 0" class="empty-state">
        <el-empty :description="$t('approvalConfig.emptyText')" />
      </div>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadConfigs"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- Dialog tạo / sửa -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? $t('approvalConfig.dialogTitleEdit') : $t('approvalConfig.dialogTitleCreate')"
      width="520px"
      :close-on-click-modal="false"
      align-center
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="160px" label-position="left">
        <el-form-item :label="$t('approvalConfig.colSegment')" prop="customerSegment">
          <el-select v-model="form.customerSegment" style="width: 100%">
            <el-option label="GROUP (Đại lý)" value="GROUP" />
            <el-option label="INDIVIDUAL (Phổ thông)" value="INDIVIDUAL" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('approvalConfig.colMinValue')" prop="minValue">
          <el-input-number
            v-model="form.minValue"
            :min="0"
            :precision="0"
            style="width: 100%"
            :placeholder="$t('approvalConfig.unlimited')"
            :controls="false"
          />
        </el-form-item>
        <el-form-item :label="$t('approvalConfig.colMaxValue')" prop="maxValue">
          <el-input-number
            v-model="form.maxValue"
            :min="0"
            :precision="0"
            style="width: 100%"
            :placeholder="$t('approvalConfig.unlimited')"
            :controls="false"
          />
        </el-form-item>
        <el-form-item :label="$t('approvalConfig.colRequiredLevels')" prop="requiredLevels">
          <el-select v-model="form.requiredLevels" style="width: 100%">
            <el-option :label="'1 - ' + $t('approvalConfig.level1Name')" :value="1" />
            <el-option :label="'2 - ' + $t('approvalConfig.level2Name')" :value="2" />
            <el-option :label="'3 - ' + $t('approvalConfig.level3Name')" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('approvalConfig.colDescription')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('approvalConfig.colStatus')">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  listLevelConfigs,
  createLevelConfig,
  updateLevelConfig,
  deleteLevelConfig,
  type ApprovalLevelConfigResponse,
} from '@/api/approvals'

const { t } = useI18n()

const configs = ref<ApprovalLevelConfigResponse[]>([])
const loading = ref(false)
const saving = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterSegment = ref('')
const filterActive = ref<boolean | ''>('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  customerSegment: 'GROUP',
  minValue: undefined as number | undefined,
  maxValue: undefined as number | undefined,
  requiredLevels: 1 as 1 | 2 | 3,
  description: '',
  isActive: true,
})

const rules: FormRules = {
  customerSegment: [{ required: true, message: t('approvalConfig.ruleSegmentRequired'), trigger: 'change' }],
  requiredLevels: [{ required: true, message: t('approvalConfig.ruleLevelsRequired'), trigger: 'change' }],
}

async function loadConfigs() {
  loading.value = true
  try {
    const res = await listLevelConfigs({
      customerSegment: filterSegment.value || undefined,
      isActive: filterActive.value === '' ? undefined : filterActive.value,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    configs.value = res.data?.content ?? []
    total.value = res.data?.totalElements ?? 0
  } catch {
    ElMessage.error(t('approvalConfig.errorLoad'))
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  currentPage.value = 1
  loadConfigs()
}

function onSizeChange() {
  currentPage.value = 1
  loadConfigs()
}

function onReload() {
  currentPage.value = 1
  loadConfigs()
}

function openCreate() {
  editingId.value = null
  form.customerSegment = 'GROUP'
  form.minValue = undefined
  form.maxValue = undefined
  form.requiredLevels = 1
  form.description = ''
  form.isActive = true
  dialogVisible.value = true
}

function openEdit(row: ApprovalLevelConfigResponse) {
  editingId.value = row.id
  form.customerSegment = row.customerSegment
  form.minValue = row.minValue != null ? Number(row.minValue) : undefined
  form.maxValue = row.maxValue != null ? Number(row.maxValue) : undefined
  form.requiredLevels = row.requiredLevels as 1 | 2 | 3
  form.description = row.description ?? ''
  form.isActive = row.isActive
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      customerSegment: form.customerSegment,
      minValue: form.minValue ?? null,
      maxValue: form.maxValue ?? null,
      requiredLevels: form.requiredLevels,
      description: form.description || undefined,
      isActive: form.isActive,
    }
    if (editingId.value) {
      await updateLevelConfig(editingId.value, payload)
      ElMessage.success(t('approvalConfig.successUpdated'))
    } else {
      await createLevelConfig(payload)
      ElMessage.success(t('approvalConfig.successCreated'))
    }
    dialogVisible.value = false
    await loadConfigs()
  } catch {
    ElMessage.error(t('approvalConfig.errorSave'))
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: ApprovalLevelConfigResponse) {
  try {
    await ElMessageBox.confirm(
      t('approvalConfig.confirmDelete', { segment: row.customerSegment }),
      t('approvalConfig.confirmDeleteTitle'),
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteLevelConfig(row.id)
    ElMessage.success(t('approvalConfig.successDeleted'))
    await loadConfigs()
  } catch {
    ElMessage.error(t('approvalConfig.errorDelete'))
  }
}

onMounted(loadConfigs)
</script>

<style scoped>
.approval-config-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.amount-text {
  font-variant-numeric: tabular-nums;
  color: #303133;
}

.description-text {
  color: #606266;
}

.text-muted {
  color: #c0c4cc;
}

.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.empty-state {
  padding: 32px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
