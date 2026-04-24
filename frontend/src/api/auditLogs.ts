import request from '@/utils/request'
import type { ApiResponse, PagedResponse } from '@/types'

export interface AdminAuditLogEntry {
  id: number
  actor: string
  action: string
  entityType: string
  entityId: string
  details: string
  createdAt: string
}

export const listAuditLogs = (params: {
  actor?: string
  action?: string
  entityType?: string
  from?: string
  to?: string
  page?: number
  size?: number
}) => request.get<any, ApiResponse<PagedResponse<AdminAuditLogEntry>>>('/api/v1/admin/audit-logs', { params })
