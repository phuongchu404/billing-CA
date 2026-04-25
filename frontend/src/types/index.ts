export interface ApiResponse<T = any> {
  code: string
  success: boolean
  message: string
  data?: T
  error?: any
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export type PlanType = 'VALIDITY_PERIOD' | 'COMBINED'

export interface Plan {
  planId: number
  planCode: string
  planType: PlanType
  planName: string
  price: number
  currency: string
  validityDays: number
  validityAmount?: number
  validityUnit?: 'DAYS' | 'MONTHS' | 'YEARS'
  maxSigningQuota: number
  maxMembers: number | null
  allowBulkSigning: boolean
  allowApiAccess: boolean
  isGroupPlan: boolean
  groupMemberValidityMode: 'GROUP_FOLLOWS' | 'INDIVIDUAL_START'
  effectiveFrom: string | null
  effectiveTo: string | null
  isVisible: boolean
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface CreatePlanRequest {
  planCode: string
  planType: PlanType
  planName: string
  price: number
  currency: string
  validityDays: number
  validityAmount?: number
  validityUnit?: 'DAYS' | 'MONTHS' | 'YEARS'
  maxSigningQuota: number | null
  maxMembers: number | null
  isGroupPlan: boolean
  effectiveFrom: string | null
  effectiveTo: string | null
  isVisible: boolean
}

export type SubscriptionStatus = 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'SUSPENDED'

export interface Subscription {
  subscriptionId: number
  subscriberType: 'INDIVIDUAL' | 'GROUP'
  userId?: string
  groupId?: number
  planCode: string
  planName: string
  status: SubscriptionStatus
  startDate?: string
  endDate?: string
  planEffectiveFrom?: string
  planEffectiveTo?: string
  planType?: string
  planValidityDays?: number
  planValidityAmount?: number
  planValidityUnit?: string
  signingQuotaTotal: number
  signingQuotaUsed: number
  activatedBy?: string
  paymentReference?: string
  featureFlags?: { allowBulkSigning: boolean; allowApiAccess: boolean }
  createdAt: string
  updatedAt: string
}

export interface Partner {
  groupId: number
  groupCode: string
  groupName: string
  username: string
  contactEmail: string
  contactPhone?: string
  refContractNo?: string
  picEmails?: string
  status: 'ACTIVE' | 'INACTIVE'
  createdBy: string
  createdAt: string
  memberCount: number
}

export interface PartnerMember {
  id: number
  groupId: number
  userId: string
  role: 'OPERATOR' | 'MEMBER'
  joinedAt: string
  addedBy: string
  memberStartDate?: string
  memberEndDate?: string
}

export interface Role {
  roleId: number
  roleName: string
  displayName: string
  description?: string
  isSystemRole: boolean
  createdAt: string
}

export interface Permission {
  permissionId: number
  permissionKey: string
  displayName: string
  moduleGroup: string
  groupName: string
  sortOrder: number
}

export interface PermissionGroup {
  groupName: string
  permissions: Permission[]
}

export interface PermissionModule {
  moduleName: string
  permissionGroups: PermissionGroup[]
}

export interface RolePermissionMatrix {
  roles: Role[]
  moduleGroups: PermissionModule[]
  rolePermissions: Record<number, number[]>
  roleUserCounts?: Record<number, number>
}

export interface UserAccount {
  userId: string
  username: string
  email: string
  fullName: string
  authProvider: 'LOCAL' | 'SSO'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  roles: Role[]
  permissions: string[]
  lastLoginAt?: string
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  refreshToken: string
}

export interface CertificateRecord {
  provisioningRecordId: number
  userId: string
  certType?: 1 | 2 | 3
  certificateId?: string
  keyId?: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'FAILED_PERMANENT'
  issuedAt?: string
  expiresAt?: string
  retryCount: number
  usageCount?: number
  failureReason?: string
  subscriptionId?: number
  planName?: string
  createdAt?: string
}

export interface PartnerPlanAction {
  id: number
  subscriptionId: number
  planCode: string
  planName: string
  action: 'ASSIGN' | 'INITIATE' | 'PAUSE' | 'RESUME' | 'CANCEL' | 'EXPIRE' | string
  actor: string
  oldStatus: string | null
  newStatus: string
  reason: string | null
  createdAt: string
}

export interface CertificateUsageEvent {
  id: number
  certificateId: string
  userId: string
  usedAt: string
}

export interface TypedSubscriptionSummary {
  active: number
  pending: number
  expired: number
  cancelled: number
  suspended: number
  newCertsThisMonth: number
  usageThisMonth: number
  expiringSoon: number
}

export interface SubscriptionSummary {
  totalActive: number
  totalExpired: number
  totalPending: number
  totalCancelled: number
  totalSuspended: number
  individualActive: number
  groupActive: number
}
