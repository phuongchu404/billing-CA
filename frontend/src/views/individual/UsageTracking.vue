<template>
  <div>
    <div class="page-header">
      <div>
        <h2>{{ $t('usageTracking.title') }}</h2>
        <p class="page-subtitle">{{ $t('menu.regularCustomer') }}</p>
      </div>
    </div>

    <el-card shadow="never">
      <!-- Stats -->
      <div class="stats-grid">
        <div class="stat-item">
          {{ $t('usageTracking.accountsLabel') }} {{ stats.accounts }}
        </div>
        <div class="stat-item">
          {{ $t('usageTracking.plansBoughtLabel') }} {{ stats.plansBought }}
        </div>
        <div class="stat-item">
          {{ $t('usageTracking.signingsLabel') }} {{ stats.signings.toLocaleString("vi-VN") }}
        </div>
        <div class="stat-item">
          {{ $t('usageTracking.ctsIndividualLabel') }} {{ stats.ctsIndividual }}
        </div>
        <div class="stat-item">
          {{ $t('usageTracking.ctsOrgLabel') }} {{ stats.ctsOrg }}
        </div>
        <div class="stat-item">
          {{ $t('usageTracking.ctsIndividualOfOrgLabel') }} {{ stats.ctsIndividualOfOrg }}
        </div>
      </div>

      <div class="action-bar">
        <el-button class="btn-primary" :icon="Grid" @click="handleExport">{{ $t('usageTracking.exportData') }}</el-button>
        <span class="last-updated">{{ $t('usageTracking.lastUpdatedLabel', { time: lastUpdated }) }}</span>
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
      <el-table :data="pagedList" v-loading="loading" border @sort-change="handleSortChange">
        <el-table-column
          width="50"
          type="index"
          header-align="center"
          align="center"
          :index="(i: number) => (page - 1) * pageSize + i + 1"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="resetFilters" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="purchasedAt" sortable="custom"width="210"  align="center">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colPurchaseDate') }}</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterPurchasedAt"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="ctsType" sortable="custom"width="165">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colCtsType') }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsType"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option :label="$t('usageTracking.individual')" value="INDIVIDUAL" />
                <el-option :label="$t('usageTracking.organization')" value="ORGANIZATION" />
                <el-option
                  :label="$t('usageTracking.individualOfOrg')"
                  value="INDIVIDUAL_OF_ORG"
                />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">{{
            ctsTypeLabel(row.ctsType)
          }}</template>
        </el-table-column>
        
        <el-table-column prop="ctsDuration" sortable="custom"width="160" align="center">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colCtsDuration') }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsDuration"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option :label="$t('usageTracking.filter1m')" value="1" />
                <el-option :label="$t('usageTracking.filter12m')" value="12" />
                <el-option :label="$t('usageTracking.filter24m')" value="24" />
                <el-option :label="$t('usageTracking.filter48m')" value="48" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">{{ row.ctsDuration }} {{ $t('usageTracking.months') }}</template>
        </el-table-column>

        <el-table-column prop="account" sortable="custom"min-width="180">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colAccount') }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="ctsStatus" sortable="custom"width="170" align="center">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colCtsStatus') }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterCtsStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option :label="$t('usageTracking.filterActive')" value="ACTIVE" />
                <el-option :label="$t('usageTracking.filterPendingActivate')" value="PENDING_ACTIVATE" />
                <el-option :label="$t('usageTracking.filterPendingApprove')" value="PENDING_APPROVE" />
                <el-option :label="$t('usageTracking.filterRevoked')" value="REVOKED" />
                <el-option :label="$t('usageTracking.filterExpired')" value="EXPIRED" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <span 
              :class="['custom-cts-tag', `status-${(row.ctsStatus || '').toLowerCase().replace('_', '-')}`]"
            >
              {{ ctsStatusLabel(row.ctsStatus) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="signings" sortable="custom"width="140" align="center">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colSigningUsed') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{
            row.signings.toLocaleString("vi-VN")
          }}</template>
        </el-table-column>

        <el-table-column prop="plan" sortable="custom"min-width="200">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colPlanApplied') }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterPlan"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option label="SmartCA 2025" value="SmartCA 2025" />
                <el-option label="SmartCA 2026" value="SmartCA 2026" />
              </el-select>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fee" sortable="custom"width="140" align="center">
          <template #header>
            <div class="col-label">{{ $t('usageTracking.colFee') }}</div>
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
          {{ $t('usageTracking.showing') }}
          <el-select
            v-model="pageSize"
            size="small"
            style="width: 64px; margin: 0 4px"
            @change="() => { page = 1; load(); }"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          {{ $t('usageTracking.totalPlans', { total: totalElements }) }}
        </span>
        <div class="custom-pagination-wrapper">
          <button
            class="custom-nav-btn"
            :disabled="page === 1"
            @click="page = 1; load()"
          >
            <el-icon><DArrowLeft /></el-icon>
          </button>

          <el-pagination
            v-model:current-page="page"
            :total="totalElements"
            :page-size="pageSize"
            layout="prev, pager, next"
            :pager-count="5"
            background
            @current-change="load"
          />

          <button
            class="custom-nav-btn"
            :disabled="page === Math.ceil(totalElements / pageSize)"
            @click="page = Math.ceil(totalElements / pageSize); load()"
          >
            <el-icon><DArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { Refresh, Grid } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { getIndividualUsageTracking, type IndividualUsageFilterParams } from "@/api/individual";
