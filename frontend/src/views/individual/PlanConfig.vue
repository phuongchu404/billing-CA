<template>
  <div>
    <div class="page-header">
      <div>
        <h2>Cấu hình gói cước</h2>
        <p class="page-subtitle">Khách hàng phổ thông</p>
      </div>
    </div>

    <el-card shadow="never">
      <div class="current-plan-info">
        <div v-if="currentPlan">
          Gói cước đang áp dụng:
          <b>{{ currentPlan.name }}</b>
          áp dụng đến ngày
          <b>{{ currentPlan.applyUntil }}</b>
        </div>
        <div v-if="nextPlan">
          Gói cước tiếp theo:
          <b>{{ nextPlan.name }}</b>
          áp dụng từ ngày
          <b>{{ nextPlan.applyFrom }}</b>
        </div>
      </div>

      <div class="info-bar">
        <el-button type="primary" :icon="Plus" @click="handleAddNew"
          >Thêm Mới</el-button
        >
        <span class="last-updated">Lần cập nhật cuối: {{ lastUpdated }}</span>
      </div>

      <!-- <div class="pagination-row">
        <span class="page-label">
          Hiển thị
          <el-select
            v-model="pageSize"
            size="small"
            style="width: 64px; margin: 0 4px"
            @change="page = 1"
          >
            <el-option :value="5" label="5" />
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
          </el-select>
          trong tổng số {{ filteredList.length }} gói cước
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="filteredList.length"
          :page-size="pageSize"
          layout="prev, pager, next, jumper"
          :pager-count="5"
          background
        />
      </div> -->

      <el-table :data="pagedList" v-loading="loading" border>
        <el-table-column
          width="55"
          type="index"
          :index="(i: number) => (page - 1) * pageSize + i + 1"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="resetFilters" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" sortable min-width="180">
          <template #header>
            <div class="col-label">TÊN GÓI</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable width="140">
          <template #header>
            <div class="col-label">TRẠNG THÁI</div>
            <div class="col-filter">
              <el-select
                v-model="filterStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              >
                <el-option label="Khả dụng" value="AVAILABLE" />
                <el-option label="Không khả dụng" value="UNAVAILABLE" />
                <el-option label="Chờ duyệt" value="PENDING" />
                <el-option label="Đã duyệt" value="APPROVED" />
                <el-option label="Đang áp dụng" value="APPLYING" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag
              :type="statusTagType(row.status)"
              :effect="row.status === 'APPLYING' ? 'dark' : 'light'"
              size="small"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="applyFrom" sortable width="130">
          <template #header>
            <div class="col-label">ÁP DỤNG TỪ</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterApplyFrom"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyFrom ?? "" }}</template>
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
                style="width: 100%"
                @change="page = 1"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyUntil ?? "" }}</template>
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
                style="width: 100%"
                @change="page = 1"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.updatedAt ?? "" }}</template>
        </el-table-column>

        <el-table-column fixed="right" width="210">
          <template #header>
            <div class="col-label">HÀNH ĐỘNG</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" :icon="Timer" @click.stop="goDetail(row)"
                >Chi tiết</el-button
              >
              <el-button
                v-if="row.status === 'AVAILABLE'"
                size="small"
                type="primary"
                plain
                @click.stop="openRequestApply(row)"
              >
                Y/c áp dụng
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="success"
                plain
                :icon="Check"
                @click.stop="openApprove(row)"
              >
                Duyệt
              </el-button>
              <el-button
                v-if="row.status === 'APPROVED' || row.status === 'APPLYING'"
                size="small"
                type="primary"
                @click.stop="openStopApply(row)"
              >
                Dừng áp dụng
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row" style="margin-top: 12px">
        <span class="page-label">
          Hiển thị
          <el-select
            v-model="pageSize"
            size="small"
            style="width: 64px; margin: 0 4px"
            @change="page = 1"
          >
            <el-option :value="5" label="5" />
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
          </el-select>
          trong tổng số {{ filteredList.length }} gói cước
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="filteredList.length"
          :page-size="pageSize"
          layout="prev, pager, next, jumper"
          :pager-count="5"
          background
        />
      </div>
    </el-card>

    <!-- Dialog 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC -->
    <el-dialog v-model="requestApplyVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC</span>
      </template>
      <div class="dlg-name-row">
        <span
          >Tên: <b>{{ activeRow?.name }}</b></span
        >
        <el-button link type="primary" @click="goDetail(activeRow!)"
          >Xem chi tiết</el-button
        >
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
        Khi bấm nút Xác Nhận, hệ thống sẽ đưa phiên bản hiện tại sẽ chuyển sang
        trạng thái "Chờ duyệt". Sau khi được duyệt, bảng gói cước sẽ được áp
        dụng từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
      </p>
      <template #footer>
        <el-button type="primary" @click="confirmRequestApply"
          >Xác Nhận</el-button
        >
        <el-button @click="requestApplyVisible = false">Huỷ Bỏ</el-button>
      </template>
    </el-dialog>

    <!-- Dialog 2: DUYỆT ÁP DỤNG GÓI CƯỚC -->
    <el-dialog v-model="approveVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">DUYỆT ÁP DỤNG GÓI CƯỚC</span>
      </template>
      <p class="dlg-body">
        Nhấn "Xác nhận" để duyệt áp dụng gói cước
        <b class="dlg-plan-name">{{ activeRow?.name }}</b>
        cho khách hàng phổ thông.
      </p>
      <el-form label-width="140px" style="margin-top: 16px">
        <el-form-item label="Thời gian áp dụng">
          <el-date-picker
            v-model="approveDateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="Từ"
            end-placeholder="Đến"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <p class="dlg-note">
        Khi chọn Xác nhận, gói cước sẽ chuyển sang trạng thái "Đã duyệt" và được
        áp dụng từ 00:00:00 ngày bắt đầu đến 23:59:59 ngày kết thúc.
      </p>
      <ul class="dlg-bullets">
        <li>
          Từ 00:00:00 ngày bắt đầu, trạng thái sẽ chuyển sang "Đang áp dụng"
        </li>
        <li>
          Sau khi hết thời gian hiệu lực, trạng thái sẽ tự động chuyển về "Khả
          dụng"
        </li>
        <li>Chọn Từ chối, trạng thái gói cước sẽ chuyển về "Khả dụng".</li>
      </ul>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="confirmReject"
            >Từ Chối</el-button
          >
          <div class="dlg-footer-right">
            <el-button type="primary" @click="confirmApprove"
              >Xác Nhận</el-button
            >
            <el-button @click="approveVisible = false">Hủy Bỏ</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- Dialog 3: DỪNG ÁP DỤNG GÓI CƯỚC -->
    <el-dialog v-model="stopApplyVisible" width="500px" align-center>
      <template #header>
        <span class="dlg-title">DỪNG ÁP DỤNG GÓI CƯỚC</span>
      </template>
      <p class="dlg-body">
        Nhấn "Xác nhận" để dừng lập tức gói cước
        <b class="dlg-plan-name">{{ activeRow?.name }}</b
        >. Sau khi dừng, trạng thái cập nhật sẽ chuyển sang "Khả dụng".
      </p>
      <template #footer>
        <el-button type="primary" @click="confirmStopApply">Xác Nhận</el-button>
        <el-button @click="stopApplyVisible = false">Hủy Bỏ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Plus, Refresh, Timer, Check } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import {
  getIndividualPlanConfigSummary,
  requestApplyPlanConfig,
  approvePlanConfig,
  rejectPlanConfig,
  stopPlanConfig,
} from "@/api/individual";
import type { IndividualPlanConfigListItem } from "@/types/individual";

