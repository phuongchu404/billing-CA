<template>
  <div class="plan-config-detail">
    <div class="page-header">
      <div>
        <h2>Chi tiết gói cước</h2>
        <p class="page-subtitle">Khách hàng phổ thông</p>
      </div>
    </div>

    <div class="top-actions">
      <el-button
        v-if="info.status === 'AVAILABLE'"
        :icon="Promotion"
        type="primary"
        plain
        @click="requestApplyVisible = true"
      >
        Y/C Áp Dụng
      </el-button>
      <el-button :icon="Remove" @click="deactivateVisible = true">Vô Hiệu Hóa</el-button>
    </div>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <div class="section-card">
      <div class="section-header" @click="toggleSection('info')">
        <span class="section-title">THÔNG TIN GÓI CƯỚC</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('info') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('info')" class="info-grid">
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">Tên gói cước:</span>
            <span class="info-value">{{ info.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Trạng thái:</span>
            <span class="info-value">{{ statusLabel(info.status) }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">Áp dụng từ:</span>
            <span class="info-value">{{ info.applyFrom ?? '' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Áp dụng đến:</span>
            <span class="info-value">{{ info.applyUntil ?? '' }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item info-item--full">
            <span class="info-label">Lịch sử áp dụng:</span>
            <span class="info-value">{{ info.applyHistory }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">Người tạo:</span>
            <span class="info-value">{{ info.createdBy }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Thời gian tạo:</span>
            <span class="info-value">{{ info.createdAt }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">Thời gian cập nhật:</span>
            <span class="info-value">{{ info.updatedAt }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <div class="section-card">
      <div class="section-header" @click="toggleSection('config')">
        <span class="section-title">CẤU HÌNH GÓI CƯỚC SMARTCA</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('config') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('config')">
        <div class="category-tabs">
          <span class="tab-prefix-label">Phân loại</span>
          <el-radio-group v-model="activeTab" size="default">
            <el-radio-button value="INDIVIDUAL">Cá nhân</el-radio-button>
            <el-radio-button value="ORGANIZATION">Tổ chức</el-radio-button>
            <el-radio-button value="INDIVIDUAL_OF_ORG">Cá nhân thuộc Tổ chức</el-radio-button>
          </el-radio-group>
        </div>

        <el-table :data="currentConfigRows" border style="margin-top: 12px">
          <el-table-column type="index" width="55" :index="(i: number) => i + 1">
            <template #header>
              <div class="col-label">#</div>
              <div class="col-filter">
                <el-button link :icon="Refresh" @click="configFilterStatus = ''" />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="subject" sortable min-width="130">
            <template #header>
              <div class="col-label">ĐỐI TƯỢNG</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ subjectLabel(row.subject) }}</template>
          </el-table-column>

          <el-table-column prop="durationMonths" sortable width="160">
            <template #header>
              <div class="col-label">THỜI HẠN<br />CHỨNG THƯ</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ row.durationMonths }} tháng</template>
          </el-table-column>

          <el-table-column prop="condition" sortable width="150">
            <template #header>
              <div class="col-label">ĐIỀU KIỆN</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ conditionLabel(row.condition) }}</template>
          </el-table-column>

          <el-table-column prop="minValue" sortable width="160" align="right">
            <template #header>
              <div class="col-label">GIÁ TRỊ MIN<br />(CỦA ĐIỀU KIỆN)</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ row.minValue }}</template>
          </el-table-column>

          <el-table-column prop="maxValue" sortable width="170" align="right">
            <template #header>
              <div class="col-label">GIÁ TRỊ MAX<br />(CỦA ĐIỀU KIỆN)</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">
              {{ row.maxValue != null ? row.maxValue : 'Không giới hạn' }}
            </template>
          </el-table-column>

          <el-table-column prop="fee" sortable width="140" align="right">
            <template #header>
              <div class="col-label">PHÍ/ ĐIỀU KIỆN</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ formatFee(row.fee) }} vnd</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- LỊCH SỬ CẬP NHẬT TRẠNG THÁI -->
    <div class="section-card">
      <div class="section-header" @click="toggleSection('history')">
        <span class="section-title">LỊCH SỬ CẬP NHẬT TRẠNG THÁI</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('history') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('history')">
        <el-table :data="statusHistory" border>
          <el-table-column type="index" width="55" :index="(i: number) => i + 1">
            <template #header>
              <div class="col-label">#</div>
              <div class="col-filter">
                <el-button link :icon="Refresh" @click="historyPage = 1" />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" sortable min-width="140">
            <template #header>
              <div class="col-label">TRẠNG THÁI</div>
              <div class="col-filter"></div>
            </template>
            <template #default="{ row }">{{ statusLabel(row.status) }}</template>
          </el-table-column>

          <el-table-column prop="updatedAt" sortable width="190">
            <template #header>
              <div class="col-label">THỜI GIAN CẬP NHẬT</div>
              <div class="col-filter"></div>
            </template>
          </el-table-column>

          <el-table-column prop="updatedBy" sortable width="180">
            <template #header>
              <div class="col-label">TÀI KHOẢN CẬP NHẬT</div>
              <div class="col-filter"></div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Dialog 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC -->
    <el-dialog v-model="requestApplyVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC</span>
      </template>
      <div class="dlg-name-row">
        <span>Tên: <b>{{ info.name }}</b></span>
      </div>
      <el-form label-width="140px" style="margin-top: 16px">
        <el-form-item label="Thời gian áp dụng">
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Từ"
            end-placeholder="Đến"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <p class="dlg-note">
        Khi bấm nút Xác Nhận, hệ thống sẽ đưa phiên bản hiện tại sẽ chuyển sang trạng thái "Chờ duyệt".
        Sau khi được duyệt, bảng gói cước sẽ được áp dụng từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
      </p>
      <template #footer>
        <el-button type="primary" @click="confirmRequestApply">Xác Nhận</el-button>
        <el-button @click="requestApplyVisible = false">Huỷ Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog 4: VÔ HIỆU HOÁ -->
    <el-dialog v-model="deactivateVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">VÔ HIỆU HOÁ</span>
      </template>
      <p class="dlg-body">
        Bạn đang vô hiệu hoá gói cước {{ info.name }}.
        Cấu hình gói cước này sẽ không còn khả dụng. Nhấn "Xác Nhận" để vô hiệu hoá.
      </p>
      <template #footer>
        <el-button type="primary" @click="confirmDeactivate">Xác Nhận</el-button>
        <el-button @click="deactivateVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Promotion, Remove, ArrowDown, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

