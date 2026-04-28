<template>
  <div>
    <div class="page-header">
      <h2>Thông tin đại lý</h2>
      <span class="breadcrumb">Khách hàng đại lý</span>
    </div>

    <!-- Action buttons: khác nhau theo trạng thái -->
    <div class="action-bar">
      <template v-if="agency.status === 'ACTIVE'">
        <el-button :icon="Download" @click="handleExport"
          >Xuất Đối Soát</el-button
        >
        <el-button :icon="Edit" @click="handleEdit">Chỉnh Sửa</el-button>
        <el-button :icon="Plus" @click="handleAddPlan">Thêm Gói Cước</el-button>
        <el-button :icon="VideoPause" @click="handleSuspend"
          >Tạm Dừng</el-button
        >
      </template>
      <template v-else>
        <el-button :icon="VideoPlay" @click="handleActivate"
          >Kích Hoạt</el-button
        >
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
          <span
            :class="
              agency.status === 'ACTIVE' ? 'text-active' : 'text-inactive'
            "
            style="margin-left: 4px"
          >
            {{ agency.status === "ACTIVE" ? "Đang hoạt động" : "Tạm dừng" }}
          </span>
        </div>
        <div class="info-row">
          <span
            ><b>Nhân sự phụ trách:</b> {{ agency.picEmails.join(", ") }}</span
          >
        </div>
        <div v-if="agency.contactEmails?.length" class="info-row">
          <span
            ><b>Người đại diện:</b> {{ agency.contactEmails.join(", ") }}</span
          >
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
              <el-select
                v-model="planFilterStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option label="Khả dụng" value="available" />
                <el-option label="Không khả dụng" value="unavailable" />
                <el-option label="Chờ duyệt" value="pending" />
                <el-option label="Đã duyệt" value="approved" />
                <el-option label="Đang áp dụng" value="active" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag
              v-if="row.status === 'available'"
              size="small"
              type="info"
              plain
              >Khả dụng</el-tag
            >
            <el-tag
              v-else-if="row.status === 'unavailable'"
              size="small"
              type="info"
              plain
              style="color: #909399; border-color: #dcdfe6"
              >Không khả dụng</el-tag
            >
            <el-tag
              v-else-if="row.status === 'pending'"
              size="small"
              type="warning"
              >Chờ duyệt</el-tag
            >
            <el-tag
              v-else-if="row.status === 'approved'"
              size="small"
              class="tag-approved"
              >Đã duyệt</el-tag
            >
            <el-tag
              v-else-if="row.status === 'active'"
              size="small"
              type="primary"
              >Đang áp dụng</el-tag
            >
          </template>
        </el-table-column>

        <el-table-column prop="applyFrom" sortable width="135">
          <template #header>
            <div class="col-label">ÁP DỤNG TỪ</div>
            <div class="col-filter">
              <el-date-picker
                v-model="planFilterFrom"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyFrom }}</template>
        </el-table-column>

        <el-table-column prop="applyTo" sortable width="135">
          <template #header>
            <div class="col-label">ÁP DỤNG ĐẾN</div>
            <div class="col-filter">
              <el-date-picker
                v-model="planFilterTo"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyTo }}</template>
        </el-table-column>

        <el-table-column prop="updatedAt" sortable width="175">
          <template #header>
            <div class="col-label">THỜI GIAN CẬP NHẬT</div>
            <div class="col-filter">
              <el-date-picker
                v-model="planFilterUpdated"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
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
              <el-button
                size="small"
                :icon="InfoFilled"
                @click="handlePlanDetail(row)"
                >Chi tiết</el-button
              >
              <template v-if="agency.status === 'ACTIVE'">
                <el-button
                  v-if="row.status === 'available'"
                  size="small"
                  type="primary"
                  :disabled="!can('group:update')"
                  @click="handleRequestApply(row)"
                  >Y/c áp dụng</el-button
                >
                <el-button
                  v-else-if="row.status === 'pending'"
                  size="small"
                  type="success"
                  :icon="Check"
                  :disabled="!canApprovePlan"
                  @click="handleApprove(row)"
                  >Duyệt</el-button
                >
                <el-button
                  v-else-if="
                    row.status === 'approved' || row.status === 'active'
                  "
                  size="small"
                  type="warning"
                  :disabled="!can('group:update')"
                  @click="handleStopApply(row)"
                  >Dừng áp dụng</el-button
                >
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- LỊCH SỬ ÁP DỤNG GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div
        class="section-title toggle-title"
        @click="showHistory = !showHistory"
      >
        LỊCH SỬ ÁP DỤNG GÓI CƯỚC
        <el-icon><ArrowDown v-if="showHistory" /><ArrowRight v-else /></el-icon>
      </div>
      <el-table v-show="showHistory" :data="historyRows" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column
          prop="applyFrom"
          label="ÁP DỤNG TỪ"
          sortable
          width="130"
        />
        <el-table-column
          prop="applyTo"
          label="ÁP DỤNG ĐẾN"
          sortable
          width="130"
        />
        <el-table-column
          prop="planName"
          label="TÊN GÓI"
          sortable
          min-width="180"
        />
        <el-table-column label="TRẠNG THÁI" width="130" sortable prop="assignmentStatus">
          <template #default="{ row }">
            <el-tag v-if="row.assignmentStatus === 'APPROVED'" size="small" type="info">Đã duyệt</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'ACTIVE'" size="small" type="primary">Đang áp dụng</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'STOPPED'" size="small" type="warning">Đã dừng</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'EXPIRED'" size="small" type="danger">Hết hạn</el-tag>
            <el-tag v-else size="small">{{ row.assignmentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="ctsCreated"
          label="SL CTS ĐÃ TẠO"
          sortable
          width="145"
        >
          <template #default="{ row }">{{
            row.ctsCreated.toLocaleString()
          }}</template>
        </el-table-column>
        <el-table-column
          prop="ctsCreatedPct"
          label="% CTS ĐÃ TẠO"
          sortable
          width="140"
        />
        <el-table-column
          prop="signingUsed"
          label="SL LƯỢT ĐÃ KÝ"
          sortable
          width="145"
        >
          <template #default="{ row }">{{
            row.signingUsed.toLocaleString()
          }}</template>
        </el-table-column>
        <el-table-column
          prop="signingUsedPct"
          label="% LƯỢT ĐÃ KÝ"
          sortable
          width="140"
        />
      </el-table>
    </el-card>

    <!-- ===== DIALOG 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="requestApplyVisible"
      title="YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-info-line">
          Tên gói cước:
          <span class="dlg-value">{{ selectedPlan?.planName }}</span>
        </p>
        <p class="dlg-info-line">
          Tên đại lý: <span class="dlg-value">{{ agency.groupName }}</span>
        </p>
        <p class="dlg-info-line">
          Mã đại lý: <span class="dlg-value">{{ agency.groupCode }}</span>
        </p>
        <div class="dlg-date-row">
          <span class="dlg-date-label">Thời gian áp dụng</span>
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            start-placeholder="Từ"
            range-separator="-"
            end-placeholder="Đến"
            value-format="YYYY-MM-DD"
            style="flex: 1"
          />
        </div>
        <div class="dlg-date-row">
          <span class="dlg-date-label">Cáº¥p phÃª duyá»‡t</span>
          <el-select v-model="requestApprovalLevel" style="flex: 1">
            <el-option label="TrÆ°á»Ÿng phÃ²ng kinh doanh" :value="1" />
            <el-option label="CFO (Finance Manager)" :value="2" />
            <el-option label="CEO" :value="3" />
          </el-select>
        </div>
        <p class="dlg-note">
          Khi bấm nút Xác Nhận, hệ thống sẽ đưa phiên bản hiện tại sẽ chuyển
          sang trạng thái
          <b>"Chờ duyệt"</b>. Sau khi được duyệt, bảng gói cước sẽ được áp dụng
          từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmRequestApply"
          >Xác Nhận</el-button
        >
        <el-button @click="requestApplyVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 2: DUYỆT ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="approveVisible"
      title="DUYỆT ÁP DỤNG GÓI CƯỚC"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-desc">
          Nhấn <b>"Xác nhận"</b> để duyệt áp dụng gói cước
          <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          cho đại lý <b>{{ agency.groupName }}</b> với mã
          <b>{{ agency.groupCode }}</b
          >.
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
            style="flex: 1"
          />
        </div>
        <p class="dlg-note">
          Khi chọn Xác nhận, gói cước sẽ chuyển sang trạng thái
          <b>"Đã duyệt"</b> và được áp dụng từ 00:00:00 ngày bắt đầu đến
          23:59:59 ngày kết thúc.
        </p>
        <ul class="dlg-bullets">
          <li>
            Từ 00:00:00 ngày bắt đầu, trạng thái sẽ chuyển sang
            <b>"Đang áp dụng"</b>
          </li>
          <li>
            Sau khi hết thời gian hiệu lực, trạng thái sẽ tự động chuyển về
            <b>"Khả dụng"</b>
          </li>
          <li>
            Chọn Từ chối, trạng thái gói cước sẽ chuyển về <b>"Khả dụng"</b>.
          </li>
        </ul>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" :disabled="!canApprovePlan" @click="confirmReject">Từ Chối</el-button>
          <div>
            <el-button type="primary" :disabled="!canApprovePlan" @click="confirmApprove"
              >Xác Nhận</el-button
            >
            <el-button @click="approveVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 3: DỪNG ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="stopApplyVisible"
      title="DỪNG ÁP DỤNG GÓI CƯỚC"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-desc">
          Nhấn <b>"Xác nhận"</b> để dừng lập tức áp dụng gói cước
          <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          cho đại lý <b>{{ agency.groupName }}</b> với mã
          <b>{{ agency.groupCode }}</b
          >. Sau khi dừng, trạng thái cập nhật sẽ chuyển sang <b>"Khả dụng"</b>.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmStopApply">Xác Nhận</el-button>
        <el-button @click="stopApplyVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 4: CHI TIẾT GÓI CƯỚC ===== -->
    <el-dialog
      v-model="planDetailVisible"
      title="CHI TIẾT GÓI CƯỚC"
      width="760px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <div class="detail-info-grid">
          <div class="detail-info-row">
            <span><b>Tên gói cước:</b> {{ selectedPlan?.planName }}</span>
            <span
              ><b>Trạng thái:</b>
              {{ planStatusLabel(selectedPlan?.status) }}</span
            >
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
            <span
              ><b>Thời gian cập nhật:</b> {{ selectedPlan?.updatedAt }}</span
            >
          </div>
        </div>

        <el-table :data="planConfigRows" border style="margin-top: 16px" v-loading="planDetailLoading">
          <el-table-column type="index" width="50" />
          <el-table-column prop="subject" label="ĐỐI TƯỢNG" width="160" />
          <el-table-column label="THỜI HẠN CHỨNG THƯ" width="160">
            <template #default="{ row }">{{ row.duration }} tháng</template>
          </el-table-column>
          <el-table-column prop="condition" label="ĐIỀU KIỆN" width="130" />
          <el-table-column
            prop="min"
            label="GIÁ TRỊ MIN (CỦA ĐIỀU KIỆN)"
            width="155"
          />
          <el-table-column
            prop="max"
            label="GIÁ TRỊ MAX (CỦA ĐIỀU KIỆN)"
            width="155"
          />
          <el-table-column label="PHÍ/ ĐIỀU KIỆN">
            <template #default="{ row }"
              >{{ row.fee.toLocaleString() }} vnđ</template
            >
          </el-table-column>
        </el-table>

        <p class="dlg-note" style="margin-top: 12px">
          <b>Lưu ý:</b> Gói cước đã tạo sẽ không thể chỉnh sửa. Vui lòng tạo gói
          cước mới trong trường hợp thay đổi.
        </p>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button
            v-if="selectedPlan?.status !== 'unavailable'"
            type="warning"
            :disabled="!can('group:update')"
            @click="openDisableDialog"
            >Vô Hiệu Hoá</el-button
          >
          <div>
            <el-button
              v-if="selectedPlan?.status === 'available'"
              type="primary"
              :disabled="!can('group:update')"
              @click="openRequestFromDetail"
              >Yêu Cầu Áp Dụng</el-button
            >
            <el-button @click="planDetailVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: XUẤT ĐỐI SOÁT ===== -->
    <el-dialog
      v-model="exportVisible"
      title="XUẤT ĐỐI SOÁT"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <div class="dlg-date-row">
          <span class="dlg-date-label">Thời gian đối soát</span>
          <el-date-picker
            v-model="exportMonth"
            type="month"
            placeholder="Chọn tháng xuất đối soát"
            format="MM/YYYY"
            value-format="YYYY-MM"
            style="flex: 1"
          />
        </div>
      </div>
      <template #footer>
        <el-button
          type="primary"
          :disabled="!exportMonth"
          @click="confirmExport"
          >Xác Nhận</el-button
        >
        <el-button @click="exportVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: KÍCH HOẠT LẠI ===== -->
    <el-dialog
      v-model="activateVisible"
      title="KÍCH HOẠT LẠI"
      width="460px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-desc">
          Bạn có chắc chắn muốn kích hoạt lại đại lý [{{ agency.groupCode }}] -
          {{ agency.groupName }}?
        </p>
        <p class="dlg-desc" style="margin-top: 8px">
          Đại lý sẽ được chuyển về trạng thái <b>"Đang hoạt động"</b> và tiếp
          tục sử dụng dịch vụ. Gói cước còn hiệu lực sẽ tiếp tục được áp dụng
          (nếu có).
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmActivate">Xác Nhận</el-button>
        <el-button @click="activateVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 5: VÔ HIỆU HOÁ ===== -->
    <el-dialog
      v-model="disableVisible"
      title="VÔ HIỆU HOÁ"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-desc">
          Bạn đang vô hiệu hoá gói cước <b>{{ selectedPlan?.planName }}</b> của
          đại lý [{{ agency.groupCode }}] - {{ agency.groupName }}.
        </p>
        <p class="dlg-desc" style="margin-top: 8px">
          Cấu hình gói cước này sẽ không còn khả dụng. Nhấn <b>"Xác Nhận"</b> để
          vô hiệu hoá.
        </p>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmDisable">Xác Nhận</el-button>
        <el-button @click="disableVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: CHỈNH SỬA THÔNG TIN ĐẠI LÝ ===== -->
    <el-dialog
      v-model="editVisible"
      title="CHỈNH SỬA THÔNG TIN ĐẠI LÝ"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="190px" class="edit-form">
        <el-form-item label="Mã đại lý">
          <div style="width: 100%">
            <el-input :value="agency.groupCode" disabled />
            <div class="field-hint">Không thể đổi mã đại lý</div>
          </div>
        </el-form-item>
        <el-form-item label="Tên đại lý">
          <el-input v-model="editForm.groupName" />
        </el-form-item>
        <el-form-item label="Email nhân sự phụ trách">
          <el-select
            v-model="editForm.picEmails"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder=""
            style="width: 100%"
          >
            <el-option
              v-for="e in editForm.picEmails"
              :key="e"
              :label="e"
              :value="e"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Email đại diện đại lý">
          <el-select
            v-model="editForm.contactEmails"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder=""
            style="width: 100%"
          >
            <el-option
              v-for="e in editForm.contactEmails"
              :key="e"
              :label="e"
              :value="e"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="editSaving" @click="confirmEdit"
          >Xác Nhận</el-button
        >
        <el-button :disabled="editSaving" @click="editVisible = false"
          >Hủy Bỏ</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Download,
  Edit,
  Plus,
  VideoPause,
  VideoPlay,
  Refresh,
  InfoFilled,
  Check,
  ArrowDown,
  ArrowRight,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { usePermission } from "@/composables/usePermission";
