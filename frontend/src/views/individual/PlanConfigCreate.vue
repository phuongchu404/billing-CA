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
        <el-radio-group v-model="activeTab" size="default">
          <el-radio-button value="INDIVIDUAL">Cá nhân</el-radio-button>
          <el-radio-button value="ORGANIZATION">Tổ chức</el-radio-button>
          <el-radio-button value="INDIVIDUAL_OF_ORG">Cá nhân thuộc Tổ chức</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="currentRows" border style="margin-top: 12px" table-layout="fixed">
        <el-table-column width="36" align="center">
          <template #default>
            <el-icon class="drag-handle"><Operation /></el-icon>
          </template>
        </el-table-column>

        <el-table-column width="36" align="center">
          <template #default="{ $index }">
            <el-icon class="delete-icon" @click="removeRow($index)"><CircleClose /></el-icon>
          </template>
        </el-table-column>

        <el-table-column label="ĐỐI TƯỢNG" min-width="110">
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
              <el-option label="Lượt ký" value="SIGNING" />
              <el-option label="Số chứng thư" value="CERT_COUNT" />
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
      <el-button type="primary" @click="handleSubmit">Xác Nhận</el-button>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  CopyDocument, CircleClose, CirclePlus, ArrowUp, ArrowDown, Operation,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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

const activeTab = ref<TabKey>('INDIVIDUAL')
const guideVisible = ref(true)
const templateDialogVisible = ref(false)
const selectedTemplate = ref<number | null>(null)

let rowIdSeq = 10

const tabData = reactive<Record<TabKey, ConfigRow[]>>({
  INDIVIDUAL: [
    { id: 1, durationMonths: 1,  condition: 'SIGNING',    minValue: 1,  maxValue: 100,       fee: 0 },
    { id: 2, durationMonths: 3,  condition: 'CERT_COUNT', minValue: 11, maxValue: undefined,  fee: 1000000 },
    { id: 3, durationMonths: 12, condition: 'CERT_COUNT', minValue: 1,  maxValue: undefined,  fee: 0 },
    { id: 4, durationMonths: 24, condition: 'CERT_COUNT', minValue: 1,  maxValue: undefined,  fee: 0 },
    { id: 5, durationMonths: undefined, condition: '', minValue: undefined, maxValue: undefined, fee: undefined },
  ],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
})

const currentRows = computed(() => tabData[activeTab.value])

const templatePlans = [
  { id: 1, name: 'Chữ ký số 2026' },
  { id: 2, name: 'Chữ ký số 2025' },
  { id: 3, name: 'Chữ ký số 2026 tháng 5' },
  { id: 4, name: 'Chữ ký số 2026 tháng 3' },
]

function subjectLabel(tab: TabKey): string {
  const map: Record<TabKey, string> = {
    INDIVIDUAL: 'Cá nhân',
    ORGANIZATION: 'Tổ chức',
    INDIVIDUAL_OF_ORG: 'Cá nhân thuộc Tổ chức',
  }
  return map[tab]
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

function applyTemplate() {
  if (!selectedTemplate.value) {
    ElMessage.warning('Vui lòng chọn gói cước mẫu')
    return
  }
  const plan = templatePlans.find(p => p.id === selectedTemplate.value)
  if (plan) {
    form.name = plan.name + ' (bản sao)'
    ElMessage.success('Đã điền thông tin từ gói cước mẫu')
  }
  templateDialogVisible.value = false
  selectedTemplate.value = null
}

function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('Vui lòng nhập tên gói cước')
    return
  }
  ElMessage.success('Đã tạo gói cước thành công')
  router.push('/individual-plan-config')
}

function handleCancel() {
  router.push('/individual-plan-config')
}
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
