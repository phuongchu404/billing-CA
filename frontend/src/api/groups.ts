import request from '@/utils/request'
import type { ApiResponse, Partner, PartnerMember, Subscription, PartnerPlanAction } from '@/types'

export const listPartners = () =>
  request.get<any, ApiResponse<Partner[]>>('/api/v1/partners')

export const createPartner = (data: object) =>
  request.post<any, ApiResponse<Partner>>('/api/v1/partners', data)

export const getPartner = (id: number) =>
  request.get<any, ApiResponse<Partner>>(`/api/v1/partners/${id}`)

export const updatePartner = (id: number, data: object) =>
  request.put<any, ApiResponse<Partner>>(`/api/v1/partners/${id}`, data)

export const deactivatePartner = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/partners/${id}/deactivate`)

export const activatePartner = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/partners/${id}/activate`)

export const getPartnerMembers = (id: number) =>
  request.get<any, ApiResponse<PartnerMember[]>>(`/api/v1/partners/${id}/members`)

export const addPartnerMember = (partnerId: number, data: { userId: string; role?: string }) =>
  request.post<any, ApiResponse<PartnerMember>>(`/api/v1/partners/${partnerId}/members`, data)

export const removePartnerMember = (partnerId: number, userId: string) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/partners/${partnerId}/members/${userId}`)

export const getPartnerSubscription = (id: number) =>
  request.get<any, ApiResponse<Subscription>>(`/api/v1/partners/${id}/subscription`)

export const getPartnerSubscriptions = (id: number) =>
  request.get<any, ApiResponse<Subscription[]>>(`/api/v1/partners/${id}/subscriptions`)

export const getPartnerPlanActions = (id: number) =>
  request.get<any, ApiResponse<PartnerPlanAction[]>>(`/api/v1/partners/${id}/plan-actions`)
