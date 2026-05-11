import request from '@/utils/request'
import type { ApiResponse, PagedResponse, UserAccount } from '@/types'

export const listUsers = (params: { status?: string; query?: string; page?: number; size?: number }) =>
  request.get<any, ApiResponse<PagedResponse<UserAccount>>>('/api/v1/admin/users', { params })

export const listPartnerUsers = (params?: { page?: number; size?: number }) =>
  request.get<any, ApiResponse<PagedResponse<UserAccount>>>('/api/v1/admin/partner-users', { params })

export const getUser = (userId: number) =>
  request.get<any, ApiResponse<UserAccount>>(`/api/v1/admin/users/${userId}`)

export const createUser = (data: { username: string; email: string; fullName: string; password: string; confirmPassword: string; roleIds?: number[]; managerUserId?: number | null }) =>
  request.post<any, ApiResponse<UserAccount>>('/api/v1/admin/users', data)

export const updateUser = (userId: number, data: { email?: string; fullName?: string }) =>
  request.put<any, ApiResponse<UserAccount>>(`/api/v1/admin/users/${userId}`, data)

export const deactivateUser = (userId: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/admin/users/${userId}/deactivate`)

export const reactivateUser = (userId: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/admin/users/${userId}/reactivate`)

export const unlockUser = (userId: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/admin/users/${userId}/unlock`)

export const deleteUser = (userId: number) =>
  request.delete<any, ApiResponse<void>>(`/api/v1/admin/users/${userId}`)

export const resetUserPassword = (userId: number, newPassword: string) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/admin/users/${userId}/password`, { newPassword })

export const assignRoles = (userId: number, roleIds: number[]) =>
  request.put<any, ApiResponse<UserAccount>>(`/api/v1/admin/users/${userId}/roles`, { roleIds })

export const getMyProfile = () =>
  request.get<any, ApiResponse<UserAccount>>('/api/v1/users/me')

export const changePassword = (data: { currentPassword: string; newPassword: string }) =>
  request.put<any, ApiResponse<void>>('/api/v1/users/me/password', data)

// ---- Manager / hierarchy ----
export const assignManager = (userId: number, managerUserId: number | null) =>
  request.patch<any, ApiResponse<UserAccount>>(`/api/v1/admin/users/${userId}/manager`, { managerUserId })

export const getSubordinates = (userId: number) =>
  request.get<any, ApiResponse<UserAccount[]>>(`/api/v1/admin/users/${userId}/subordinates`)


