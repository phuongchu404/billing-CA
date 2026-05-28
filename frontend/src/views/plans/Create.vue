<template>
  <div>
    <div class="page-header">
      <h2>{{ $t("agency.createTitle") }}</h2>
      <span class="breadcrumb">{{ $t("agency.breadcrumb") }}</span>
    </div>

    <el-button
      :icon="Document"
      class="btn"
      style="margin-bottom: 20px"
      @click="handleChooseTemplate"
    >
      {{ $t("agency.chooseTemplate") }}
    </el-button>

    <!-- THÔNG TIN ĐẠI LÝ -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t("agency.infoSection") }}</div>
      <el-form :model="form" label-width="280px" label-position="left">
        <el-form-item :label="$t('agency.agencyNameLabel')">
          <el-input
            v-model="form.groupName"
            :placeholder="$t('agency.agencyNamePlaceholder')"
          />
          <div class="field-hint">
            {{ $t("agency.agencyNameHint") }}
          </div>
        </el-form-item>

        <el-form-item :label="$t('agency.picEmailLabel')">
          <div class="tag-input-wrap" @click="focusPicInput">
            <el-tag
              v-for="(email, i) in form.picEmails"
              :key="i"
              closable
              @close="form.picEmails.splice(i, 1)"
              >{{ email }}</el-tag
            >
            <input
              ref="picInputRef"
              v-model="picEmailInput"
              class="tag-input"
              @keydown.space.prevent="addEmail('pic')"
              @keydown.enter.prevent="addEmail('pic')"
            />
          </div>
          <div class="field-hint">
            {{ $t("agency.picEmailHint") }}
          </div>
        </el-form-item>

        <el-form-item :label="$t('agency.contactEmailLabel')">
          <div class="tag-input-wrap" @click="focusContactInput">
            <el-tag
              v-for="(email, i) in form.contactEmails"
              :key="i"
              closable
              size="small"
              @close="form.contactEmails.splice(i, 1)"
              >{{ email }}</el-tag
            >
            <input
              ref="contactInputRef"
              v-model="contactEmailInput"
              class="tag-input"
              @keydown.space.prevent="addEmail('contact')"
              @keydown.enter.prevent="addEmail('contact')"
            />
          </div>
          <div class="field-hint">
            {{ $t("agency.contactEmailHint") }}
          </div>
        </el-form-item>

        <el-form-item :label="$t('agency.refContractLabel')">
          <el-input
            v-model="form.refContractNo"
            :placeholder="$t('agency.refContractPlaceholder')"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- THÔNG TIN GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t("agency.planSection") }}</div>
      <el-form :model="form" label-width="280px" label-position="left">
        <el-form-item :label="$t('agency.planNameLabel')">
          <el-input
            v-model="form.planName"
            :placeholder="$t('agency.planNamePlaceholder')"
          />
          <div class="field-hint">{{ $t("agency.planNameHint") }}</div>
        </el-form-item>

        <el-form-item :label="$t('agency.applyDateLabel')">
          <el-date-picker
            v-model="form.applyDateRange"
            type="daterange"
            :start-placeholder="$t('agency.dateFrom')"
            range-separator="-"
            :end-placeholder="$t('agency.dateTo')"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div class="plan-notes">
        <p>
          {{ $t("agency.applyNotePendingPrefix") }}
          {{ $t("agency.applyNotePendingStatus") }}
          {{ $t("agency.applyNotePendingSuffix") }}
        </p>
        <p>
          {{ $t("agency.applyNoteAvailablePrefix") }}
          {{ $t("agency.applyNoteAvailableStatus") }}
          {{ $t("agency.applyNoteAvailableSuffix") }}
        </p>
      </div>
    </el-card>

    <!-- CẤU HÌNH GÓI CƯỚC SMARTCA -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t("agency.configSection") }}</div>
      <el-table :data="configRows" border>
        <el-table-column type="index" width="50" align="center"/>
        <el-table-column :label="$t('agency.colSubject')" width="200" sortable>
          <template #default="{ row }">
            <span class="subject-label">{{ row.subject }}</span>
          </template>
        </el-table-column>

        <el-table-column
          :label="$t('agency.colCtsDuration')" width="180" header-align="left" align="right" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number
                v-model="row.duration"
                :min="0"
                :max="48"
                controls-position="right"
                style="width: 80px"
                size="small"
              />
              <span>{{ $t("agency.monthUnit") }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          :label="$t('agency.colCondition')"
          width="180"
          sortable
        >
          <template #default="{ row }">
            <el-select v-model="row.condition" size="small" style="width: 100%">
              <el-option
                :label="$t('agency.conditionSigning')"
                value="signing"
              />
              <el-option
                :label="$t('agency.conditionCertificate')"
                value="certificate"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMinValue')" width="180" sortable>
          <template #default="{ row }">
            <el-input-number
              v-model="row.minValue"
              :min="1"
              controls-position="right"
              style="width: 100%"
              size="small"
            />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colMaxValue')" width="180" sortable>
          <template #default="{ row }">
            <el-input-number
              v-model="row.maxValue"
              :min="0"
              :value-on-clear="null"
              controls-position="right"
              style="width: 100%"
              size="small"
              :placeholder="$t('agency.maxValuePlaceholder')"
              @change="autoCalcTotalPrice(row)"
            />
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colFeePerCondition')" width="180" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number
                v-model="row.fee"
                :min="0"
                controls-position="right"
                style="flex: 1"
                size="small"
                @change="autoCalcTotalPrice(row)"
              />
              <span>{{ $t("agency.vnd") }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('agency.colTotalPrice')" width="200" sortable>
          <template #default="{ row }">
            <div class="cell-row">
              <el-input-number
                v-model="row.totalPrice"
                :min="0"
                :controls="false"
                style="flex: 1"
                size="small"
                :placeholder="$t('agency.totalPricePlaceholder')"
              />
              <span>{{ $t("agency.vnd") }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="guide-toggle" @click="showGuide = !showGuide">
        {{ $t("agency.guideToggle") }}
        <el-icon><ArrowUp v-if="showGuide" /><ArrowDown v-else /></el-icon>
      </div>

      <div v-show="showGuide" class="guide-content">
        <ul>
          <li class="text-primary"><b>{{ $t("agency.guideIntro") }}</b></li>
          <li>
            <b class="text-primary">{{ $t("agency.guideDurationLabel") }}</b>
            {{ $t("agency.guideDurationText") }}
          </li>
          <li>
            <b class="text-primary">{{ $t("agency.guideConditionLabel") }}</b>
            {{ $t("agency.guideConditionText") }}
          </li>
          <li>
            <span>
              <b class="text-primary">{{ $t("agency.guideMinLabel") }}</b> 
              {{ $t("agency.guideMinText") }}
            </span>
            <span>
              <b class="text-primary">{{ $t("agency.guideMaxLabel") }}</b> 
              {{ $t("agency.guideMaxText") }}
            </span>
            <br />
            <span>
              <u>{{ $t("agency.guideNoteLabel") }}</u>
              {{ $t("agency.guideNoteText") }}
            </span>
          </li>
          <li>
            <b class="text-primary">{{ $t("agency.guideFeeLabel") }}</b>
            {{ $t("agency.guideFeeText") }}
          </li>
        </ul>
      </div>
    </el-card>

    <!-- TÀI KHOẢN ĐĂNG NHẬP ĐỐI TÁC (tùy chọn) -->
    <!-- <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.partnerAccountSection') }}</div>
      <el-checkbox v-model="createPartnerAccount" style="margin-bottom:16px">
        {{ $t('agency.partnerAccountToggle') }}
      </el-checkbox>

      <el-form v-if="createPartnerAccount" :model="partnerForm" label-width="220px" label-position="left">
        <el-form-item :label="$t('agency.partnerUsernameLabel')">
          <el-input v-model="partnerForm.username" :placeholder="$t('agency.partnerUsernamePlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('agency.partnerEmailLabel')">
          <el-input v-model="partnerForm.email" :placeholder="$t('agency.partnerEmailPlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('agency.partnerFullNameLabel')">
          <el-input v-model="partnerForm.fullName" :placeholder="$t('agency.partnerFullNamePlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('agency.partnerPasswordLabel')">
          <el-input
            v-model="partnerForm.password"
            type="password"
            show-password
            :placeholder="$t('agency.partnerPasswordPlaceholder')"
          />
          <div class="field-hint">{{ $t('agency.partnerPasswordHint') }}</div>
        </el-form-item>

        <el-form-item :label="$t('agency.partnerConfirmPasswordLabel')">
          <el-input
            v-model="partnerForm.confirmPassword"
            type="password"
            show-password
            :placeholder="$t('agency.partnerConfirmPasswordPlaceholder')"
          />
        </el-form-item>
      </el-form>
    </el-card> -->

    <div class="footer-btns">
      <el-button type="primary" :loading="submitting" @click="handleSubmit">{{
        $t("common.confirm")
      }}</el-button>
      <el-button @click="handleCancel" class="btn-cancel">{{ $t("roles.cancelEdit") }}</el-button>
    </div>

    <!-- DIALOG CHỌN GÓI CƯỚC MẪU -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="$t('agency.chooseTemplateTitle')"
      width="480px"
      destroy-on-close
    >
      <p
        style="
          margin: 0 0 16px;
          color: #606266;
          font-size: 13px;
          line-height: 1.6;
        "
      >
        {{ $t("agency.chooseTemplateDesc") }}
      </p>
      <el-form label-width="90px" label-position="left">
        <el-form-item :label="$t('agency.chooseTemplatePlanLabel')">
          <div v-loading="templateLoading" style="width: 100%">
            <el-select
              v-model="selectedTemplateId"
              :placeholder="$t('agency.chooseTemplatePlaceholder')"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="tpl in templates"
                :key="tpl.planTemplateId"
                :label="tpl.planName"
                :value="tpl.planTemplateId"
              >
                <span>{{ tpl.planName }}</span>
                <span style="float: right; color: #909399; font-size: 12px">{{
                  tpl.planCode
                }}</span>
              </el-option>
            </el-select>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :disabled="!selectedTemplateId"
          @click="applyTemplate"
        >
          {{ $t("common.confirm") }}
        </el-button>
        <el-button @click="templateDialogVisible = false">{{
          $t("common.cancel")
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { Document, ArrowUp, ArrowDown } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { provisionGroup } from "@/api/groups";
import { listPlanTemplates } from "@/api/planTemplates";
import type {
  ProvisionGroupRequest,
  PartnerAccountRequest,
} from "@/types/group";
import type {
  PlanTemplate,
  PlanPricingRuleRequest,
} from "@/types/planTemplate";

const { t } = useI18n();
const router = useRouter();

const createPartnerAccount = ref(false);
const partnerForm = reactive({
  username: "",
  email: "",
  fullName: "",
  password: "",
  confirmPassword: "",
});

interface ConfigRow {
  subject: string;
  subjectType: "INDIVIDUAL" | "ORGANIZATION" | "INDIVIDUAL_OF_ORG";
  duration: number;
  condition: "signing" | "certificate";
  minValue: number;
  maxValue: number | null;
  fee: number;
  totalPrice: number | null;
}

const form = reactive({
  groupName: "",
  picEmails: [] as string[],
  contactEmails: [] as string[],
  refContractNo: "",
  planName: "",
  applyDateRange: null as [string, string] | null,
});

const picEmailInput = ref("");
const contactEmailInput = ref("");
const picInputRef = ref<HTMLInputElement>();
const contactInputRef = ref<HTMLInputElement>();
const showGuide = ref(true);
const submitting = ref(false);

const configRows = reactive<ConfigRow[]>([
  {
    subject: t("agency.subjectIndividual"),
    subjectType: "INDIVIDUAL",
    duration: 1,
    condition: "signing",
    minValue: 1,
    maxValue: null,
    fee: 0,
    totalPrice: null,
  },
  {
    subject: t("agency.subjectOrganization"),
    subjectType: "ORGANIZATION",
    duration: 24,
    condition: "certificate",
    minValue: 1,
    maxValue: null,
    fee: 0,
    totalPrice: null,
  },
  {
    subject: t("agency.subjectIndividualOfOrg"),
    subjectType: "INDIVIDUAL_OF_ORG",
    duration: 12,
    condition: "certificate",
    minValue: 1,
    maxValue: null,
    fee: 0,
    totalPrice: null,
  },
]);

function autoCalcTotalPrice(row: ConfigRow) {
  if (row.maxValue != null) {
    row.totalPrice = row.fee * row.maxValue;
  }
}

function addEmail(type: "pic" | "contact") {
  const inputRef = type === "pic" ? picEmailInput : contactEmailInput;
  const emails = type === "pic" ? form.picEmails : form.contactEmails;
  const val = inputRef.value.trim();
  if (val && !emails.includes(val)) emails.push(val);
  inputRef.value = "";
}

function focusPicInput() {
  picInputRef.value?.focus();
}
function focusContactInput() {
  contactInputRef.value?.focus();
}

// --- Template picker dialog ---
const templateDialogVisible = ref(false);
const templateLoading = ref(false);
const templates = ref<PlanTemplate[]>([]);
const selectedTemplateId = ref<number | null>(null);

async function handleChooseTemplate() {
  templateDialogVisible.value = true;
  selectedTemplateId.value = null;
  if (templates.value.length === 0) {
    templateLoading.value = true;
    try {
      const res = await listPlanTemplates("GROUP");
      if (res.success && res.data) {
        templates.value = res.data;
      } else {
        ElMessage.error(t("agency.errorLoadTemplate"));
      }
    } catch {
      ElMessage.error(t("agency.errorLoadTemplate"));
    } finally {
      templateLoading.value = false;
    }
  }
}

function applyTemplate() {
  const tpl = templates.value.find(
    (tmpl) => tmpl.planTemplateId === selectedTemplateId.value,
  );
  if (!tpl) {
    ElMessage.warning(t("agency.warningChooseTemplate"));
    return;
  }

  if (tpl.planName) form.planName = tpl.planName;

  const subjectOrder: ConfigRow["subjectType"][] = [
    "INDIVIDUAL",
    "ORGANIZATION",
    "INDIVIDUAL_OF_ORG",
  ];
  subjectOrder.forEach((subjectType, idx) => {
    const rule = tpl.pricingRules.find((r) => r.subjectType === subjectType);
    if (rule) {
      configRows[idx].duration = rule.certificateValidityValue;
      configRows[idx].condition =
        rule.pricingMetric === "SIGNING_COUNT" ? "signing" : "certificate";
      configRows[idx].minValue = rule.rangeMin;
      configRows[idx].maxValue = rule.rangeMax;
      configRows[idx].fee = rule.unitPrice as unknown as number;
      configRows[idx].totalPrice = rule.totalPrice as unknown as number | null;
    }
  });

  ElMessage.success(t("agency.applyTemplateSuccess"));
  templateDialogVisible.value = false;
}

async function handleSubmit() {
  if (!form.groupName.trim()) {
    ElMessage.warning(t("agency.warningAgencyName"));
    return;
  }
  if (!form.planName.trim()) {
    ElMessage.warning(t("agency.warningPlanName"));
    return;
  }
  if (configRows.some((row) => row.totalPrice == null || row.totalPrice < 0)) {
    ElMessage.warning(t("agency.warningTotalPrice"));
    return;
  }
  if (configRows.some((row) => row.maxValue != null && row.maxValue < row.minValue)) {
    ElMessage.warning("Giá trị max phải lớn hơn hoặc bằng giá trị min");
    return;
  }

  // Validate partner account fields nếu được bật
  if (createPartnerAccount.value) {
    if (!partnerForm.username.trim()) {
      ElMessage.warning(t("agency.warningPartnerUsername"));
      return;
    }
    if (
      !partnerForm.email.trim() ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(partnerForm.email)
    ) {
      ElMessage.warning(t("agency.warningPartnerEmail"));
      return;
    }
    if (!partnerForm.password) {
      ElMessage.warning(t("agency.warningPartnerPassword"));
      return;
    }
    if (partnerForm.password !== partnerForm.confirmPassword) {
      ElMessage.warning(t("agency.warningPartnerPasswordMismatch"));
      return;
    }
  }

  // Flush email đang gõ dở chưa nhấn Space/Enter
  addEmail("pic");
  addEmail("contact");

  submitting.value = true;
  try {
    const pricingRules: PlanPricingRuleRequest[] = configRows.map((row, i) => ({
      subjectType: row.subjectType,
      certificateValidityValue: row.duration,
      certificateValidityUnit: "MONTH",
      pricingMetric:
        row.condition === "signing" ? "SIGNING_COUNT" : "CERTIFICATE_COUNT",
      rangeMin: row.minValue,
      rangeMax: row.maxValue,
      unitPrice: row.fee,
      totalPrice: row.totalPrice,
      currency: "VND",
      quotaTotal: null,
      sortOrder: i + 1,
      isActive: true,
    }));

    const partnerAccount: PartnerAccountRequest | undefined =
      createPartnerAccount.value
        ? {
            username: partnerForm.username.trim(),
            email: partnerForm.email.trim(),
            fullName: partnerForm.fullName.trim() || undefined,
            password: partnerForm.password,
            confirmPassword: partnerForm.confirmPassword,
          }
        : undefined;

    const req: ProvisionGroupRequest = {
      groupName: form.groupName,
      picEmails: form.picEmails,
      contactEmails: form.contactEmails,
      refContractNo: form.refContractNo || undefined,
      planName: form.planName,
      effectiveFrom: form.applyDateRange ? form.applyDateRange[0] : null,
      effectiveTo: form.applyDateRange ? form.applyDateRange[1] : null,
      requestedBy: "system",
      pricingRules,
      partnerAccount,
    };

    const res = await provisionGroup(req);
    if (!res.success || !res.data) {
      ElMessage.error(res.message || t("agency.errorCreateAgency"));
      return;
    }

    const msg = res.data.partnerUser
      ? t("agency.successCreateAgencyWithPartner")
      : t("agency.successCreateAgency");
    ElMessage.success(msg);
    router.push("/plans/" + res.data.group.groupId);
  } catch (e) {
    ElMessage.error(t("agency.errorServer"));
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  router.push("/plans");
}
</script>

<style scoped>
.breadcrumb {
  font-size: 15px;
  color: var(--el-text-color-regular);
}

/*form*/
:deep(.el-input),
:deep(.tag-input-wrap) {
  height: 3rem;
}

:deep(.el-date-editor.el-input__wrapper) {
  height: 1.5rem;
}

:deep(.tag-input-wrap),
:deep(.el-input__wrapper) {
  padding: 0.75rem 1rem;
}

.field-hint {
  margin: 0.25rem 0 0 1rem;
}

.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  min-height: 36px;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 3px 8px;
  box-sizing: border-box;
  cursor: text;
  transition: border-color 0.2s;
}
.tag-input-wrap:focus-within {
  border-color: #409eff;
}
.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 60px;
  font-size: 14px;
  padding: 2px 0;
  background: transparent;
}

:deep(.el-tag.el-tag--primary){
  background-color: var(--el-color-primary-light-8);
  color: var(--el-text-color-primary);
  font-size: 15px;
  padding: 0.5rem 0.75rem;
  border-radius: 1rem;
}

.cell-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.subject-label {
  color: #909399;
}

.guide-toggle {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  cursor: pointer;
  color: var(--el-color-primary);
  font-size: 18px;
  font-weight: 500;
  font-style: italic;
  user-select: none;
}

.guide-content {
  font-size: 18px;
  color: var(--el-text-color-regular);
  margin-top: 8px;
  line-height: 1.6;
  font-style: italic;
}
.guide-content ul {
  padding-left: 16px;
  margin: 0;
  list-style: disc;
}
.guide-content li {
  margin-bottom: 6px;
}

.guide-content li b {
  font-weight: 600!important;
}

.footer-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 8px;
}
</style>
