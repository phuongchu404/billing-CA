<template>
  <div>
    <div class="page-header">
      <h2>Thông tin đại lý</h2>
      <span class="breadcrumb">Khách hàng đại lý</span>
    </div>

    <!-- Action buttons: khác nhau theo trạng thái -->
    <div class="action-bar">
      <template v-if="agency.status === 'ACTIVE'">
        <el-button :icon="Download" @click="handleExport">Xuất Đối Soát</el-button>
        <el-button :icon="Edit" @click="handleEdit">Chỉnh Sửa</el-button>
        <el-button :icon="Plus" @click="handleAddPlan">Thêm Gói Cước</el-button>
        <el-button :icon="VideoPause" @click="handleSuspend">Tạm Dừng</el-button>
      </template>
      <template v-else>
        <el-button :icon="VideoPlay" @click="handleActivate">Kích Hoạt</el-button>
      </template>
    </div>

    <!-- THÔNG TIN CHI TIẾT -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">THÔNG TIN CHI TIẾT</div>
      <div class="info-grid">
        <div class="info-row two-col">
          <span><b>Tên đại lý:</b> {{ agency.groupName }}</span>
          <span><b>Mã đại lý:</b> {{ agency.groupCode }}</span>
        </div>
        <div class="info-row">
          <b>Trạng thái:</b>
          <span :class="agency.status === 'ACTIVE' ? 'text-active' : 'text-inactive'" style="margin-left:4px">
            {{ agency.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm dừng' }}
          </span>
        </div>
        <div class="info-row">
          <span><b>Nhân sự phụ trách:</b> {{ agency.picEmails.join(', ') }}</span>
        </div>
        <div v-if="agency.contactEmail" class="info-row">
          <span><b>Người đại diện:</b> {{ agency.contactEmail }}</span>
        </div>
        <div class="info-row">
          <span><b>Mã hợp đồng tham chiếu:</b> {{ agency.refContractNo }}</span>
        </div>
        <div class="info-row two-col">
          <span><b>Người tạo:</b> {{ agency.createdBy }}</span>
          <span><b>Thời gian tạo:</b> {{ agency.createdAt }}</span>
        </div>
        <div class="info-row">
          <span><b>Thời gian cập nhật:</b> {{ agency.updatedAt }}</span>
        </div>
      </div>
    </el-card>

    <!-- QUẢN LÝ GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">QUẢN LÝ GÓI CƯỚC</div>
      <el-table :data="filteredPlans" border>
        <el-table-column type="index" width="50">
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="loadPlans" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="planName" sortable min-width="180">
          <template #header>
            <div class="col-label">TÊN GÓI</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable width="145">
          <template #header>
            <div class="col-label">TRẠNG THÁI</div>
            <div class="col-filter">
              <el-select v-model="planFilterStatus" size="small" clearable placeholder="" style="width:100%">
                <el-option label="Khả dụng" value="available" />
                <el-option label="Không khả dụng" value="unavailable" />
                <el-option label="Chờ duyệt" value="pending" />
                <el-option label="Đã duyệt" value="approved" />
                <el-option label="Đang áp dụng" value="active" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag v-if="row.status === 'available'" size="small" type="info" plain>Khả dụng</el-tag>
            <el-tag v-else-if="row.status === 'unavailable'" size="small" type="info" plain style="color:#909399;border-color:#dcdfe6">Không khả dụng</el-tag>
            <el-tag v-else-if="row.status === 'pending'" size="small" type="warning">Chờ duyệt</el-tag>
            <el-tag v-else-if="row.status === 'approved'" size="small" class="tag-approved">Đã duyệt</el-tag>
            <el-tag v-else-if="row.status === 'active'" size="small" type="primary">Đang áp dụng</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="applyFrom" sortable width="135">
          <template #header>
            <div class="col-label">ÁP DỤNG TỪ</div>
            <div class="col-filter">
              <el-date-picker v-model="planFilterFrom" type="date" size="small" clearable placeholder="" style="width:100%" />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyFrom }}</template>
        </el-table-column>

        <el-table-column prop="applyTo" sortable width="135">
          <template #header>
            <div class="col-label">ÁP DỤNG ĐẾN</div>
            <div class="col-filter">
              <el-date-picker v-model="planFilterTo" type="date" size="small" clearable placeholder="" style="width:100%" />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyTo }}</template>
        </el-table-column>

        <el-table-column prop="updatedAt" sortable width="175">
          <template #header>
            <div class="col-label">THỜI GIAN CẬP NHẬT</div>
            <div class="col-filter">
              <el-date-picker v-model="planFilterUpdated" type="date" size="small" clearable placeholder="" style="width:100%" />
            </div>
          </template>
          <template #default="{ row }">{{ row.updatedAt }}</template>
        </el-table-column>

        <el-table-column width="190" fixed="right">
          <template #header>
            <div class="col-label">HÀNH ĐỘNG</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" :icon="InfoFilled" @click="handlePlanDetail(row)">Chi tiết</el-button>
              <template v-if="agency.status === 'ACTIVE'">
                <el-button v-if="row.status === 'available'" size="small" type="primary" @click="handleRequestApply(row)">Y/c áp dụng</el-button>
                <el-button v-else-if="row.status === 'pending'" size="small" type="success" :icon="Check" @click="handleApprove(row)">Duyệt</el-button>
                <el-button v-else-if="row.status === 'approved' || row.status === 'active'" size="small" type="warning" @click="handleStopApply(row)">Dừng áp dụng</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- LỊCH SỬ ÁP DỤNG GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title toggle-title" @click="showHistory = !showHistory">
        LỊCH SỬ ÁP DỤNG GÓI CƯỚC
        <el-icon><ArrowDown v-if="showHistory" /><ArrowRight v-else /></el-icon>
      </div>
      <el-table v-show="showHistory" :data="historyRows" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="applyFrom" label="ÁP DỤNG TỪ" sortable width="130" />
        <el-table-column prop="applyTo" label="ÁP DỤNG ĐẾN" sortable width="130" />
        <el-table-column prop="planName" label="TÊN GÓI" sortable min-width="180" />
        <el-table-column prop="ctsCreated" label="SL CTS ĐÃ TẠO" sortable width="145">
          <template #default="{ row }">{{ row.ctsCreated.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="ctsCreatedPct" label="% CTS ĐÃ TẠO" sortable width="140" />
        <el-table-column prop="signingUsed" label="SL LƯỢT ĐÃ KÝ" sortable width="145">
          <template #default="{ row }">{{ row.signingUsed.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="signingUsedPct" label="% LƯỢT ĐÃ KÝ" sortable width="140" />
      </el-table>
    </el-card>

    <!-- ===== DIALOG 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC ===== -->
    <el-dialog v-model="requestApplyVisible" title="YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC" width="480px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-info-line">Tên gói cước: <span class="dlg-value">{{ selectedPlan?.planName }}</span></p>
        <p class="dlg-info-line">Tên đại lý: <span class="dlg-value">{{ agency.groupName }}</span></p>
        <p class="dlg-info-line">Mã đại lý: <span class="dlg-value">{{ agency.groupCode }}</span></p>
        <div class="dlg-date-row">
          <span class="dlg-date-label">Thời gian áp dụng</span>
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            start-placeholder="Từ"
            range-separator="-"
            end-placeholder="Đến"
            value-format="YYYY-MM-DD"
            style="flex:1"
          />
        </div>
        <p class="dlg-note">
          Khi bấm nút Xác Nhận, hệ thống sẽ đưa phiên bản hiện tại sẽ chuyển sang trạng thái
          <b>"Chờ duyệt"</b>. Sau khi được duyệt, bảng gói cước sẽ được áp dụng từ 00:00:00 ngày bắt
          đầu đến 23:59:59 ngày kết thúc.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmRequestApply">Xác Nhận</el-button>
        <el-button @click="requestApplyVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 2: DUYỆT ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog v-model="approveVisible" title="DUYỆT ÁP DỤNG GÓI CƯỚC" width="520px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          Nhấn <b>"Xác nhận"</b> để duyệt áp dụng gói cước
          <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          cho đại lý <b>{{ agency.groupName }}</b> với mã <b>{{ agency.groupCode }}</b>.
        </p>
        <div class="dlg-date-row">
          <span class="dlg-date-label">Thời gian áp dụng</span>
          <el-date-picker
            v-model="approveDateRange"
            type="daterange"
            start-placeholder="Từ"
            range-separator="-"
            end-placeholder="Đến"
            value-format="YYYY-MM-DD"
            style="flex:1"
          />
        </div>
        <p class="dlg-note">
          Khi chọn Xác nhận, gói cước sẽ chuyển sang trạng thái <b>"Đã duyệt"</b> và được áp dụng
          từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
        </p>
        <ul class="dlg-bullets">
          <li>Từ 00:00:00 ngày bắt đầu, trạng thái sẽ chuyển sang <b>"Đang áp dụng"</b></li>
          <li>Sau khi hết thời gian hiệu lực, trạng thái sẽ tự động chuyển về <b>"Khả dụng"</b></li>
          <li>Chọn Từ chối, trạng thái gói cước sẽ chuyển về <b>"Khả dụng"</b>.</li>
        </ul>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" @click="confirmReject">Từ Chối</el-button>
          <div>
            <el-button type="primary" @click="confirmApprove">Xác Nhận</el-button>
            <el-button @click="approveVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 3: DỪNG ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog v-model="stopApplyVisible" title="DỪNG ÁP DỤNG GÓI CƯỚC" width="480px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          Nhấn <b>"Xác nhận"</b> để dừng lập tức áp dụng gói cước
          <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          cho đại lý <b>{{ agency.groupName }}</b> với mã <b>{{ agency.groupCode }}</b>.
          Sau khi dừng, trạng thái cập nhật sẽ chuyển sang <b>"Khả dụng"</b>.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmStopApply">Xác Nhận</el-button>
        <el-button @click="stopApplyVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 4: CHI TIẾT GÓI CƯỚC ===== -->
    <el-dialog v-model="planDetailVisible" title="CHI TIẾT GÓI CƯỚC" width="760px" :close-on-click-modal="false">
      <div class="dlg-body">
        <div class="detail-info-grid">
          <div class="detail-info-row">
            <span><b>Tên gói cước:</b> {{ selectedPlan?.planName }}</span>
            <span><b>Trạng thái:</b> {{ planStatusLabel(selectedPlan?.status) }}</span>
          </div>
          <div class="detail-info-row">
            <span><b>Mã đại lý:</b> {{ agency.groupCode }}</span>
            <span><b>Tên đại lý:</b> {{ agency.groupName }}</span>
          </div>
          <div class="detail-info-row">
            <span><b>Áp dụng từ:</b> {{ selectedPlan?.applyFrom }}</span>
            <span><b>Áp dụng đến:</b> {{ selectedPlan?.applyTo }}</span>
          </div>
          <div class="detail-info-row">
            <span><b>Thời gian cập nhật:</b> {{ selectedPlan?.updatedAt }}</span>
          </div>
        </div>

        <el-table :data="planConfigRows" border style="margin-top:16px">
          <el-table-column type="index" width="50" />
          <el-table-column prop="subject" label="ĐỐI TƯỢNG" width="160" />
          <el-table-column label="THỜI HẠN CHỨNG THƯ" width="160">
            <template #default="{ row }">{{ row.duration }} tháng</template>
          </el-table-column>
          <el-table-column prop="condition" label="ĐIỀU KIỆN" width="130" />
          <el-table-column prop="min" label="GIÁ TRỊ MIN (CỦA ĐIỀU KIỆN)" width="155" />
          <el-table-column prop="max" label="GIÁ TRỊ MAX (CỦA ĐIỀU KIỆN)" width="155" />
          <el-table-column label="PHÍ/ ĐIỀU KIỆN">
            <template #default="{ row }">{{ row.fee.toLocaleString() }} vnđ</template>
          </el-table-column>
        </el-table>

        <p class="dlg-note" style="margin-top:12px">
          <b>Lưu ý:</b> Gói cước đã tạo sẽ không thể chỉnh sửa. Vui lòng tạo gói cước mới trong trường hợp thay đổi.
        </p>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" @click="openDisableDialog">Vô Hiệu Hoá</el-button>
          <div>
            <el-button v-if="selectedPlan?.status === 'available'" type="primary" @click="openRequestFromDetail">Yêu Cầu Áp Dụng</el-button>
            <el-button @click="planDetailVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: XUẤT ĐỐI SOÁT ===== -->
    <el-dialog v-model="exportVisible" title="XUẤT ĐỐI SOÁT" width="420px" :close-on-click-modal="false">
      <div class="dlg-body">
        <div class="dlg-date-row">
          <span class="dlg-date-label">Thời gian đối soát</span>
          <el-date-picker
            v-model="exportMonth"
            type="month"
            placeholder="Chọn tháng xuất đối soát"
            format="MM/YYYY"
            value-format="YYYY-MM"
            style="flex:1"
          />
        </div>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!exportMonth" @click="confirmExport">Xác Nhận</el-button>
        <el-button @click="exportVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: KÍCH HOẠT LẠI ===== -->
    <el-dialog v-model="activateVisible" title="KÍCH HOẠT LẠI" width="460px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          Bạn có chắc chắn muốn kích hoạt lại đại lý
          [{{ agency.groupCode }}] - {{ agency.groupName }}?
        </p>
        <p class="dlg-desc" style="margin-top:8px">
          Đại lý sẽ được chuyển về trạng thái <b>"Đang hoạt động"</b> và tiếp tục sử dụng
          dịch vụ. Gói cước còn hiệu lực sẽ tiếp tục được áp dụng (nếu có).
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmActivate">Xác Nhận</el-button>
        <el-button @click="activateVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 5: VÔ HIỆU HOÁ ===== -->
    <el-dialog v-model="disableVisible" title="VÔ HIỆU HOÁ" width="480px" :close-on-click-modal="false">
      <div class="dlg-body">
        <p class="dlg-desc">
          Bạn đang vô hiệu hoá gói cước <b>{{ selectedPlan?.planName }}</b>
          của đại lý [{{ agency.groupCode }}] - {{ agency.groupName }}.
        </p>
        <p class="dlg-desc" style="margin-top:8px">
          Cấu hình gói cước này sẽ không còn khả dụng. Nhấn <b>"Xác Nhận"</b> để vô hiệu hoá.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmDisable">Xác Nhận</el-button>
        <el-button @click="disableVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Download, Edit, Plus, VideoPause, VideoPlay,
  Refresh, InfoFilled, Check, ArrowDown, ArrowRight,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const agencyId = Number(route.params.id)

