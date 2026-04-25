<template>
  <div class="plan-config-create">
    <div class="page-header">
      <div>
        <h2>Thêm mới gói cước</h2>
        <p class="page-subtitle">Khách hàng phổ thông</p>
      </div>
    </div>

    <el-button :icon="CopyDocument" @click="templateDialogVisible = true" style="margin-bottom: 20px">
      Chọn Gói Cước Mẫu
    </el-button>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <div class="section-card">
      <div class="section-title">THÔNG TIN GÓI CƯỚC</div>
      <el-form :model="form" label-width="240px" class="section-form">
        <el-form-item required>
          <template #label>
            <span>Tên gói cước</span>
          </template>
          <div style="width: 100%">
            <el-input v-model="form.name" placeholder="Nhập tên gói cước" />
            <div class="field-hint">Nhập tên gói cước không trùng với gói cước đã có</div>
          </div>
        </el-form-item>
        <el-form-item label="Thời gian áp dụng (không bắt buộc)">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Từ"
            end-placeholder="Đến"
            style="width: 100%"
          />
        </el-form-item>
        <div class="note-text">
          <p>Nếu chọn thời gian áp dụng, hệ thống sẽ đưa gói cước sang trạng thái "Chờ duyệt". Sau khi được duyệt, bảng gói cước sẽ áp dụng từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.</p>
          <p>Nếu không chọn thời gian áp dụng, hệ thống sẽ đưa gói cước sang trạng thái "Khả dụng" và cho phép Yêu cầu áp dụng.</p>
        </div>
      </el-form>
    </div>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <div class="section-card">
      <div class="section-title">CẤU HÌNH GÓI CƯỚC SMARTCA</div>

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

      <el-table :data="currentRows" border style="margin-top: 12px" table-layout="fixed">
        <el-table-column width="60" align="center">
          <template #default="{ $index }">
            <div class="action-cell">
              <el-icon class="drag-handle"><Operation /></el-icon>
              <el-icon class="delete-icon" @click="removeRow($index)"><CircleClose /></el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="PHÂN LOẠI ĐỐI TƯỢNG" min-width="130" sortable>
          <template #default>{{ subjectLabel(activeTab) }}</template>
        </el-table-column>

        <el-table-column label="THỜI HẠN CHỨNG THƯ" width="160" align="center">
          <template #header>
            <span>THỜI HẠN</span><br /><span>CHỨNG THƯ</span>
          </template>
          <template #default="{ row }">
            <div class="inline-cell">
              <el-input-number
                v-model="row.durationMonths"
                :min="1"
                :max="48"
                :controls="false"
                placeholder="Nhập số"
                size="small"
                style="width: 68px"
              />
              <span class="unit-text">tháng</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="ĐIỀU KIỆN" width="150" align="center">
          <template #default="{ row }">
            <el-select v-model="row.condition" size="small" placeholder="Chọn điều kiện" style="width: 100%">
              <el-option label="Lượt ký" value="SIGNING_COUNT" />
              <el-option label="Số chứng thư" value="CERTIFICATE_COUNT" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="GIÁ TRỊ MIN (CỦA ĐIỀU KIỆN)" width="160" align="center">
          <template #header>
            <span>GIÁ TRỊ MIN</span><br /><span>(CỦA ĐIỀU KIỆN)</span>
          </template>
          <template #default="{ row }">
            <el-input-number
              v-model="row.minValue"
              :min="1"
              :controls="false"
              placeholder="Nhập số"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>

        <el-table-column label="GIÁ TRỊ MAX (CỦA ĐIỀU KIỆN)" width="180" align="center">
          <template #header>
            <span>GIÁ TRỊ MAX</span><br /><span>(CỦA ĐIỀU KIỆN)</span>
          </template>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :controls="false"
              placeholder="Để trống là không giới hạn số lượng"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>

        <el-table-column label="PHÍ/ ĐIỀU KIỆN" width="140" align="right">
          <template #default="{ row }">
            <div class="inline-cell fee-cell">
              <el-input-number
                v-model="row.fee"
                :min="0"
                :controls="false"
                placeholder="Nhập số"
                size="small"
                style="width: 80px"
              />
              <span class="unit-text">vnd</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-button link type="primary" :icon="CirclePlus" @click="addRow">Thêm</el-button>
        <el-button link type="primary" @click="guideVisible = !guideVisible">
          Hướng dẫn cấu hình
          <el-icon style="margin-left: 4px"><component :is="guideVisible ? ArrowUp : ArrowDown" /></el-icon>
        </el-button>
      </div>

      <div v-if="guideVisible" class="guide-text">
        <ol>
          <li>Chọn Phân loại đối tượng: Nhấn vào tab tương ứng với đối tượng áp dụng của gói cước (Cá nhân, Tổ chức, hoặc Cá nhân thuộc Tổ chức) và điền đầy đủ cấu hình phí mỗi loại.</li>
          <li>
            <b>Thiết lập chi tiết các thông số:</b>
            <ul>
              <li><b>Thời hạn chứng thư:</b> Nhập số tháng hiệu lực của chứng thư số (VD: 1, 3, 12, 24… giới hạn là 48 tháng)</li>
              <li><b>Điều kiện:</b> Nhấp vào danh sách thả xuống để chọn tiêu chí tính phí (Lượt ký hoặc Số chứng thư).</li>
              <li>
                <b>Giá trị Min</b> (của điều kiện): Nhập giá trị bắt đầu áp dụng mức phí (giá trị tính từ 1).
                <b>Giá trị Max</b> (của điều kiện): Nhập giá trị tối đa để áp dụng mức phí.
                (VD: Nếu bán gói theo lượt ký từ 1 đến 100 lượt, điền Min = 1, Max = 100. Với bước nhảy tiếp theo, điền Min = 101. Nếu không giới hạn số lượng, để trống ô Max)
              </li>
              <li><b>Phí/ Điều kiện:</b> Nhập số tiền (VNĐ) tương ứng với điều kiện (phí trên 1 chứng thư/ lượt ký). Nhập 0 với mức miễn phí hoặc đổi soát ngoài).</li>
            </ul>
          </li>
          <li>Thao tác với bảng dữ liệu: Nhấn (+ Thêm) để tạo các mốc thời gian hoặc điều kiện tính phí mới. Nhấn vào biểu tượng (X) ở đầu dòng để xóa cấu hình tương ứng.</li>
        </ol>
      </div>
    </div>

    <!-- Bottom action bar -->
    <div class="action-bar">
      <el-button type="primary" :loading="submitting" @click="handleSubmit">Xác Nhận</el-button>
      <el-button @click="handleCancel">Hủy Bỏ</el-button>
    </div>

    <!-- Dialog: Chọn Gói Cước Mẫu -->
    <el-dialog
      v-model="templateDialogVisible"
      title="CHỌN GÓI CƯỚC MẪU"
      width="480px"
      align-center
    >
      <p class="dialog-desc">
        Chọn một gói cước đã có, hệ thống tự động điền (copy) toàn bộ thông số cấu hình xuống phía dưới cho gói cước mới này.
      </p>
      <el-form label-width="80px" style="margin-top: 16px">
        <el-form-item label="Gói cước">
          <el-select
            v-model="selectedTemplate"
            placeholder="Chọn gói cước đã có của khách hàng phổ thông"
            style="width: 100%"
          >
            <el-option v-for="p in templatePlans" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>

        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="applyTemplate">Xác Nhận</el-button>
        <el-button @click="templateDialogVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CopyDocument, CircleClose, CirclePlus, ArrowUp, ArrowDown, Operation, CircleCheck,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getIndividualPlanConfigSummary,
  getIndividualPlanConfigDetail,
  createIndividualPlanConfig,
} from '@/api/individual'
import type { IndividualPlanConfigListItem } from '@/types/individual'

