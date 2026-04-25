<template>
  <div>
    <!-- Page header -->
    <div class="page-header">
      <h2 class="page-title">{{ t("menu.logsManagement") }}</h2>
    </div>

    <el-card shadow="never">
      <!-- Top bar -->
      <div class="top-bar">
        <el-button :icon="Upload" type="primary" @click="exportData"
          >Xuất Dữ Liệu</el-button
        >
        <span class="last-updated">Lần cập nhật cuối: {{ lastUpdated }}</span>
      </div>

      <!-- Pagination top -->
      <!-- <div class="pagination-bar">
        <div class="pagination-info">
          Hiển thị
          <el-select v-model="size" style="width:68px" size="small" @change="onSizeChange">
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ total }} logs
        </div>
        <div class="pagination-btns">
          <button class="pg-btn" :disabled="page === 1" @click="goPage(1)">«</button>
          <button class="pg-btn" :disabled="page === 1" @click="goPage(page - 1)">‹</button>
          <button
            v-for="p in pageRange"
            :key="p"
            class="pg-btn"
            :class="{ active: p === page }"
            @click="goPage(p)"
          >{{ p }}</button>
          <button class="pg-btn" :disabled="page === totalPages" @click="goPage(page + 1)">›</button>
          <button class="pg-btn" :disabled="page === totalPages" @click="goPage(totalPages)">»</button>
        </div>
      </div> -->

      <!-- Table -->
      <div class="table-wrap">
        <table class="logs-table">
          <thead>
            <tr class="header-row">
              <th class="th-num">#</th>
              <th class="th-sort" @click="setSort('action')">
                THAO TÁC
                <SortIndicator
                  field="action"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('message')">
                THÔNG ĐIỆP
                <SortIndicator
                  field="message"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('actor')">
                TÀI KHOẢN
                <SortIndicator
                  field="actor"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-sort" @click="setSort('createdAt')">
                THỜI GIAN
                <SortIndicator
                  field="createdAt"
                  :current="sortField"
                  :dir="sortDir"
                />
              </th>
              <th class="th-actions">HÀNH ĐỘNG</th>
            </tr>
            <!-- Filter row -->
            <tr class="filter-row">
              <td class="filter-refresh">
                <el-button
                  :icon="RefreshRight"
                  circle
                  size="small"
                  @click="resetFilters"
                />
              </td>
              <td />
              <td />
              <td />
              <td />
              <td />
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" style="text-align:center;padding:24px;color:#909399">Đang tải...</td>
            </tr>
            <tr v-else-if="pagedLogs.length === 0">
              <td colspan="6" style="text-align:center;padding:24px;color:#909399">Không có dữ liệu</td>
            </tr>
            <tr v-for="(log, i) in pagedLogs" :key="log.id" class="data-row">
              <td class="td-num">{{ (page - 1) * size + i + 1 }}</td>
              <td>{{ log.action }}</td>
              <td>{{ log.details }}</td>
              <td>{{ log.actor }}</td>
              <td>{{ formatDate(log.createdAt) }}</td>
              <td class="td-actions">
                <el-button
                  size="small"
                  :icon="InfoFilled"
                  plain
                  @click="openDetail(log)"
                  >Chi tiết</el-button
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination bottom -->
      <div class="pagination-bar">
        <div class="pagination-info">
          Hiển thị
          <el-select
            v-model="size"
            style="width: 68px"
            size="small"
            @change="onSizeChange"
          >
            <el-option :value="10" label="10" />
            <el-option :value="20" label="20" />
            <el-option :value="50" label="50" />
          </el-select>
          trong tổng số {{ total }} logs
        </div>
        <div class="pagination-btns">
          <button class="pg-btn" :disabled="page === 1" @click="goPage(1)">
            «
          </button>
          <button
            class="pg-btn"
            :disabled="page === 1"
            @click="goPage(page - 1)"
          >
            ‹
          </button>
          <button
            v-for="p in pageRange"
            :key="p"
            class="pg-btn"
            :class="{ active: p === page }"
            @click="goPage(p)"
          >
            {{ p }}
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(page + 1)"
          >
            ›
          </button>
          <button
            class="pg-btn"
            :disabled="page === totalPages"
            @click="goPage(totalPages)"
          >
            »
          </button>
        </div>
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      width="520px"
      :close-on-click-modal="false"
    >
      <template #header>
        <span class="dlg-title">CHI TIẾT LOGS</span>
      </template>
      <div class="dlg-meta">
        <div class="dlg-meta-row">
          <span><strong>Thao tác:</strong> {{ detailLog?.action }}</span>
          <span><strong>Đối tượng:</strong> {{ detailLog?.entityType }}</span>
        </div>
        <div class="dlg-meta-row">
          <span><strong>ID đối tượng:</strong> {{ detailLog?.entityId }}</span>
        </div>
        <div class="dlg-meta-row">
          <span><strong>Thời gian:</strong> {{ formatDate(detailLog?.createdAt ?? '') }}</span>
          <span><strong>Tài khoản:</strong> {{ detailLog?.actor }}</span>
        </div>
        <div class="dlg-meta-row" v-if="detailLog?.details">
          <span><strong>Chi tiết:</strong> {{ detailLog?.details }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">Đóng</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineComponent, h } from "vue";
import {
  Upload,
  RefreshRight,
  InfoFilled,
  DCaret,
  CaretTop,
  CaretBottom,
} from "@element-plus/icons-vue";
import { ElIcon, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { listAuditLogs, type AdminAuditLogEntry } from "@/api/auditLogs";

const { t } = useI18n();

// ── Sort indicator ──
const SortIndicator = defineComponent({
  props: { field: String, current: String, dir: String },
  setup(props) {
    return () => {
      if (props.field !== props.current)
        return h(ElIcon, { class: "sort-icon" }, () => h(DCaret));
      return h(ElIcon, { class: "sort-icon active" }, () =>
        props.dir === "asc" ? h(CaretTop) : h(CaretBottom),
      );
    };
  },
});

// ── State ──
const logs = ref<AdminAuditLogEntry[]>([]);
const loading = ref(false);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const totalPages = ref(1);
const sortField = ref("");
const sortDir = ref<"asc" | "desc">("asc");
const lastUpdated = ref("");

// ── Fetch ──
async function fetchLogs() {
  loading.value = true;
  try {
    const res = await listAuditLogs({ page: page.value - 1, size: size.value });
    if (res?.data) {
      logs.value = res.data.content ?? [];
      total.value = res.data.totalElements ?? 0;
      totalPages.value = Math.max(1, res.data.totalPages ?? 1);
    }
  } catch {
    ElMessage.error("Không thể tải dữ liệu audit logs");
  } finally {
    loading.value = false;
    const now = new Date();
    lastUpdated.value =
      now.toLocaleDateString("vi-VN") + " " + now.toLocaleTimeString("vi-VN");
  }
}

// ── Computed ──
const pagedLogs = computed(() => {
  const list = [...logs.value];
  if (sortField.value) {
    list.sort((a, b) => {
      const va = String((a as any)[sortField.value] ?? "");
      const vb = String((b as any)[sortField.value] ?? "");
      return sortDir.value === "asc"
        ? va.localeCompare(vb)
        : vb.localeCompare(va);
    });
  }
  return list;
});

const pageRange = computed(() => {
  const tp = totalPages.value,
    cur = page.value,
    maxShow = 5;
  let start = Math.max(1, cur - Math.floor(maxShow / 2));
  let end = start + maxShow - 1;
  if (end > tp) {
    end = tp;
    start = Math.max(1, end - maxShow + 1);
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

function setSort(field: string) {
  if (sortField.value === field)
    sortDir.value = sortDir.value === "asc" ? "desc" : "asc";
  else {
    sortField.value = field;
    sortDir.value = "asc";
  }
}
async function goPage(p: number) {
  page.value = Math.max(1, Math.min(p, totalPages.value));
  await fetchLogs();
}
async function onSizeChange() {
  page.value = 1;
  await fetchLogs();
}
async function resetFilters() {
  sortField.value = "";
  page.value = 1;
  await fetchLogs();
}
function exportData() {
  /* TODO: implement export */
}

function formatDate(val: string) {
  if (!val) return "";
  const d = new Date(val);
  if (isNaN(d.getTime())) return val;
  return d.toLocaleDateString("vi-VN") + " " + d.toLocaleTimeString("vi-VN");
}

// ── Detail dialog ──
const detailVisible = ref(false);
const detailLog = ref<AdminAuditLogEntry | null>(null);

function openDetail(log: AdminAuditLogEntry) {
  detailLog.value = log;
  detailVisible.value = true;
}

onMounted(fetchLogs);
</script>

<style scoped>
.page-header {
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}

/* Top bar */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.last-updated {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-style: italic;
}

/* Pagination bar */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0;
  font-size: 13px;
  color: #606266;
}
.pagination-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pagination-btns {
  display: flex;
  gap: 4px;
}
.pg-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) {
  border-color: #1b60cb;
  color: #1b60cb;
}
.pg-btn.active {
  background: #1b60cb;
  border-color: #1b60cb;
  color: #fff;
  font-weight: 600;
}
.pg-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Table */
.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}
.logs-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.header-row th {
  background: #fff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 10px 14px;
  font-weight: 600;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  text-align: left;
}
.th-num {
  width: 48px;
  text-align: center;
}
.th-actions {
  width: 110px;
  text-align: center;
}
.th-sort {
  cursor: pointer;
  user-select: none;
}
.th-sort:hover {
  color: #1b60cb;
}

:deep(.sort-icon) {
  font-size: 12px;
  vertical-align: middle;
  margin-left: 2px;
  color: #c0c4cc;
}
:deep(.sort-icon.active) {
  color: #1b60cb;
}

/* Filter row */
.filter-row {
  background: #fafafa;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.filter-row td {
  padding: 6px 8px;
}
.filter-refresh {
  text-align: center;
}

/* Data rows */
.data-row td {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 9px 14px;
  color: #303133;
  font-size: 14px;
}
.data-row:hover td {
  background: #f5f7fa;
}
.td-num {
  text-align: center;
  color: #909399;
}
.td-actions {
  text-align: center;
}

/* Dialog */
.dlg-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
}

.dlg-meta {
  margin-bottom: 14px;
  font-size: 14px;
  color: #303133;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.dlg-meta-row {
  display: flex;
  gap: 32px;
}
.dlg-meta-row span {
  white-space: nowrap;
}

</style>
