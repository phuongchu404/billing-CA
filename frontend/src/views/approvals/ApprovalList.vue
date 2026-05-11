<template>
  <div class="approval-list-page">
    <div class="page-header">
      <div>
        <h2>{{ t('approvals.listTitle') }}</h2>
        <p class="page-subtitle">{{ t('approvals.listSubtitle') }}</p>
      </div>
    </div>

    <!-- Filter bar -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select v-model="filterStatus" :placeholder="t('approvals.allStatuses')" clearable style="width: 200px" @change="onFilterChange">
          <el-option :label="t('approvals.all')" value="" />
          <el-option :label="t('approvals.statusDraft')" value="DRAFT" />
          <el-option :label="t('approvals.statusInApproval')" value="IN_APPROVAL" />
          <el-option :label="t('approvals.statusNeedRevision')" value="NEED_REVISION" />
          <el-option :label="t('approvals.statusApproved')" value="APPROVED" />
          <el-option :label="t('approvals.statusRejected')" value="REJECTED" />
        </el-select>

        <el-select v-model="filterSegment" :placeholder="t('approvals.customerType')" clearable style="width: 200px" @change="onFilterChange">
          <el-option :label="t('approvals.all')" value="" />
          <el-option :label="t('approvals.segmentIndividual')" value="INDIVIDUAL" />
          <el-option :label="t('approvals.segmentGroup')" value="GROUP" />
        </el-select>

        <el-button :icon="Refresh" @click="onFilterChange">{{ t('common.refresh') }}</el-button>
      </div>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column width="55" label="#">
          <template #default="{ $index }">{{ (currentPage - 1) * pageSize + $index + 1 }}</template>
        </el-table-column>

        <el-table-column :label="t('approvals.requestCode')" width="100" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">#{{ row.id }}</el-link>
          </template>
        </el-table-column>

        <el-table-column :label="t('approvals.customerTypeShort')" width="160">
          <template #default="{ row }">
            <el-tag :type="row.customerSegment === 'GROUP' ? 'warning' : 'info'" size="small">
              {{ segmentLabel(row.customerSegment) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('approvals.description')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description }}</template>
        </el-table-column>

        <el-table-column :label="t('approvals.createdBy')" width="130">
          <template #default="{ row }">{{ row.requestedBy }}</template>
        </el-table-column>

        <el-table-column :label="t('approvals.contractValue')" width="150" align="right">
          <template #default="{ row }">
            {{ row.contractValue ? formatAmount(row.contractValue) : '—' }}
          </template>
        </el-table-column>

        <el-table-column :label="t('approvals.progress')" width="130" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'IN_APPROVAL'" class="level-badge">
              {{ t('approvals.levelProgress', { current: row.currentLevel, total: row.totalLevels }) }}
            </span>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.status')" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('approvals.createdAt')" width="160">
          <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
        </el-table-column>

        <el-table-column :label="t('common.actions')" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="goDetail(row.id)">{{ t('common.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="load"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { listApprovals } from '@/api/approvals'
import type { MultiLevelApprovalResponse, MultiApprovalStatus, CustomerSegment } from '@/api/approvals'

const router = useRouter()
const { t, locale } = useI18n()
const loading = ref(false)
const list = ref<MultiLevelApprovalResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const filterStatus = ref('')
const filterSegment = ref('')

async function load() {
  loading.value = true
  try {
    const res = await listApprovals({
      status: filterStatus.value || undefined,
      customerSegment: filterSegment.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    if (res.success && res.data) {
      list.value = res.data.content
      total.value = res.data.totalElements
    }
  } catch {
    ElMessage.error(t('approvals.loadListError'))
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  currentPage.value = 1
  load()
}

function onSizeChange() {
  currentPage.value = 1
  load()
}

function goDetail(id: number) {
  router.push(`/approvals/${id}`)
}

function segmentLabel(segment: CustomerSegment): string {
  return segment === 'GROUP' ? t('approvals.segmentGroupShort') : t('approvals.segmentIndividualShort')
}

function statusLabel(status: MultiApprovalStatus): string {
  const map: Record<MultiApprovalStatus, string> = {
    DRAFT: t('approvals.statusDraft'),
    IN_APPROVAL: t('approvals.statusInApproval'),
    NEED_REVISION: t('approvals.statusNeedRevision'),
    APPROVED: t('approvals.statusApproved'),
    REJECTED: t('approvals.statusRejected'),
  }
  return map[status] ?? status
}

function statusType(status: MultiApprovalStatus): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<MultiApprovalStatus, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    DRAFT: 'info',
    IN_APPROVAL: 'primary',
    NEED_REVISION: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return map[status]
}

function formatAmount(value: number): string {
  return new Intl.NumberFormat(locale.value === 'vi' ? 'vi-VN' : 'en-US').format(value) + ' VND'
}

function fmtDate(dt?: string): string {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString(locale.value === 'vi' ? 'vi-VN' : 'en-US', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(load)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.page-subtitle { margin: 4px 0 0; color: #909399; font-size: 13px; }

.filter-card { margin-bottom: 16px; }
.filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }

.level-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