import type { IndividualUsageRow, IndividualUsageStats } from "@/types/individual";

const { t } = useI18n();

type CtsType = "INDIVIDUAL" | "ORGANIZATION" | "INDIVIDUAL_OF_ORG";
type CtsStatus =
  | "ACTIVE"
  | "PENDING_ACTIVATE"
  | "PENDING_APPROVE"
  | "REVOKED"
  | "EXPIRED";

const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const currentSort = ref<{ prop: string; order: string | null }>({ prop: '', order: null });
const filterPurchasedAt = ref<Date | null>(null);
const filterCtsType = ref("");
const filterCtsDuration = ref("");
const filterCtsStatus = ref("");
const filterPlan = ref("");

const lastUpdated = ref<string | null>(null);

const stats = ref<IndividualUsageStats>({
  accounts: 0,
  plansBought: 0,
  signings: 0,
  ctsIndividual: 0,
  ctsOrg: 0,
  ctsIndividualOfOrg: 0,
});

const list = ref<IndividualUsageRow[]>([]);

const pagedList = computed(() => list.value);

function ctsTypeLabel(type: CtsType): string {
  const map: Record<CtsType, string> = {
    INDIVIDUAL: t('usageTracking.individual'),
    ORGANIZATION: t('usageTracking.organization'),
    INDIVIDUAL_OF_ORG: t('usageTracking.individualOfOrg'),
  };
  return map[type];
}

