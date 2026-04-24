import request from '@/utils/request'

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'DENIED'
export type RequestType = 'CREATE_PLAN' | 'ASSIGN_GROUP_PLAN' | 'CANCEL_SUBSCRIPTION' | 'SUSPEND_SUBSCRIPTION'

export interface ApprovalResponse {
  id: number
  requestType: RequestType
  status: ApprovalStatus
  requestedBy: string
  description: string
  payload: Record<string, unknown>
  reviewedBy?: string
  reviewNote?: string
  reviewedAt?: string
  createdAt: string
  updatedAt: string
}

export interface SubmitApprovalRequest {
  requestType: string
  description: string
  payload: Record<string, unknown>
}

export interface ReviewRequest {
  note?: string
}

export const listApprovals = (params?: { status?: string; page?: number; size?: number }) =>
  request.get('/api/v1/approvals', { params })

export const getApproval = (id: number) =>
  request.get(`/api/v1/approvals/${id}`)

export const submitApproval = (data: SubmitApprovalRequest) =>
  request.post('/api/v1/approvals', data)

export const approveRequest = (id: number, data: ReviewRequest) =>
  request.post(`/api/v1/approvals/${id}/approve`, data)

export const denyRequest = (id: number, data: ReviewRequest) =>
  request.post(`/api/v1/approvals/${id}/deny`, data)
