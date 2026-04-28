<template>
  <div class="approval-list-page">
    <div class="page-header">
      <div>
        <h2>Phê duyệt yêu cầu</h2>
        <p class="page-subtitle">Quản lý các yêu cầu phê duyệt gói cước</p>
      </div>
    </div>

    <!-- Filter bar -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select v-model="filterStatus" placeholder="Tất cả trạng thái" clearable style="width: 200px" @change="load">
          <el-option label="Tất cả" value="" />
          <el-option label="Bản nháp" value="DRAFT" />
          <el-option label="Đang duyệt" value="IN_APPROVAL" />
          <el-option label="Cần chỉnh sửa" value="NEED_REVISION" />
          <el-option label="Đã duyệt" value="APPROVED" />
          <el-option label="Bị từ chối" value="REJECTED" />
        </el-select>

        <el-select v-model="filterSegment" placeholder="Loại khách hàng" clearable style="width: 200px" @change="load">
          <el-option label="Tất cả" value="" />
          <el-option label="Khách hàng phổ thông" value="INDIVIDUAL" />
          <el-option label="Khách hàng đại lý" value="GROUP" />
        </el-select>

        <el-button :icon="Refresh" @click="load">Làm mới</el-button>
      </div>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table :data="filteredList" v-loading="loading" border stripe>
        <el-table-column type="index" width="55" :index="i => i + 1">
          <template #header><span>#</span></template>
        </el-table-column>

        <el-table-column label="Mã request" width="100" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">#{{ row.id }}</el-link>
          </template>
        </el-table-column>

        <el-table-column label="Loại KH" width="160">
          <template #default="{ row }">
            <el-tag :type="row.customerSegment === 'GROUP' ? 'warning' : 'info'" size="small">
              {{ segmentLabel(row.customerSegment) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Mô tả" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description }}</template>
        </el-table-column>

        <el-table-column label="Người tạo" width="130">
          <template #default="{ row }">{{ row.requestedBy }}</template>
        </el-table-column>

        <el-table-column label="Giá trị HĐ" width="150" align="right">
          <template #default="{ row }">
            {{ row.contractValue ? formatAmount(row.contractValue) : '—' }}
          </template>
        </el-table-column>

        <el-table-column label="Tiến trình" width="130" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'IN_APPROVAL'" class="level-badge">
              Cấp {{ row.currentLevel }}/{{ row.totalLevels }}
            </span>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column label="Trạng thái" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Ngày tạo" width="160">
          <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
        </el-table-column>

        <el-table-column label="Thao tác" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="goDetail(row.id)">Chi tiết</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listApprovals } from '@/api/approvals'
import type { MultiLevelApprovalResponse, MultiApprovalStatus, CustomerSegment } from '@/api/approvals'

const router = useRouter()
const loading = ref(false)
const list = ref<MultiLevelApprovalResponse[]>([])
const filterStatus = ref('')
const filterSegment = ref('')

const filteredList = computed(() => {
  return list.value.filter(item => {
    const matchStatus = !filterStatus.value || item.status === filterStatus.value
    const matchSegment = !filterSegment.value || item.customerSegment === filterSegment.value
    return matchStatus && matchSegment
  })
})

async function load() {
  loading.value = true
  try {
    const res = await listApprovals()
    list.value = (res as any).data ?? res
  } catch {
    ElMessage.error('Không thể tải danh sách phê duyệt')
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/approvals/${id}`)
}

function segmentLabel(segment: CustomerSegment): string {
  return segment === 'GROUP' ? 'Đại lý' : 'Phổ thông'
}

function statusLabel(status: MultiApprovalStatus): string {
  const map: Record<MultiApprovalStatus, string> = {
    DRAFT: 'Bản nháp',
    IN_APPROVAL: 'Đang duyệt',
    NEED_REVISION: 'Cần chỉnh sửa',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Bị từ chối',
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
  return new Intl.NumberFormat('vi-VN').format(value) + ' VND'
}

function fmtDate(dt?: string): string {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
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
</style>


