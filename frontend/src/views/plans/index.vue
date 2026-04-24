<template>
  <div>
    <div class="page-header">
      <h2>Khách hàng đại lý</h2>
    </div>

    <el-card shadow="never">
      <div class="info-bar">
        <span>SL đại lý đang hoạt động: <b>{{ activeCount }}</b></span>
        <el-button type="primary" :icon="Plus" @click="handleAddNew">Thêm Mới</el-button>
        <el-button :icon="Download" @click="handleExport">Xuất Dữ Liệu Đối Soát</el-button>
        <span class="last-updated">Lần cập nhật cuối: {{ lastUpdated }}</span>
      </div>

      <div class="pagination-row">
        <span class="page-label">
          Hiển thị
          <el-select v-model="pageSize" size="small" style="width:64px;margin:0 4px" @change="page = 1">
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ filteredList.length }} đại lý
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="filteredList.length"
          :page-size="pageSize"
          layout="prev, pager, next"
          :pager-count="5"
          background
        />
      </div>

      <el-table :data="pagedList" v-loading="loading" border>
        <el-table-column width="55" type="index" :index="(i: number) => (page - 1) * pageSize + i + 1">
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="load" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="groupCode" sortable width="110">
          <template #header>
            <div class="col-label">MÃ ĐẠI LÝ</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="groupName" sortable min-width="180">
          <template #header>
            <div class="col-label">TÊN ĐẠI LÝ</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable width="140">
          <template #header>
            <div class="col-label">TRẠNG THÁI</div>
            <div class="col-filter">
              <el-select v-model="filterStatus" size="small" clearable placeholder="" style="width:100%">
                <el-option label="Đang hoạt động" value="ACTIVE" />
                <el-option label="Tạm dừng" value="INACTIVE" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <span :class="row.status === 'ACTIVE' ? 'text-active' : 'text-inactive'">
              {{ row.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm dừng' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="currentPlan" sortable min-width="160">
          <template #header>
            <div class="col-label">GÓI CƯỚC HIỆN TẠI</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.currentPlan ?? '' }}</template>
        </el-table-column>

        <el-table-column prop="applyUntil" sortable width="130">
          <template #header>
            <div class="col-label">ÁP DỤNG ĐẾN</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterApplyUntil"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width:100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyUntil ?? '' }}</template>
        </el-table-column>

        <el-table-column prop="ctsCreated" sortable width="130">
          <template #header>
            <div class="col-label">SL CTS ĐÃ TẠO</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{ row.ctsCreated != null ? row.ctsCreated.toLocaleString() : '' }}
          </template>
        </el-table-column>

        <el-table-column prop="ctsCreatedPct" sortable width="130">
          <template #header>
            <div class="col-label">% CTS ĐÃ TẠO</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.ctsCreatedPct ?? '' }}</template>
        </el-table-column>

        <el-table-column prop="signingUsed" sortable width="130">
          <template #header>
            <div class="col-label">SL LƯỢT ĐÃ KÝ</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{ row.signingUsed != null ? row.signingUsed.toLocaleString() : '' }}
          </template>
        </el-table-column>

        <el-table-column prop="signingUsedPct" sortable width="130">
          <template #header>
            <div class="col-label">% LƯỢT ĐÃ KÝ</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.signingUsedPct ?? '' }}</template>
        </el-table-column>

        <el-table-column prop="updatedAt" sortable width="175">
          <template #header>
            <div class="col-label">THỜI GIAN CẬP NHẬT</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterUpdatedAt"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width:100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.updatedAt ?? '' }}</template>
        </el-table-column>

        <el-table-column fixed="right" width="195">
          <template #header>
            <div class="col-label">HÀNH ĐỘNG</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" :icon="View" @click.stop="goDetail(row)">Chi tiết</el-button>
              <el-button size="small" :icon="Document" @click.stop="handleExportRow(row)">Xuất đối soát</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row" style="margin-top:12px">
        <span class="page-label">
          Hiển thị
          <el-select v-model="pageSize" size="small" style="width:64px;margin:0 4px" @change="page = 1">
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ filteredList.length }} đại lý
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="filteredList.length"
          :page-size="pageSize"
          layout="prev, pager, next"
          :pager-count="5"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Download, Refresh, View, Document } from '@element-plus/icons-vue'