import {
  getGroup,
  updateGroup,
  suspendGroup,
  activateGroup,
  listGroupAssignments,
  createGroupAssignment,
  reviewAssignment,
  getGroupPlanHistory,
} from "@/api/groups";
import { getPlanTemplate } from "@/api/planTemplates";
import type {
  GroupDetail,
  GroupPlanAssignment,
  PlanHistory,
} from "@/types/group";

const route = useRoute();
const router = useRouter();
const agencyId = Number(route.params.id);
const { can } = usePermission();

// ---- State ----
const agency = ref<GroupDetail>({
  groupId: agencyId,
  groupCode: "",
  groupName: "",
  status: "ACTIVE",
  picEmails: [],
  contactEmails: [],
  refContractNo: null,
  createdBy: null,
  createdAt: null,
  updatedAt: null,
  ownerUserId: null,
  ownerName: null,
});

interface PlanRow {
  id: number;
  planTemplateId: number;
  approvalRequestId?: number;
  planName: string;
  status: "available" | "unavailable" | "pending" | "approved" | "active";
  applyFrom: string;
  applyTo: string;
  rawApplyFrom: string | null;
  rawApplyTo: string | null;
  updatedAt: string;
}

const plans = ref<PlanRow[]>([]);
const historyRows = ref<PlanHistory[]>([]);
const loading = ref(false);
const canApprovePlan = computed(() =>
  can("approval:level1") || can("approval:level2") || can("approval:level3")
);

