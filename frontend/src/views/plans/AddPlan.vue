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

    <!-- Dialog: Approval success -->
    <el-dialog v-model="approvalSuccessVisible" title="YÊU CẦU ĐÃ ĐƯỢC GỬI" width="440px" align-center :close-on-click-modal="false">
      <div style="text-align:center; padding: 8px 0 16px">
        <el-icon style="font-size:48px; color:#67c23a"><CircleCheck /></el-icon>
        <p style="margin:12px 0 4px; font-size:15px; font-weight:600; color:#303133">
          Gói cước đã được tạo và gửi yêu cầu duyệt!
        </p>
        <p style="font-size:13px; color:#606266; margin:0">
          Email thông báo đã được gửi đến người phê duyệt cấp 1.<br />
          Theo dõi tiến trình duyệt tại màn Phê duyệt.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToApproval">Xem tiến trình duyệt</el-button>
        <el-button @click="router.push('/plans/' + agencyId)">Về danh sách gói cước</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, ArrowUp, ArrowDown, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getGroup, addGroupPlan } from '@/api/groups'
import { listPlanTemplates, getPlanTemplate } from '@/api/planTemplates'
import type { GroupDetail, AddGroupPlanRequest } from '@/types/group'
import type { PlanTemplate, PlanPricingRuleRequest } from '@/types/planTemplate'

const route = useRoute()
const router = useRouter()

const agencyId = Number(route.params.id)

interface ConfigRow {
  subject: string
  subjectType: 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
  duration: number
  condition: 'signing' | 'certificate'
  minValue: number
  maxValue: number | null
  fee: number
}

// Dữ liệu đại lý (read-only) — load từ API
const agency = reactive<GroupDetail>({
  groupId: 0,
  groupCode: '',
  groupName: '',
  status: 'ACTIVE',
  picEmails: [],
  contactEmails: [],
  refContractNo: null,
  createdBy: null,
  createdAt: null,
  updatedAt: null,
  ownerUserId: null,
  ownerName: null,
})

const form = reactive({
  planName: '',
  applyDateRange: null as [string, string] | null,
})

const configRows = reactive<ConfigRow[]>([
  { subject: 'Cá nhân', subjectType: 'INDIVIDUAL', duration: 1, condition: 'signing', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Tổ chức', subjectType: 'ORGANIZATION', duration: 24, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Cá nhân thuộc tổ chức', subjectType: 'INDIVIDUAL_OF_ORG', duration: 12, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
])

const showGuide = ref(true)
const submitting = ref(false)
const loadingAgency = ref(false)
const approvalSuccessVisible = ref(false)
const lastApprovalId = ref<number | null>(null)

// --- Template dialog ---
const templateVisible = ref(false)
const selectedTemplate = ref<number | null>(null)
const templateOptions = ref<{ label: string; value: number }[]>([])

async function loadAgency() {
  if (!agencyId) return
  loadingAgency.value = true
  try {
    const res = await getGroup(agencyId)
    if (res.success && res.data) {
      Object.assign(agency, res.data)
    } else {
      ElMessage.error(res.message || 'Không tìm thấy đại lý')
    }
  } catch {
    ElMessage.error('Lỗi kết nối server')
  } finally {
    loadingAgency.value = false
  }
}

async function loadTemplateOptions() {
  try {
    const res = await listPlanTemplates()
    if (res.success && res.data) {
      templateOptions.value = res.data
        .filter(t => t.status === 'AVAILABLE' || t.status === 'DRAFT')
        .map(t => ({ label: t.planName, value: t.planTemplateId }))
    }
  } catch {
    // silently ignore
  }
}

async function confirmTemplate() {
  if (!selectedTemplate.value) return
  try {
    const res = await getPlanTemplate(selectedTemplate.value)
    if (res.success && res.data) {
      const tmpl: PlanTemplate = res.data
      // Copy pricing rules vào configRows
      tmpl.pricingRules.forEach((rule, i) => {
        if (i < configRows.length) {
          configRows[i].duration = rule.certificateValidityValue
          configRows[i].condition = rule.pricingMetric === 'SIGNING_COUNT' ? 'signing' : 'certificate'
          configRows[i].minValue = rule.rangeMin
          configRows[i].maxValue = rule.rangeMax
          configRows[i].fee = Number(rule.unitPrice)
        }
      })
    }
  } catch {
    ElMessage.error('Không thể tải template')
  }
  templateVisible.value = false
}

async function handleSubmit() {
  if (!form.planName.trim()) {
    ElMessage.warning('Vui lòng nhập tên gói cước')
    return
  }

  submitting.value = true
  try {
    const pricingRules: PlanPricingRuleRequest[] = configRows.map((row, i) => ({
      subjectType: row.subjectType,
      certificateValidityValue: row.duration,
      certificateValidityUnit: 'MONTH',
      pricingMetric: row.condition === 'signing' ? 'SIGNING_COUNT' : 'CERTIFICATE_COUNT',
      rangeMin: row.minValue,
      rangeMax: row.maxValue,
      unitPrice: row.fee,
      currency: 'VND',
      quotaTotal: null,
      sortOrder: i + 1,
      isActive: true,
    }))

    const req: AddGroupPlanRequest = {
      planName: form.planName,
      applyFrom: form.applyDateRange ? form.applyDateRange[0] : null,
      applyTo: form.applyDateRange ? form.applyDateRange[1] : null,
      requestedBy: 'system',
      pricingRules,
    }

    const res = await addGroupPlan(agencyId, req)
    if (res.success) {
      if (res.data?.approvalRequestId) {
        lastApprovalId.value = res.data.approvalRequestId
        approvalSuccessVisible.value = true
      } else {
        ElMessage.success('Tạo gói cước thành công!')
        router.push('/plans/' + agencyId)
      }
    } else {
      ElMessage.error(res.message || 'Không thể tạo gói cước')
    }
  } catch {
    ElMessage.error('Lỗi kết nối server')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/plans/' + agencyId)
}

function goToApproval() {
  approvalSuccessVisible.value = false
  router.push(lastApprovalId.value ? `/approvals/${lastApprovalId.value}` : '/approvals')
}

onMounted(() => {
  loadAgency()
  loadTemplateOptions()
})
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
