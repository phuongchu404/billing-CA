<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text :icon="ArrowLeft" @click="router.back()">{{ t('common.back') }}</el-button>
      <h2>{{ partner?.groupName || '...' }}</h2>
      <el-tag :type="(partner?.status === 'ACTIVE' ? 'success' : 'danger') as any" size="small">{{ partner?.status }}</el-tag>
      <div style="margin-left:auto;display:flex;gap:8px">
        <template v-if="!editing">
          <el-popconfirm
            :title="partner?.status === 'ACTIVE' ? t('partners.deactivateConfirm') : t('partners.activateConfirm')"
            @confirm="toggleStatus"
          >
            <template #reference>
              <el-button
                :type="partner?.status === 'ACTIVE' ? 'danger' : 'success'"
                :loading="toggling"
              >
                {{ partner?.status === 'ACTIVE' ? t('common.deactivate') : t('common.activate') }}
              </el-button>
            </template>
          </el-popconfirm>
          <el-button :icon="EditIcon" @click="startEdit">{{ t('common.edit') }}</el-button>
        </template>
        <template v-else>
          <el-button @click="cancelEdit">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="saving" @click="doSave">{{ t('common.save') }}</el-button>
        </template>
      </div>
    </div>

    <!-- Partner Info -->
    <el-card shadow="never" :header="t('partners.info')">
      <!-- Read-only view -->
      <el-descriptions v-if="!editing" :column="2" size="small" border>
        <el-descriptions-item :label="t('partners.partnerCode')">{{ partner?.groupCode }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.partnerName')">{{ partner?.groupName }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.username')">{{ partner?.username }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.refContractNo')">{{ partner?.refContractNo || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.contactEmail')">{{ partner?.contactEmail }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.contactPhone')">{{ partner?.contactPhone || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.picEmails')">{{ partner?.picEmails || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('partners.members')">{{ partner?.memberCount }}</el-descriptions-item>
      </el-descriptions>

      <!-- Edit form -->
      <el-form v-else ref="partnerFormRef" :model="editForm" :rules="editRules" label-width="140px">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item :label="t('partners.partnerCode')">
              <el-input :model-value="partner?.groupCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.partnerName')" prop="groupName">
              <el-input v-model="editForm.groupName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.username')" prop="username">
              <el-input v-model="editForm.username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.newPassword')" prop="password">
              <el-input v-model="editForm.password" type="password" show-password :placeholder="t('partners.keepCurrentPassword')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.contactEmail')" prop="contactEmail">
              <el-input v-model="editForm.contactEmail" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.contactPhone')" prop="contactPhone">
              <el-input v-model="editForm.contactPhone" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.refContractNo')" prop="refContractNo">
              <el-input v-model="editForm.refContractNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('partners.picEmails')" prop="picEmails">
              <el-input v-model="editForm.picEmails" type="textarea" :rows="2" :placeholder="t('partners.multipleHint')" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Subscriptions / Plans -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>{{ t('partners.plans') }}</span>
          <el-button type="primary" size="small" :icon="Plus" :disabled="!editing" @click="openPlanDialog">
            {{ t('partners.addPlan') }}
          </el-button>
        </div>
      </template>

      <el-empty v-if="subscriptions.length === 0 && stagingPlans.length === 0" :description="t('partners.noPlansAdded')" :image-size="60" />

      <!-- Existing subscriptions -->
      <el-table v-if="subscriptions.length > 0" :data="pagedSubscriptions" stripe style="width:100%">
        <el-table-column type="index" label="#" width="50" :index="(i: number) => (subsPage - 1) * subsPageSize + i + 1" />
        <el-table-column prop="planCode" :label="t('plans.code')" width="140" />
        <el-table-column prop="planName" :label="t('plans.name')" min-width="120" />
        <el-table-column :label="t('common.status')" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.quota')" width="140">
          <template #default="{ row }">
            <span v-if="row.signingQuotaTotal">{{ row.signingQuotaUsed }} / {{ row.signingQuotaTotal }}</span>
            <span v-else style="color:#bbb">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.effectiveFrom')" width="110">
          <template #default="{ row }">
            <span v-if="row.planEffectiveFrom">{{ row.planEffectiveFrom }}</span>
            <span v-else style="color:#bbb">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('plans.effectiveTo')" width="110">
          <template #default="{ row }">
            <span v-if="row.planEffectiveTo">{{ row.planEffectiveTo }}</span>
            <span v-else style="color:#bbb">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('subscriptions.updatedAt')" width="165">
          <template #default="{ row }">
            <span v-if="row.updatedAt">{{ row.updatedAt.slice(0, 16) }}</span>
            <span v-else style="color:#bbb">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="130" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <!-- Detail -->
              <el-tooltip :content="t('subscriptions.detail')" placement="top">
                <el-button size="small" :icon="InfoFilled" @click="openDetail(row)" />
              </el-tooltip>
              <!-- Pause / Resume -->
              <el-popconfirm v-if="row.status === 'ACTIVE'" :title="t('subscriptions.pauseConfirm')" :disabled="!editing" @confirm="doPause(row.subscriptionId)">
                <template #reference>
                  <el-button size="small" type="warning" :icon="VideoPause" :disabled="!editing" :title="t('subscriptions.pause')" />
                </template>
              </el-popconfirm>
              <el-popconfirm v-else-if="row.status === 'SUSPENDED'" :title="t('subscriptions.resumeConfirm')" :disabled="!editing" @confirm="doResume(row.subscriptionId)">
                <template #reference>
                  <el-button size="small" type="success" :icon="VideoPlay" :disabled="!editing" :title="t('subscriptions.resume')" />
                </template>
              </el-popconfirm>
              <!-- Cancel -->
              <el-popconfirm v-if="row.status === 'ACTIVE' || row.status === 'PENDING'" :title="t('subscriptions.cancelConfirm')" :disabled="!editing" @confirm="doCancel(row.subscriptionId)">
                <template #reference>
                  <el-button size="small" type="danger" :icon="CircleClose" :disabled="!editing" :title="t('subscriptions.cancel')" />
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="subscriptions.length > subsPageSize" class="pagination-bar">
        <el-pagination
          v-model:current-page="subsPage"
          v-model:page-size="subsPageSize"
          :total="subscriptions.length"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          small
        />
      </div>

      <!-- Staged (pending) plans — shown only in edit mode -->
      <template v-if="stagingPlans.length > 0">
        <div class="staging-header">
          <el-icon style="color:#e6a23c"><Warning /></el-icon>
          <span>{{ t('partners.pendingPlans') }}</span>
        </div>
        <el-table :data="stagingPlans" stripe style="width:100%" row-class-name="staging-row">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="planCode" :label="t('plans.code')" width="140" />
          <el-table-column prop="planName" :label="t('plans.name')" min-width="160" />
          <el-table-column :label="t('plans.type')" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="row.planType === 'VALIDITY_PERIOD' ? 'info' : 'primary'">
                {{ row.planType === 'VALIDITY_PERIOD' ? t('plans.byDate') : t('plans.perUsage') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('plans.price')" width="130">
            <template #default="{ row }">{{ row.price.toLocaleString() }} {{ row.currency }}</template>
          </el-table-column>
          <el-table-column :label="t('plans.validityDays')" width="110">
            <template #default="{ row }">{{ formatStagedValidity(row) }}</template>
          </el-table-column>
          <el-table-column :label="t('common.actions')" width="90" fixed="right">
            <template #default="{ $index }">
              <el-button size="small" type="danger" :icon="Delete" @click="removeStagedPlan($index)" />
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- Plan Action History -->
    <el-card shadow="never">
      <template #header>
        <span>{{ t('partners.planActions') }}</span>
      </template>
      <el-empty v-if="!loadingActions && planActions.length === 0" :description="t('partners.noPlanActions')" :image-size="60" />
      <el-table v-else :data="pagedPlanActions" v-loading="loadingActions" stripe style="width:100%">
        <el-table-column type="index" label="#" width="50" :index="(i: number) => (actionsPage - 1) * actionsPageSize + i + 1" />
        <el-table-column :label="t('plans.code')" width="140">
          <template #default="{ row }">
            <span style="font-weight:500">{{ row.planCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="planName" :label="t('plans.name')" min-width="150" />
        <el-table-column :label="t('partners.planAction')" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="actionTagType(row.action)">{{ t('partners.action_' + row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('partners.statusChange')" width="180">
          <template #default="{ row }">
            <span style="color:#bbb;font-size:12px" v-if="!row.oldStatus">— → {{ row.newStatus }}</span>
            <span style="font-size:12px" v-else>{{ row.oldStatus }} → {{ row.newStatus }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actor" :label="t('subscriptions.actor')" width="130" />
        <el-table-column prop="reason" :label="t('partners.actionReason')" min-width="160">
          <template #default="{ row }">{{ row.reason || '—' }}</template>
        </el-table-column>
        <el-table-column :label="t('auditLogs.createdAt')" width="150">
          <template #default="{ row }">{{ row.createdAt?.slice(0, 16) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="planActions.length > actionsPageSize" class="pagination-bar">
        <el-pagination
          v-model:current-page="actionsPage"
          v-model:page-size="actionsPageSize"
          :total="planActions.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          small
        />
      </div>
    </el-card>

    <!-- Members (temporarily hidden) -->
    <el-card v-if="false" shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>{{ t('partners.membersTitle') }}</span>
          <el-button type="primary" size="small" :icon="Plus" :disabled="!editing" @click="addMemberVisible = true">
            {{ t('partners.addMember') }}
          </el-button>
        </div>
      </template>
      <el-table :data="members" v-loading="loadingMembers" stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="userId" :label="t('partners.userId')" min-width="200" />
        <el-table-column :label="t('partners.role')" width="110">
          <template #default="{ row }">
            <el-tag :type="(row.role === 'OPERATOR' ? 'warning' : 'info') as any" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="addedBy" :label="t('partners.addedBy')" width="160" />
        <el-table-column :label="t('partners.joined')" width="160">
          <template #default="{ row }">{{ row.joinedAt?.slice(0, 16) }}</template>
        </el-table-column>
        <el-table-column :label="t('partners.memberValidity')" width="200">
          <template #default="{ row }">
            <span v-if="row.memberStartDate">{{ row.memberStartDate }} → {{ row.memberEndDate }}</span>
            <span v-else style="color:#999">{{ t('partners.followsPartner') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="90" fixed="right">
          <template #default="{ row }">
            <el-popconfirm :title="t('partners.removeMemberConfirm')" @confirm="doRemoveMember(row)">
              <template #reference>
                <el-button size="small" type="danger">{{ t('common.remove') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Subscription Detail Drawer -->
    <el-drawer v-model="detailDialogVisible" :title="t('subscriptions.detailTitle')" size="400px" direction="rtl">
      <el-descriptions v-if="selectedSubscription" :column="1" size="small" border>
        <el-descriptions-item :label="t('plans.code')">{{ selectedSubscription.planCode }}</el-descriptions-item>
        <el-descriptions-item :label="t('plans.name')">{{ selectedSubscription.planName }}</el-descriptions-item>
        <el-descriptions-item :label="t('plans.expiryType')">
          <el-tag size="small" type="info">
            {{ selectedSubscription.planType === 'VALIDITY_PERIOD' ? t('plans.byDate') : selectedSubscription.planType === 'COMBINED' ? t('plans.perUsage') : '—' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('plans.validityDays')">{{ formatSubscriptionValidity(selectedSubscription) }}</el-descriptions-item>
        <el-descriptions-item :label="t('plans.effectiveFrom')">{{ selectedSubscription.planEffectiveFrom || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('plans.effectiveTo')">{{ selectedSubscription.planEffectiveTo || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag size="small" :type="statusTagType(selectedSubscription.status)">{{ selectedSubscription.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('subscriptions.validity')">
          <span v-if="selectedSubscription.startDate">{{ selectedSubscription.startDate }} → {{ selectedSubscription.endDate }}</span>
          <span v-else style="color:#bbb">—</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('plans.quota')">
          <span v-if="selectedSubscription.signingQuotaTotal">{{ selectedSubscription.signingQuotaUsed }} / {{ selectedSubscription.signingQuotaTotal }}</span>
          <span v-else style="color:#bbb">—</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('subscriptions.subscriberType')">{{ selectedSubscription.subscriberType }}</el-descriptions-item>
        <el-descriptions-item :label="t('subscriptions.activatedBy')">{{ selectedSubscription.activatedBy || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('subscriptions.paymentRef')">{{ selectedSubscription.paymentReference || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('auditLogs.createdAt')">{{ selectedSubscription.createdAt?.slice(0, 16) }}</el-descriptions-item>
        <el-descriptions-item :label="t('subscriptions.updatedAt')">{{ selectedSubscription.updatedAt?.slice(0, 16) }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- Add Plan Dialog -->
    <el-dialog v-model="planDialogVisible" :title="t('partners.addPlan')" width="520px" :close-on-click-modal="false">
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="140px">
        <el-form-item :label="t('plans.code')" prop="planCode">
          <el-input v-model="planForm.planCode" placeholder="e.g. RS-PTN-365" />
        </el-form-item>
        <el-form-item :label="t('plans.name')" prop="planName">
          <el-input v-model="planForm.planName" />
        </el-form-item>
        <el-form-item :label="t('plans.expiryType')" prop="planType">
          <el-radio-group v-model="planForm.planType">
            <el-radio value="VALIDITY_PERIOD">{{ t('plans.byDate') }}</el-radio>
            <el-radio value="COMBINED">{{ t('plans.perUsage') }}</el-radio>
          </el-radio-group>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            <template v-if="planForm.planType === 'VALIDITY_PERIOD'">{{ t('plans.expiryDescByDate') }}</template>
            <template v-else-if="planForm.isGroupPlan">{{ t('plans.expiryDescPartnerCombined') }}</template>
            <template v-else>{{ t('plans.expiryDescCombined') }}</template>
          </div>
        </el-form-item>
        <el-form-item :label="t('plans.price')" prop="price">
          <el-input-number v-model="planForm.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.currency')" prop="currency">
          <el-select v-model="planForm.currency" style="width:100%">
            <el-option label="VND" value="VND" />
            <el-option label="USD" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('plans.validityDays')" prop="validityDays">
          <div style="display:flex;flex-direction:column;gap:6px;width:100%">
            <div style="display:flex;gap:6px">
              <el-button size="small" round :type="planValidityUnit === 'DAYS' ? 'primary' : ''" @click="setPlanValidityUnit('DAYS')">{{ t('plans.unitDays') }}</el-button>
              <el-button size="small" round :type="planValidityUnit === 'MONTHS' ? 'primary' : ''" @click="setPlanValidityUnit('MONTHS')">{{ t('plans.unitMonths') }}</el-button>
              <el-button size="small" round :type="planValidityUnit === 'YEARS' ? 'primary' : ''" @click="setPlanValidityUnit('YEARS')">{{ t('plans.unitYears') }}</el-button>
            </div>
            <div style="display:flex;gap:8px;align-items:center">
              <el-input-number v-model="planValidityAmount" :min="1" style="width:140px" />
              <span v-if="planValidityUnit !== 'DAYS'" style="font-size:12px;color:#909399;white-space:nowrap">= {{ planForm.validityDays }} {{ t('plans.unitDays').toLowerCase() }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="planForm.isGroupPlan" :label="t('plans.maxUsers')">
          <el-input-number v-model="planForm.maxMembers" :min="1" :value-on-clear="null" controls-position="right" style="width:160px" />
          <span style="font-size:12px;color:#909399;margin-left:8px">{{ t('plans.unlimitedHint') }}</span>
        </el-form-item>
        <el-form-item v-if="planForm.planType === 'COMBINED'" :label="t('plans.maxUsageCount')" prop="maxSigningQuota">
          <el-input-number v-model="planForm.maxSigningQuota" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.partnerPlan')">
          <el-switch v-model="planForm.isGroupPlan" disabled />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveFrom')">
          <el-date-picker v-model="planForm.effectiveFrom" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.effectiveTo')">
          <el-date-picker v-model="planForm.effectiveTo" type="date" value-format="YYYY-MM-DD" clearable style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('plans.isVisible')">
          <el-switch v-model="planForm.isVisible" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="doAddPlan">{{ t('common.add') }}</el-button>
      </template>
    </el-dialog>

    <!-- Add Member Dialog -->
    <el-dialog v-model="addMemberVisible" :title="t('partners.addMember')" width="400px">
      <el-form :model="memberForm" label-width="100px">
        <el-form-item :label="t('partners.userId')">
          <el-input v-model="memberForm.userId" :placeholder="t('partners.rsUserId')" />
        </el-form-item>
        <el-form-item :label="t('partners.role')">
          <el-select v-model="memberForm.role" style="width:100%">
            <el-option label="Member" value="MEMBER" />
            <el-option label="Operator" value="OPERATOR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="doAddMember">{{ t('common.add') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus, Edit as EditIcon, Delete, Warning, InfoFilled, VideoPause, VideoPlay, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getPartner, getPartnerMembers, addPartnerMember, removePartnerMember,
  getPartnerSubscriptions, getPartnerPlanActions, updatePartner, deactivatePartner, activatePartner,
} from '@/api/groups'
import { assignGroupPlan, cancelSubscription, suspendSubscription, reactivateSubscription } from '@/api/subscriptions'
import { createPlan } from '@/api/plans'
import type { Partner, PartnerMember, Subscription, PartnerPlanAction, CreatePlanRequest, PlanType } from '@/types'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const partnerId = Number(route.params.id)

const partner = ref<Partner | null>(null)
const members = ref<PartnerMember[]>([])
const subscriptions = ref<Subscription[]>([])
const loadingMembers = ref(false)
const editing = ref(false)
const saving = ref(false)
const toggling = ref(false)
const planDialogVisible = ref(false)
const stagingPlans = ref<CreatePlanRequest[]>([])
const addMemberVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedSubscription = ref<Subscription | null>(null)
const planActions = ref<PartnerPlanAction[]>([])
const loadingActions = ref(false)
const partnerFormRef = ref<FormInstance>()
const planFormRef = ref<FormInstance>()

// Pagination — Plans table
const subsPage = ref(1)
const subsPageSize = ref(5)
const pagedSubscriptions = computed(() => {
  const start = (subsPage.value - 1) * subsPageSize.value
  return subscriptions.value.slice(start, start + subsPageSize.value)
})

// Pagination — Plan Action History table
const actionsPage = ref(1)
const actionsPageSize = ref(10)
const pagedPlanActions = computed(() => {
  const start = (actionsPage.value - 1) * actionsPageSize.value
  return planActions.value.slice(start, start + actionsPageSize.value)
})

const editForm = reactive({
  groupName: '', username: '', password: '',
  contactEmail: '', contactPhone: '',
  refContractNo: '', picEmails: '',
})

const editRules = computed(() => ({
  groupName:    [{ required: true, message: t('common.required') }],
  username:     [{ required: true, message: t('common.required') }],
  contactEmail: [{ required: true, message: t('common.required') }],
}))

const memberForm = reactive({ userId: '', role: 'MEMBER' })

// ── Plan dialog form ──────────────────────────────────────────
const planValidityAmount = ref(15)
const planValidityUnit = ref<'DAYS' | 'MONTHS' | 'YEARS'>('DAYS')
const planUnitValues = reactive<Record<string, number>>({ DAYS: 15, MONTHS: 3, YEARS: 1 })
const PLAN_UNIT_MULTIPLIER: Record<string, number> = { DAYS: 1, MONTHS: 30, YEARS: 365 }

function setPlanValidityUnit(unit: 'DAYS' | 'MONTHS' | 'YEARS') {
  planUnitValues[planValidityUnit.value] = planValidityAmount.value
  planValidityUnit.value = unit
  planValidityAmount.value = planUnitValues[unit]
}

const BLANK_PLAN = (): CreatePlanRequest => ({
  planCode: '', planName: '', price: 0, currency: 'VND',
  planType: 'VALIDITY_PERIOD' as PlanType,
  validityDays: 15,
  validityAmount: 15,
  validityUnit: 'DAYS',
  maxSigningQuota: null,
  maxMembers: null,
  isGroupPlan: true,
  effectiveFrom: null, effectiveTo: null,
  isVisible: false,
})

const planForm = reactive<CreatePlanRequest>(BLANK_PLAN())

watchEffect(() => {
  planForm.validityDays = (planValidityAmount.value || 1) * PLAN_UNIT_MULTIPLIER[planValidityUnit.value]
  planForm.validityAmount = planValidityAmount.value
  planForm.validityUnit = planValidityUnit.value
})

function formatSubscriptionValidity(sub: Subscription): string {
  if (!sub.planValidityDays) return '—'
  if (sub.planValidityUnit && sub.planValidityAmount) {
    const label = sub.planValidityUnit === 'DAYS' ? t('plans.unitDays')
      : sub.planValidityUnit === 'MONTHS' ? t('plans.unitMonths')
      : t('plans.unitYears')
    return `${sub.planValidityAmount} ${label}`
  }
  return `${sub.planValidityDays} ${t('plans.unitDays')}`
}

function formatStagedValidity(row: CreatePlanRequest): string {
  if (!row.validityDays) return '—'
  if (row.validityUnit && row.validityAmount) {
    const label = row.validityUnit === 'DAYS' ? t('plans.unitDays')
      : row.validityUnit === 'MONTHS' ? t('plans.unitMonths')
      : t('plans.unitYears')
    return `${row.validityAmount} ${label}`
  }
  return `${row.validityDays} ${t('plans.unitDays')}`
}

const planRules = computed(() => ({
  planCode:  [{ required: true, message: t('common.required') }],
  planName:  [{ required: true, message: t('common.required') }],
  price:     [{ required: true, message: t('common.required') }],
  currency:  [{ required: true, message: t('common.required') }],
  validityDays: [{ required: true, message: t('common.required') }],
  maxSigningQuota: [{ required: false, validator: (_: any, v: any, cb: any) => {
    if (planForm.planType === 'COMBINED' && (!v || v < 1)) cb(new Error(t('common.required')))
    else cb()
  }}],
}))

function actionTagType(action: string): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'primary'> = {
    ASSIGN: 'primary', INITIATE: 'primary',
    PAUSE: 'warning',
    RESUME: 'success',
    CANCEL: 'danger',
    EXPIRE: 'info',
  }
  return map[action] ?? 'info'
}

function statusTagType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    ACTIVE: 'success', PENDING: 'warning', SUSPENDED: 'warning',
    CANCELLED: 'danger', EXPIRED: 'info',
  }
  return map[status] ?? 'info'
}

async function loadPartner() {
  const res = await getPartner(partnerId)
  if (res.success && res.data) {
    partner.value = res.data
    Object.assign(editForm, {
      groupName: res.data.groupName,
      username: res.data.username,
      password: '',
      contactEmail: res.data.contactEmail,
      contactPhone: res.data.contactPhone ?? '',
      refContractNo: res.data.refContractNo ?? '',
      picEmails: res.data.picEmails ?? '',
    })
  }
}

async function loadMembers() {
  loadingMembers.value = true
  try {
    const res = await getPartnerMembers(partnerId)
    if (res.success) members.value = res.data || []
  } finally { loadingMembers.value = false }
}

async function loadSubscriptions() {
  try {
    const res = await getPartnerSubscriptions(partnerId)
    if (res.success) { subscriptions.value = res.data || []; subsPage.value = 1 }
  } catch { subscriptions.value = [] }
}

async function loadPlanActions() {
  loadingActions.value = true
  try {
    const res = await getPartnerPlanActions(partnerId)
    if (res.success) { planActions.value = res.data || []; actionsPage.value = 1 }
  } catch { planActions.value = [] }
  finally { loadingActions.value = false }
}

async function toggleStatus() {
  toggling.value = true
  try {
    if (partner.value?.status === 'ACTIVE') {
      await deactivatePartner(partnerId)
      ElMessage.success(t('partners.deactivatedMsg'))
    } else {
      await activatePartner(partnerId)
      ElMessage.success(t('partners.activatedMsg'))
    }
    await loadPartner()
  } catch (e: any) {
    ElMessage.error(e?.message)
  } finally {
    toggling.value = false
  }
}

function startEdit() {
  Object.assign(editForm, {
    groupName: partner.value?.groupName ?? '',
    username: partner.value?.username ?? '',
    password: '',
    contactEmail: partner.value?.contactEmail ?? '',
    contactPhone: partner.value?.contactPhone ?? '',
    refContractNo: partner.value?.refContractNo ?? '',
    picEmails: partner.value?.picEmails ?? '',
  })
  partnerFormRef.value?.clearValidate()
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  stagingPlans.value = []
  partnerFormRef.value?.clearValidate()
}

async function doSave() {
  if (!await partnerFormRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    const payload = {
      groupCode: partner.value?.groupCode,
      groupName: editForm.groupName,
      username: editForm.username,
      password: editForm.password.trim() || null,
      contactEmail: editForm.contactEmail,
      contactPhone: editForm.contactPhone,
      refContractNo: editForm.refContractNo || null,
      picEmails: editForm.picEmails || null,
    }
    const res = await updatePartner(partnerId, payload)
    if (!res.success) throw new Error(res.message)

    for (const plan of stagingPlans.value) {
      const planRes = await createPlan(plan)
      if (!planRes.success) throw new Error(planRes.message)
      await assignGroupPlan(partnerId, { planCode: plan.planCode })
    }

    ElMessage.success(t('partners.updatedMsg'))
    editing.value = false
    stagingPlans.value = []
    loadPartner()
    loadSubscriptions()
    loadPlanActions()
  } catch (e: any) {
    ElMessage.error(e?.message || t('partners.createFailed'))
  } finally { saving.value = false }
}

function openPlanDialog() {
  Object.assign(planUnitValues, { DAYS: 15, MONTHS: 3, YEARS: 1 })
  planValidityUnit.value = 'DAYS'
  planValidityAmount.value = 15
  Object.assign(planForm, BLANK_PLAN())
  planFormRef.value?.clearValidate()
  planDialogVisible.value = true
}

async function doAddPlan() {
  if (!await planFormRef.value?.validate().catch(() => false)) return

  const isDuplicate =
    stagingPlans.value.some(p => p.planCode === planForm.planCode) ||
    subscriptions.value.some(s => s.planCode === planForm.planCode)
  if (isDuplicate) {
    ElMessage.warning(t('partners.planCodeDuplicate'))
    return
  }

  stagingPlans.value.push({ ...planForm })
  planDialogVisible.value = false
}

function removeStagedPlan(index: number) {
  stagingPlans.value.splice(index, 1)
}

function openDetail(row: Subscription) {
  selectedSubscription.value = row
  detailDialogVisible.value = true
}

async function doCancel(subscriptionId: number) {
  try {
    await cancelSubscription(subscriptionId)
    ElMessage.success(t('subscriptions.cancelled'))
    loadSubscriptions()
    loadPlanActions()
  } catch (e: any) {
    ElMessage.error(e?.message)
  }
}

async function doPause(subscriptionId: number) {
  try {
    await suspendSubscription(subscriptionId)
    ElMessage.success(t('subscriptions.paused'))
    loadSubscriptions()
    loadPlanActions()
  } catch (e: any) {
    ElMessage.error(e?.message)
  }
}

async function doResume(subscriptionId: number) {
  try {
    await reactivateSubscription(subscriptionId)
    ElMessage.success(t('subscriptions.resumed'))
    loadSubscriptions()
    loadPlanActions()
  } catch (e: any) {
    ElMessage.error(e?.message)
  }
}

async function doAddMember() {
  try {
    await addPartnerMember(partnerId, memberForm)
    ElMessage.success(t('partners.memberAdded'))
    addMemberVisible.value = false
    memberForm.userId = ''
    memberForm.role = 'MEMBER'
    loadMembers()
    loadPartner()
  } catch (e: any) {
    ElMessage.error(e?.message)
  }
}

async function doRemoveMember(member: PartnerMember) {
  try {
    await removePartnerMember(partnerId, member.userId)
    ElMessage.success(t('partners.memberRemoved'))
    loadMembers()
    loadPartner()
  } catch (e: any) {
    ElMessage.error(e?.message)
  }
}

onMounted(() => { loadPartner(); loadMembers(); loadSubscriptions(); loadPlanActions() })
</script>

<style scoped>
.page-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-header h2 { margin: 0; }
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.staging-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}
:deep(.staging-row) {
  background-color: #fffbf0 !important;
}
.action-btns {
  display: flex;
  gap: 4px;
  align-items: center;
}
.action-btns :deep(.el-button) { margin: 0; }
.pagination-bar {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
