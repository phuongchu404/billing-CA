/** Pricing rule trong một plan template */
export interface PlanPricingRule {
  planPricingRuleId?: number
  subjectType: 'INDIVIDUAL' | 'ORGANIZATION' | 'INDIVIDUAL_OF_ORG'
  certificateValidityValue: number
  certificateValidityUnit: 'DAY' | 'MONTH' | 'YEAR'
  pricingMetric: 'SIGNING_COUNT' | 'CERTIFICATE_COUNT'
  rangeMin: number
  rangeMax: number | null
  unitPrice: number
  totalPrice: number | null
  currency?: string
  quotaTotal?: number | null
  sortOrder?: number
  isActive?: boolean
}

/** Plan template response từ backend */
export interface PlanTemplate {
  planTemplateId: number
  planCode: string
  planName: string
  description?: string
  customerSegment: string
  templateScope: string
  status: string
  effectiveFrom?: string | null
  effectiveTo?: string | null
  isVisible: boolean
  allowBulkSigning: boolean
  allowApiAccess: boolean
  createdBy?: string
  clonedFromTemplateId?: number | null
  versionNo: number
  pricingRules: PlanPricingRule[]
  createdAt?: string
  updatedAt?: string
}

/** Request tạo plan template mới */
export interface CreatePlanTemplateRequest {
  planCode: string
  planName: string
  description?: string
  customerSegment: string
  templateScope: string
  status: string
  effectiveFrom?: string | null
  effectiveTo?: string | null
  isVisible: boolean
  allowBulkSigning: boolean
  allowApiAccess: boolean
  createdBy?: string
  pricingRules: PlanPricingRuleRequest[]
}

export interface PlanPricingRuleRequest {
  subjectType: string
  certificateValidityValue: number
  certificateValidityUnit: string
  pricingMetric: string
  rangeMin: number
  rangeMax: number | null
  unitPrice: number
  totalPrice: number | null
  currency: string
  quotaTotal?: number | null
  sortOrder: number
  isActive: boolean
}
