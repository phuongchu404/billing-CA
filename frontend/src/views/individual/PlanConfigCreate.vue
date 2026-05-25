<template>
  <div class="plan-config-create">
    <div class="page-header">
      <div>
        <h2>{{ t("individualPlan.createTitle") }}</h2>
        <p class="page-subtitle">{{ t("individualPlan.subtitle") }}</p>
      </div>
    </div>

    <el-button
      icon="CopyDocument"
      @click="templateDialogVisible = true"
      class="btn-select"
    >
      {{ t("agency.chooseTemplate") }}
    </el-button>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <div class="section-card">
      <div class="section-title">{{ t("agency.planSection") }}</div>
      <el-form
        :model="form"
        label-width="280px"
        label-position="left"
        class="section-form"
      >
        <el-form-item required>
          <template #label>
            <span>{{ t("agency.planNameLabel") }}</span>
          </template>
          <div style="width: 100%">
            <el-input
              v-model="form.name"
              :placeholder="t('agency.planNamePlaceholder')"
            />
            <div class="field-hint">{{ t("agency.planNameHint") }}</div>
          </div>
        </el-form-item>
        <el-form-item :label="t('agency.applyDateLabel')">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="-"
            :start-placeholder="t('agency.dateFrom')"
            :end-placeholder="t('agency.dateTo')"
            style="width: 100%"
            format="DD/MM/YYYY"
          />
        </el-form-item>
      </el-form>
      <div class="note-text">
        <p v-html="t('individualPlan.applyDatePendingNote')"></p>
      </div>
    </div>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <div class="section-card">
      <div class="section-title">{{ t("agency.configSection") }}</div>

      <div class="category-tabs">
        <span class="tab-prefix-label">{{ t("individualPlan.category") }}</span>
        <div class="cat-tab-group">
          <button
            v-for="tab in TABS"
            :key="tab.key"
            :class="[
              'cat-tab',
              {
                'is-active': activeTab === tab.key,
                'is-done': isTabCompleted(tab.key),
              },
            ]"
            @click="activeTab = tab.key"
            type="button"
          >
            <el-icon v-if="isTabCompleted(tab.key)" class="tab-done-icon"
              ><CircleCheck
            /></el-icon>
            {{ tab.label }}
          </button>
        </div>
      </div>

      <!-- Hiển thị card và mô tả của từng loại đối tượng (ô đỏ trong thiết kế) -->
      <!-- <div class="subject-display-config">
        <div class="subject-display-title">Hiển thị trên trang công khai</div>
        <div class="subject-display-body"> -->

      <!-- Cột trái: ảnh icon -->
      <!-- <div class="icon-col">
            <div class="field-label">Ảnh đại diện</div>
            <el-upload
              :show-file-list="false"
              :before-upload="(file: File) => handleIconUpload(file, activeTab)"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="icon-uploader"
            >
              <div class="icon-box">
                <img
                  v-if="currentSubjectConfig.iconPreviewUrl"
                  :src="currentSubjectConfig.iconPreviewUrl"
                  class="icon-img"
                  alt="Icon"
                />
                <div v-else class="icon-empty">
                  <el-icon :size="28" color="#c0c4cc"><Picture /></el-icon>
                  <span>Chọn ảnh</span>
                </div>
                <div v-if="currentSubjectConfig.uploading" class="icon-loading">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
              </div>
            </el-upload>
            <div v-if="currentSubjectConfig.iconPreviewUrl">
              <el-upload
                :show-file-list="false"
                :before-upload="(file: File) => handleIconUpload(file, activeTab)"
                accept="image/jpeg,image/png,image/webp,image/gif"
              >
                <el-button link type="primary" size="small" style="padding: 0; margin-top: 4px">Thay ảnh</el-button>
              </el-upload>
            </div>
            <div class="field-hint" style="margin-top: 4px">JPEG · PNG · WEBP · GIF</div>
          </div> -->

      <!-- Cột phải: tính năng + ghi chú giá tự tính -->
      <!-- <div class="meta-col">
            <div class="meta-row" style="flex: 1">
              <div class="field-label">Danh sách tính năng</div>
              <el-input
                type="textarea"
                v-model="currentSubjectConfig.featuresText"
                :rows="6"
                placeholder="Mỗi dòng là một tính năng. Ví dụ:&#10;Ký điện tử không giới hạn&#10;Hỗ trợ chứng thư số&#10;Hỗ trợ 24/7"
                style="width: 100%"
              />
              <div class="field-hint">Mỗi dòng một tính năng — hiển thị dạng danh sách trên trang công khai.</div>
            </div>
            <div class="price-auto-note">
              <el-icon><InfoFilled /></el-icon>
              Giá hiển thị được tính tự động từ mức phí thấp nhất trong bảng cấu hình bên dưới.
            </div>
          </div> -->

      <!-- </div>
      </div> -->

      <el-table
        :data="currentRows"
        border
        style="margin-top: 12px"
        table-layout="fixed"
      >
        <el-table-column width="60" align="center">
          <template #default="{ $index }">
            <div class="action-cell">
              <el-icon class="drag-handle"><Operation /></el-icon>
              <el-icon class="delete-icon" @click="removeRow($index)"
                ><CircleClose
              /></el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('individualPlan.colSubjectType')"
          min-width="130"
          sortable
        >
          <template #default>{{ subjectLabel(activeTab) }}</template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colCtsDuration')"
          width="160"
          align="center"
        >
          <template #header>
            <span>{{ t("individualPlan.durationHeader1") }}</span
            ><br /><span>{{ t("individualPlan.durationHeader2") }}</span>
          </template>
          <template #default="{ row }">
            <div class="inline-cell">
              <el-input-number
                v-model="row.durationMonths"
                :min="1"
                :max="48"
                :controls="false"
                :placeholder="t('individualPlan.numberPlaceholder')"
                size="small"
                style="width: 68px"
              />
              <span class="unit-text">{{ t("agency.monthUnit") }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colCondition')"
          width="150"
          align="center"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.condition"
              size="small"
              :placeholder="t('individualPlan.conditionPlaceholder')"
              style="width: 100%"
            >
              <el-option
                :label="t('agency.conditionSigning')"
                value="SIGNING_COUNT"
              />
              <el-option
                :label="t('agency.conditionCertificate')"
                value="CERTIFICATE_COUNT"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colMinValue')"
          width="160"
          align="center"
        >
          <template #header>
            <span>{{ t("individualPlan.minHeader") }}</span
            ><br /><span>{{ t("individualPlan.conditionHeader") }}</span>
          </template>
          <template #default="{ row }">
            <el-input-number
              v-model="row.minValue"
              :min="1"
              :controls="false"
              :placeholder="t('individualPlan.numberPlaceholder')"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colMaxValue')"
          width="180"
          align="center"
        >
          <template #header>
            <span>{{ t("individualPlan.maxHeader") }}</span
            ><br /><span>{{ t("individualPlan.conditionHeader") }}</span>
          </template>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :controls="false"
              :placeholder="t('agency.maxValuePlaceholder')"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colFeePerCondition')"
          width="140"
          align="right"
        >
          <template #default="{ row }">
            <div class="inline-cell fee-cell">
              <el-input-number
                v-model="row.fee"
                :min="0"
                :controls="false"
                :placeholder="t('individualPlan.numberPlaceholder')"
                size="small"
                style="width: 80px"
              />
              <span class="unit-text">{{ t("agency.vnd") }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          :label="t('agency.colTotalPrice')"
          width="160"
          align="right"
        >
          <template #default="{ row }">
            <div class="inline-cell fee-cell">
              <el-input-number
                v-model="row.totalFee"
                :min="0"
                :controls="false"
                :placeholder="t('agency.totalPricePlaceholder')"
                size="small"
                style="width: 90px"
              />
              <span class="unit-text">{{ t("agency.vnd") }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-button link type="primary" :icon="CirclePlus" @click="addRow">{{
          t("common.add")
        }}</el-button>
        <el-button link type="primary" @click="guideVisible = !guideVisible">
          {{ t("agency.guideToggle") }}
          <el-icon style="margin-left: 4px"
            ><component :is="guideVisible ? ArrowUp : ArrowDown"
          /></el-icon>
        </el-button>
      </div>

      <div v-if="guideVisible" class="guide-text">
        <ol>
          <li>{{ t("individualPlan.guideSubject") }}</li>
          <li>
            <b>{{ t("agency.guideIntro") }}</b>
            <ul>
              <li>
                <b>{{ t("agency.guideDurationLabel") }}</b>
                {{ t("agency.guideDurationText") }}
              </li>
              <li>
                <b>{{ t("agency.guideConditionLabel") }}</b>
                {{ t("agency.guideConditionText") }}
              </li>
              <li>
                <b>{{ t("agency.guideMinLabel") }}</b>
                {{ t("agency.guideMinText") }}
                <b>{{ t("agency.guideMaxLabel") }}</b>
                {{ t("agency.guideMaxText") }}
                {{ t("individualPlan.guideRangeExample") }}
              </li>
              <li>
                <b>{{ t("agency.guideFeeLabel") }}</b>
                {{ t("agency.guideFeeText") }}
              </li>
            </ul>
          </li>
          <li>{{ t("individualPlan.guideTableAction") }}</li>
        </ol>
      </div>
    </div>

    <!-- Bottom action bar -->
    <div class="action-bar">
      <el-button type="primary" :loading="submitting" @click="handleSubmit">{{
        t("common.confirm")
      }}</el-button>
      <el-button @click="handleCancel">{{ t("common.cancel") }}</el-button>
    </div>

    <!-- Dialog: Chọn Gói Cước Mẫu -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="t('agency.chooseTemplateTitle')"
      width="660px"
      height="280px"
      align-center
    >
      <p class="dialog-desc">
        {{ t("agency.chooseTemplateDesc") }}
      </p>
      <el-form label-width="90px" label-position="left">
        <el-form-item :label="t('agency.dialogPlanName')">
          <el-select
            v-model="selectedTemplate"
            :placeholder="t('individualPlan.chooseTemplatePlaceholder')"
            style="width: 100%; height: 3rem"
          >
            <el-option
              v-for="p in templatePlans"
              :key="p.id"
              :label="p.name"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="applyTemplate">{{
          t("common.confirm")
        }}</el-button>
        <el-button @click="templateDialogVisible = false" class="btn-cancel">{{
          t("individualPlan.cancel")
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CopyDocument,
  CircleClose,
  CirclePlus,
  ArrowUp,
  ArrowDown,
  Operation,
  CircleCheck,
  Picture,
  Loading,
  InfoFilled,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import {
  getIndividualPlanConfigSummary,
  getIndividualPlanConfigDetail,
  createIndividualPlanConfig,
  uploadPlanIcon,
} from "@/api/individual";
import type { IndividualPlanConfigListItem } from "@/types/individual";

type TabKey = "INDIVIDUAL" | "ORGANIZATION" | "INDIVIDUAL_OF_ORG";

interface ConfigRow {
  id: number;
  durationMonths: number | undefined;
  condition: string;
  minValue: number | undefined;
  maxValue: number | undefined;
  fee: number | undefined;
  totalFee: number | undefined;
}

interface SubjectDisplayConfig {
  iconStoragePath: string | null;
  iconPreviewUrl: string | null;
  featuresText: string;
  uploading: boolean;
}

const router = useRouter();
const { t } = useI18n();

const form = reactive({
  name: "",
  dateRange: null as [Date, Date] | null,
});

const TABS: { key: TabKey; label: string }[] = [
  { key: "INDIVIDUAL", label: t("approvals.subjectIndividual") },
  { key: "ORGANIZATION", label: t("approvals.subjectOrganization") },
  { key: "INDIVIDUAL_OF_ORG", label: t("approvals.subjectIndividualOfOrg") },
];

const activeTab = ref<TabKey>("INDIVIDUAL");
const guideVisible = ref(true);
const templateDialogVisible = ref(false);
const selectedTemplate = ref<number | null>(null);
const templatePlans = ref<IndividualPlanConfigListItem[]>([]);
const submitting = ref(false);

let rowIdSeq = 10;

const tabData = reactive<Record<TabKey, ConfigRow[]>>({
  INDIVIDUAL: [],
  ORGANIZATION: [],
  INDIVIDUAL_OF_ORG: [],
});

const subjectDisplayData = reactive<Record<TabKey, SubjectDisplayConfig>>({
  INDIVIDUAL: {
    iconStoragePath: null,
    iconPreviewUrl: null,
    featuresText: "",
    uploading: false,
  },
  ORGANIZATION: {
    iconStoragePath: null,
    iconPreviewUrl: null,
    featuresText: "",
    uploading: false,
  },
  INDIVIDUAL_OF_ORG: {
    iconStoragePath: null,
    iconPreviewUrl: null,
    featuresText: "",
    uploading: false,
  },
});

const currentRows = computed(() => tabData[activeTab.value]);
const currentSubjectConfig = computed(
  () => subjectDisplayData[activeTab.value],
);

function subjectLabel(tab: TabKey): string {
  const map: Record<TabKey, string> = {
    INDIVIDUAL: t("approvals.subjectIndividual"),
    ORGANIZATION: t("approvals.subjectOrganization"),
    INDIVIDUAL_OF_ORG: t("approvals.subjectIndividualOfOrg"),
  };
  return map[tab];
}

function isTabCompleted(tab: TabKey): boolean {
  const rows = tabData[tab];
  return (
    rows.length > 0 &&
    rows.every(
      (r) =>
        r.durationMonths != null &&
        r.condition !== "" &&
        r.minValue != null &&
        r.fee != null &&
        r.totalFee != null,
    )
  );
}

// Auto-calculate totalFee = fee * (maxValue - minValue) when maxValue is set
watch(tabData, (data) => {
  (Object.values(data) as ConfigRow[][]).forEach((rows) => {
    rows.forEach((row) => {
      if (row.maxValue != null && row.fee != null) {
        row.totalFee = row.fee * row.maxValue;
      }
    });
  });
}, { deep: true });

function addRow() {
  tabData[activeTab.value].push({
    id: ++rowIdSeq,
    durationMonths: undefined,
    condition: "",
    minValue: undefined,
    maxValue: undefined,
    fee: undefined,
    totalFee: undefined,
  });
}

function removeRow(index: number) {
  tabData[activeTab.value].splice(index, 1);
}

async function handleIconUpload(file: File, tab: TabKey): Promise<false> {
  subjectDisplayData[tab].uploading = true;
  try {
    const res = await uploadPlanIcon(file);
    if (res.success && res.data) {
      subjectDisplayData[tab].iconStoragePath = res.data.storagePath;
      subjectDisplayData[tab].iconPreviewUrl = res.data.url;
      ElMessage.success("Upload ảnh thành công");
    }
  } catch {
    ElMessage.error("Upload ảnh thất bại");
  } finally {
    subjectDisplayData[tab].uploading = false;
  }
  return false; // prevent el-upload default behavior
}

async function applyTemplate() {
  if (!selectedTemplate.value) {
    ElMessage.warning(t("individualPlan.warningChooseTemplate"));
    return;
  }
  try {
    const res = await getIndividualPlanConfigDetail(selectedTemplate.value);
    if (res.success && res.data) {
      const detail = res.data;
      form.name = detail.name + t("individualPlan.copySuffix");

      tabData.INDIVIDUAL = [];
      tabData.ORGANIZATION = [];
      tabData.INDIVIDUAL_OF_ORG = [];

      detail.pricingRules.forEach((r) => {
        const tab = r.subject as TabKey;
        if (tabData[tab]) {
          tabData[tab].push({
            id: ++rowIdSeq,
            durationMonths: r.durationMonths,
            condition: r.condition,
            minValue: r.minValue,
            maxValue: r.maxValue ?? undefined,
            fee: r.fee,
            totalFee: r.totalFee ?? undefined,
          });
        }
      });

      // Copy subject display config from template
      if (detail.subjectConfigs) {
        detail.subjectConfigs.forEach((sc) => {
          const tab = sc.subjectType as TabKey;
          if (subjectDisplayData[tab]) {
            subjectDisplayData[tab].iconPreviewUrl = sc.iconUrl;
            subjectDisplayData[tab].iconStoragePath = sc.iconUrl;
            subjectDisplayData[tab].featuresText = sc.featuresText ?? "";
            // displayPrice is auto-computed by backend from pricing rules
          }
        });
      }

      ElMessage.success(t("individualPlan.applyTemplateSuccess"));
    }
  } catch (error) {
    console.error("Lỗi khi tạo gói cước:", error);
  }
  templateDialogVisible.value = false;
  selectedTemplate.value = null;
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning(t("agency.warningPlanName"));
    return;
  }

  const allRows = [...tabData.INDIVIDUAL, ...tabData.ORGANIZATION, ...tabData.INDIVIDUAL_OF_ORG];
  if (allRows.length > 0 && allRows.some((r) => r.totalFee == null || r.totalFee < 0)) {
    ElMessage.warning(t("agency.warningTotalPrice"));
    return;
  }

  const allRules = [
    ...tabData.INDIVIDUAL.map((r, i) => ({
      ...r,
      subject: "INDIVIDUAL",
      sortOrder: i,
    })),
    ...tabData.ORGANIZATION.map((r, i) => ({
      ...r,
      subject: "ORGANIZATION",
      sortOrder: i,
    })),
    ...tabData.INDIVIDUAL_OF_ORG.map((r, i) => ({
      ...r,
      subject: "INDIVIDUAL_OF_ORG",
      sortOrder: i,
    })),
  ]
    .filter((r) => r.durationMonths && r.condition)
    .map((r) => ({
      subject: r.subject as string,
      durationMonths: r.durationMonths!,
      condition: r.condition,
      minValue: r.minValue,
      maxValue: r.maxValue,
      fee: r.fee,
      totalFee: r.totalFee,
      sortOrder: r.sortOrder,
    }));

  const fmt = (d: Date) => d.toISOString().slice(0, 10);

  const subjectConfigs = (Object.keys(subjectDisplayData) as TabKey[])
    .filter(
      (tab) =>
        subjectDisplayData[tab].iconStoragePath ||
        subjectDisplayData[tab].featuresText,
    )
    .map((tab) => ({
      subjectType: tab,
      iconUrl: subjectDisplayData[tab].iconStoragePath,
      featuresText: subjectDisplayData[tab].featuresText || null,
    }));

  const payload = {
    name: form.name.trim(),
    applyFrom: form.dateRange ? fmt(form.dateRange[0]) : null,
    applyUntil: form.dateRange ? fmt(form.dateRange[1]) : null,
    pricingRules: allRules,
    subjectConfigs,
  };

  submitting.value = true;
  try {
    const res = await createIndividualPlanConfig(payload);
    if (res.success) {
      ElMessage.success(t("individualPlan.createSuccess"));
      router.push("/individual-plan-config");
    }
  } catch {
    ElMessage.error(t("individualPlan.createFailed"));
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  router.push("/individual-plan-config");
}

async function loadTemplatePlans() {
  try {
    const res = await getIndividualPlanConfigSummary();
    if (res.success && res.data) {
      templatePlans.value = res.data.list ?? [];
    }
  } catch {
    // Optional template selector; ignore failures here.
  }
}

onMounted(loadTemplatePlans);
</script>

<style scoped>
.plan-config-create {
  padding-bottom: 80px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0 0 1rem 0;
  color: var(--el-text-color-primary);
  font-weight: 500;
}
.page-subtitle {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 15px;
  font-weight: 400;
}

.btn-select {
  padding: 0.75rem 1.5rem;
  margin-bottom: 1.25rem;
  width: 230px;
  height: 3rem;
  font-size: 17px;
  border-radius: 0.5rem;
  color: var(--el-text-color-primary);
  border-color: var(--el-text-color-primary);
}

.section-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: #fff;
  border-radius: 6px;
  padding: 22px;
  margin-bottom: 16px;
  box-shadow: 0px 3px 12px 0px #2f2b3d24;
}

.section-title {
  display: block;
  font-weight: 700;
  font-size: 18px;
  color: var(--el-color-primary);
  letter-spacing: 0.3px;
}

/* Form layout */
.section-form {
  max-width: 100%;
}

.field-hint {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin: 0 0 0 1rem;
  font-style: italic;
}

.note-text {
  font-size: 17px;
  color: var(--el-text-color-regular);
  line-height: 26px;
  font-style: italic;
  font-weight: 500;
}

.category-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tab-prefix-label {
  font-size: 14px;
  color: #303133;
  flex-shrink: 0;
}

.cat-tab-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 18px;
  font-size: 14px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  user-select: none;
  transition:
    border-color 0.15s,
    background 0.15s,
    color 0.15s;
  line-height: 1.4;
  outline: none;
  white-space: nowrap;
}

.cat-tab.is-active {
  border-color: #409eff;
  color: #409eff;
  font-weight: 600;
  background: #fff;
}

.cat-tab.is-done {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}

.cat-tab.is-done.is-active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}

.tab-done-icon {
  font-size: 14px;
  color: #409eff;
  flex-shrink: 0;
}

/* ─── Hiển thị trên trang công khai ────────────────────────────────────────── */
.subject-display-config {
  margin: 16px 0 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 14px 16px 16px;
  background: #fafafa;
}

.subject-display-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 14px;
}

.subject-display-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Cột ảnh — cố định 130px */
.icon-col {
  flex: 0 0 130px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.icon-uploader {
  display: block;
}

.icon-box {
  position: relative;
  width: 118px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.15s;
}
.icon-box:hover {
  border-color: #409eff;
}

.icon-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 6px;
}

.icon-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
  user-select: none;
}

.icon-loading {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #409eff;
}

/* Cột phải — giá + tính năng */
.meta-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.price-auto-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  border-radius: 4px;
  padding: 8px 10px;
  line-height: 1.5;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

/* Action cell */
.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.drag-handle {
  cursor: grab;
  color: #909399;
  font-size: 16px;
}

.delete-icon {
  cursor: pointer;
  color: #909399;
  font-size: 16px;
  transition: color 0.15s;
}
.delete-icon:hover {
  color: #f56c6c;
}

.inline-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
.fee-cell {
  justify-content: flex-end;
}
.unit-text {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.guide-text {
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}
.guide-text ol {
  margin: 0;
  padding-left: 18px;
}
.guide-text li {
  margin-bottom: 6px;
}
.guide-text ul {
  margin: 4px 0;
  padding-left: 18px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 12px 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  z-index: 100;
}

.dialog-desc {
  font-size: 18px;
  color: var(--el-text-color-regular);
  line-height: 28px;
  font-weight: 500;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
:deep(.el-table td.el-table__cell) {
  padding: 6px 0;
}
</style>
