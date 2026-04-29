export const AuthProvider = { LOCAL: 'LOCAL', SSO: 'SSO' } as const
export type AuthProvider = typeof AuthProvider[keyof typeof AuthProvider]

export const UserStatus = { ACTIVE: 'ACTIVE', INACTIVE: 'INACTIVE', LOCKED: 'LOCKED' } as const
export type UserStatus = typeof UserStatus[keyof typeof UserStatus]

export const CustomerSegment = { GROUP: 'GROUP', INDIVIDUAL: 'INDIVIDUAL' } as const
export type CustomerSegment = typeof CustomerSegment[keyof typeof CustomerSegment]

export const TemplateScope = { PUBLIC: 'PUBLIC', PARTNER_PRIVATE: 'PARTNER_PRIVATE', SYSTEM: 'SYSTEM' } as const
export type TemplateScope = typeof TemplateScope[keyof typeof TemplateScope]

export const TemplateStatus = { DRAFT: 'DRAFT', AVAILABLE: 'AVAILABLE', INACTIVE: 'INACTIVE', ARCHIVED: 'ARCHIVED' } as const
export type TemplateStatus = typeof TemplateStatus[keyof typeof TemplateStatus]

export const SubjectType = { INDIVIDUAL: 'INDIVIDUAL', ORGANIZATION: 'ORGANIZATION', INDIVIDUAL_OF_ORG: 'INDIVIDUAL_OF_ORG' } as const
export type SubjectType = typeof SubjectType[keyof typeof SubjectType]

export const PricingMetric = { SIGNING_COUNT: 'SIGNING_COUNT', CERTIFICATE_COUNT: 'CERTIFICATE_COUNT' } as const
export type PricingMetric = typeof PricingMetric[keyof typeof PricingMetric]

export const GroupStatus = { ACTIVE: 'ACTIVE', INACTIVE: 'INACTIVE' } as const
export type GroupStatus = typeof GroupStatus[keyof typeof GroupStatus]

export const MemberRole = { OPERATOR: 'OPERATOR', MEMBER: 'MEMBER' } as const
export type MemberRole = typeof MemberRole[keyof typeof MemberRole]

export const AssignmentStatus = {
  AVAILABLE: 'AVAILABLE',
  REQUESTED: 'REQUESTED',
  APPROVED: 'APPROVED',
  ACTIVE: 'ACTIVE',
  REJECTED: 'REJECTED',
  STOPPED: 'STOPPED',
  EXPIRED: 'EXPIRED',
} as const
export type AssignmentStatus = typeof AssignmentStatus[keyof typeof AssignmentStatus]

export const ReviewDecision = { APPROVE: 'APPROVE', REJECT: 'REJECT', STOP: 'STOP', ACTIVATE: 'ACTIVATE' } as const
export type ReviewDecision = typeof ReviewDecision[keyof typeof ReviewDecision]

export const SubscriptionStatus = {
  PENDING: 'PENDING',
  ACTIVE: 'ACTIVE',
  EXPIRED: 'EXPIRED',
  CANCELLED: 'CANCELLED',
  SUSPENDED: 'SUSPENDED',
} as const
export type SubscriptionStatus = typeof SubscriptionStatus[keyof typeof SubscriptionStatus]

export const SubscriberType = { INDIVIDUAL: 'INDIVIDUAL', GROUP: 'GROUP' } as const
export type SubscriberType = typeof SubscriberType[keyof typeof SubscriberType]

export const ProvisioningStatus = { PENDING: 'PENDING', COMPLETED: 'COMPLETED', FAILED: 'FAILED', FAILED_PERMANENT: 'FAILED_PERMANENT' } as const
export type ProvisioningStatus = typeof ProvisioningStatus[keyof typeof ProvisioningStatus]

export const MultiApprovalStatus = {
  DRAFT: 'DRAFT',
  IN_APPROVAL: 'IN_APPROVAL',
  NEED_REVISION: 'NEED_REVISION',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const
export type MultiApprovalStatus = typeof MultiApprovalStatus[keyof typeof MultiApprovalStatus]

export const ApprovalStepStatus = { PENDING: 'PENDING', APPROVED: 'APPROVED', REJECTED: 'REJECTED', SKIPPED: 'SKIPPED' } as const
export type ApprovalStepStatus = typeof ApprovalStepStatus[keyof typeof ApprovalStepStatus]

export const ApprovalLevel = { LEVEL_1: 'LEVEL_1', LEVEL_2: 'LEVEL_2', LEVEL_3: 'LEVEL_3' } as const
export type ApprovalLevel = typeof ApprovalLevel[keyof typeof ApprovalLevel]

export const IndividualPlanStatus = { AVAILABLE: 'AVAILABLE', UNAVAILABLE: 'UNAVAILABLE', PENDING: 'PENDING', APPROVED: 'APPROVED', APPLYING: 'APPLYING' } as const
export type IndividualPlanStatus = typeof IndividualPlanStatus[keyof typeof IndividualPlanStatus]

export const IndividualCtsStatus = { ACTIVE: 'ACTIVE', PENDING_ACTIVATE: 'PENDING_ACTIVATE', PENDING_APPROVE: 'PENDING_APPROVE', REVOKED: 'REVOKED', EXPIRED: 'EXPIRED' } as const
export type IndividualCtsStatus = typeof IndividualCtsStatus[keyof typeof IndividualCtsStatus]