const router = useRouter();

type PlanStatus =
  | "AVAILABLE"
  | "UNAVAILABLE"
  | "PENDING"
  | "APPROVED"
  | "APPLYING";

type PlanConfigRow = IndividualPlanConfigListItem;

const loading = ref(false);
const page = ref(1);
const pageSize = ref(5);
const filterStatus = ref("");
const filterApplyFrom = ref<Date | null>(null);
const filterApplyUntil = ref<Date | null>(null);
const filterUpdatedAt = ref<Date | null>(null);

const lastUpdated = ref<string | null>(null);
const currentPlan = ref<{ name: string; applyUntil: string | null } | null>(null);
const nextPlan = ref<{ name: string; applyFrom: string | null } | null>(null);

// Dialog state
const activeRow = ref<PlanConfigRow | null>(null);
const requestApplyVisible = ref(false);
const requestApplyDateRange = ref<[Date, Date] | null>(null);
const approveVisible = ref(false);
const approveDateRange = ref<[Date, Date] | null>(null);
const stopApplyVisible = ref(false);

const list = ref<PlanConfigRow[]>([]);

const filteredList = computed(() => {
  return list.value.filter((row) => {
    if (filterStatus.value && row.status !== filterStatus.value) return false;
    return true;
  });
});

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

function statusLabel(status: PlanStatus): string {
  const map: Record<PlanStatus, string> = {
    AVAILABLE: "Khả dụng",
    UNAVAILABLE: "Không khả dụng",
    PENDING: "Chờ duyệt",
    APPROVED: "Đã duyệt",
    APPLYING: "Đang áp dụng",
  };
  return map[status] ?? status;
}

