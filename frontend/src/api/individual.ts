import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type {
  IndividualPlanConfigSummary,
  IndividualPlanConfigDetail,
  CreateIndividualPlanConfigRequest,
  IndividualRequestApplyRequest,
  IndividualApproveRequest,
  IndividualUsageTrackingResponse,
} from '@/types/individual'

const BASE = '/api/v1/individual'

// ─── Plan Config ────────────────────────────────────────────────────────────

export interface IndividualPlanConfigFilterParams {
  status?: string
  applyFrom?: string
  applyUntil?: string
  updatedAt?: string
}

export const getIndividualPlanConfigSummary = (params?: IndividualPlanConfigFilterParams) =>
  request.get<any, ApiResponse<IndividualPlanConfigSummary>>(`${BASE}/plan-configs`, { params })

export const getIndividualPlanConfigDetail = (id: number) =>
  request.get<any, ApiResponse<IndividualPlanConfigDetail>>(`${BASE}/plan-configs/${id}`)

export const createIndividualPlanConfig = (data: CreateIndividualPlanConfigRequest) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(`${BASE}/plan-configs`, data)

export const requestApplyPlanConfig = (id: number, data: IndividualRequestApplyRequest) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(`${BASE}/plan-configs/${id}/request-apply`, data)

export const approvePlanConfig = (id: number, data: IndividualApproveRequest) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(`${BASE}/plan-configs/${id}/approve`, data)

export const rejectPlanConfig = (id: number, actor?: string) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(
    `${BASE}/plan-configs/${id}/reject`,
    null,
    { params: { actor: actor ?? 'system' } }
  )

export const stopPlanConfig = (id: number, actor?: string) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(
    `${BASE}/plan-configs/${id}/stop`,
    null,
    { params: { actor: actor ?? 'system' } }
  )

export const deactivatePlanConfig = (id: number, actor?: string) =>
  request.post<any, ApiResponse<IndividualPlanConfigDetail>>(
    `${BASE}/plan-configs/${id}/deactivate`,
    null,
    { params: { actor: actor ?? 'system' } }
  )

// ─── Usage Tracking ─────────────────────────────────────────────────────────

export interface IndividualUsageFilterParams {
  purchasedAt?: string
  ctsType?: string
  ctsDuration?: string
  ctsStatus?: string
  plan?: string
}

export const getIndividualUsageTracking = (params?: IndividualUsageFilterParams) =>
  request.get<any, ApiResponse<IndividualUsageTrackingResponse>>(`${BASE}/usage-tracking`, { params })
