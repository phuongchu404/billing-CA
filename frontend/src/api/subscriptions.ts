import request from '@/utils/request'
import type { ApiResponse, PagedResponse, Subscription } from '@/types'

export const getMySubscriptions = () =>
  request.get<any, ApiResponse<Subscription[]>>('/api/v1/subscriptions/me')

export const listSubscriptions = (params: { subscriberType?: string; status?: string; query?: string; page?: number; size?: number }) =>
  request.get<any, ApiResponse<PagedResponse<Subscription>>>('/api/v1/subscriptions', { params })

export const getSubscription = (id: number) =>
  request.get<any, ApiResponse<Subscription>>(`/api/v1/subscriptions/${id}`)

export const initiateIndividual = (data: { planCode: string; paymentReference?: string }) =>
  request.post<any, ApiResponse<Subscription>>('/api/v1/subscriptions/individual', data)

export const assignGroupPlan = (groupId: number, data: { planCode: string }) =>
  request.post<any, ApiResponse<Subscription>>(`/api/v1/subscriptions/group/${groupId}`, data)

export const cancelSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/subscriptions/${id}/cancel`)

export const suspendSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/subscriptions/${id}/suspend`)

export const reactivateSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/subscriptions/${id}/reactivate`)

export const approveSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/subscriptions/${id}/approve`)

export const getAuditLog = (id: number) =>
  request.get<any, ApiResponse<any[]>>(`/api/v1/subscriptions/${id}/audit`)