function statusTagType(
  status: PlanStatus,
): undefined | "success" | "warning" | "info" | "danger" {
  const map: Record<
    PlanStatus,
    undefined | "success" | "warning" | "info" | "danger"
  > = {
    AVAILABLE: "info",
    UNAVAILABLE: "info",
    PENDING: "warning",
    APPROVED: "success",
    APPLYING: undefined,
  };
  return map[status] ?? "info";
}

function resetFilters() {
  filterStatus.value = "";
  filterApplyFrom.value = null;
  filterApplyUntil.value = null;
  filterUpdatedAt.value = null;
  page.value = 1;
  load();
}

function handleAddNew() {
  router.push("/individual-plan-config/new");
}

function goDetail(row: PlanConfigRow) {
  router.push("/individual-plan-config/" + row.id);
}

// Dialog openers
function openRequestApply(row: PlanConfigRow) {
  activeRow.value = row;
  requestApplyDateRange.value = null;
  requestApplyVisible.value = true;
}

function openApprove(row: PlanConfigRow) {
  activeRow.value = row;
  approveDateRange.value = null;
  approveVisible.value = true;
}

function openStopApply(row: PlanConfigRow) {
  activeRow.value = row;
  stopApplyVisible.value = true;
}

// Dialog confirmations
async function confirmRequestApply() {
  if (!activeRow.value || !requestApplyDateRange.value) {
    ElMessage.warning("Vui lòng chọn thời gian áp dụng");
    return;
  }
  const [from, to] = requestApplyDateRange.value;
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  try {
    await requestApplyPlanConfig(activeRow.value.id, {
      applyFrom: fmt(from),
      applyUntil: fmt(to),
    });
    ElMessage.success("Đã gửi yêu cầu áp dụng");
    requestApplyVisible.value = false;
    load();
  } catch {
    ElMessage.error("Gửi yêu cầu thất bại");
  }
}

async function confirmApprove() {
  if (!activeRow.value) return;
  const payload: { applyFrom?: string; applyUntil?: string } = {};
  if (approveDateRange.value) {
    const [from, to] = approveDateRange.value;
    const fmt = (d: Date) => d.toISOString().slice(0, 10);
    payload.applyFrom = fmt(from);
    payload.applyUntil = fmt(to);
  }
  try {
    await approvePlanConfig(activeRow.value.id, payload);
    ElMessage.success("Đã duyệt gói cước");
    approveVisible.value = false;
    load();
  } catch {
    ElMessage.error("Duyệt gói cước thất bại");
  }
}

async function confirmReject() {
  if (!activeRow.value) return;
  try {
    await rejectPlanConfig(activeRow.value.id);
    ElMessage.info("Đã từ chối duyệt gói cước");
    approveVisible.value = false;
    load();
  } catch {
    ElMessage.error("Từ chối thất bại");
  }
}

async function confirmStopApply() {
  if (!activeRow.value) return;
  try {
    await stopPlanConfig(activeRow.value.id);
    ElMessage.success("Đã dừng áp dụng gói cước");
    stopApplyVisible.value = false;
    load();
  } catch {
    ElMessage.error("Dừng áp dụng thất bại");
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await getIndividualPlanConfigSummary();
    if (res.success && res.data) {
      list.value = res.data.list ?? [];
      currentPlan.value = res.data.currentPlan ?? null;
      nextPlan.value = res.data.nextPlan ?? null;
      lastUpdated.value = res.data.lastUpdated ?? null;
    }
  } catch {
    ElMessage.error("Không thể tải danh sách gói cước");
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
}
.page-subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.current-plan-info {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

.info-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.last-updated {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.page-label {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
}

.col-label {
  font-weight: 600;
  font-size: 13px;
  white-space: normal;
  line-height: 1.3;
}
.col-filter {
  margin-top: 6px;
  min-height: 28px;
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

/* Dialog styles */
.dlg-title {
  font-weight: 700;
  font-size: 15px;
  color: #303133;
}

.dlg-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #303133;
}

.dlg-body {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin: 0 0 4px;
}

.dlg-plan-name {
  color: #1b60cb;
}

.dlg-note {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 12px 0 0;
}

.dlg-bullets {
  margin: 6px 0 0;
  padding-left: 20px;
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.dlg-footer-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.dlg-footer-right {
  display: flex;
  gap: 8px;
}
</style>
