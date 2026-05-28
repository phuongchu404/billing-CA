<template>
  <div>
    <div class="page-header">
      <div>
        <h2>{{ t("individualPlan.title") }}</h2>
        <p class="page-subtitle">{{ t("individualPlan.subtitle") }}</p>
      </div>
    </div>

    <el-card shadow="never">
      <div class="current-plan-info text-regular">
        <div v-if="currentPlan">
          {{ t("individualPlan.currentPlan") }}
          <span class="text-primary">{{ currentPlan.name }}</span>
          {{ t("individualPlan.applyUntil") }}
          <span class="text-primary">{{ currentPlan.applyUntil }}</span>
        </div>
        <div v-if="nextPlan">
          {{ t("individualPlan.nextPlan") }}
          <span class="text-primary">{{ nextPlan.name }}</span>
          {{ t("individualPlan.applyFrom") }}
          <span class="text-primary">{{ nextPlan.applyFrom }}</span>
        </div>
      </div>

      <div class="info-bar">
        <el-button
          type="primary"
          icon="Plus"
          :disabled="!can('plan:create')"
          @click="handleAddNew"
          >{{ t("common.add") }}</el-button
        >
        <span class="last-updated">{{
          t("agency.lastUpdated", { time: lastUpdated })
        }}</span>
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

      <el-table :data="pagedList" v-loading="loading" border @sort-change="handleSort">
        <el-table-column
          width="60"
          type="index"
          :index="(i: number) => (page - 1) * pageSize + i + 1"
          align="center"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link icon="Refresh" @click="resetFilters" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" sortable="custom" min-width="240" :sort-orders="['ascending', 'descending']">
          <template #header>
            <div class="col-label">{{ t("individualPlan.colPlanName") }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable width="170" align="center">
          <template #header>
            <div class="col-label">{{ t("common.status") }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option
                  :label="t('individualPlan.statusAvailable')"
                  value="AVAILABLE"
                />
                <el-option
                  :label="t('individualPlan.statusUnavailable')"
                  value="UNAVAILABLE"
                />
                <el-option
                  :label="t('individualPlan.statusPending')"
                  value="PENDING"
                />
                <el-option
                  :label="t('individualPlan.statusApproved')"
                  value="APPROVED"
                />
                <el-option
                  :label="t('individualPlan.statusApplying')"
                  value="APPLYING"
                />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag
              disable-transitions
              :class="['custom-tag', `tag-${row.status.toLowerCase()}`]"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="applyFrom" sortable width="160" align="center">
          <template #header>
            <div class="col-label">{{ t("agency.colApplyFrom") }}</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterApplyFrom"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyFrom ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="applyUntil" sortable width="160" align="center">
          <template #header>
            <div class="col-label">{{ t("agency.colApplyTo") }}</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterApplyUntil"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyUntil ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="updatedAt" sortable width="210" align="center">
          <template #header>
            <div class="col-label">{{ t("agency.colUpdatedAt") }}</div>
            <div class="col-filter">
              <el-date-picker
                v-model="filterUpdatedAt"
                type="date"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.updatedAt ?? "" }}</template>
        </el-table-column>

        <el-table-column fixed="right" width="240" header-align="center">
          <template #header>
            <div class="col-label">{{ t("common.action") }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button
                size="small"
                icon="InfoFilled" 
                @click.stop="goDetail(row)"
              >
                {{ t("common.detail") }}
              </el-button>

              <el-button
                v-if="row.status === 'AVAILABLE'"
                size="small"
                class="btn-light-blue"
                icon="CopyDocument" 
                :disabled="!can('plan:update')"
                @click.stop="openRequestApply(row)"
              >
                {{ t("agency.btnRequestApply") }}
              </el-button>

              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                class="btn-light-blue"
                icon="Check"
                :disabled="!can('plan:update')"
                @click.stop="openApprove(row)"
              >
                {{ t("agency.btnApprove") }}
              </el-button>

              <el-button
                v-if="row.status === 'APPROVED' || row.status === 'APPLYING'"
                size="small"
                type="primary"
                plain
                :disabled="!can('plan:update')"
                @click.stop="openStopApply(row)"
              >
                {{ t("agency.btnStopApply") }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row" style="margin-top: 12px">
        <span class="page-label">
          {{ t("common.showing") }}
          <el-select
            v-model="pageSize"
            size="small"
            style="width: 5rem; margin: 0 0.5rem;"
            @change="page = 1"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
          </el-select>
          {{ t("individualPlan.totalPlans", { total: filteredList.length }) }}
        </span>
        <div class="custom-pagination-wrapper">
          <button 
            class="custom-nav-btn" 
            :disabled="page === 1"
            @click="page = 1"
          >
            <el-icon><DArrowLeft /></el-icon>
          </button>

          <el-pagination
            v-model:current-page="page"
            :total="filteredList.length"
            :page-size="pageSize"
            layout="prev, pager, next"
            :pager-count="5"
            background
          />

          <button 
            class="custom-nav-btn" 
            :disabled="page === Math.ceil(filteredList.length / pageSize)"
            @click="page = Math.ceil(filteredList.length / pageSize)"
          >
            <el-icon><DArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </el-card>

    <!-- Dialog 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC -->
    <el-dialog v-model="requestApplyVisible" width="720px" height="460px" align-center>
      <template #header>
        <span class="dlg-title">{{ t("agency.dialogRequestApply") }}</span>
      </template>
      <div style="font-size: 17px;color: var(--el-text-color-primary);">
        <p>{{ t("common.name") }}: {{ activeRow?.name }}</p>
        <el-button link type="primary" @click="goDetail(activeRow!)" style="font-size: 17px;text-decoration: underline; font-style: italic;width: 100%;justify-content: end;">
          {{t("individualPlan.viewDetails")}}
        </el-button>
      </div>
      <el-form label-width="auto" style="margin-top: 1rem">
        <el-form-item :label="t('agency.dialogApplyPeriod')">
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            range-separator="-"
            :start-placeholder="t('agency.dateFrom')"
            :end-placeholder="t('agency.dateTo')"
            style="width: 100%"
            :disabled-date="disabledDate" 
          />
        </el-form-item>
      </el-form>
      <p class="dlg-note" v-html="t('individualPlan.requestApplyNote')"></p>
      <template #footer>
        <el-button type="primary" @click="confirmRequestApply">{{
          t("common.confirm")
        }}</el-button>
        <el-button @click="requestApplyVisible = false">{{
          t("common.cancel")
        }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog 2: DUYỆT ÁP DỤNG GÓI CƯỚC -->
    <el-dialog v-model="approveVisible" width="700px" height="560px" align-center>
      <template #header>
        <span class="dlg-title">{{ t("agency.dialogApproveApply") }}</span>
      </template>
      <p class="text-primary">
        {{ t("individualPlan.approveDescPrefix") }}
        <b class="text-color-primary">{{ activeRow?.name }}</b>
        {{ t("individualPlan.approveDescSuffix") }}
      </p>
      <el-form label-width="150px" label-position="left">
        <el-form-item :label="t('agency.dialogApplyPeriod')">
          <el-date-picker
            v-model="approveDateRange"
            type="daterange"
            range-separator="-"
            :start-placeholder="t('agency.dateFrom')"
            :end-placeholder="t('agency.dateTo')"
            style="width: 100%; height: 40px!important;padding: 0 1rem;"
            :disabled-date="disabledDate" 
            format="DD/MM/YYYY"
          />
        </el-form-item>
      </el-form>
      <p class="text-primary">
        {{ t("individualPlan.approveNote") }}
      </p>
      <ul class="text-primary dlg-bullets">
        <li>
          {{ t("individualPlan.approveBullet1") }}
        </li>
        <li>
          {{ t("individualPlan.approveBullet2") }}
        </li>
        <li>{{ t("individualPlan.approveBullet3") }}</li>
      </ul>
      <p class="text-primary" style="color: #2F2B3D99; font-style: italic;">
        {{ t('individualPlan.requestApplyDesc') }}
      </p>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" plain @click="confirmReject">{{
            t("agency.btnReject")
          }}</el-button>
          <div class="dlg-footer-right">
            <el-button type="primary" @click="confirmApprove">{{
              t("common.confirm")
            }}</el-button>
            <el-button @click="approveVisible = false" class="btn-cancel">{{
              t("individualPlan.cancel")
            }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- Dialog 3: DỪNG ÁP DỤNG GÓI CƯỚC -->
    <el-dialog v-model="stopApplyVisible" width="660px" height="240px" align-center>
      <template #header>
        <span class="dlg-title">{{ t("agency.dialogStopApply") }}</span>
      </template>
      <p class="text-primary" style="font-weight: 600;margin: 0;">
        {{ t("individualPlan.stopDescPrefix") }}
        <b class="text-color-primary">{{ activeRow?.name }}</b
        >. {{ t("individualPlan.stopDescSuffix") }}
      </p>
      <template #footer>
        <el-button type="primary" @click="confirmStopApply">{{
          t("common.confirm")
        }}</el-button>
        <el-button @click="stopApplyVisible = false" class="btn-cancel">{{
          t("individualPlan.cancel")
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { usePermission } from "@/composables/usePermission";
import { useI18n } from "vue-i18n";

const { can } = usePermission();
const { t } = useI18n();
import { ElMessage } from "element-plus";
import {
  getIndividualPlanConfigSummary,
  requestApplyPlanConfig,
  approvePlanConfig,
  rejectPlanConfig,
  stopPlanConfig,
  type IndividualPlanConfigFilterParams,
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
const pageSize = ref(10);
const filterStatus = ref("");
const filterApplyFrom = ref<Date | null>(null);
const filterApplyUntil = ref<Date | null>(null);
const filterUpdatedAt = ref<Date | null>(null);

const lastUpdated = ref<string | null>(null);
const currentPlan = ref<{ name: string; applyUntil: string | null } | null>(
  null,
);
const nextPlan = ref<{ name: string; applyFrom: string | null } | null>(null);

// Dialog state
const activeRow = ref<PlanConfigRow | null>(null);
const requestApplyVisible = ref(false);
const requestApplyDateRange = ref<[Date, Date] | null>(null);
const approveVisible = ref(false);
const approveDateRange = ref<[Date, Date] | null>(null);
const stopApplyVisible = ref(false);

const list = ref<PlanConfigRow[]>([]);

const filteredList = computed(() => list.value);

const disabledDate = (time: Date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return time.getTime() < today.getTime();
};

const currentSort = ref({ prop: '', order: null as string | null })
const handleSort = ({ prop, order }: any) => {
  currentSort.value = { prop, order }
}

const pagedList = computed(() => {
  let result = [...filteredList.value]

  if (currentSort.value.prop === 'name' && currentSort.value.order !== null) {
    result.sort((a, b) => {
      const nameA = (a.name || '').toString().trim().toLowerCase()
      const nameB = (b.name || '').toString().trim().toLowerCase()

      const compare = nameA.localeCompare(nameB, 'vi')
      return currentSort.value.order === 'ascending' ? compare : -compare
    })
  }

  const start = (page.value - 1) * pageSize.value
  return result.slice(start, start + pageSize.value)
  //return filteredList.value.slice(start, start + pageSize.value);
});

function statusLabel(status: PlanStatus): string {
  const map: Record<PlanStatus, string> = {
    AVAILABLE: t("individualPlan.statusAvailable"),
    UNAVAILABLE: t("individualPlan.statusUnavailable"),
    PENDING: t("individualPlan.statusPending"),
    APPROVED: t("individualPlan.statusApproved"),
    APPLYING: t("individualPlan.statusApplying"),
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
    ElMessage.warning(t("individualPlan.warningApplyPeriod"));
    return;
  }
  const [from, to] = requestApplyDateRange.value;
  const today = new Date();
  today.setHours(0, 0, 0, 0); 
  
  if (from.getTime() < today.getTime()) {
    ElMessage.error(t("individualPlan.errorPastDate") || "Ngày áp dụng không được nhỏ hơn ngày hiện tại");
    return;
  }
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  try {
    await requestApplyPlanConfig(activeRow.value.id, {
      applyFrom: fmt(from),
      applyUntil: fmt(to),
    });
    ElMessage.success(t("individualPlan.requestApplySuccess"));
    requestApplyVisible.value = false;
    load();
  } catch {
    ElMessage.error(t("individualPlan.requestApplyFailed"));
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
    ElMessage.success(t("individualPlan.approveSuccess"));
    approveVisible.value = false;
    load();
  } catch {
    ElMessage.error(t("individualPlan.approveFailed"));
  }
}

async function confirmReject() {
  if (!activeRow.value) return;
  try {
    await rejectPlanConfig(activeRow.value.id);
    ElMessage.info(t("individualPlan.rejectSuccess"));
    approveVisible.value = false;
    load();
  } catch {
    ElMessage.error(t("individualPlan.rejectFailed"));
  }
}

async function confirmStopApply() {
  if (!activeRow.value) return;
  try {
    await stopPlanConfig(activeRow.value.id);
    ElMessage.success(t("individualPlan.stopSuccess"));
    stopApplyVisible.value = false;
    load();
  } catch {
    ElMessage.error(t("individualPlan.stopFailed"));
  }
}

function toIsoDate(d: Date | null): string | undefined {
  if (!d) return undefined;
  return d instanceof Date ? d.toISOString().slice(0, 10) : undefined;
}

async function load() {
  loading.value = true;
  const params: IndividualPlanConfigFilterParams = {
    status: filterStatus.value || undefined,
    applyFrom: toIsoDate(filterApplyFrom.value),
    applyUntil: toIsoDate(filterApplyUntil.value),
    updatedAt: toIsoDate(filterUpdatedAt.value),
  };
  try {
    const res = await getIndividualPlanConfigSummary(params);
    if (res.success && res.data) {
      list.value = res.data.list ?? [];
      currentPlan.value = res.data.currentPlan ?? null;
      nextPlan.value = res.data.nextPlan ?? null;
      lastUpdated.value = res.data.lastUpdated ?? null;
    }
  } catch {
    ElMessage.error(t("individualPlan.loadListError"));
  } finally {
    loading.value = false;
  }
}

watch([filterStatus, filterApplyFrom, filterApplyUntil, filterUpdatedAt], () => {
  page.value = 1;
  load();
});

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
  font-weight: 500;
  color: var(--el-text-color-primary);
}
.page-subtitle {
  margin: 4px 0 0;
  color: var(--el-text-color-regular);
  font-size: 15px;
}

.current-plan-info {
  margin-bottom: 12px;
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
  font-style: italic;
}

/*pagination*/
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

:deep(.el-select__wrapper) {
  padding: 0.25px 0.75rem!important;
}

.col-label {
  font-weight: 600;
  font-size: 13px;
  white-space: normal;
  line-height: 1.3;
  text-transform: uppercase;
}
.col-filter {
  margin-top: 6px;
  min-height: 28px;
}

:deep(.el-table th.el-table__cell) {
  vertical-align: top;
  padding: 8px 0;
}

/* Dialog styles */

:deep(.el-dialog > .el-form-item--label-right .el-form-item__label) {
  font-size: 17px;
  color: var(--el-text-color-regular);
}

.dlg-title {
  font-weight: 500;
  font-size: 18px;
  color: var(--el-text-color-primary);
  padding: 0;
}

.dlg-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 17px;
  color: var(--el-text-color-regular);
}

.dlg-note {
  font-size: 17px;
  color: var(--el-text-color-regular);
  line-height: 26px;
  margin-top: 1.5rem;
  font-style: italic;
}

.dlg-bullets {
  padding-left: 2rem;
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
