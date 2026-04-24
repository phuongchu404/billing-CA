import request from '@/utils/request'
import type { ApiResponse, CertificateRecord, CertificateUsageEvent, PagedResponse } from '@/types'

export const getMyCertificate = () =>
  request.get<any, ApiResponse<CertificateRecord>>('/api/v1/certificates/me')

export const requestCertificate = () =>
  request.post<any, ApiResponse<CertificateRecord>>('/api/v1/certificates/request')

export const getSubscriptionCertificates = (subscriptionId: number) =>
  request.get<any, ApiResponse<CertificateRecord[]>>(`/api/v1/subscriptions/${subscriptionId}/certificates`)

export const getPendingCertificates = () =>
  request.get<any, ApiResponse<CertificateRecord[]>>('/api/v1/admin/certificates/pending')

export const retryCertificate = (recordId: number) =>
  request.post<any, ApiResponse<CertificateRecord>>(`/api/v1/admin/certificates/${recordId}/retry`)

export const getAllCertificates = (params: {
  status?: string
  userId?: string
  page: number
  size: number
}) => request.get<any, ApiResponse<PagedResponse<CertificateRecord>>>('/api/v1/admin/certificates', { params })

export const getCertificateUsageHistory = (certId: string, params: { page: number; size: number }) =>
  request.get<any, ApiResponse<PagedResponse<CertificateUsageEvent>>>(
    `/api/v1/admin/certificates/${encodeURIComponent(certId)}/usage-history`, { params })

export const getCertificateUsageDaily = (certId: string, params: { from: string; to: string }) =>
  request.get<any, ApiResponse<{ usageDate: string; usageCount: number; distinctUsers: number }[]>>(
    `/api/v1/admin/certificates/${encodeURIComponent(certId)}/usage-daily`, { params })