type TabKey = 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'

interface ConfigRow {
  id: number
  durationMonths: number | undefined
  condition: string
  minValue: number | undefined
  maxValue: number | undefined
  fee: number | undefined
}

const router = useRouter()

const form = reactive({
  name: '',
  dateRange: null as [Date, Date] | null,
})

const TABS: { key: TabKey; label: string }[] = [
  { key: 'INDIVIDUAL',       label: 'Cá nhân' },
  { key: 'ORGANIZATION',     label: 'Tổ chức' },
  { key: 'INDIVIDUAL_OF_ORG', label: 'Cá nhân thuộc Tổ chức' },
]

const activeTab = ref<TabKey>('INDIVIDUAL')
const guideVisible = ref(true)
const templateDialogVisible = ref(false)
const selectedTemplate = ref<number | null>(null)
const templatePlans = ref<IndividualPlanConfigListItem[]>([])
const submitting = ref(false)

let rowIdSeq = 10

const tabData = reactive<Record<TabKey, ConfigRow[]>>({
  INDIVIDUAL: [],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
})

const currentRows = computed(() => tabData[activeTab.value])

function subjectLabel(tab: TabKey): string {
  const map: Record<TabKey, string> = {
    INDIVIDUAL: 'Cá nhân',
    ORGANIZATION: 'Tổ chức',
    INDIVIDUAL_OF_ORG: 'Cá nhân thuộc Tổ chức',
  }
  return map[tab]
}

function isTabCompleted(tab: TabKey): boolean {
  const rows = tabData[tab]
  return rows.length > 0 && rows.every(r =>
    r.durationMonths != null &&
    r.condition !== '' &&
    r.minValue != null &&
    r.fee != null
  )
}

function addRow() {
  tabData[activeTab.value].push({
    id: ++rowIdSeq,
    durationMonths: undefined,
    condition: '',
    minValue: undefined,
    maxValue: undefined,
    fee: undefined,
  })
}

