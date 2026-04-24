import request from '@/utils/request'
import type { ApiResponse, Role, RolePermissionMatrix } from '@/types'

export const listRoles = () =>
  request.get<any, ApiResponse<Role[]>>('/api/v1/admin/roles')

export const createRole = (data: { roleName: string; displayName: string; description?: string }) =>
  request.post<any, ApiResponse<Role>>('/api/v1/admin/roles', data)

export const updateRole = (roleId: number, data: { displayName: string; description?: string }) =>
  request.put<any, ApiResponse<Role>>(`/api/v1/admin/roles/${roleId}`, data)

export const deleteRole = (roleId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/admin/roles/${roleId}`)

export const getPermissionMatrix = () =>
  request.get<any, ApiResponse<RolePermissionMatrix>>('/api/v1/admin/roles/permissions/matrix')

export const assignPermissions = (roleId: number, permissionIds: number[]) =>
  request.put<any, ApiResponse<number[]>>(`/api/v1/admin/roles/${roleId}/permissions`, { permissionIds })