// ---- Mapping helpers ----
function mapAssignmentStatus(s: string): PlanRow["status"] {
  const map: Record<string, PlanRow["status"]> = {
    AVAILABLE: "available",
    REQUESTED: "pending",
    APPROVED: "approved",
    ACTIVE: "active",
    REJECTED: "unavailable",
    STOPPED: "unavailable",
    EXPIRED: "unavailable",
  };
  return map[s] ?? "unavailable";
}

function formatDate(iso: string | null): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

function formatDatetime(iso: string | null): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

function mapAssignment(a: GroupPlanAssignment): PlanRow {
  return {
    id: a.groupPlanAssignmentId,
    planTemplateId: a.planTemplateId,
    approvalRequestId: a.approvalRequestId,
    planName: a.planName,
    status: mapAssignmentStatus(a.assignmentStatus),
    applyFrom: formatDate(a.applyFrom),
    applyTo: formatDate(a.applyTo),
    rawApplyFrom: a.applyFrom,
    rawApplyTo: a.applyTo,
    updatedAt: "",
  };
}

// ---- Load data ----
async function loadAgency() {
  try {
    const res = await getGroup(agencyId);
    if (res.success && res.data) {
      agency.value = res.data;
    } else {
      ElMessage.error(res.message || "Không tìm thấy đại lý");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
}

async function loadPlans() {
  try {
    const res = await listGroupAssignments(agencyId);
    if (res.success && res.data) {
      plans.value = res.data.map(mapAssignment);
    }
  } catch {
    ElMessage.error("Lỗi khi tải danh sách gói cước");
  }
}

async function loadHistory() {
  try {
    const res = await getGroupPlanHistory(agencyId);
    if (res.success && Array.isArray(res.data)) {
      historyRows.value = res.data;
    }
  } catch {
    ElMessage.error("Lỗi khi tải lịch sử áp dụng gói cước");
  }
}

// ---- Filter ----
const showHistory = ref(true);
const planFilterStatus = ref("");
const planFilterFrom = ref<Date | null>(null);
const planFilterTo = ref<Date | null>(null);
const planFilterUpdated = ref<Date | null>(null);

const filteredPlans = computed(() => {
  let result = plans.value;
  if (planFilterStatus.value)
    result = result.filter((p) => p.status === planFilterStatus.value);
  return result;
});

// ---- Dialog state ----
const selectedPlan = ref<PlanRow | null>(null);

const requestApplyVisible = ref(false);
const requestApplyDateRange = ref<[string, string] | null>(null);
const requestApprovalLevel = ref<1 | 2 | 3>(1);

const approveVisible = ref(false);
const approveDateRange = ref<[string, string] | null>(null);

const stopApplyVisible = ref(false);

const planDetailVisible = ref(false);
const planDetailLoading = ref(false);
const planConfigRows = ref<any[]>([]);

const disableVisible = ref(false);

const exportVisible = ref(false);
const exportMonth = ref<string | null>(null);

const activateVisible = ref(false);

const editVisible = ref(false);
const editSaving = ref(false);
const editForm = reactive({
  groupName: "",
  picEmails: [] as string[],
  contactEmails: [] as string[],
});

// --- helpers ---
function planStatusLabel(status?: string): string {
  const map: Record<string, string> = {
    available: "Khả dụng",
    unavailable: "Không khả dụng",
    pending: "Chờ duyệt",
    approved: "Đã duyệt",
    active: "Đang áp dụng",
  };
  return status ? (map[status] ?? status) : "";
}

// ---- Action handlers ----
function handleExport() {
  exportMonth.value = null;
  exportVisible.value = true;
}
function handleAddPlan() {
  router.push(`/plans/${agencyId}/add-plan`);
}

function handleEdit() {
  editForm.groupName = agency.value.groupName;
  editForm.picEmails = [...agency.value.picEmails];
  editForm.contactEmails = [...agency.value.contactEmails];
  editVisible.value = true;
}

async function handleSuspend() {
  try {
    await ElMessageBox.confirm("Tạm dừng đại lý này?", "Xác nhận", {
      type: "warning",
    });
  } catch {
    return; // user cancelled
  }
  try {
    const res = await suspendGroup(agencyId);
    if (res.success) {
      agency.value.status = "INACTIVE";
      ElMessage.success("Đã tạm dừng đại lý");
    } else {
      ElMessage.error(res.message || "Không thể tạm dừng");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
}

function handleActivate() {
  activateVisible.value = true;
}

function handleRequestApply(row: PlanRow) {
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền yêu cầu áp dụng gói cước");
    return;
  }
  selectedPlan.value = row;
  requestApplyDateRange.value =
    row.rawApplyFrom && row.rawApplyTo
      ? [row.rawApplyFrom, row.rawApplyTo]
      : null;
  requestApprovalLevel.value = 1;
  requestApplyVisible.value = true;
}

function handleApprove(row: PlanRow) {
  if (!canApprovePlan.value) {
    ElMessage.warning("Bạn không có quyền duyệt gói cước");
    return;
  }
  if (!row.approvalRequestId) {
    ElMessage.warning("KhÃ´ng tÃ¬m tháº¥y yÃªu cáº§u phÃª duyá»‡t cá»§a gÃ³i cÆ°á»›c");
    return;
  }
  router.push(`/approvals/${row.approvalRequestId}`);
}

function handleStopApply(row: PlanRow) {
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền dừng áp dụng gói cước");
    return;
  }
  selectedPlan.value = row;
  stopApplyVisible.value = true;
}

const subjectTypeLabel: Record<string, string> = {
  INDIVIDUAL: "Cá nhân",
  ORGANIZATION: "Tổ chức",
  INDIVIDUAL_OF_ORG: "Cá nhân thuộc tổ chức",
};
const pricingMetricLabel: Record<string, string> = {
  CERTIFICATE_COUNT: "Số lượng CTS",
  SIGNING_COUNT: "Số lượt ký",
};

async function handlePlanDetail(row: PlanRow) {
  selectedPlan.value = row;
  planConfigRows.value = [];
  planDetailVisible.value = true;
  planDetailLoading.value = true;
  try {
    const res = await getPlanTemplate(row.planTemplateId);
    if (res.success && res.data) {
      planConfigRows.value = (res.data.pricingRules ?? [])
        .filter((r) => r.isActive !== false)
        .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
        .map((r) => ({
          subject: subjectTypeLabel[r.subjectType] ?? r.subjectType,
          duration: r.certificateValidityValue,
          condition: pricingMetricLabel[r.pricingMetric] ?? r.pricingMetric,
          min: r.rangeMin,
          max: r.rangeMax ?? "Không giới hạn",
          fee: Number(r.unitPrice),
        }));
    }
  } catch {
    ElMessage.error("Không thể tải chi tiết gói cước");
  } finally {
    planDetailLoading.value = false;
  }
}

function openDisableDialog() {
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền vô hiệu hoá gói cước");
    return;
  }
  planDetailVisible.value = false;
  disableVisible.value = true;
}

function openRequestFromDetail() {
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền yêu cầu áp dụng gói cước");
    return;
  }
  planDetailVisible.value = false;
  requestApplyDateRange.value =
    selectedPlan.value?.rawApplyFrom && selectedPlan.value?.rawApplyTo
      ? [selectedPlan.value.rawApplyFrom, selectedPlan.value.rawApplyTo]
      : null;
  requestApprovalLevel.value = 1;
  requestApplyVisible.value = true;
}

