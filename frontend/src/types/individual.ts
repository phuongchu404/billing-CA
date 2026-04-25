export type IndividualPlanStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'PENDING' | 'APPROVED' | 'APPLYING'

export type IndividualSubjectType = 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'

export type IndividualCtsStatus = 'ACTIVE' | 'PENDING_ACTIVATE' | 'PENDING_APPROVE' | 'REVOKED' | 'EXPIRED'

export interface IndividualPlanConfigListItem {
  id: number
  name: string
  status: IndividualPlanStatus
  applyFrom: string | null
  applyUntil: string | null
  updatedAt: string
}

export interface IndividualPlanConfigSummary {
  list: IndividualPlanConfigListItem[]
  currentPlan: { name: string; applyUntil: string | null } | null
  nextPlan: { name: string; applyFrom: string | null } | null
  lastUpdated: string | null
}

export interface IndividualPricingRuleRow {
  id: number
  subject: IndividualSubjectType
  durationMonths: number
  condition: 'SIGNING_COUNT' | 'CERTIFICATE_COUNT'
  minValue: number
  maxValue: number | null
  fee: number
  sortOrder: number
}

export interface IndividualStatusHistoryRow {
  status: IndividualPlanStatus
  updatedAt: string
  updatedBy: string
}

export interface IndividualPlanConfigDetail {
  id: number
  name: string
  status: IndividualPlanStatus
  applyFrom: string | null
  applyUntil: string | null
  applyHistory: string | null
  createdBy: string | null
  createdAt: string | null
  updatedAt: string | null
  pricingRules: IndividualPricingRuleRow[]
  statusHistory: IndividualStatusHistoryRow[]
}

export interface IndividualPricingRuleRequest {
  subject: string
  durationMonths: number | undefined
  condition: string
  minValue: number | undefined
  maxValue: number | undefined
  fee: number | undefined
  sortOrder: number
}

export interface CreateIndividualPlanConfigRequest {
  name: string
  applyFrom?: string | null
  applyUntil?: string | null
  requestedBy?: string
  pricingRules: IndividualPricingRuleRequest[]
}

export interface IndividualRequestApplyRequest {
  applyFrom: string
  applyUntil: string
  requestedBy?: string
}

export interface IndividualApproveRequest {
  applyFrom?: string | null
  applyUntil?: string | null
  approvedBy?: string
}

// Usage Tracking

export interface IndividualUsageStats {
  accounts: number
  plansBought: number
  signings: number
  ctsIndividual: number
  ctsOrg: number
  ctsIndividualOfOrg: number
}

export interface IndividualUsageRow {
  id: number
  account: string
  purchasedAt: string
  ctsType: IndividualSubjectType
  ctsDuration: number
  ctsStatus: IndividualCtsStatus
  signings: number
  plan: string
  fee: number
}

export interface IndividualUsageTrackingResponse {
  stats: IndividualUsageStats
  list: IndividualUsageRow[]
  lastUpdated: string | null
}
