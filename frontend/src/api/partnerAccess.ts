import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type { PartnerGroupAccess } from '@/types/group'

export const grantPartnerAccess = (partnerUserId: number, groupId: number) =>
  request.post<any, ApiResponse<PartnerGroupAccess>>('/api/v1/partner-access', { partnerUserId, groupId })

export const revokePartnerAccess = (accessId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/partner-access/${accessId}`)

export const revokePartnerAccessByGroup = (partnerUserId: number, groupId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/partner-access/partner/${partnerUserId}/group/${groupId}`)

export const getPartnerActiveAccess = (partnerUserId: number) =>
  request.get<any, ApiResponse<PartnerGroupAccess[]>>(`/api/v1/partner-access/partner/${partnerUserId}`)

export const getPartnerAccessHistory = (partnerUserId: number) =>
  request.get<any, ApiResponse<PartnerGroupAccess[]>>(`/api/v1/partner-access/partner/${partnerUserId}/history`)


