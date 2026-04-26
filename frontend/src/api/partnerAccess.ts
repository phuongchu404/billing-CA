import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type { PartnerGroupAccess } from '@/types/group'

export const grantPartnerAccess = (partnerUserId: string, groupId: number) =>
  request.post<any, ApiResponse<PartnerGroupAccess>>('/api/v1/partner-access', { partnerUserId, groupId })

export const revokePartnerAccess = (accessId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/partner-access/${accessId}`)

export const revokePartnerAccessByGroup = (partnerUserId: string, groupId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/partner-access/partner/${partnerUserId}/group/${groupId}`)

export const getPartnerActiveAccess = (partnerUserId: string) =>
  request.get<any, ApiResponse<PartnerGroupAccess[]>>(`/api/v1/partner-access/partner/${partnerUserId}`)

export const getPartnerAccessHistory = (partnerUserId: string) =>
  request.get<any, ApiResponse<PartnerGroupAccess[]>>(`/api/v1/partner-access/partner/${partnerUserId}/history`)