import { listPartners } from '@/api/groups'
import type { Partner } from '@/types'

const router = useRouter()

interface AgencyRow {
  groupId: number
  groupCode: string
  groupName: string
  status: 'ACTIVE' | 'INACTIVE'
  currentPlan: string | null
  applyUntil: string | null
  ctsCreated: number | null
  ctsCreatedPct: string | null
  signingUsed: number | null
  signingUsedPct: string | null
  updatedAt: string | null
}

const MOCK_DATA: AgencyRow[] = [
  { groupId: 1, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '01/05/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 2, groupCode: 'TCB 1', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'INACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: 'Hết hạn', ctsCreated: null, ctsCreatedPct: null, signingUsed: null, signingUsedPct: null, updatedAt: '27/01/2026 18:29:00' },
  { groupId: 3, groupCode: 'TCB 2', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '01/05/2026', ctsCreated: 10000, ctsCreatedPct: 'Không giới hạn', signingUsed: 20000, signingUsedPct: '88%', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 4, groupCode: 'TCB 3', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '01/05/2026', ctsCreated: 10000, ctsCreatedPct: 'Không giới hạn', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 5, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 6, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 7, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 8, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 9, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
  { groupId: 10, groupCode: 'TCB', groupName: 'Ngân hàng TMCP Kỹ Thương Việt Nam', status: 'ACTIVE', currentPlan: 'TCB_ Ưu đãi tháng 3', applyUntil: '27/03/2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn', updatedAt: '27/03/2026 18:29:00' },
]

const list = ref<AgencyRow[]>(MOCK_DATA)
const loading = ref(false)
const lastUpdated = ref('29/03/2026 16:29:00')
const filterStatus = ref('')
const filterApplyUntil = ref<Date | null>(null)
const filterUpdatedAt = ref<Date | null>(null)
const page = ref(1)
const pageSize = ref(10)

const activeCount = computed(() => list.value.filter(r => r.status === 'ACTIVE').length)

function formatDatetime(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function mapPartner(p: Partner): AgencyRow {
  return {
    groupId: p.groupId,
    groupCode: p.groupCode,
    groupName: p.groupName,
    status: p.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
    currentPlan: null,
    applyUntil: null,
    ctsCreated: null,
    ctsCreatedPct: null,
    signingUsed: null,
    signingUsedPct: null,
    updatedAt: p.createdAt ? formatDatetime(new Date(p.createdAt)) : null,
  }
}

const filteredList = computed(() => {
  let result = list.value
  if (filterStatus.value) result = result.filter(r => r.status === filterStatus.value)
  if (filterApplyUntil.value) {
    const d = filterApplyUntil.value
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
    result = result.filter(r => r.applyUntil?.startsWith(dateStr))
  }
  if (filterUpdatedAt.value) {
    const d = filterUpdatedAt.value
    const dateStr = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
    result = result.filter(r => r.updatedAt?.startsWith(dateStr))
  }
  return result
})

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

watch([filterStatus, filterApplyUntil, filterUpdatedAt], () => { page.value = 1 })

async function load() {
  loading.value = true
  try {
    const res = await listPartners()
    if (res.success && res.data && res.data.length > 0) {
      list.value = res.data.map(mapPartner)
    }
    lastUpdated.value = formatDatetime(new Date())
  } finally {
    loading.value = false
  }
}

function handleAddNew() {
  router.push('/plans/new')
}

function handleExport() {
  // TODO: implement bulk export
}

function goDetail(row: AgencyRow) {
  router.push('/plans/' + row.groupId)
}

function handleExportRow(_row: AgencyRow) {
  // TODO: implement per-row export
}

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }

.info-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.last-updated { margin-left: auto; font-size: 12px; color: #909399; }

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.page-label { font-size: 13px; color: #606266; display: flex; align-items: center; }

.col-label { font-weight: 600; font-size: 13px; white-space: normal; line-height: 1.3; }
.col-filter { margin-top: 6px; min-height: 28px; }

.text-active { color: #1B60CB; font-weight: 500; }
.text-inactive { color: #FF9F43; font-weight: 500; }

.action-btns { display: flex; gap: 4px; flex-wrap: nowrap; }
.action-btns :deep(.el-button) { margin: 0; }

:deep(.el-table th.el-table__cell) { vertical-align: top; padding: 8px 0; }
</style>
