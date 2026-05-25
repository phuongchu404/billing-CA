<template>
  <div>
    <div class="page-header">
      <h2>{{ $t('agency.detailTitle') }}</h2>
      <span class="breadcrumb">{{ $t('agency.breadcrumb') }}</span>
    </div>

    <!-- Action buttons: khác nhau theo trạng thái -->
    <div class="action-bar">
      <template v-if="agency.status === 'ACTIVE'">
        <el-button :icon="Download" @click="handleExport"
          >{{ $t('agency.btnExportReconciliation') }}</el-button
        >
        <el-button :icon="Edit" @click="handleEdit">{{ $t('agency.btnEdit') }}</el-button>
        <el-button :icon="Plus" @click="handleAddPlan">{{ $t('agency.btnAddPlan') }}</el-button>
        <el-button :icon="VideoPause" @click="handleSuspend"
          >{{ $t('agency.btnSuspend') }}</el-button
        >
      </template>
      <template v-else>
        <el-button :icon="VideoPlay" @click="handleActivate"
          >{{ $t('agency.btnActivate') }}</el-button
        >
      </template>
    </div>

    <!-- THÔNG TIN CHI TIẾT -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.detailSection') }}</div>
      <div class="info-grid">
        <div class="info-row two-col">
          <span><b>{{ $t('agency.agencyNameField') }}</b> {{ agency.groupName }}</span>
          <span><b>{{ $t('agency.agencyCodeField') }}</b> {{ agency.groupCode }}</span>
        </div>
        <div class="info-row">
          <b>{{ $t('agency.statusField') }}</b>
          <span
            :class="
              agency.status === 'ACTIVE' ? 'text-active' : 'text-inactive'
            "
            style="margin-left: 4px"
          >
            {{ agency.status === "ACTIVE" ? $t("agency.statusActive") : $t("agency.statusInactive") }}
          </span>
        </div>
        <div class="info-row">
          <span
            ><b>{{ $t('agency.picField') }}</b> {{ agency.picEmails.join(", ") }}</span
          >
        </div>
        <div v-if="agency.contactEmails?.length" class="info-row">
          <span
            ><b>{{ $t('agency.representativeField') }}</b> {{ agency.contactEmails.join(", ") }}</span
          >
        </div>
        <div class="info-row">
          <span><b>{{ $t('agency.refContractField') }}</b> {{ agency.refContractNo }}</span>
        </div>
        <div class="info-row two-col">
          <span><b>{{ $t('agency.createdByField') }}</b> {{ agency.createdBy }}</span>
          <span><b>{{ $t('agency.createdAtField') }}</b> {{ agency.createdAt }}</span>
        </div>
        <div class="info-row">
          <span><b>{{ $t('agency.updatedAtField') }}</b> {{ agency.updatedAt }}</span>
        </div>
      </div>
    </el-card>

    <!-- QUẢN LÝ GÓI CƯỚC -->
    <el-card shadow="never" class="section-card">
      <div class="section-title">{{ $t('agency.planSectionTitle') }}</div>
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
            <div class="col-label">{{ $t('agency.colPlanName') }}</div>
            <div class="col-filter"></div>
          </template>
        </el-table-column>

        <el-table-column prop="status" sortable width="145">
          <template #header>
            <div class="col-label">{{ $t('agency.colStatus') }}</div>
            <div class="col-filter">
              <el-select
                v-model="planFilterStatus"
                size="small"
                clearable
                placeholder=""
                style="width: 100%"
              >
                <el-option :label="$t('agency.statusAvailable')" value="available" />
                <el-option :label="$t('agency.statusUnavailable')" value="unavailable" />
                <el-option :label="$t('agency.statusPending')" value="pending" />
                <el-option :label="$t('agency.statusApproved')" value="approved" />
                <el-option :label="$t('agency.planStatusActive')" value="active" />
              </el-select>
            </div>
          </template>
          <template #default="{ row }">
            <el-tag
              v-if="row.status === 'available'"
              size="small"
              type="info"
              plain
              >{{ $t('agency.statusAvailable') }}</el-tag
            >
            <el-tag
              v-else-if="row.status === 'unavailable'"
              size="small"
              type="info"
              plain
              style="color: #909399; border-color: #dcdfe6"
              >{{ $t('agency.statusUnavailable') }}</el-tag
            >
            <el-tag
              v-else-if="row.status === 'pending'"
              size="small"
              type="warning"
              >{{ $t('agency.statusPending') }}</el-tag
            >
            <el-tag
              v-else-if="row.status === 'approved'"
              size="small"
              class="tag-approved"
              >{{ $t('agency.statusApproved') }}</el-tag
            >
            <el-tag
              v-else-if="row.status === 'active'"
              size="small"
              type="primary"
              >{{ $t('agency.planStatusActive') }}</el-tag
            >
          </template>
        </el-table-column>

        <el-table-column prop="applyFrom" sortable width="135">
          <template #header>
            <div class="col-label">{{ $t('agency.colApplyFrom') }}</div>
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
            <div class="col-label">{{ $t('agency.colApplyTo') }}</div>
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
            <div class="col-label">{{ $t('agency.colUpdatedAt') }}</div>
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
            <div class="col-label">{{ $t('agency.colActions') }}</div>
            <div class="col-filter"></div>
          </template>
          <template #default="{ row }">
            <div class="action-btns">
              <el-button
                size="small"
                :icon="InfoFilled"
                @click="handlePlanDetail(row)"
                >{{ $t('agency.btnDetail') }}</el-button
              >
              <template v-if="agency.status === 'ACTIVE'">
                <el-button
                  v-if="row.status === 'available'"
                  size="small"
                  type="primary"
                  :disabled="!can('group:update')"
                  @click="handleRequestApply(row)"
                  >{{ $t('agency.btnRequestApply') }}</el-button
                >
                <el-button
                  v-else-if="row.status === 'pending'"
                  size="small"
                  type="success"
                  :icon="Check"
                  :disabled="!canApprovePlan"
                  @click="handleApprove(row)"
                  >{{ $t('agency.btnApprove') }}</el-button
                >
                <el-button
                  v-else-if="
                    row.status === 'approved' || row.status === 'active'
                  "
                  size="small"
                  type="warning"
                  :disabled="!can('group:update')"
                  @click="handleStopApply(row)"
                  >{{ $t('agency.btnStopApply') }}</el-button
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
        {{ $t('agency.historySectionTitle') }}
        <el-icon><ArrowDown v-if="showHistory" /><ArrowRight v-else /></el-icon>
      </div>
      <el-table v-show="showHistory" :data="historyRows" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column
          prop="applyFrom"
          :label="$t('agency.colApplyFrom')"
          sortable
          width="130"
        />
        <el-table-column
          prop="applyTo"
          :label="$t('agency.colApplyTo')"
          sortable
          width="130"
        />
        <el-table-column
          prop="planName"
          :label="$t('agency.colPlanName')"
          sortable
          min-width="180"
        />
        <el-table-column :label="$t('agency.colStatus')" width="130" sortable prop="assignmentStatus">
          <template #default="{ row }">
            <el-tag v-if="row.assignmentStatus === 'APPROVED'" size="small" type="info">{{ $t('agency.statusApproved') }}</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'ACTIVE'" size="small" type="primary">{{ $t('agency.planStatusActive') }}</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'STOPPED'" size="small" type="warning">{{ $t('agency.planStatusStopped') }}</el-tag>
            <el-tag v-else-if="row.assignmentStatus === 'EXPIRED'" size="small" type="danger">{{ $t('agency.planStatusExpired') }}</el-tag>
            <el-tag v-else size="small">{{ row.assignmentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="ctsCreated"
          :label="$t('agency.colCtsCreated')"
          sortable
          width="145"
        >
          <template #default="{ row }">{{
            row.ctsCreated.toLocaleString()
          }}</template>
        </el-table-column>
        <el-table-column
          prop="ctsCreatedPct"
          :label="$t('agency.colCtsCreatedPct')"
          sortable
          width="140"
        />
        <el-table-column
          prop="signingUsed"
          :label="$t('agency.colSigningUsed')"
          sortable
          width="145"
        >
          <template #default="{ row }">{{
            row.signingUsed.toLocaleString()
          }}</template>
        </el-table-column>
        <el-table-column
          prop="signingUsedPct"
          :label="$t('agency.colSigningUsedPct')"
          sortable
          width="140"
        />
      </el-table>
    </el-card>

    <!-- ===== DIALOG 1: YÊU CẦU ÁP DỤNG BẢNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="requestApplyVisible"
      :title="$t('agency.dialogRequestApply')"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-info-line">
          {{ $t('agency.dialogPlanName') }}
          <span class="dlg-value">{{ selectedPlan?.planName }}</span>
        </p>
        <p class="dlg-info-line">
          {{ $t('agency.dialogAgencyName') }} <span class="dlg-value">{{ agency.groupName }}</span>
        </p>
        <p class="dlg-info-line">
          {{ $t('agency.dialogAgencyCode') }} <span class="dlg-value">{{ agency.groupCode }}</span>
        </p>
        <div class="dlg-date-row">
          <span class="dlg-date-label">{{ $t('agency.dialogApplyPeriod') }}</span>
          <el-date-picker
            v-model="requestApplyDateRange"
            type="daterange"
            :start-placeholder="$t('agency.dateFrom')"
            range-separator="-"
            :end-placeholder="$t('agency.dateTo')"
            value-format="YYYY-MM-DD"
            style="flex: 1"
          />
        </div>
        <div class="dlg-date-row">
          <span class="dlg-date-label">{{ $t('agency.dialogApprovalLevel') }}</span>
          <el-select v-model="requestApprovalLevel" style="flex: 1">
            <el-option :label="$t('agency.dialogSalesManager')" :value="1" />
            <el-option :label="$t('agency.dialogCFO')" :value="2" />
            <el-option :label="$t('agency.dialogCEO')" :value="3" />
          </el-select>
        </div>
        <i18n-t keypath="agency.dialogRequestNote" tag="p" class="dlg-note">
          <template #status>
            <b v-html="$t('agency.dialogPendingApprovalStatus')"></b>
          </template>
        </i18n-t>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmRequestApply"
          >{{ $t('common.confirm') }}</el-button
        >
        <el-button @click="requestApplyVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 2: DUYỆT ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="approveVisible"
      :title="$t('agency.dialogApproveApply')"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <i18n-t keypath="agency.dialogApproveDesc" tag="p" class="dlg-desc">
          <template #action>
            <b>{{ $t('agency.dialogConfirmAction') }}</b>
          </template>
          <template #planName>
            <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          </template>
          <template #agencyName>
            <b>{{ agency.groupName }}</b>
          </template>
          <template #agencyCode>
            <b>{{ agency.groupCode }}</b>
          </template>
        </i18n-t>
        <div class="dlg-date-row">
          <span class="dlg-date-label">{{ $t('agency.dialogApplyPeriod') }}</span>
          <el-date-picker
            v-model="approveDateRange"
            type="daterange"
            :start-placeholder="$t('agency.dateFrom')"
            range-separator="-"
            :end-placeholder="$t('agency.dateTo')"
            value-format="YYYY-MM-DD"
            style="flex: 1"
          />
        </div>
        <i18n-t keypath="agency.dialogApproveNote" tag="p" class="dlg-note">
          <template #status>
            <b>{{ $t('agency.dialogApprovedStatus') }}</b>
          </template>
        </i18n-t>
        <ul class="dlg-bullets">
          <i18n-t keypath="agency.dialogApproveBullet1" tag="li">
            <template #status>
              <b>{{ $t('agency.dialogActiveStatus') }}</b>
            </template>
          </i18n-t>
          <i18n-t keypath="agency.dialogApproveBullet2" tag="li">
            <template #status>
              <b>{{ $t('agency.dialogAvailableStatus') }}</b>
            </template>
          </i18n-t>
          <i18n-t keypath="agency.dialogApproveBullet3" tag="li">
            <template #status>
              <b>{{ $t('agency.dialogAvailableStatus') }}</b>
            </template>
          </i18n-t>
        </ul>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button type="warning" :disabled="!canApprovePlan" @click="confirmReject">{{ $t('agency.btnReject') }}</el-button>
          <div>
            <el-button type="primary" :disabled="!canApprovePlan" @click="confirmApprove"
              >{{ $t('common.confirm') }}</el-button
            >
            <el-button @click="approveVisible = false">{{ $t('common.cancel') }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 3: DỪNG ÁP DỤNG GÓI CƯỚC ===== -->
    <el-dialog
      v-model="stopApplyVisible"
      :title="$t('agency.dialogStopApply')"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <i18n-t keypath="agency.dialogStopDesc" tag="p" class="dlg-desc">
          <template #action>
            <b>{{ $t('agency.dialogConfirmAction') }}</b>
          </template>
          <template #planName>
            <b class="dlg-plan-name">{{ selectedPlan?.planName }}</b>
          </template>
          <template #agencyName>
            <b>{{ agency.groupName }}</b>
          </template>
          <template #agencyCode>
            <b>{{ agency.groupCode }}</b>
          </template>
          <template #status>
            <b>{{ $t('agency.dialogAvailableStatus') }}</b>
          </template>
        </i18n-t>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmStopApply">{{ $t('common.confirm') }}</el-button>
        <el-button @click="stopApplyVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 4: CHI TIẾT GÓI CƯỚC ===== -->
    <el-dialog
      v-model="planDetailVisible"
      :title="$t('agency.dialogPlanDetail')"
      width="760px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <div class="detail-info-grid">
          <div class="detail-info-row">
            <span><b>{{ $t('agency.dialogPlanName') }}</b> {{ selectedPlan?.planName }}</span>
            <span
              ><b>{{ $t('agency.statusField') }}</b>
              {{ planStatusLabel(selectedPlan?.status) }}</span
            >
          </div>
          <div class="detail-info-row">
            <span><b>{{ $t('agency.agencyCodeField') }}</b> {{ agency.groupCode }}</span>
            <span><b>{{ $t('agency.agencyNameField') }}</b> {{ agency.groupName }}</span>
          </div>
          <div class="detail-info-row">
            <span><b>{{ $t('agency.colApplyFrom') }}:</b> {{ selectedPlan?.applyFrom }}</span>
            <span><b>{{ $t('agency.colApplyTo') }}:</b> {{ selectedPlan?.applyTo }}</span>
          </div>
          <div class="detail-info-row">
            <span
              ><b>{{ $t('agency.updatedAtField') }}</b> {{ selectedPlan?.updatedAt }}</span
            >
          </div>
        </div>

        <el-table :data="planConfigRows" border style="margin-top: 16px" v-loading="planDetailLoading">
          <el-table-column type="index" width="50" />
          <el-table-column prop="subject" :label="$t('agency.colSubject')" width="160" />
          <el-table-column :label="$t('agency.colCtsDuration')" width="160">
            <template #default="{ row }">{{ row.duration }} {{ $t('agency.monthUnit') }}</template>
          </el-table-column>
          <el-table-column prop="condition" :label="$t('agency.colCondition')" width="130" />
          <el-table-column
            prop="min"
            :label="$t('agency.colMinValue')"
            width="155"
          />
          <el-table-column
            prop="max"
            :label="$t('agency.colMaxValue')"
            width="155"
          />
          <el-table-column :label="$t('agency.colFeePerCondition')" width="155">
            <template #default="{ row }"
              >{{ row.fee.toLocaleString() }} {{ $t('agency.vnd') }}</template
            >
          </el-table-column>
          <el-table-column :label="$t('agency.colTotalPrice')">
            <template #default="{ row }">
              <span v-if="row.totalPrice != null">{{ row.totalPrice.toLocaleString() }} {{ $t('agency.vnd') }}</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>

        <i18n-t keypath="agency.dialogPlanNote" tag="p" class="dlg-note" style="margin-top: 12px">
          <template #label>
            <b>{{ $t('agency.dialogPlanNoteLabel') }}</b>
          </template>
        </i18n-t>
      </div>
      <template #footer>
        <div class="dlg-footer-split">
          <el-button
            v-if="selectedPlan?.status !== 'unavailable'"
            type="warning"
            :disabled="!can('group:update')"
            @click="openDisableDialog"
            >{{ $t('agency.btnDisable') }}</el-button
          >
          <div>
            <el-button
              v-if="selectedPlan?.status === 'available'"
              type="primary"
              :disabled="!can('group:update')"
              @click="openRequestFromDetail"
              >{{ $t('agency.btnRequestApply') }}</el-button
            >
            <el-button @click="planDetailVisible = false">{{ $t('common.cancel') }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: XUẤT ĐỐI SOÁT ===== -->
    <el-dialog
      v-model="exportVisible"
      :title="$t('agency.dialogExport')"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <div class="dlg-date-row">
          <span class="dlg-date-label">{{ $t('agency.dialogExportPeriod') }}</span>
          <el-date-picker
            v-model="exportMonth"
            type="month"
            :placeholder="$t('agency.dialogExportPlaceholder')"
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
          :loading="exporting"
          @click="confirmExport"
          >{{ $t('common.confirm') }}</el-button
        >
        <el-button :disabled="exporting" @click="exportVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: KÍCH HOẠT LẠI ===== -->
    <el-dialog
      v-model="activateVisible"
      :title="$t('agency.dialogActivateTitle')"
      width="460px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <p class="dlg-desc">{{ $t('agency.dialogActivateConfirm', { code: agency.groupCode, name: agency.groupName }) }}</p>
        <i18n-t keypath="agency.dialogActivateNote" tag="p" class="dlg-desc" style="margin-top: 8px">
          <template #status>
            <b>{{ $t('agency.dialogActiveAgencyStatus') }}</b>
          </template>
        </i18n-t>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmActivate">{{ $t('common.confirm') }}</el-button>
        <el-button @click="activateVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG 5: VÔ HIỆU HOÁ ===== -->
    <el-dialog
      v-model="disableVisible"
      :title="$t('agency.dialogDisableTitle')"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="dlg-body">
        <i18n-t keypath="agency.dialogDisableDesc" tag="p" class="dlg-desc">
          <template #planName>
            <b>{{ selectedPlan?.planName }}</b>
          </template>
          <template #code>
            {{ agency.groupCode }}
          </template>
          <template #name>
            {{ agency.groupName }}
          </template>
        </i18n-t>
        <i18n-t keypath="agency.dialogDisableNote" tag="p" class="dlg-desc" style="margin-top: 8px">
          <template #action>
            <b>{{ $t('agency.dialogConfirmAction') }}</b>
          </template>
        </i18n-t>
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!can('group:update')" @click="confirmDisable">{{ $t('common.confirm') }}</el-button>
        <el-button @click="disableVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- ===== DIALOG: CHỈNH SỬA THÔNG TIN ĐẠI LÝ ===== -->
    <el-dialog
      v-model="editVisible"
      :title="$t('agency.dialogEditTitle')"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="190px" class="edit-form">
        <el-form-item :label="$t('agency.dialogEditCodeLabel')">
          <div style="width: 100%">
            <el-input :value="agency.groupCode" disabled />
            <div class="field-hint">{{ $t('agency.dialogEditCodeHint') }}</div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('agency.dialogEditNameLabel')">
          <el-input v-model="editForm.groupName" />
        </el-form-item>
        <el-form-item :label="$t('agency.dialogEditPicLabel')">
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
        <el-form-item :label="$t('agency.dialogEditContactLabel')">
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
          >{{ $t('common.confirm') }}</el-button
        >
        <el-button :disabled="editSaving" @click="editVisible = false"
          >{{ $t('common.cancel') }}</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
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
  downloadSettlementExport,
  triggerBlobDownload,
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
const { t } = useI18n();

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
      ElMessage.error(res.message || t('agency.errorNotFound'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
}

async function loadPlans() {
  try {
    const res = await listGroupAssignments(agencyId);
    if (res.success && res.data) {
      plans.value = res.data.map(mapAssignment);
    }
  } catch {
    ElMessage.error(t('agency.errorLoadPlans'));
  }
}

async function loadHistory() {
  try {
    const res = await getGroupPlanHistory(agencyId);
    if (res.success && Array.isArray(res.data)) {
      historyRows.value = res.data;
    }
  } catch {
    ElMessage.error(t('agency.errorLoadHistory'));
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
const exporting = ref(false);

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
    available: t('agency.statusAvailable'),
    unavailable: t('agency.statusUnavailable'),
    pending: t('agency.statusPending'),
    approved: t('agency.statusApproved'),
    active: t('agency.planStatusActive'),
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
    await ElMessageBox.confirm(t('agency.confirmSuspend'), t('agency.confirmTitle'), {
      type: "warning",
    });
  } catch {
    return; // user cancelled
  }
  try {
    const res = await suspendGroup(agencyId);
    if (res.success) {
      agency.value.status = "INACTIVE";
      ElMessage.success(t('agency.successSuspended'));
    } else {
      ElMessage.error(res.message || t('agency.errorCannotSuspend'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
}

function handleActivate() {
  activateVisible.value = true;
}

function handleRequestApply(row: PlanRow) {
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionRequestApply'));
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
    ElMessage.warning(t('agency.noPermissionApprove'));
    return;
  }
  if (!row.approvalRequestId) {
    ElMessage.warning(t('agency.noApprovalRequest'));
    return;
  }
  router.push(`/approvals/${row.approvalRequestId}`);
}

function handleStopApply(row: PlanRow) {
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionStopApply'));
    return;
  }
  selectedPlan.value = row;
  stopApplyVisible.value = true;
}

const subjectTypeLabel: Record<string, string> = {
  INDIVIDUAL: t('agency.subjectIndividual'),
  ORGANIZATION: t('agency.subjectOrganization'),
  INDIVIDUAL_OF_ORG: t('agency.subjectIndividualOfOrg'),
};
const pricingMetricLabel: Record<string, string> = {
  CERTIFICATE_COUNT: t('agency.metricCertificateCount'),
  SIGNING_COUNT: t('agency.metricSigningCount'),
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
          max: r.rangeMax ?? t('agency.unlimited'),
          fee: Number(r.unitPrice),
          totalPrice: r.totalPrice != null ? Number(r.totalPrice) : null,
        }));
    }
  } catch {
    ElMessage.error(t('agency.errorLoadPlanDetail'));
  } finally {
    planDetailLoading.value = false;
  }
}