interface AgencyInfo {
  groupId: number
  groupCode: string
  groupName: string
  status: 'ACTIVE' | 'INACTIVE'
  picEmails: string[]
  contactEmail: string
  refContractNo: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

interface PlanRow {
  id: number
  planName: string
  status: 'available' | 'unavailable' | 'pending' | 'approved' | 'active'
  applyFrom: string
  applyTo: string
  updatedAt: string
}

interface HistoryRow {
  applyFrom: string
  applyTo: string
  planName: string
  ctsCreated: number
  ctsCreatedPct: string
  signingUsed: number
  signingUsedPct: string
}

interface ConfigRow {
  subject: string
  duration: number
  condition: string
  min: number
  max: number
  fee: number
}

const agency = ref<AgencyInfo>(
  agencyId % 2 === 0
    ? {
        groupId: agencyId,
        groupCode: 'TCB_01',
        groupName: 'Techcombank',
        status: 'INACTIVE',
        picEmails: ['quangbt-hn@mk.com.vn', 'baclt-hn@mk.com.vn', 'anhnt-hn@mk.com.vn'],
        contactEmail: '',
        refContractNo: 'HĐ/ĐL/AG001_202603_0001, HĐ/ĐL/AG015_202604_0002',
        createdBy: 'admin123',
        createdAt: '30/03/2026 16:29:00',
        updatedAt: '30/03/2026 16:29:00',
      }
    : {
        groupId: agencyId,
        groupCode: 'TCB_01',
        groupName: 'Techcombank',
        status: 'ACTIVE',
        picEmails: ['quangbt-hn@mk.com.vn', 'baclt-hn@mk.com.vn', 'anhnt-hn@mk.com.vn'],
        contactEmail: 'ngocng@techcombank.com.vn',
        refContractNo: 'HĐ/ĐL/AG001_202603_0001, HĐ/ĐL/AG015_202604_0002',
        createdBy: 'admin123',
        createdAt: '30/03/2026 16:29:00',
        updatedAt: '30/03/2026 16:29:00',
      }
)

const plans = ref<PlanRow[]>(
  agency.value.status === 'ACTIVE'
    ? [
        { id: 1, planName: 'Chữ ký số 2026', status: 'available', applyFrom: '', applyTo: '', updatedAt: '27/03/2026 18:29:00' },
        { id: 2, planName: 'Chữ ký số 2026 tháng 5_2', status: 'pending', applyFrom: '08/05/2026', applyTo: '11/05/2026', updatedAt: '27/03/2026 18:29:00' },
        { id: 3, planName: 'Chữ ký số 2026 tháng 5', status: 'approved', applyFrom: '01/05/2026', applyTo: '07/05/2026', updatedAt: '27/03/2026 18:29:00' },
        { id: 4, planName: 'Chữ ký số 2026 tháng 3', status: 'active', applyFrom: '27/03/2026', applyTo: '30/04/2026', updatedAt: '27/03/2026 18:29:00' },
      ]
    : [
        { id: 1, planName: 'Chữ ký số 2026', status: 'available', applyFrom: '', applyTo: '', updatedAt: '27/03/2026 18:29:00' },
        { id: 2, planName: 'Chữ ký số 2026 tháng 5_2', status: 'pending', applyFrom: '08/05/2026', applyTo: '11/05/2026', updatedAt: '27/03/2026 18:29:00' },
        { id: 3, planName: 'Chữ ký số 2026 tháng 5_2', status: 'unavailable', applyFrom: '08/05/2026', applyTo: '11/05/2026', updatedAt: '27/03/2026 18:29:00' },
        { id: 4, planName: 'Chữ ký số 2026 tháng 5', status: 'approved', applyFrom: '01/05/2026', applyTo: '07/05/2026', updatedAt: '27/03/2026 18:29:00' },
        { id: 5, planName: 'Chữ ký số 2026 tháng 3', status: 'active', applyFrom: '27/03/2026', applyTo: '30/04/2026', updatedAt: '27/03/2026 18:29:00' },
      ]
)

const historyRows = ref<HistoryRow[]>([
  { applyFrom: '08/05/2026', applyTo: '-', planName: 'Chữ ký số 2026', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn' },
  { applyFrom: '01/03/2026', applyTo: '07/05/2026', planName: 'Chữ ký số 2026 tháng 5_2', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn' },
  { applyFrom: '01/01/2026', applyTo: '28/02/2026', planName: 'Chữ ký số 2026 tháng 5_2', ctsCreated: 10000, ctsCreatedPct: '63%', signingUsed: 20000, signingUsedPct: 'Không giới hạn' },
])

const planConfigRows: ConfigRow[] = [
  { subject: 'Cá nhân', duration: 1, condition: 'Lượt ký', min: 1, max: 1, fee: 1000 },
  { subject: 'Tổ chức', duration: 24, condition: 'Số chứng thư', min: 1, max: 1, fee: 0 },
  { subject: 'Cá nhân thuộc tổ chức', duration: 12, condition: 'Số chứng thư', min: 1, max: 1, fee: 0 },
]

// --- filter state ---
const showHistory = ref(true)
const planFilterStatus = ref('')
const planFilterFrom = ref<Date | null>(null)
const planFilterTo = ref<Date | null>(null)
const planFilterUpdated = ref<Date | null>(null)

const filteredPlans = computed(() => {
  let result = plans.value
  if (planFilterStatus.value) result = result.filter(p => p.status === planFilterStatus.value)
  return result
})

// --- dialog state ---
const selectedPlan = ref<PlanRow | null>(null)

const requestApplyVisible = ref(false)
const requestApplyDateRange = ref<[string, string] | null>(null)

const approveVisible = ref(false)
const approveDateRange = ref<[string, string] | null>(null)

const stopApplyVisible = ref(false)

const planDetailVisible = ref(false)

const disableVisible = ref(false)

const exportVisible = ref(false)
const exportMonth = ref<string | null>(null)

const activateVisible = ref(false)

// --- helpers ---
function planStatusLabel(status?: string): string {
  const map: Record<string, string> = {
    available: 'Khả dụng',
    unavailable: 'Không khả dụng',
    pending: 'Chờ duyệt',
    approved: 'Đã duyệt',
    active: 'Đang áp dụng',
  }
  return status ? (map[status] ?? status) : ''
}

// --- action handlers ---
function loadPlans() { /* TODO: reload from API */ }
function handleExport() {
  exportMonth.value = null
  exportVisible.value = true
}
function handleEdit() { /* TODO */ }
function handleAddPlan() { router.push(`/plans/${agencyId}/add-plan`) }
function handleSuspend() { agency.value.status = 'INACTIVE' }
function handleActivate() { activateVisible.value = true }

function handleRequestApply(row: PlanRow) {
  selectedPlan.value = row
  requestApplyDateRange.value = null
  requestApplyVisible.value = true
}

function handleApprove(row: PlanRow) {
  selectedPlan.value = row
  approveDateRange.value = row.applyFrom && row.applyTo ? [row.applyFrom, row.applyTo] : null
  approveVisible.value = true
}

function handleStopApply(row: PlanRow) {
  selectedPlan.value = row
  stopApplyVisible.value = true
}

function handlePlanDetail(row: PlanRow) {
  selectedPlan.value = row
  planDetailVisible.value = true
}

function openDisableDialog() {
  planDetailVisible.value = false
  disableVisible.value = true
}

function openRequestFromDetail() {
  planDetailVisible.value = false
  requestApplyDateRange.value = null
  requestApplyVisible.value = true
}

// --- confirm handlers ---
function confirmRequestApply() {
  // TODO: call API
  requestApplyVisible.value = false
}

function confirmApprove() {
  // TODO: call API
  approveVisible.value = false
}

function confirmReject() {
  // TODO: call API - reject sets status back to 'available'
  approveVisible.value = false
}

function confirmStopApply() {
  // TODO: call API
  stopApplyVisible.value = false
}

function confirmDisable() {
  // TODO: call API
  disableVisible.value = false
}

function confirmExport() {
  // TODO: call API with exportMonth.value
  exportVisible.value = false
}

function confirmActivate() {
  agency.value.status = 'ACTIVE'
  activateVisible.value = false
}
</script>

<style scoped>
.page-header { margin-bottom: 12px; }
.page-header h2 { margin: 0; font-size: 20px; }
.breadcrumb { font-size: 13px; color: #909399; }

.action-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }

.section-card { margin-bottom: 16px; }
.section-title {
  color: #1B60CB;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  margin-bottom: 14px;
}
.toggle-title { display: flex; align-items: center; gap: 6px; cursor: pointer; user-select: none; }

.info-grid { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #303133; }
.info-row { line-height: 1.6; }
.info-row.two-col { display: flex; gap: 60px; }

.text-active { color: #1B60CB; font-weight: 500; }
.text-inactive { color: #FF9F43; font-weight: 500; }

.col-label { font-weight: 600; font-size: 13px; }
.col-filter { margin-top: 6px; min-height: 28px; }

:deep(.tag-approved) { background-color: #303133; border-color: #303133; color: #fff; }

.action-btns { display: flex; gap: 4px; flex-wrap: nowrap; }
.action-btns :deep(.el-button) { margin: 0; }

:deep(.el-table th.el-table__cell) { vertical-align: top; padding: 8px 0; }

/* Dialog common */
.dlg-body { font-size: 13px; color: #303133; line-height: 1.7; }
.dlg-info-line { margin: 4px 0; }
.dlg-value { font-weight: 500; }
.dlg-plan-name { color: #1B60CB; }
.dlg-desc { margin: 0 0 8px; }
.dlg-note { margin: 12px 0 0; font-size: 12px; color: #606266; font-style: italic; }
.dlg-bullets { margin: 6px 0 0 0; padding-left: 18px; }
.dlg-bullets li { margin-bottom: 4px; }

.dlg-date-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0;
}
.dlg-date-label { white-space: nowrap; font-size: 13px; }

.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.dlg-footer-split > div { display: flex; gap: 8px; }

/* Dialog 4 info grid */
.detail-info-grid { display: flex; flex-direction: column; gap: 6px; font-size: 13px; }
.detail-info-row { display: flex; gap: 40px; }
.detail-info-row span { flex: 1; }
</style>
