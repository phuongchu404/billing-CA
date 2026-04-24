<template>
  <div>
    <div class="page-header">
      <div>
        <h2>Theo dõi sử dụng</h2>
        <p class="page-subtitle">Khách hàng phổ thông</p>
      </div>
    </div>

    <el-card shadow="never">
      <!-- Stats -->
      <div class="stats-grid">
        <div class="stat-item">
          SL tài khoản: <b>{{ stats.accounts }}</b>
        </div>
        <div class="stat-item">
          SL gói cước đã mua: <b>{{ stats.plansBought }}</b>
        </div>
        <div class="stat-item">
          SL lượt đã ký: <b>{{ stats.signings.toLocaleString("vi-VN") }}</b>
        </div>
        <div class="stat-item">
          SL CTS cá nhân: <b>{{ stats.ctsIndividual }}</b>
        </div>
        <div class="stat-item">
          SL CTS tổ chức: <b>{{ stats.ctsOrg }}</b>
        </div>
        <div class="stat-item">
          SL CTS cá nhân thuộc tổ chức: <b>{{ stats.ctsIndividualOfOrg }}</b>
        </div>
      </div>

      <div class="action-bar">
        <el-button :icon="Grid" @click="handleExport">Xuất Dữ Liệu</el-button>
        <span class="last-updated">Lần cập nhật cuối: {{ lastUpdated }}</span>
      </div>

      <!-- Pagination top -->
      <!-- <div class="pagination-row">
        <span class="page-label">
          Hiển thị
          <el-select v-model="pageSize" size="small" style="width:64px;margin:0 4px" @change="page = 1">
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ filteredList.length }} lượt mua gói cước
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

      <!-- Table -->
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

        <el-table-column prop="account" sortable min-width="145">
          <template #header>
            <div class="col-label">TÀI KHOẢN</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="purchasedAt" sortable width="165">
          <template #header>
            <div class="col-label">THỜI GIAN MUA GÓI</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterPurchasedAt"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="ctsType" sortable width="155">
          <template #header>
            <div class="col-label">PHÂN LOẠI CTS</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsType"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              >
                <el-option label="Cá nhân" value="INDIVIDUAL" />
                <el-option label="Tổ chức" value="ORGANIZATION" />
                <el-option
                  label="Cá nhân thuộc tổ chức"
                  value="INDIVIDUAL_OF_ORG"
                />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">{{
            ctsTypeLabel(row.ctsType)
          }}</template>
        </el-table-column>

        <el-table-column prop="ctsDuration" sortable width="130">
          <template #header>
            <div class="col-label">THỜI HẠN CTS</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsDuration"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              >
                <el-option label="1 tháng" value="1" />
                <el-option label="12 tháng" value="12" />
                <el-option label="24 tháng" value="24" />
                <el-option label="48 tháng" value="48" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">{{ row.ctsDuration }} tháng</template>
        </el-table-column>

        <el-table-column prop="ctsStatus" sortable width="145">
          <template #header>
            <div class="col-label">TRẠNG THÁI CTS</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              >
                <el-option label="Đang hoạt động" value="ACTIVE" />
                <el-option label="Chờ kích hoạt" value="PENDING_ACTIVATE" />
                <el-option label="Chờ cấp duyệt" value="PENDING_APPROVE" />
                <el-option label="Đã thu hồi" value="REVOKED" />
                <el-option label="Hết hạn" value="EXPIRED" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag
              v-if="row.ctsStatus === 'PENDING_ACTIVATE'"
              type="primary"
              size="small"
              effect="plain"
              >Chờ kích hoạt</el-tag
            >
            <el-tag
              v-else-if="row.ctsStatus === 'REVOKED'"
              type="info"
              size="small"
              effect="plain"
              >Đã thu hồi</el-tag
            >
            <span v-else :class="ctsStatusClass(row.ctsStatus)">{{
              ctsStatusLabel(row.ctsStatus)
            }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="signings" sortable width="120" align="right">
          <template #header>
            <div class="col-label">LƯỢT ĐÃ KÝ</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{
            row.signings.toLocaleString("vi-VN")
          }}</template>
        </el-table-column>

        <el-table-column prop="plan" sortable min-width="145">
          <template #header>
            <div class="col-label">GÓI CƯỚC ÁP DỤNG</div>
            <div class="col-filter">
              <el-select
                v-model="filterPlan"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
                @change="page = 1"
              >
                <el-option label="SmartCA 2025" value="SmartCA 2025" />
                <el-option label="SmartCA 2026" value="SmartCA 2026" />
              </el-select>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fee" sortable width="120" align="right">
          <template #header>
            <div class="col-label">PHÍ (VNĐ)</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{
            row.fee.toLocaleString("vi-VN")
          }}</template>
        </el-table-column>
      </el-table>

      <!-- Pagination bottom -->
      <div class="pagination-row" style="margin-top: 12px">
        <span class="page-label">
          Hiển thị
          <el-select
            v-model="pageSize"
            size="small"
            style="width: 64px; margin: 0 4px"
            @change="page = 1"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ filteredList.length }} lượt mua gói cước
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { Refresh, Grid } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