type TabKey = 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
type PlanStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'PENDING' | 'APPROVED' | 'APPLYING'

interface ConfigRow {
  id: number
  subject: TabKey
  durationMonths: number
  condition: string
  minValue: number
  maxValue: number | null
  fee: number
}

interface HistoryRow {
  id: number
  status: PlanStatus
  updatedAt: string
  updatedBy: string
}

const route = useRoute()
const router = useRouter()

const openSections = ref<Set<string>>(new Set(['info', 'config', 'history']))
const activeTab = ref<TabKey>('INDIVIDUAL')
const configFilterStatus = ref('')
const historyPage = ref(1)

// Dialog state
const requestApplyVisible = ref(false)
const requestApplyDateRange = ref<[Date, Date] | null>(null)
const deactivateVisible = ref(false)

function toggleSection(key: string) {
  const s = new Set(openSections.value)
  s.has(key) ? s.delete(key) : s.add(key)
  openSections.value = s
}

const info = reactive({
  id: Number(route.params.id) || 1,
  name: 'Chữ ký số 2026 tháng 5_2',
  status: 'AVAILABLE' as PlanStatus,
  applyFrom: null as string | null,
  applyUntil: null as string | null,
  applyHistory: '01/01/2026 - 31/01/2026, 15/03/2026-22/03/2026',
  createdBy: 'admin123',
  createdAt: '30/03/2026 16:29:00',
  updatedAt: '30/03/2026 16:29:00',
})

const tabData: Record<TabKey, ConfigRow[]> = reactive({
  INDIVIDUAL: [
    { id: 1, subject: 'INDIVIDUAL', durationMonths: 1,  condition: 'SIGNING',    minValue: 1,  maxValue: 100,  fee: 0 },
    { id: 2, subject: 'INDIVIDUAL', durationMonths: 3,  condition: 'CERT_COUNT', minValue: 1,  maxValue: null, fee: 300000 },
    { id: 3, subject: 'INDIVIDUAL', durationMonths: 12, condition: 'CERT_COUNT', minValue: 1,  maxValue: null, fee: 1000000 },
    { id: 4, subject: 'INDIVIDUAL', durationMonths: 24, condition: 'CERT_COUNT', minValue: 1,  maxValue: null, fee: 1800000 },
    { id: 5, subject: 'INDIVIDUAL', durationMonths: 48, condition: 'CERT_COUNT', minValue: 1,  maxValue: 10,   fee: 3000000 },
    { id: 6, subject: 'INDIVIDUAL', durationMonths: 48, condition: 'CERT_COUNT', minValue: 11, maxValue: null, fee: 2700000 },
  ],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
})

