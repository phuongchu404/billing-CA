import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type { PlanTemplate, CreatePlanTemplateRequest } from '@/types/planTemplate'

export const listPlanTemplates = (segment?: string) =>
  request.get<any, ApiResponse<PlanTemplate[]>>('/api/v1/plan-templates', { params: segment ? { segment } : undefined })

export const getPlanTemplate = (id: number) =>
  request.get<any, ApiResponse<PlanTemplate>>(`/api/v1/plan-templates/${id}`)

export const createPlanTemplate = (data: CreatePlanTemplateRequest) =>
  request.post<any, ApiResponse<PlanTemplate>>('/api/v1/plan-templates', data)