type CtsType = "INDIVIDUAL" | "ORGANIZATION" | "INDIVIDUAL_OF_ORG";
type CtsStatus =
  | "ACTIVE"
  | "PENDING_ACTIVATE"
  | "PENDING_APPROVE"
  | "REVOKED"
  | "EXPIRED";

interface UsageRow {
  id: number;
  account: string;
  purchasedAt: string;
  ctsType: CtsType;
  ctsDuration: number;
  ctsStatus: CtsStatus;
  signings: number;
  plan: string;
  fee: number;
}

const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const filterPurchasedAt = ref<Date | null>(null);
const filterCtsType = ref("");
const filterCtsDuration = ref("");
const filterCtsStatus = ref("");
const filterPlan = ref("");

const lastUpdated = ref("29/03/2026 16:29:00");

const stats = ref({
  accounts: 22,
  plansBought: 36,
  signings: 3800,
  ctsIndividual: 28,
  ctsOrg: 6,
  ctsIndividualOfOrg: 2,
});

const CTS_TYPES: CtsType[] = [
  "INDIVIDUAL",
  "ORGANIZATION",
  "INDIVIDUAL_OF_ORG",
  "INDIVIDUAL_OF_ORG",
  "INDIVIDUAL_OF_ORG",
];
const CTS_STATUSES: CtsStatus[] = [
  "ACTIVE",
  "PENDING_ACTIVATE",
  "PENDING_APPROVE",
  "REVOKED",
  "EXPIRED",
  "ACTIVE",
  "ACTIVE",
  "ACTIVE",
  "ACTIVE",
  "ACTIVE",
];
const CTS_DURATIONS = [1, 12, 24, 48, 1, 1, 1, 1, 1, 1];
const SIGNINGS = [
  3800, 0, 0, 150000, 100, 150000, 150000, 150000, 150000, 150000,
];

const MOCK_DATA: UsageRow[] = Array.from({ length: 36 }, (_, i) => ({
  id: i + 1,
  account: "001300002288",
  purchasedAt: "27/03/2026 18:29:00",
  ctsType: CTS_TYPES[i % CTS_TYPES.length],
  ctsDuration: CTS_DURATIONS[i % CTS_DURATIONS.length],
  ctsStatus: CTS_STATUSES[i % CTS_STATUSES.length],
  signings: SIGNINGS[i % SIGNINGS.length],
  plan: "SmartCA 2025",
  fee: 1200000,
}));

const list = ref<UsageRow[]>([]);

const filteredList = computed(() => {
  return list.value.filter((row) => {
    if (filterCtsType.value && row.ctsType !== filterCtsType.value)
      return false;
    if (
      filterCtsDuration.value &&
      String(row.ctsDuration) !== filterCtsDuration.value
    )
      return false;
    if (filterCtsStatus.value && row.ctsStatus !== filterCtsStatus.value)
      return false;
    if (filterPlan.value && row.plan !== filterPlan.value) return false;
    return true;
  });
});

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

function ctsTypeLabel(type: CtsType): string {
  const map: Record<CtsType, string> = {
    INDIVIDUAL: "Cá nhân",
    ORGANIZATION: "Tổ chức",
    INDIVIDUAL_OF_ORG: "Cá nhân thuộc tổ chức",
  };
  return map[type];
}

function ctsStatusLabel(status: CtsStatus): string {
  const map: Record<CtsStatus, string> = {
    ACTIVE: "Đang hoạt động",
    PENDING_ACTIVATE: "Chờ kích hoạt",
    PENDING_APPROVE: "Chờ cấp duyệt",
    REVOKED: "Đã thu hồi",
    EXPIRED: "Hết hạn",
  };
  return map[status] ?? status;
}

function ctsStatusClass(status: CtsStatus): string {
  const map: Record<CtsStatus, string> = {
    ACTIVE: "status-active",
    PENDING_ACTIVATE: "status-pending",
    PENDING_APPROVE: "status-pending",
    REVOKED: "status-inactive",
    EXPIRED: "status-inactive",
  };
  return map[status] ?? "";
}

function resetFilters() {
  filterPurchasedAt.value = null;
  filterCtsType.value = "";
  filterCtsDuration.value = "";
  filterCtsStatus.value = "";
  filterPlan.value = "";
  page.value = 1;
  load();
}

function handleExport() {
  ElMessage.info("Chức năng đang phát triển");
}

function load() {
  loading.value = true;
  setTimeout(() => {
    list.value = [...MOCK_DATA];
    loading.value = false;
  }, 300);
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px 24px;
  font-size: 14px;
  color: #303133;
  margin-bottom: 16px;
  line-height: 1.8;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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

:deep(.el-table th.el-table__cell) {
  vertical-align: top;
  padding: 8px 0;
}

.status-active {
  color: #67c23a;
  font-size: 13px;
}
.status-pending {
  color: #1b60cb;
  font-size: 13px;
}
.status-inactive {
  color: #909399;
  font-size: 13px;
}
</style>
