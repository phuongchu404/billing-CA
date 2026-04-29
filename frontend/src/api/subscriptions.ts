import request from '@/utils/request'
import type { ApiResponse, PagedResponse, Subscription } from '@/types'
import { SubscriptionStatus } from '@/types/enums'

// Backend: RuntimeSubscriptionController @ /api/v1/runtime-subscriptions
// @PreAuthorize: subscription:view (GET), subscription:create (POST), subscription:update (PATCH)
export const getMySubscriptions = () =>
  request.get<any, ApiResponse<Subscription[]>>('/api/v1/runtime-subscriptions')

export const listSubscriptions = (params: { subscriberType?: string; status?: string; query?: string; page?: number; size?: number }) =>
  request.get<any, ApiResponse<PagedResponse<Subscription>>>('/api/v1/runtime-subscriptions', { params })

export const getSubscription = (id: number) =>
  request.get<any, ApiResponse<Subscription>>(`/api/v1/runtime-subscriptions/${id}`)

export const initiateIndividual = (data: { planCode: string; paymentReference?: string }) =>
  request.post<any, ApiResponse<Subscription>>('/api/v1/runtime-subscriptions', data)

export const assignGroupPlan = (groupId: number, data: { planCode: string }) =>
  request.post<any, ApiResponse<Subscription>>(`/api/v1/runtime-subscriptions`, { ...data, groupId })

export const cancelSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/runtime-subscriptions/${id}/status`, { status: SubscriptionStatus.CANCELLED })

export const suspendSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/runtime-subscriptions/${id}/status`, { status: SubscriptionStatus.SUSPENDED })

export const reactivateSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/runtime-subscriptions/${id}/status`, { status: SubscriptionStatus.ACTIVE })

export const approveSubscription = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/runtime-subscriptions/${id}/status`, { status: 'APPROVED' })

export const getAuditLog = (id: number) =>
  request.get<any, ApiResponse<any[]>>(`/api/v1/audit-timelines/subscriptions/${id}`)