function removeRow(index: number) {
  tabData[activeTab.value].splice(index, 1)
}

async function applyTemplate() {
  if (!selectedTemplate.value) {
    ElMessage.warning('Vui lòng chọn gói cước mẫu')
    return
  }
  try {
    const res = await getIndividualPlanConfigDetail(selectedTemplate.value)
    if (res.success && res.data) {
      const detail = res.data
      form.name = detail.name + ' (bản sao)'

      // Gán pricing rules vào tabData theo subjectType
      tabData.INDIVIDUAL = []
      tabData.ORGANIZATION = []
      tabData.INDIVIDUAL_OF_ORG = []

      detail.pricingRules.forEach((r, i) => {
        const tab = r.subject as TabKey
        if (tabData[tab]) {
          tabData[tab].push({
            id: ++rowIdSeq,
            durationMonths: r.durationMonths,
            condition: r.condition,
            minValue: r.minValue,
            maxValue: r.maxValue ?? undefined,
            fee: r.fee,
          })
        }
      })

      ElMessage.success('Đã điền thông tin từ gói cước mẫu')
    }
  } catch {
    ElMessage.error('Không thể tải gói cước mẫu')
  }
  templateDialogVisible.value = false
  selectedTemplate.value = null
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('Vui lòng nhập tên gói cước')
    return
  }

  const allRules = [
    ...tabData.INDIVIDUAL.map((r, i) => ({ ...r, subject: 'INDIVIDUAL', sortOrder: i })),
    ...tabData.ORGANIZATION.map((r, i) => ({ ...r, subject: 'ORGANIZATION', sortOrder: i })),
    ...tabData.INDIVIDUAL_OF_ORG.map((r, i) => ({ ...r, subject: 'INDIVIDUAL_OF_ORG', sortOrder: i })),
  ].filter(r => r.durationMonths && r.condition)
   .map(r => ({
     subject: r.subject as string,
     durationMonths: r.durationMonths!,
     condition: r.condition,
     minValue: r.minValue,
     maxValue: r.maxValue,
     fee: r.fee,
     sortOrder: r.sortOrder,
   }))

  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  const payload = {
    name: form.name.trim(),
    applyFrom: form.dateRange ? fmt(form.dateRange[0]) : null,
    applyUntil: form.dateRange ? fmt(form.dateRange[1]) : null,
    pricingRules: allRules,
  }

  submitting.value = true
  try {
    const res = await createIndividualPlanConfig(payload)
    if (res.success) {
      ElMessage.success('Đã tạo gói cước thành công')
      router.push('/individual-plan-config')
    }
  } catch {
    ElMessage.error('Tạo gói cước thất bại')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/individual-plan-config')
}

async function loadTemplatePlans() {
  try {
    const res = await getIndividualPlanConfigSummary()
    if (res.success && res.data) {
      templatePlans.value = res.data.list ?? []
    }
  } catch {
    // không hiển thị lỗi – chỉ là danh sách chọn mẫu
  }
}

onMounted(loadTemplatePlans)
</script>

<style scoped>
.plan-config-create {
  padding-bottom: 80px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0; }
.page-subtitle { margin: 4px 0 0; color: #909399; font-size: 13px; }

.section-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px 24px 24px;
  margin-bottom: 16px;
}

.section-title {
  display: block;
  font-weight: 700;
  font-size: 14px;
  color: #1B60CB;
  letter-spacing: 0.3px;
  margin-bottom: 20px;
}

.section-form {
  max-width: 860px;
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.note-text {
  margin-left: 240px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
}
.note-text p { margin: 0 0 4px; }

.category-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tab-prefix-label {
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
}

/* Custom tab group */
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

/* Tab đang active (đang chọn) */
.cat-tab.is-active {
  border-color: #409eff;
  color: #409eff;
  font-weight: 600;
  background: #fff;
}

/* Tab đã điền đủ dữ liệu bắt buộc */
.cat-tab.is-done {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}

/* Tab vừa done vừa active */
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

/* Action cell (merged drag + delete) */
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.drag-handle {
  cursor: grab;
  color: #909399;
  font-size: 16px;
}

.delete-icon {
  cursor: pointer;
  color: #909399;
  font-size: 16px;
  transition: color 0.15s;
}
.delete-icon:hover { color: #f56c6c; }

.inline-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
.fee-cell { justify-content: flex-end; }
.unit-text { font-size: 13px; color: #606266; white-space: nowrap; }

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.guide-text {
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}
.guide-text ol { margin: 0; padding-left: 18px; }
.guide-text li { margin-bottom: 6px; }
.guide-text ul { margin: 4px 0; padding-left: 18px; }

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 12px 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  z-index: 100;
}

.dialog-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

:deep(.el-input-number .el-input__inner) { text-align: left; }
:deep(.el-table td.el-table__cell) { padding: 6px 0; }
</style>
