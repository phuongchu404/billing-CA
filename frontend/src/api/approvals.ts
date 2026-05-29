import request from '@/utils/request'
import type { ApprovalLevel, ApprovalStepStatus, CustomerSegment, MultiApprovalStatus } from '@/types/enums'
import type { ApiResponse, PagedResponse } from '@/types'
export type { CustomerSegment, MultiApprovalStatus } from '@/types/enums'

// ─── Types ────────────────────────────────────────────────────────────────────

export interface ApprovalStepResponse {
  id: number
  stepLevel: number
  requiredApprovalLevel: ApprovalLevel
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
  reviewNote?: string
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

export interface UpsertApprovalLevelConfigRequest {
  customerSegment: string
  minValue?: number | null
  maxValue?: number | null
  requiredLevels: 1 | 2 | 3
  description?: string
  isActive?: boolean
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
export const listApprovals = (params?: { status?: string; customerSegment?: string; page?: number; size?: number }) =>
  request.get<any, ApiResponse<PagedResponse<MultiLevelApprovalResponse>>>('/api/v1/approval-requests', { params })

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
  request.get<any, ApiResponse<ApprovalLevelConfigResponse[]>>('/api/v1/approval-requests/level-configs')

// POST /api/v1/approval-requests/level-configs
export const createLevelConfig = (data: UpsertApprovalLevelConfigRequest) =>
  request.post<any, ApiResponse<ApprovalLevelConfigResponse>>('/api/v1/approval-requests/level-configs', data)

// PUT /api/v1/approval-requests/level-configs/{id}
export const updateLevelConfig = (id: number, data: UpsertApprovalLevelConfigRequest) =>
  request.put<any, ApiResponse<ApprovalLevelConfigResponse>>(`/api/v1/approval-requests/level-configs/${id}`, data)

// DELETE /api/v1/approval-requests/level-configs/{id}
export const deleteLevelConfig = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/approval-requests/level-configs/${id}`)

// GET /api/v1/retail-plan-schedules/{id}
export const getRetailPlanSchedule = (id: number) =>
  request.get<any>(`/api/v1/retail-plan-schedules/${id}`)


