<template>
  <div>
    <div class="page-header">
      <h2>Thêm mới đại lý</h2>
      <span class="breadcrumb">Khách hàng đại lý</span>
    </div>

    <el-button :icon="Document" style="margin-bottom:20px" @click="handleChooseTemplate">
      Chọn Gói Cước Mẫu
    </el-button>

    <!-- THÔNG TIN ĐẠI LÝ -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">THÔNG TIN ĐẠI LÝ</div>
      <el-form :model="form" label-width="220px" label-position="left">
        <el-form-item label="Tên đại lý">
          <el-input v-model="form.groupName" placeholder="Nhập tên đại lý" />
          <div class="field-hint">
            Nhập tên đại lý không trùng với các đại lý đã có. Hệ thống sẽ tự gán mã đại lý
          </div>
        </el-form-item>

        <el-form-item label="Email nhân sự phụ trách">
          <div class="tag-input-wrap" @click="focusPicInput">
            <el-tag
              v-for="(email, i) in form.picEmails"
              :key="i"
              closable
              size="small"
              @close="form.picEmails.splice(i, 1)"
            >{{ email }}</el-tag>
            <input
              ref="picInputRef"
              v-model="picEmailInput"
              class="tag-input"
              @keydown.space.prevent="addEmail('pic')"
              @keydown.enter.prevent="addEmail('pic')"
            />
          </div>
          <div class="field-hint">
            Sử dụng dấu cách để nhận biết email. Các thông tin của đại lý sẽ được gửi tới email của nhân sự phụ trách.
          </div>
        </el-form-item>

        <el-form-item label="Email đại diện đại lý">
          <div class="tag-input-wrap" @click="focusContactInput">
            <el-tag
              v-for="(email, i) in form.contactEmails"
              :key="i"
              closable
              size="small"
              @close="form.contactEmails.splice(i, 1)"
            >{{ email }}</el-tag>
            <input
              ref="contactInputRef"
              v-model="contactEmailInput"
              class="tag-input"
              @keydown.space.prevent="addEmail('contact')"
              @keydown.enter.prevent="addEmail('contact')"
            />
          </div>
          <div class="field-hint">
            Sử dụng dấu cách để nhận biết email. Các thông tin của đại lý sẽ được gửi tới email của người đại diện.
          </div>
        </el-form-item>

        <el-form-item label="Mã hợp đồng tham chiếu (Không bắt buộc)">
          <el-input v-model="form.refContractNo" placeholder="Nhập (các) các mã Hợp đồng" />
        </el-form-item>
      </el-form>
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
              <el-input-number
                v-model="row.duration"
                :min="0"
                :max="48"
                controls-position="right"
                style="width:80px"
                size="small"
              />
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
            <el-input-number
              v-model="row.minValue"
              :min="1"
              controls-position="right"
              style="width:100%"
              size="small"
            />
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
              <el-input-number
                v-model="row.fee"
                :min="0"
                controls-position="right"
                style="flex:1"
                size="small"
              />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Document, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createGroup, createGroupAssignment } from '@/api/groups'
import { createPlanTemplate } from '@/api/planTemplates'
import type { UpsertGroupRequest, CreateGroupPlanAssignmentRequest } from '@/types/group'
import type { CreatePlanTemplateRequest, PlanPricingRuleRequest } from '@/types/planTemplate'

const router = useRouter()

interface ConfigRow {
  subject: string
  subjectType: 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
  duration: number
  condition: 'signing' | 'certificate'
  minValue: number
  maxValue: number | null
  fee: number
}

const form = reactive({
  groupName: '',
  picEmails: [] as string[],
  contactEmails: [] as string[],
  refContractNo: '',
  planName: '',
  applyDateRange: null as [string, string] | null,
})

const picEmailInput = ref('')
const contactEmailInput = ref('')
const picInputRef = ref<HTMLInputElement>()
const contactInputRef = ref<HTMLInputElement>()
const showGuide = ref(true)
const submitting = ref(false)