function ctsStatusLabel(status: CtsStatus): string {
  const map: Record<CtsStatus, string> = {
    ACTIVE: t('usageTracking.statusActive'),
    PENDING_ACTIVATE: t('usageTracking.statusPendingActivate'),
    PENDING_APPROVE: t('usageTracking.statusPendingApprove'),
    REVOKED: t('usageTracking.statusRevoked'),
    EXPIRED: t('usageTracking.statusExpired'),
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

function handleSortChange({ prop, order }: { prop: string; order: string | null }) {
  currentSort.value = { prop: prop || '', order };
  page.value = 1;
  load();
}

function resetFilters() {
  filterPurchasedAt.value = null;
  filterCtsType.value = "";
  filterCtsDuration.value = "";
  filterCtsStatus.value = "";
  filterPlan.value = "";
  currentSort.value = { prop: '', order: null };
  page.value = 1;
  load();
}

function handleExport() {
  ElMessage.info(t('usageTracking.developing'));
}

function toIsoDate(d: Date | null): string | undefined {
  if (!d) return undefined;
  return d instanceof Date ? d.toISOString().slice(0, 10) : undefined;
}

watch([filterPurchasedAt, filterCtsType, filterCtsDuration, filterCtsStatus, filterPlan], () => {
  page.value = 1;
  load();
});

async function load() {
  loading.value = true;
  const params: IndividualUsageFilterParams = {
    purchasedAt: toIsoDate(filterPurchasedAt.value),
    ctsType: filterCtsType.value || undefined,
    ctsDuration: filterCtsDuration.value || undefined,
    ctsStatus: filterCtsStatus.value || undefined,
    plan: filterPlan.value || undefined,
    page: page.value - 1,
    size: pageSize.value,
    sortBy: currentSort.value.prop || undefined,
    sortDir: currentSort.value.order === 'ascending' ? 'asc' : 'desc',
  };
  try {
    const res = await getIndividualUsageTracking(params);
    if (res.success && res.data) {
      list.value = res.data.list ?? [];
      stats.value = res.data.stats ?? stats.value;
      lastUpdated.value = res.data.lastUpdated ?? null;
      totalElements.value = res.data.totalElements ?? 0;
    }
  } catch {
    ElMessage.error(t('usageTracking.errorLoad'));
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
  color: var(--el-text-color-primary);
  font-weight: 500;
}
.page-subtitle {
  margin: 4px 0 0;
  color: var(--el-text-color-regular);
  font-weight: 400;
  font-size: 15px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem 1.5rem;
  font-size: 18px;
  font-weight: 500; 
  color: var(--el-text-color-regular);
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

:deep(.el-card) {
  padding: 28px 34px;
}

/*custom-tag */
.custom-cts-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-weight: 500;
  font-size: 15px; 
  white-space: nowrap;
  width: 120px;
  height: 34px;
}

/* 1. Đang hoạt động (Chữ xanh lá, không nền) */
.status-active {
  color: var(--el-color-success); 
  background-color: transparent;
  padding: 6px 0; 
}

/* 2. Chờ kích hoạt (Chữ xanh dương, nền xanh lơ nhạt) */
.status-pending-activate {
  color: var(--el-color-primary); 
  background-color: #1B60CB14; 
}

/* 3. Chờ cấp duyệt (Chữ xanh dương, không nền) */
.status-pending-approve {
  color: var(--el-color-primary); 
  background-color: transparent;
  padding: 6px 0;
}

/* 4. Đã thu hồi (Chữ xám, nền xám nhạt) */
.status-revoked {
  color: var(--el-text-color-regular); 
  background-color: var(--color-light-grey); 
}

/* 5. Hết hạn (Chữ xám, không nền) */
.status-expired {
  color: var(--el-text-color-regular); 
  background-color: transparent;
  padding: 6px 0;
}

/*pagination*/
:deep(.el-select__wrapper) {
  padding: 0.25px 0.75rem!important;
}


.custom-pagination-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
}

.custom-pagination-wrapper :deep(.el-pagination) {
  padding: 0;
}

.custom-nav-btn,
.custom-pagination-wrapper :deep(.el-pagination.is-background .btn-next),
.custom-pagination-wrapper :deep(.el-pagination.is-background .btn-prev),
.custom-pagination-wrapper :deep(.el-pagination.is-background .el-pager li) {
  background-color: #f4f5f7 !important;
  border-radius: 6px !important;        
  min-width: 36px;
  height: 36px;
  border: none;
  font-weight: 500;
  color: #4b5563 !important; 
  margin: 0 4px !important;  
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.custom-nav-btn:not(:disabled):hover,
.custom-pagination-wrapper :deep(.el-pagination.is-background .el-pager li:not(.is-active):hover),
.custom-pagination-wrapper :deep(.el-pagination.is-background .btn-next:hover),
.custom-pagination-wrapper :deep(.el-pagination.is-background .btn-prev:hover) {
  background-color: #e5e7eb !important; 
}

.custom-pagination-wrapper :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--el-color-primary) !important; 
  color: #ffffff !important;            
  font-weight: 600;
}

.custom-nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
