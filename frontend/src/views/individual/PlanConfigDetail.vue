<template>
  <div class="plan-config-detail">
    <div class="page-header">
      <div>
        <h2>Chi tiết gói cước</h2>
        <p class="page-subtitle">Khách hàng phổ thông</p>
      </div>
    </div>

    <div class="top-actions" v-if="info">
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
    <div class="section-card" v-loading="loading">
      <div class="section-header" @click="toggleSection('info')">
        <span class="section-title">THÔNG TIN GÓI CƯỚC</span>
        <el-icon class="chevron" :class="{ rotated: !openSections.has('info') }"><ArrowDown /></el-icon>
      </div>
      <div v-show="openSections.has('info')" class="info-grid" v-if="info">
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">Tên gói cước:</span>
            <span class="info-value">{{ info.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Trạng thái:</span>
            <span class="info-value">{{ statusLabel(info.status as any) }}</span>
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
          <div class="cat-tab-group">
            <button
              v-for="tab in TABS"
              :key="tab.key"
              :class="['cat-tab', { 'is-active': activeTab === tab.key, 'is-done': isTabCompleted(tab.key) }]"
              @click="activeTab = tab.key"
              type="button"
            >
              <el-icon v-if="isTabCompleted(tab.key)" class="tab-done-icon"><CircleCheck /></el-icon>
              {{ tab.label }}
            </button>
          </div>
        </div>

        <el-table :data="currentConfigRows" border style="margin-top: 12px" table-layout="fixed">
          <el-table-column type="index" width="55" :index="(i: number) => i + 1">
            <template #header>
              <span>#</span>
            </template>
          </el-table-column>

          <el-table-column prop="subject" sortable min-width="130">
            <template #header>
              <span>PHÂN LOẠI ĐỐI TƯỢNG</span>
            </template>
            <template #default>{{ subjectLabel(activeTab) }}</template>
          </el-table-column>

          <el-table-column prop="durationMonths" sortable width="160" align="center">
            <template #header>
              <span>THỜI HẠN</span><br /><span>CHỨNG THƯ</span>
            </template>
            <template #default="{ row }">{{ row.durationMonths }} tháng</template>
          </el-table-column>

          <el-table-column prop="condition" sortable width="150" align="center">
            <template #header>
              <span>ĐIỀU KIỆN</span>
            </template>
            <template #default="{ row }">{{ conditionLabel(row.condition) }}</template>
          </el-table-column>

          <el-table-column prop="minValue" sortable width="160" align="center">
            <template #header>
              <span>GIÁ TRỊ MIN</span><br /><span>(CỦA ĐIỀU KIỆN)</span>
            </template>
            <template #default="{ row }">{{ row.minValue }}</template>
          </el-table-column>

          <el-table-column prop="maxValue" sortable width="180" align="center">
            <template #header>
              <span>GIÁ TRỊ MAX</span><br /><span>(CỦA ĐIỀU KIỆN)</span>
            </template>
            <template #default="{ row }">
              {{ row.maxValue != null ? row.maxValue : 'Không giới hạn' }}
            </template>
          </el-table-column>

          <el-table-column prop="fee" sortable width="140" align="right">
            <template #header>
              <span>PHÍ/ ĐIỀU KIỆN</span>
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
        <span>Tên: <b>{{ info?.name }}</b></span>
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
        <el-form-item label="Cáº¥p phÃª duyá»‡t">
          <el-select v-model="requestApprovalLevel" style="width: 100%">
            <el-option label="TrÆ°á»Ÿng phÃ²ng kinh doanh" :value="1" />
            <el-option label="CFO (Finance Manager)" :value="2" />
            <el-option label="CEO" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="dlg-note">
        Khi bấm nút Xác Nhận, hệ thống sẽ tự động tạo yêu cầu phê duyệt nhiều cấp và gửi email thông báo
        cho người phê duyệt cấp 1. Số cấp duyệt được xác định theo giá trị gói cước.
      </p>
      <template #footer>
        <el-button type="primary" :loading="requestApplyLoading" @click="confirmRequestApply">Xác Nhận</el-button>
        <el-button @click="requestApplyVisible = false">Huỷ Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog: Approval success -->
    <el-dialog v-model="approvalSuccessVisible" title="YÊU CẦU ĐÃ ĐƯỢC GỬI" width="440px" align-center>
      <div style="text-align:center; padding: 8px 0 16px">
        <el-icon style="font-size:48px; color:#67c23a"><CircleCheck /></el-icon>
        <p style="margin:12px 0 4px; font-size:15px; font-weight:600; color:#303133">
          Yêu cầu áp dụng đã được gửi!
        </p>
        <p style="font-size:13px; color:#606266; margin:0">
          Email thông báo đã được gửi đến người phê duyệt cấp 1.<br />
          Theo dõi tiến trình duyệt tại màn Phê duyệt.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToApproval">Xem tiến trình duyệt</el-button>
        <el-button @click="approvalSuccessVisible = false">Đóng</el-button>
      </template>
    </el-dialog>

    <!-- Dialog 4: VÔ HIỆU HOÁ -->
    <el-dialog v-model="deactivateVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">VÔ HIỆU HOÁ</span>
      </template>
      <p class="dlg-body">
        Bạn đang vô hiệu hoá gói cước {{ info?.name }}.
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Promotion, Remove, ArrowDown, Refresh, CircleCheck } from '@element-plus/icons-vue'
// CircleCheck already imported above — used in approval success dialog
import { ElMessage } from 'element-plus'
import {
  getIndividualPlanConfigDetail,
  requestApplyPlanConfig,
  deactivatePlanConfig,
} from '@/api/individual'
import type { IndividualPlanConfigDetail, IndividualPricingRuleRow, IndividualStatusHistoryRow } from '@/types/individual'

type TabKey = 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
type PlanStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'PENDING' | 'APPROVED' | 'APPLYING'

const route = useRoute()
const router = useRouter()

const planId = Number(route.params.id)
const loading = ref(false)
const openSections = ref<Set<string>>(new Set(['info', 'config', 'history']))
const activeTab = ref<TabKey>('INDIVIDUAL')
const historyPage = ref(1)

const TABS: { key: TabKey; label: string }[] = [
  { key: 'INDIVIDUAL', label: 'Cá nhân' },
  { key: 'ORGANIZATION', label: 'Tổ chức' },
  { key: 'INDIVIDUAL_OF_ORG', label: 'Cá nhân thuộc Tổ chức' },
]

// Dialog state
const requestApplyVisible = ref(false)
const requestApplyDateRange = ref<[Date, Date] | null>(null)
const requestApprovalLevel = ref<1 | 2 | 3>(1)
const requestApplyLoading = ref(false)
const approvalSuccessVisible = ref(false)
const lastApprovalId = ref<number | null>(null)
const deactivateVisible = ref(false)

const info = ref<IndividualPlanConfigDetail | null>(null)

const tabData = reactive<Record<TabKey, IndividualPricingRuleRow[]>>({
  INDIVIDUAL: [],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
})

const statusHistory = ref<IndividualStatusHistoryRow[]>([])

const currentConfigRows = computed(() => tabData[activeTab.value])

function toggleSection(key: string) {
  const s = new Set(openSections.value)
  s.has(key) ? s.delete(key) : s.add(key)
  openSections.value = s
}

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

function isTabCompleted(tab: TabKey): boolean {
  return tabData[tab].length > 0
}

function conditionLabel(condition: string): string {
  if (condition === 'SIGNING_COUNT') return 'Lượt ký'
  if (condition === 'CERTIFICATE_COUNT') return 'Số chứng thư'
  return condition
}

function formatFee(fee: number): string {
  return fee.toLocaleString('vi-VN')
}

async function load() {
  loading.value = true
  try {
    const res = await getIndividualPlanConfigDetail(planId)
    if (res.success && res.data) {
      info.value = res.data

      tabData.INDIVIDUAL = res.data.pricingRules.filter(r => r.subject === 'INDIVIDUAL')
      tabData.ORGANIZATION = res.data.pricingRules.filter(r => r.subject === 'ORGANIZATION')
      tabData.INDIVIDUAL_OF_ORG = res.data.pricingRules.filter(r => r.subject === 'INDIVIDUAL_OF_ORG')

      statusHistory.value = res.data.statusHistory ?? []
    }
  } catch {
    ElMessage.error('Không thể tải thông tin gói cước')
  } finally {
    loading.value = false
  }
}

async function confirmRequestApply() {
  if (!requestApplyDateRange.value) {
    ElMessage.warning('Vui lòng chọn thời gian áp dụng')
    return
  }
  const [from, to] = requestApplyDateRange.value
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  requestApplyLoading.value = true
  try {
    const res = await requestApplyPlanConfig(planId, {
      applyFrom: fmt(from),
      applyUntil: fmt(to),
      approvalLevel: requestApprovalLevel.value,
    })
    if (res.data?.approvalRequestId) {
      lastApprovalId.value = res.data.approvalRequestId
    }
    requestApplyVisible.value = false
    approvalSuccessVisible.value = true
    load()
  } catch {
    ElMessage.error('Gửi yêu cầu thất bại')
  } finally {
    requestApplyLoading.value = false
  }
}

function goToApproval() {
  approvalSuccessVisible.value = false
  router.push(lastApprovalId.value ? `/approvals/${lastApprovalId.value}` : '/approvals')
}

async function confirmDeactivate() {
  try {
    await deactivatePlanConfig(planId)
    ElMessage.success('Đã vô hiệu hóa gói cước')
    deactivateVisible.value = false
    router.push('/individual-plan-config')
  } catch {
    ElMessage.error('Vô hiệu hóa thất bại')
  }
}

onMounted(load)
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
.cat-tab-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 18px;
  font-size: 14px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  user-select: none;
  transition: border-color 0.15s, background 0.15s, color 0.15s;
  line-height: 1.4;
  outline: none;
  white-space: nowrap;
}
.cat-tab.is-active {
  border-color: #409eff;
  color: #409eff;
  font-weight: 600;
  background: #fff;
}
.cat-tab.is-done {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}
.cat-tab.is-done.is-active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}
.tab-done-icon {
  font-size: 14px;
  color: #409eff;
  flex-shrink: 0;
}

/* Section inner padding for tables */
.section-card > div:not(.section-header) {
  padding: 0 20px 20px;
}

/* Column filter row pattern */
:deep(.el-table th.el-table__cell) {
  color: #606266;
  font-size: 12px;
  font-weight: 600;
}
.col-label { font-weight: 600; font-size: 13px; white-space: normal; line-height: 1.3; }
.col-filter { min-height: 28px; }

:deep(.el-table th.el-table__cell) { vertical-align: top; padding: 8px 0; }
:deep(.el-table td.el-table__cell) { padding: 8px 0; }

/* Dialog styles */
.dlg-title { font-weight: 700; font-size: 15px; color: #303133; }
.dlg-name-row { font-size: 14px; color: #303133; }
.dlg-body { font-size: 14px; color: #303133; line-height: 1.6; margin: 0; }
.dlg-note { font-size: 13px; color: #606266; line-height: 1.6; margin: 12px 0 0; }
</style>
