import request from '@/utils/request'

// ─── Types ────────────────────────────────────────────────────────────────────

export type MultiApprovalStatus = 'DRAFT' | 'IN_APPROVAL' | 'NEED_REVISION' | 'APPROVED' | 'REJECTED'
export type ApprovalStepStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SKIPPED'
export type CustomerSegment = 'INDIVIDUAL' | 'GROUP'

export interface ApprovalStepResponse {
  id: number
  stepLevel: number
  requiredApprovalLevel: 'LEVEL_1' | 'LEVEL_2' | 'LEVEL_3'
  status: ApprovalStepStatus
  decidedBy?: string
  comment?: string
  decidedAt?: string
  createdAt: string
}

export interface MultiLevelApprovalResponse {
  id: number
  requestType: string
  customerSegment: CustomerSegment
  status: MultiApprovalStatus
  requestedBy: string
  entityType: string
  entityId: string
  description: string
  contractValue?: number
  totalLevels: number
  currentLevel: number
  payload?: Record<string, unknown>
  steps: ApprovalStepResponse[]
  createdAt: string
  updatedAt: string
}

export interface ApprovalLevelConfigResponse {
  id: number
  customerSegment: CustomerSegment
  minValue?: number
  maxValue?: number
  requiredLevels: number
  description?: string
  isActive: boolean
}

export interface SubmitApprovalRequest {
  submittedBy: string
  contractValue?: number
}

export interface ApproveStepRequest {
  approvedBy: string
  comment?: string
}

export interface RejectApprovalRequest {
  rejectedBy: string
  reason: string
}

export interface RevisionApprovalRequest {
  requestedBy: string
  reason: string
}

// ─── API calls ────────────────────────────────────────────────────────────────

// GET /api/v1/approval-requests
export const listApprovals = () =>
  request.get<MultiLevelApprovalResponse[]>('/api/v1/approval-requests')

// GET /api/v1/approval-requests/{id}
export const getApproval = (id: number) =>
  request.get<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}`)

// POST /api/v1/approval-requests/{id}/submit  — Sale submit DRAFT → IN_APPROVAL
export const submitApproval = (id: number, data: SubmitApprovalRequest) =>
  request.post<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}/submit`, data)

// POST /api/v1/approval-requests/{id}/approve — Approver duyệt step hiện tại
export const approveStep = (id: number, data: ApproveStepRequest) =>
  request.post<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}/approve`, data)

// POST /api/v1/approval-requests/{id}/reject — Từ chối
export const rejectRequest = (id: number, data: RejectApprovalRequest) =>
  request.post<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}/reject`, data)

// POST /api/v1/approval-requests/{id}/revision — Yêu cầu sửa
export const requestRevision = (id: number, data: RevisionApprovalRequest) =>
  request.post<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}/revision`, data)

// POST /api/v1/approval-requests/{id}/resubmit — Sale resubmit sau khi sửa
export const resubmitApproval = (id: number, data: SubmitApprovalRequest) =>
  request.post<MultiLevelApprovalResponse>(`/api/v1/approval-requests/${id}/resubmit`, data)

// GET /api/v1/approval-requests/level-configs
export const listLevelConfigs = () =>
  request.get<ApprovalLevelConfigResponse[]>('/api/v1/approval-requests/level-configs')