function openDisableDialog() {
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionDisable'));
    return;
  }
  planDetailVisible.value = false;
  disableVisible.value = true;
}

function openRequestFromDetail() {
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionRequestFromDetail'));
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
    ElMessage.warning(t('agency.noPermissionRequestApply'));
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
      ElMessage.success(t('agency.successRequestApply'));
      await loadPlans();
    } else {
      ElMessage.error(res.message || t('agency.errorCannotRequest'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
  requestApplyVisible.value = false;
}

async function confirmApprove() {
  if (!selectedPlan.value) return;
  if (!canApprovePlan.value) {
    ElMessage.warning(t('agency.noPermissionApprove'));
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
      ElMessage.success(t('agency.successApproved'));
      await loadPlans();
    } else {
      ElMessage.error(res.message || t('agency.errorCannotApprove'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
  approveVisible.value = false;
}

async function confirmReject() {
  if (!selectedPlan.value) return;
  if (!canApprovePlan.value) {
    ElMessage.warning(t('agency.noPermissionApprove'));
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "REJECT",
      actor: "system",
    });
    if (res.success) {
      ElMessage.success(t('agency.successRejected'));
      await loadPlans();
    } else {
      ElMessage.error(res.message || t('agency.errorCannotReject'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
  approveVisible.value = false;
}

async function confirmStopApply() {
  if (!selectedPlan.value) return;
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionStopApply'));
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "STOP",
      actor: "system",
      note: t('agency.stopNote'),
    });
    if (res.success) {
      ElMessage.success(t('agency.successStopped'));
      await loadPlans();
    } else {
      ElMessage.error(res.message || t('agency.errorCannotStop'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
  stopApplyVisible.value = false;
}

async function confirmDisable() {
  if (!selectedPlan.value) return;
  if (!can("group:update")) {
    ElMessage.warning(t('agency.noPermissionDisable'));
    return;
  }
  try {
    const res = await reviewAssignment(selectedPlan.value.id, {
      decision: "STOP",
      actor: "system",
      note: t('agency.disableNote'),
    });
    if (res.success) {
      ElMessage.success(t('agency.successDisabled'));
      await loadPlans();
    } else {
      ElMessage.error(res.message || t('agency.errorCannotDisable'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
  }
  disableVisible.value = false;
}

async function confirmExport() {
  if (!exportMonth.value) return;
  exporting.value = true;
  try {
    const blob = await downloadSettlementExport({
      groupId: agencyId,
      month: exportMonth.value,
    });
    triggerBlobDownload(
      blob,
      `doi-soat-${agency.value.groupCode}-${exportMonth.value}.xlsx`,
    );
    exportVisible.value = false;
  } catch {
    ElMessage.error(t('agency.errorExport'));
  } finally {
    exporting.value = false;
  }
}

async function confirmActivate() {
  try {
    const res = await activateGroup(agencyId);
    if (res.success) {
      agency.value.status = "ACTIVE";
      ElMessage.success(t('agency.successActivated'));
    } else {
      ElMessage.error(res.message || t('agency.errorCannotActivate'));
    }
  } catch {
    ElMessage.error(t('agency.errorServer'));
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
      ElMessage.success(t('agency.successEditAgency'));
      editVisible.value = false;
    } else {
      ElMessage.error(res.message || t('agency.errorEditAgency'));
    }
  } catch {
    ElMessage.error(t('agency.errorOccurred'));
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
