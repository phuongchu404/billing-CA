<template>
  <div>
    <div class="page-header">
      <h2>{{ $t('agency.title') }}</h2>
    </div>

    <el-card shadow="never">
      <div class="info-bar">
        <div>
          <p class="text-regular">{{ $t('agency.activeAgencyCount', { count: '' }) }} {{ activeCount }}</p>
          <p class="text-regular text-italic">{{ $t('agency.subTitle') }}</p>
        </div>
        <el-row>
          <el-button
            type="primary"
            icon="Plus"
            :disabled="!can('group:create')"
            @click="handleAddNew"
            class="btn-primary"
            style="min-width: 130px;"
            >{{ $t('agency.addNewAgency') }}</el-button
          >
          <el-button icon="Download" :loading="exportingAll" @click="handleExport" class="btn-primary"
            >{{ $t('agency.exportReconciliation') }}</el-button
          >
        </el-row>
        <span class="last-updated text-italic">{{ $t('agency.lastUpdated', { time: lastUpdated }) }}</span>
      </div>

      <!-- <div class="pagination-row">
        <span class="page-label">
          Hiển thị
          <el-select v-model="pageSize" size="small" style="width:64px;margin:0 4px" @change="page = 1">
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ filteredList.length }} đại lý
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="filteredList.length"
          :page-size="pageSize"
          layout="prev, pager, next"
          :pager-count="5"
          background
        />
      </div> -->

      <el-table :data="pagedList" v-loading="loading" border @sort-change="handleSortChange">
        <el-table-column
          width="60"
          type="index"
          :index="(i: number) => (page - 1) * pageSize + i + 1"
          align="center"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="load" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="groupCode" sortable="custom" width="140">
          <template #header>
            <div class="col-label">{{ $t('agency.colCode') }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="groupName" sortable="custom" min-width="200">
          <template #header>
            <div class="col-label">{{ $t('agency.colName') }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable="custom" width="170" align="center">
          <template #header>
            <div class="col-label">{{ $t('agency.colStatus') }}</div>
            <div class="col-filter">
              <el-select
                v-model="filterStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option :label="$t('agency.statusActive')" value="ACTIVE" />
                <el-option :label="$t('agency.statusInactive')" value="INACTIVE" />
              </el-select>
            </div>
          </template>
          
          <template #default="{ row }">
            <span
              :class="['custom-status-tag', row.status === 'ACTIVE' ? 'status-active' : 'status-inactive']"
            >
              {{ row.status === "ACTIVE" ? $t("agency.statusActive") : $t("agency.statusInactive") }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="ownerName" sortable="custom" min-width="200">
          <template #header>
            <div class="col-label">{{ $t('agency.colOwner') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <span v-if="row.ownerName">{{ row.ownerName }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="currentPlan" sortable="custom" min-width="200">
          <template #header>
            <div class="col-label">{{ $t('agency.colCurrentPlan') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.currentPlan ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="applyUntil" sortable="custom" width="160">
          <template #header>
            <div class="col-label">{{ $t('agency.colApplyUntil') }}</div>
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

        <el-table-column prop="ctsCreated" sortable width="160">
          <template #header>
            <div class="col-label">{{ $t('agency.colCtsCreated') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{ row.ctsCreated != null ? row.ctsCreated.toLocaleString() : "" }}
          </template>
        </el-table-column>

        <el-table-column prop="ctsCreatedPct" sortable width="160">
          <template #header>
            <div class="col-label">{{ $t('agency.colCtsCreatedPct') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.ctsCreatedPct ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="signingUsed" sortable width="160">
          <template #header>
            <div class="col-label">{{ $t('agency.colSigningUsed') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{
              row.signingUsed != null ? row.signingUsed.toLocaleString() : ""
            }}
          </template>
        </el-table-column>

        <el-table-column prop="signingUsedPct" sortable width="160">
          <template #header>
            <div class="col-label">{{ $t('agency.colSigningUsedPct') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.signingUsedPct ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="updatedAt" sortable width="210">
          <template #header>
            <div class="col-label">{{ $t('agency.colUpdatedAt') }}</div>
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

        <el-table-column fixed="right" width="350" header-align="center">
          <template #header>
            <div class="col-label">{{ $t('agency.colActions') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" icon="InfoFilled" @click.stop="goDetail(row)">
                {{ $t('agency.btnDetail') }}</el-button>
              <el-button size="small" :icon="UserFilled" :disabled="!canAssignOwner" @click.stop="openAssignOwner(row)">
                {{ $t('agency.btnAssignOwner') }}
              </el-button>
              <el-button size="small" icon="Document" :loading="exportingRowId === row.groupId" @click.stop="handleExportRow(row)">
                {{ $t('agency.btnExportRow') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row" style="margin-top: 12px">
        <span class="page-label">
          {{ $t('agency.showOf', { size: '', total: totalElements }).split('  ')[0] }}
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
          {{ $t('agency.showOf', { size: '', total: totalElements }).split('  ')[1] || '' }}
        </span>
        <el-pagination
          v-model:current-page="page"
          :total="totalElements"
          :page-size="pageSize"
          layout="prev, pager, next"
          :pager-count="5"
          background
        />
      </div>
    </el-card>

    <!-- Assign Owner Dialog -->
    <el-dialog
      v-model="assignOwnerVisible"
      width="420px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span style="font-size: 15px; font-weight: 700"
          >{{ $t('agency.dialogAssignOwnerTitle') }}</span
        >
      </template>
      <el-form label-width="130px" label-position="left">
        <el-form-item :label="$t('agency.dialogAgencyLabel')">
          <span>{{ assignOwnerTarget?.groupName }}</span>
        </el-form-item>
        <el-form-item :label="$t('agency.dialogStaffLabel')">
          <el-select
            v-model="assignOwnerUserId"
            clearable
            :placeholder="$t('agency.dialogStaffPlaceholder')"
            style="width: 100%"
          >
            <el-option
              v-for="u in allStaffUsers"
              :key="u.userId"
              :label="`${u.fullName} (${u.username})`"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <el-button
            type="primary"
            :loading="savingOwner"
            @click="handleAssignOwner"
            >{{ $t('common.confirm') }}</el-button
          >
          <el-button @click="assignOwnerVisible = false">{{ $t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import {
  Plus,
  Download,
  Refresh,
  View,
  Document,
  UserFilled,
} from "@element-plus/icons-vue";
import { listGroups, assignGroupOwner, downloadSettlementExport, triggerBlobDownload, type GroupListFilterParams } from "@/api/groups";
import { listUsers } from "@/api/users";
import { usePermission } from "@/composables/usePermission";
import { ElMessage } from "element-plus";

const { can } = usePermission();
const { t } = useI18n();
import type { GroupListItem } from "@/types/group";
import type { UserAccount } from "@/types";

const route = useRoute();
const router = useRouter();

// AgencyRow khớp với GroupListItem từ backend
type AgencyRow = GroupListItem;

const list = ref<AgencyRow[]>([]);
const loading = ref(false);
const lastUpdated = ref("");
const filterStatus = ref("");
const filterApplyUntil = ref<Date | null>(null);
const filterUpdatedAt = ref<Date | null>(null);
const page = ref(1);
const pageSize = ref(10);
const sortBy = ref("");
const sortDir = ref("desc");
const totalElements = ref(0);

const activeCount = ref(0);
const canAssignOwner = computed(() => can("group:assign:owner"));

function formatDate(iso: string | null): string {
  if (!iso) return "";
  // Nếu là LocalDate (YYYY-MM-DD) hay ISO datetime
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

function mapRow(item: GroupListItem): AgencyRow {
  return {
    ...item,
    // applyUntil đến từ backend là "YYYY-MM-DD", hiển thị dạng "DD/MM/YYYY"
    applyUntil: item.applyUntil ? formatDate(item.applyUntil) : null,
    updatedAt: item.updatedAt ? formatDatetime(item.updatedAt) : null,
  };
}

const pagedList = computed(() => list.value);

function toIsoDate(d: Date | null): string | undefined {
  if (!d) return undefined;
  return d instanceof Date ? d.toISOString().slice(0, 10) : undefined;
}

watch([filterStatus, filterApplyUntil, filterUpdatedAt], () => {
  page.value = 1;
  load();
});

watch(page, (newPage) => {
  router.push({ query: { ...route.query, page: newPage } });
  load();
});

function handleSortChange({ prop, order }: { prop: string; order: string | null }) {
  sortBy.value = order ? prop : '';
  sortDir.value = order === 'ascending' ? 'asc' : 'desc';
  page.value = 1;
  load();
}

async function load() {
  loading.value = true;
  const params: GroupListFilterParams = {
    status: filterStatus.value || undefined,
    applyUntil: toIsoDate(filterApplyUntil.value),
    updatedAt: toIsoDate(filterUpdatedAt.value),
    page: page.value - 1,
    size: pageSize.value,
    sortBy: sortBy.value || undefined,
    sortDir: sortDir.value,
  };
  try {
    const res = await listGroups(params);
    if (res.success && res.data) {
      list.value = res.data.list.map(mapRow);
      activeCount.value = res.data.activeCount;
      totalElements.value = res.data.totalElements;
    } else {
      ElMessage.error(res.message || t('agency.errorNotFound'));
    }
    lastUpdated.value = formatDatetime(new Date().toISOString());
  } catch (e) {
    ElMessage.error(t('agency.errorServer'));
  } finally {
    loading.value = false;
  }
}

function handleAddNew() {
  router.push("/plans/new");
}

const exportingAll = ref(false);
const exportingRowId = ref<number | null>(null);

async function handleExport() {
  exportingAll.value = true;
  try {
    const blob = await downloadSettlementExport({});
    triggerBlobDownload(blob, `doi-soat-tat-ca.xlsx`);
  } catch {
    ElMessage.error(t('agency.errorExport'));
  } finally {
    exportingAll.value = false;
  }
}

function goDetail(row: AgencyRow) {
  router.push("/plans/" + row.groupId);
}

async function handleExportRow(row: AgencyRow) {
  exportingRowId.value = row.groupId;
  try {
    const blob = await downloadSettlementExport({ groupId: row.groupId });
    triggerBlobDownload(blob, `doi-soat-${row.groupCode}.xlsx`);
  } catch {
    ElMessage.error(t('agency.errorExport'));
  } finally {
    exportingRowId.value = null;
  }
}

// ── Assign owner dialog ──
const assignOwnerVisible = ref(false);
const assignOwnerTarget = ref<AgencyRow | null>(null);
const assignOwnerUserId = ref<number | null>(null);
const allStaffUsers = ref<UserAccount[]>([]);
const savingOwner = ref(false);

async function openAssignOwner(row: AgencyRow) {
  if (!canAssignOwner.value) return;
  assignOwnerTarget.value = row;
  assignOwnerUserId.value = row.ownerUserId ?? null;
  assignOwnerVisible.value = true;
  if (!allStaffUsers.value.length) {
    const res = await listUsers({ page: 0, size: 200 });
    if (res.success && res.data) allStaffUsers.value = res.data.content;
  }
}

async function handleAssignOwner() {
  if (!assignOwnerTarget.value) return;
  savingOwner.value = true;
  try {
    const res = await assignGroupOwner(
      assignOwnerTarget.value.groupId,
      assignOwnerUserId.value || null,
    );
    if (res.success) {
      ElMessage.success(t('agency.successAssignOwner'));
      assignOwnerVisible.value = false;
      await load();
    }
  } finally {
    savingOwner.value = false;
  }
}

onMounted(() => {
  page.value = Number(route.query.page) || 1
  load();
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-weight: 500;
  font-size: 1.5rem;
}

.info-bar {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.last-updated {
  margin-left: auto;
  font-size: 15px;
  color: #2F2B3D99;
  margin-bottom: 1rem;
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

.text-active {
  color: #1b60cb;
  font-weight: 500;
}
.text-inactive {
  color: #ff9f43;
  font-weight: 500;
}

/*custom-tag*/
.custom-status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
}

.status-active {
  color: var(--el-color-primary); 
  background-color: var(--color-light-grey); 
}

.status-inactive {
  color: var(--el-color-warning); 
  background-color: transparent;
  padding: 6px 0; 
}

.action-btns {
  gap: 0.5rem!important;
}
.action-btns :deep(.el-button) {
  margin: 0;
}

:deep(.el-table th.el-table__cell) {
  vertical-align: top;
  padding: 8px 0;
}
</style>
