<template>
  <div>
    <div class="page-header">
      <h2>Thêm mới gói cước</h2>
      <span class="breadcrumb">Khách hàng đại lý</span>
    </div>

    <el-button :icon="Document" style="margin-bottom:20px" @click="templateVisible = true">
      Chọn Gói Cước Mẫu
    </el-button>

    <!-- THÔNG TIN ĐẠI LÝ (read-only) -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">THÔNG TIN ĐẠI LÝ</div>
      <div class="info-grid">
        <div class="info-row two-col">
          <span><b>Tên đại lý:</b> {{ agency.groupName }}</span>
          <span><b>Mã đại lý:</b> {{ agency.groupCode }}</span>
        </div>
        <div class="info-row">
          <span><b>Email nhân sự phụ trách:</b> {{ agency.picEmails.join(', ') }}</span>
        </div>
        <div class="info-row">
          <span><b>Email người đại diện đại lý:</b> {{ agency.contactEmails.join(', ') }}</span>
        </div>
        <div class="info-row">
          <span><b>Mã hợp đồng tham chiếu:</b> {{ agency.refContractNo }}</span>
        </div>
      </div>
    </el-card>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">THÔNG TIN GÓI CƯỚC</div>
      <el-form :model="form" label-width="220px" label-position="left">
        <el-form-item label="Tên gói cước">
          <el-input v-model="form.planName" placeholder="Nhập tên gói cước" />
          <div class="field-hint">Nhập tên gói cước không trùng với gói cước đã có</div>
        </el-form-item>
        <el-form-item label="Thời gian áp dụng (không bắt buộc)">
          <el-date-picker
            v-model="form.applyDateRange"
            type="daterange"
            start-placeholder="Từ"
            range-separator="-"
            end-placeholder="Đến"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <div class="plan-notes">
        <p>
          Nếu chọn thời gian áp dụng, hệ thống sẽ đưa gói cước sang trạng thái <b>"Chờ duyệt"</b>.
          Sau khi được duyệt, bảng gói cước sẽ được áp dụng từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
        </p>
        <p>
          Nếu không chọn thời gian áp dụng, hệ thống sẽ đưa gói cước sang trạng thái <b>"Khả dụng"</b>
          và cho phép Yêu cầu áp dụng.
        </p>
      </div>
    </el-card>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">CẤU HÌNH GÓI CƯỚC SMARTCA</div>
      <el-table :data="configRows" border>
        <el-table-column type="index" width="50" />
        <el-table-column label="ĐỐI TƯỢNG" width="160" sortable>
          <template #default="{ row }">
            <span class="subject-label">{{ row.subject }}</span>
          </template>
        </el-table-column>

        <el-table-column label="THỜI HẠN CHỨNG THƯ" width="165" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.duration" :min="0" :max="48" controls-position="right" style="width:80px" size="small" />
              <span>tháng</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="ĐIỀU KIỆN" width="150" sortable>
          <template #default="{ row }">
            <el-select v-model="row.condition" size="small" style="width:100%">
              <el-option label="Lượt ký" value="signing" />
              <el-option label="Số chứng thư" value="certificate" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="GIÁ TRỊ MIN (CỦA ĐIỀU KIỆN)" width="175" sortable>
          <template #default="{ row }">
            <el-input-number v-model="row.minValue" :min="1" controls-position="right" style="width:100%" size="small" />
          </template>
        </el-table-column>

        <el-table-column label="GIÁ TRỊ MAX (CỦA ĐIỀU KIỆN)" width="175" sortable>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :value-on-clear="null"
              controls-position="right"
              style="width:100%"
              size="small"
              placeholder="Để trống là không giới hạn số lượng"
            />
          </template>
        </el-table-column>

        <el-table-column label="PHÍ/ ĐIỀU KIỆN" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number v-model="row.fee" :min="0" controls-position="right" style="flex:1" size="small" />
              <span>vnđ</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="guide-toggle" @click="showGuide = !showGuide">
        Hướng dẫn cấu hình
        <el-icon><ArrowUp v-if="showGuide" /><ArrowDown v-else /></el-icon>
      </div>

      <div v-show="showGuide" class="guide-content">
        <ul>
          <li>Thiết lập chi tiết các thông số:</li>
          <li>
            <b>Thời hạn chứng thư:</b> Nhập số tháng hiệu lực của chứng thư số
            (VD: 1, 3, 12, 24... giới hạn là 48 tháng). Nhập 0 nếu không cung cấp dịch vụ cho phân loại đối tượng tương ứng.
          </li>
          <li>
            <b>Điều kiện:</b> Nhập vào danh sách thả xuống để chọn tiêu chí tính phí (Lượt ký hoặc Số chứng thư).
          </li>
          <li>
            <b>Giá trị Min (của điều kiện):</b> Nhập giá trị bắt đầu áp dụng mức phí (giá trị tính từ 1).
            <b>Giá trị Max (của điều kiện):</b> Nhập giá trị tối đa áp dụng mức phí.
            (VD: Nếu bán gói theo lượt ký từ 1 đến 100 lượt, điền Min = 1, Max = 100.
            Nếu không giới hạn số lượng, để trống ô Max)
            <br />
            <u>Lưu ý:</u> Khi số chứng thư đạt 85% giá trị Max, hệ thống sẽ gửi email thông báo
            tới Nhân sự phụ trách và Đại diện đại lý.
          </li>
          <li>
            <b>Phí/ Điều kiện:</b> Nhập số tiền (VND) tương ứng với điều kiện
            (phí trên 1 chứng thư/ lượt ký). Nhập 0 với mức miễn phí hoặc đối soát ngoài).
          </li>
        </ul>
      </div>
    </el-card>

    <div class="footer-btns">
      <el-button type="primary" @click="handleSubmit">Xác Nhận</el-button>
      <el-button @click="handleCancel">Hủy Bỏ</el-button>
    </div>

    <!-- ===== DIALOG: CHỌN GÓI CƯỚC MẪU ===== -->
    <el-dialog v-model="templateVisible" title="CHỌN GÓI CƯỚC MẪU" width="460px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          Chọn một gói cước đã có, hệ thống tự động điền (copy) toàn bộ thông số
          cấu hình xuống phía dưới cho gói cước mới này.
        </p>
        <div class="dlg-select-row">
          <span class="dlg-select-label">Gói cước</span>
          <el-select
            v-model="selectedTemplate"
            placeholder="Chọn gói cước đã có của khách hàng đại lý"
            style="flex:1"
            clearable
          >
            <el-option
              v-for="t in templateOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!selectedTemplate" @click="confirmTemplate">Xác Nhận</el-button>
        <el-button @click="templateVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const agencyId = Number(route.params.id)