const configRows = reactive<ConfigRow[]>([
  { subject: 'Cá nhân', subjectType: 'INDIVIDUAL', duration: 1, condition: 'signing', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Tổ chức', subjectType: 'ORGANIZATION', duration: 24, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
  { subject: 'Cá nhân thuộc tổ chức', subjectType: 'INDIVIDUAL_OF_ORG', duration: 12, condition: 'certificate', minValue: 1, maxValue: null, fee: 0 },
])

function addEmail(type: 'pic' | 'contact') {
  const inputRef = type === 'pic' ? picEmailInput : contactEmailInput
  const emails = type === 'pic' ? form.picEmails : form.contactEmails
  const val = inputRef.value.trim()
  if (val && !emails.includes(val)) emails.push(val)
  inputRef.value = ''
}

function focusPicInput() { picInputRef.value?.focus() }
function focusContactInput() { contactInputRef.value?.focus() }

function handleChooseTemplate() {
  // TODO: open template picker dialog
}

async function handleSubmit() {
  if (!form.groupName.trim()) {
    ElMessage.warning('Vui lòng nhập tên đại lý')
    return
  }
  if (!form.planName.trim()) {
    ElMessage.warning('Vui lòng nhập tên gói cước')
    return
  }

  submitting.value = true
  try {
    // Bước 1: Tạo group
    const groupReq: UpsertGroupRequest = {
      groupName: form.groupName,
      picEmails: form.picEmails,
      contactEmails: form.contactEmails,
      refContractNo: form.refContractNo || undefined,
    }
    const groupRes = await createGroup(groupReq)
    if (!groupRes.success || !groupRes.data) {
      ElMessage.error(groupRes.message || 'Không thể tạo đại lý')
      return
    }
    const newGroupId = groupRes.data.groupId

    // Bước 2: Tạo plan template
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

    // Tạo planCode tự động từ groupCode + timestamp
    const planCode = `PLN_${newGroupId}_${Date.now()}`
    const templateReq: CreatePlanTemplateRequest = {
      planCode,
      planName: form.planName,
      customerSegment: 'GROUP',
      templateScope: 'PARTNER_PRIVATE',
      status: form.applyDateRange ? 'DRAFT' : 'AVAILABLE',
      effectiveFrom: form.applyDateRange ? form.applyDateRange[0] : null,
      effectiveTo: form.applyDateRange ? form.applyDateRange[1] : null,
      isVisible: true,
      allowBulkSigning: false,
      allowApiAccess: false,
      createdBy: 'system',
      pricingRules,
    }
    const templateRes = await createPlanTemplate(templateReq)
    if (!templateRes.success || !templateRes.data) {
      ElMessage.warning(`Đại lý đã tạo (ID: ${newGroupId}) nhưng không thể tạo gói cước: ${templateRes.message}`)
      router.push('/plans/' + newGroupId)
      return
    }

    const newTemplateId = templateRes.data.planTemplateId

    // Bước 3: Gán gói cước cho đại lý (Assignment)
    const assignReq: CreateGroupPlanAssignmentRequest = {
      planTemplateId: newTemplateId,
      requestedBy: 'system', // Có thể lấy từ auth context nếu có
      applyFrom: form.applyDateRange ? form.applyDateRange[0] : null,
      applyTo: form.applyDateRange ? form.applyDateRange[1] : null,
    }
    const assignRes = await createGroupAssignment(newGroupId, assignReq)
    if (!assignRes.success) {
      ElMessage.warning(`Lỗi khi gán gói cước cho đại lý: ${assignRes.message}`)
      router.push('/plans/' + newGroupId)
      return
    }

    ElMessage.success('Tạo đại lý và gán gói cước thành công!')
    router.push('/plans/' + newGroupId)
  } catch (e) {
    ElMessage.error('Lỗi kết nối server')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/plans')
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

.field-hint { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.4; }

.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  min-height: 36px;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 3px 8px;
  box-sizing: border-box;
  cursor: text;
  transition: border-color 0.2s;
}
.tag-input-wrap:focus-within { border-color: #409eff; }
.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 60px;
  font-size: 14px;
  padding: 2px 0;
  background: transparent;
}

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
</style>