// ---- Confirm handlers ----
async function confirmRequestApply() {
  if (!selectedPlan.value) return;
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền yêu cầu áp dụng gói cước");
    return;
  }
  try {
    const res = await createGroupAssignment(agencyId, {
      planTemplateId: selectedPlan.value.planTemplateId,
      requestedBy: "system",
      applyFrom: requestApplyDateRange.value
        ? requestApplyDateRange.value[0]
        : null,
      applyTo: requestApplyDateRange.value
        ? requestApplyDateRange.value[1]
        : null,
      approvalLevel: requestApprovalLevel.value,
    });
    if (res.success) {
      ElMessage.success("Đã gửi yêu cầu áp dụng");
      await loadPlans();
    } else {
      ElMessage.error(res.message || "Không thể gửi yêu cầu");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  requestApplyVisible.value = false;
}

async function confirmApprove() {
  if (!selectedPlan.value) return;
  if (!canApprovePlan.value) {
    ElMessage.warning("Bạn không có quyền duyệt gói cước");
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "APPROVE",
      actor: "system",
      applyFrom: approveDateRange.value ? approveDateRange.value[0] : null,
      applyTo: approveDateRange.value ? approveDateRange.value[1] : null,
    });
    if (res.success) {
      ElMessage.success("Đã duyệt gói cước");
      await loadPlans();
    } else {
      ElMessage.error(res.message || "Không thể duyệt");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  approveVisible.value = false;
}

async function confirmReject() {
  if (!selectedPlan.value) return;
  if (!canApprovePlan.value) {
    ElMessage.warning("Bạn không có quyền từ chối duyệt gói cước");
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "REJECT",
      actor: "system",
    });
    if (res.success) {
      ElMessage.success("Đã từ chối gói cước");
      await loadPlans();
    } else {
      ElMessage.error(res.message || "Không thể từ chối");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  approveVisible.value = false;
}

async function confirmStopApply() {
  if (!selectedPlan.value) return;
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền dừng áp dụng gói cước");
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "STOP",
      actor: "system",
      note: "Dừng áp dụng",
    });
    if (res.success) {
      ElMessage.success("Đã dừng áp dụng gói cước");
      await loadPlans();
    } else {
      ElMessage.error(res.message || "Không thể dừng");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  stopApplyVisible.value = false;
}

