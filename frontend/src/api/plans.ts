import request from '@/utils/request'
import type { ApiResponse, Plan, CreatePlanRequest } from '@/types'

export const getPlans = () =>
  request.get<any, ApiResponse<Plan[]>>('/api/v1/plans')

export const getPlan = (code: string) =>
  request.get<any, ApiResponse<Plan>>(`/api/v1/plans/${code}`)

export const createPlan = (data: CreatePlanRequest) =>
  request.post<any, ApiResponse<Plan>>('/api/v1/plans', data)

export const updatePlan = (code: string, data: Partial<CreatePlanRequest>) =>
  request.put<any, ApiResponse<Plan>>(`/api/v1/plans/${code}`, data)

export const deactivatePlan = (code: string) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/plans/${code}/deactivate`)