const currentConfigRows = computed(() => tabData[activeTab.value])

const statusHistory: HistoryRow[] = [
  { id: 1, status: 'UNAVAILABLE', updatedAt: '30/03/2026 16:29:00', updatedBy: 'admin' },
  { id: 2, status: 'AVAILABLE',   updatedAt: '30/03/2026 16:29:00', updatedBy: 'admin' },
  { id: 3, status: 'APPROVED',    updatedAt: '30/03/2026 16:29:00', updatedBy: 'system' },
  { id: 4, status: 'PENDING',     updatedAt: '30/03/2026 16:29:00', updatedBy: 'system' },
  { id: 5, status: 'AVAILABLE',   updatedAt: '30/03/2026 16:29:00', updatedBy: 'system' },
]

function statusLabel(status: PlanStatus): string {
  const map: Record<PlanStatus, string> = {
    AVAILABLE: 'Khả dụng',
    UNAVAILABLE: 'Không khả dụng',
    PENDING: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    APPLYING: 'Đang áp dụng',
  }
  return map[status] ?? status
}

function subjectLabel(tab: TabKey): string {
  const map: Record<TabKey, string> = {
    INDIVIDUAL: 'Cá nhân',
    ORGANIZATION: 'Tổ chức',
    INDIVIDUAL_OF_ORG: 'Cá nhân thuộc Tổ chức',
  }
  return map[tab]
}

function conditionLabel(condition: string): string {
  return condition === 'SIGNING' ? 'Lượt ký' : condition === 'CERT_COUNT' ? 'Số chứng thư' : condition
}

function formatFee(fee: number): string {
  return fee.toLocaleString('vi-VN')
}

function confirmRequestApply() {
  ElMessage.success('Đã gửi yêu cầu áp dụng')
  requestApplyVisible.value = false
}

function confirmDeactivate() {
  ElMessage.success('Đã vô hiệu hóa gói cước')
  deactivateVisible.value = false
  router.push('/individual-plan-config')
}
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
.page-header h2 { margin: 0; }
.page-subtitle { margin: 4px 0 0; color: #909399; font-size: 13px; }

.top-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

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
  cursor: pointer;
  user-select: none;
}
.section-header:hover { background: #fafafa; }

.section-title {
  font-weight: 700;
  font-size: 14px;
  color: #1B60CB;
  letter-spacing: 0.3px;
}

.chevron {
  color: #1B60CB;
  font-size: 14px;
  transition: transform 0.2s;
}
.chevron.rotated { transform: rotate(-90deg); }

/* Info grid */
.info-grid {
  padding: 4px 20px 20px;
}
.info-row {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}
.info-item {
  flex: 1;
  display: flex;
  gap: 4px;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}
.info-item--full { flex: 2; }
.info-label { color: #303133; white-space: nowrap; }
.info-value { color: #303133; }

/* Tabs */
.category-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 0;
  margin: 0 0 0;
}
.tab-prefix-label {
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
  padding-left: 0;
}

/* Section inner padding for tables */
.section-card > div:not(.section-header) {
  padding: 0 20px 20px;
}

/* Column filter row pattern */
.col-label { font-weight: 600; font-size: 13px; white-space: normal; line-height: 1.3; }
.col-filter { margin-top: 6px; min-height: 28px; }

:deep(.el-table th.el-table__cell) { vertical-align: top; padding: 8px 0; }
:deep(.el-table td.el-table__cell) { padding: 8px 0; }

/* Dialog styles */
.dlg-title { font-weight: 700; font-size: 15px; color: #303133; }
.dlg-name-row { font-size: 14px; color: #303133; }
.dlg-body { font-size: 14px; color: #303133; line-height: 1.6; margin: 0; }
.dlg-note { font-size: 13px; color: #606266; line-height: 1.6; margin: 12px 0 0; }
</style>
