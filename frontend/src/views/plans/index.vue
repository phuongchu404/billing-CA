<template>
  <div>
    <div class="page-header">
      <h2>Khách hàng đại lý</h2>
    </div>

    <el-card shadow="never">
      <div class="info-bar">
        <span
          >SL đại lý đang hoạt động: <b>{{ activeCount }}</b></span
        >
        <el-button
          type="primary"
          :icon="Plus"
          :disabled="!can('group:create')"
          @click="handleAddNew"
          >Thêm Mới</el-button
        >
        <el-button :icon="Download" @click="handleExport"
          >Xuất Dữ Liệu Đối Soát</el-button
        >
        <span class="last-updated">Lần cập nhật cuối: {{ lastUpdated }}</span>
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

      <el-table :data="pagedList" v-loading="loading" border>
        <el-table-column
          width="55"
          type="index"
          :index="(i: number) => (page - 1) * pageSize + i + 1"
        >
          <template #header>
            <div class="col-label">#</div>
            <div class="col-filter">
              <el-button link :icon="Refresh" @click="load" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="groupCode" sortable width="110">
          <template #header>
            <div class="col-label">MÃ ĐẠI LÝ</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="groupName" sortable min-width="180">
          <template #header>
            <div class="col-label">TÊN ĐẠI LÝ</div>
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
              >
                <el-option label="Đang hoạt động" value="ACTIVE" />
                <el-option label="Tạm dừng" value="INACTIVE" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <span
              :class="row.status === 'ACTIVE' ? 'text-active' : 'text-inactive'"
            >
              {{ row.status === "ACTIVE" ? "Đang hoạt động" : "Tạm dừng" }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="ownerName" sortable min-width="150">
          <template #header>
            <div class="col-label">NHÂN VIÊN PHỤ TRÁCH</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <span v-if="row.ownerName">{{ row.ownerName }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="currentPlan" sortable min-width="160">
          <template #header>
            <div class="col-label">GÓI CƯỚC HIỆN TẠI</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.currentPlan ?? "" }}</template>
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
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.applyUntil ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="ctsCreated" sortable width="130">
          <template #header>
            <div class="col-label">SL CTS ĐÃ TẠO</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{ row.ctsCreated != null ? row.ctsCreated.toLocaleString() : "" }}
          </template>
        </el-table-column>

        <el-table-column prop="ctsCreatedPct" sortable width="130">
          <template #header>
            <div class="col-label">% CTS ĐÃ TẠO</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.ctsCreatedPct ?? "" }}</template>
        </el-table-column>

        <el-table-column prop="signingUsed" sortable width="130">
          <template #header>
            <div class="col-label">SL LƯỢT ĐÃ KÝ</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            {{
              row.signingUsed != null ? row.signingUsed.toLocaleString() : ""
            }}
          </template>
        </el-table-column>

        <el-table-column prop="signingUsedPct" sortable width="130">
          <template #header>
            <div class="col-label">% LƯỢT ĐÃ KÝ</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">{{ row.signingUsedPct ?? "" }}</template>
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
              />
            </div>
          </template>
          <template #default="{ row }">{{ row.updatedAt ?? "" }}</template>
        </el-table-column>

        <el-table-column fixed="right" width="330">
          <template #header>
            <div class="col-label">HÀNH ĐỘNG</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" :icon="View" @click.stop="goDetail(row)"
                >Chi tiết</el-button
              >
              <el-button
                size="small"
                :icon="UserFilled"
                :disabled="!canAssignOwner"
                @click.stop="openAssignOwner(row)"
                >Phụ trách</el-button
              >
              <el-button
                size="small"
                :icon="Document"
                @click.stop="handleExportRow(row)"
                >Xuất đối soát</el-button
              >
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
          >GÁN NHÂN VIÊN PHỤ TRÁCH</span
        >
      </template>
      <el-form label-width="130px" label-position="left">
        <el-form-item label="Đại lý:">
          <span>{{ assignOwnerTarget?.groupName }}</span>
        </el-form-item>
        <el-form-item label="Nhân viên:">
          <el-select
            v-model="assignOwnerUserId"
            clearable
            placeholder="Chọn nhân viên"
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
            >Xác Nhận</el-button
          >
          <el-button @click="assignOwnerVisible = false">Hủy</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  Plus,
  Download,
  Refresh,
  View,
  Document,
  UserFilled,
} from "@element-plus/icons-vue";
import { listGroups, assignGroupOwner } from "@/api/groups";
import { listUsers } from "@/api/users";
import { usePermission } from "@/composables/usePermission";
import { ElMessage } from "element-plus";

const { can } = usePermission();
import type { GroupListItem } from "@/types/group";
import type { UserAccount } from "@/types";

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

const activeCount = computed(
  () => list.value.filter((r) => r.status === "ACTIVE").length,
);
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

const filteredList = computed(() => {
  let result = list.value;
  if (filterStatus.value)
    result = result.filter((r) => r.status === filterStatus.value);
  if (filterApplyUntil.value) {
    const d = filterApplyUntil.value;
    const dateStr = `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
    result = result.filter((r) => r.applyUntil?.startsWith(dateStr));
  }
  if (filterUpdatedAt.value) {
    const d = filterUpdatedAt.value;
    const dateStr = `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
    result = result.filter((r) => r.updatedAt?.startsWith(dateStr));
  }
  return result;
});

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

watch([filterStatus, filterApplyUntil, filterUpdatedAt], () => {
  page.value = 1;
});

async function load() {
  loading.value = true;
  try {
    const res = await listGroups();
    if (res.success && res.data) {
      list.value = res.data.map(mapRow);
    } else {
      ElMessage.error(res.message || "Không thể tải danh sách đại lý");
    }
    lastUpdated.value = formatDatetime(new Date().toISOString());
  } catch (e) {
    ElMessage.error("Lỗi kết nối server");
  } finally {
    loading.value = false;
  }
}

function handleAddNew() {
  router.push("/plans/new");
}

function handleExport() {
  // TODO: implement bulk export
}

function goDetail(row: AgencyRow) {
  router.push("/plans/" + row.groupId);
}

function handleExportRow(_row: AgencyRow) {
  // TODO: implement per-row export
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
      ElMessage.success("Đã cập nhật nhân viên phụ trách");
      assignOwnerVisible.value = false;
      await load();
    }
  } finally {
    savingOwner.value = false;
  }
}

onMounted(load);
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
}

.info-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
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

.text-active {
  color: #1b60cb;
  font-weight: 500;
}
.text-inactive {
  color: #ff9f43;
  font-weight: 500;
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
</style>
