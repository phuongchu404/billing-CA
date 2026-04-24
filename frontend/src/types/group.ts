/** Một dòng trong bảng danh sách đại lý (index.vue) */
export interface GroupListItem {
  groupId: number
  groupCode: string
  groupName: string
  status: 'ACTIVE' | 'INACTIVE'
  currentPlan: string | null
  applyUntil: string | null   // LocalDate → "YYYY-MM-DD"
  ctsCreated: number | null
  ctsCreatedPct: string | null
  signingUsed: number | null
  signingUsedPct: string | null
  updatedAt: string | null    // LocalDateTime → ISO string
}

/** Chi tiết đại lý (Detail.vue, AddPlan.vue) */
export interface GroupDetail {
  groupId: number
  groupCode: string
  groupName: string
  status: 'ACTIVE' | 'INACTIVE'
  picEmails: string[]
  contactEmails: string[]
  refContractNo: string | null
  createdBy: string | null
  createdAt: string | null
  updatedAt: string | null
}

/** Request tạo/cập nhật đại lý */
export interface UpsertGroupRequest {
  groupName: string
  picEmails: string[]
  contactEmails: string[]
  refContractNo?: string
}

/** Một dòng lịch sử áp dụng gói cước */
export interface PlanHistory {
  applyFrom: string | null   // "YYYY-MM-DD"
  applyTo: string | null
  planName: string
  ctsCreated: number
  ctsCreatedPct: string
  signingUsed: number
  signingUsedPct: string
}

/** Một gói cước đang quản lý của group (Detail.vue) */
export interface GroupPlanAssignment {
  groupPlanAssignmentId: number
  groupId: number
  planTemplateId: number
  planCode: string
  planName: string
  assignmentStatus: 'REQUESTED' | 'APPROVED' | 'ACTIVE' | 'REJECTED' | 'STOPPED' | 'EXPIRED'
  requestedBy: string | null
  requestedAt: string | null
  approvedBy: string | null
  approvedAt: string | null
  applyFrom: string | null
  applyTo: string | null
  activatedAt: string | null
  stoppedAt: string | null
}

/** Request tạo assignment mới */
export interface CreateGroupPlanAssignmentRequest {
  planTemplateId: number
  assignmentStatus?: string
  requestedBy: string
  applyFrom?: string | null
  applyTo?: string | null
}

/** Request review (approve/reject/stop) */
export interface ReviewAssignmentRequest {
  decision: 'APPROVE' | 'REJECT' | 'STOP' | 'ACTIVATE'
  actor: string
  note?: string
}
