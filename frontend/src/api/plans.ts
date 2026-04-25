// NOTE: file này đã được thay thế bởi planTemplates.ts
// Backend: PlanTemplateController @ /api/v1/plan-templates (public GET, plan:create/update/delete)
// Dùng planTemplates.ts thay thế cho file này
import request from '@/utils/request'
import type { ApiResponse, Plan, CreatePlanRequest } from '@/types'

export const getPlans = () =>
  request.get<any, ApiResponse<Plan[]>>('/api/v1/plan-templates')

export const getPlan = (id: number) =>
  request.get<any, ApiResponse<Plan>>(`/api/v1/plan-templates/${id}`)

export const createPlan = (data: CreatePlanRequest) =>
  request.post<any, ApiResponse<Plan>>('/api/v1/plan-templates', data)

export const updatePlan = (id: number, data: Partial<CreatePlanRequest>) =>
  request.put<any, ApiResponse<Plan>>(`/api/v1/plan-templates/${id}`, data)

export const deactivatePlan = (id: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/plan-templates/${id}`)