interface ConfigRow {
  subject: string
  duration: number
  condition: 'signing' | 'certificate'
  minValue: number
  maxValue: number | null
  fee: number
}

// Dữ liệu đại lý hiển thị read-only — thay bằng API call theo agencyId thực tế
const agency = reactive({
  groupCode: 'TCB_01',
  groupName: 'Techcombank',
  picEmails: ['quangbt-hn@mk.com.vn', 'baclt-hn@mk.com.vn', 'anhnt-hn@mk.com.vn'],
  contactEmails: ['quangbt-hn@mk.com.vn', 'baclt-hn@mk.com.vn', 'anhnt-hn@mk.com.vn'],
  refContractNo: 'HĐ/ĐL/AG001_202603_0001, HĐ/ĐL/AG015_202604_0002',
})

const form = reactive({
  planName: '',
  applyDateRange: null as [string, string] | null,
})

const configRows = reactive<ConfigRow[]>([
  { subject: 'Cá nhân', duration: 1, condition: 'signing', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Tổ chức', duration: 24, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Cá nhân thuộc tổ chức', duration: 12, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
])

const showGuide = ref(true)

// --- Template dialog ---
const templateVisible = ref(false)
const selectedTemplate = ref<string | null>(null)

const templateOptions = [
  { label: 'Chữ ký số 2026', value: 'plan_1' },
  { label: 'Chữ ký số 2026 tháng 5_2', value: 'plan_2' },
  { label: 'Chữ ký số 2026 tháng 5', value: 'plan_3' },
  { label: 'Chữ ký số 2026 tháng 3', value: 'plan_4' },
]

function confirmTemplate() {
  // TODO: fetch config of selectedTemplate and fill configRows
  templateVisible.value = false
}

function handleSubmit() {
  // TODO: validate and call API
  router.push('/plans/' + agencyId)
}

function handleCancel() {
  router.push('/plans/' + agencyId)
}
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
.page-header h2 { margin: 0; font-size: 20px; }
.breadcrumb { font-size: 13px; color: #909399; }

.section-card { margin-bottom: 16px; }
.section-title {
  color: #1B60CB;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
}

.info-grid { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #303133; }
.info-row { line-height: 1.6; }
.info-row.two-col { display: flex; gap: 60px; }

.field-hint { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.4; }

.plan-notes {
  padding-left: 220px;
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.plan-notes p { margin: 4px 0; }

.cell-row { display: flex; align-items: center; gap: 6px; }
.subject-label { color: #909399; }

.guide-toggle {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  cursor: pointer;
  color: #1B60CB;
  font-size: 13px;
  user-select: none;
}

.guide-content {
  font-size: 13px;
  color: #606266;
  margin-top: 8px;
  line-height: 1.6;
}
.guide-content ul { padding-left: 16px; margin: 0; list-style: disc; }
.guide-content li { margin-bottom: 6px; }

.footer-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 8px;
}

/* Dialog */
.dlg-body { font-size: 13px; color: #303133; line-height: 1.7; }
.dlg-desc { margin: 0 0 16px; }
.dlg-select-row { display: flex; align-items: center; gap: 12px; }
.dlg-select-label { white-space: nowrap; font-size: 13px; }
</style>