async function confirmDisable() {
  if (!selectedPlan.value) return;
  if (!can("group:update")) {
    ElMessage.warning("Bạn không có quyền vô hiệu hoá gói cước");
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "STOP",
      actor: "system",
      note: "Vô hiệu hoá",
    });
    if (res.success) {
      ElMessage.success("Đã vô hiệu hoá gói cước");
      await loadPlans();
    } else {
      ElMessage.error(res.message || "Không thể vô hiệu hoá");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  disableVisible.value = false;
}

function confirmExport() {
  // TODO: call export API with exportMonth.value
  exportVisible.value = false;
}

async function confirmActivate() {
  try {
    const res = await activateGroup(agencyId);
    if (res.success) {
      agency.value.status = "ACTIVE";
      ElMessage.success("Đã kích hoạt đại lý");
    } else {
      ElMessage.error(res.message || "Không thể kích hoạt");
    }
  } catch {
    ElMessage.error("Lỗi kết nối server");
  }
  activateVisible.value = false;
}

onMounted(async () => {
  loading.value = true;
  await Promise.all([loadAgency(), loadPlans(), loadHistory()]);
  loading.value = false;
});
async function confirmEdit() {
  editSaving.value = true;
  try {
    const res = await updateGroup(agency.value.groupId, {
      groupName: editForm.groupName,
      picEmails: editForm.picEmails,
      contactEmails: editForm.contactEmails,
      refContractNo: agency.value.refContractNo ?? undefined,
    });
    if (res.success) {
      agency.value.groupName = editForm.groupName;
      agency.value.picEmails = [...editForm.picEmails];
      agency.value.contactEmails = [...editForm.contactEmails];
      ElMessage.success("Cập nhật thông tin đại lý thành công");
      editVisible.value = false;
    } else {
      ElMessage.error(res.message || "Cập nhật thất bại");
    }
  } catch {
    ElMessage.error("Đã xảy ra lỗi, vui lòng thử lại");
  } finally {
    editSaving.value = false;
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 12px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.breadcrumb {
  font-size: 13px;
  color: #909399;
}

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.section-card {
  margin-bottom: 16px;
}
.section-title {
  color: #1b60cb;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  margin-bottom: 14px;
}
.toggle-title {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #303133;
}
.info-row {
  line-height: 1.6;
}
.info-row.two-col {
  display: flex;
  gap: 60px;
}

.text-active {
  color: #1b60cb;
  font-weight: 500;
}
.text-inactive {
  color: #ff9f43;
  font-weight: 500;
}

.col-label {
  font-weight: 600;
  font-size: 13px;
}
.col-filter {
  margin-top: 6px;
  min-height: 28px;
}

:deep(.tag-approved) {
  background-color: #303133;
  border-color: #303133;
  color: #fff;
}

.action-btns {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
}
.action-btns :deep(.el-button) {
  margin: 0;
}

:deep(.el-table th.el-table__cell) {
  vertical-align: top;
  padding: 8px 0;
}

/* Dialog common */
.dlg-body {
  font-size: 13px;
  color: #303133;
  line-height: 1.7;
}
.dlg-info-line {
  margin: 4px 0;
}
.dlg-value {
  font-weight: 500;
}
.dlg-plan-name {
  color: #1b60cb;
}
.dlg-desc {
  margin: 0 0 8px;
}
.dlg-note {
  margin: 12px 0 0;
  font-size: 12px;
  color: #606266;
  font-style: italic;
}
.dlg-bullets {
  margin: 6px 0 0 0;
  padding-left: 18px;
}
.dlg-bullets li {
  margin-bottom: 4px;
}

.dlg-date-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0;
}
.dlg-date-label {
  white-space: nowrap;
  font-size: 13px;
}

.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.dlg-footer-split > div {
  display: flex;
  gap: 8px;
}

/* Dialog 4 info grid */
.detail-info-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}
.detail-info-row {
  display: flex;
  gap: 40px;
}
.detail-info-row span {
  flex: 1;
}
</style>
