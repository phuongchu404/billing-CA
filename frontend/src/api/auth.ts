import request from '@/utils/request'
import type { ApiResponse, LoginRequest, LoginResponse } from '@/types'

export const login = (data: LoginRequest) =>
  request.post<any, ApiResponse<LoginResponse>>('/api/v1/auth/login', data)

export const refreshToken = (refreshToken: string) =>
  request.post<any, ApiResponse<LoginResponse>>('/api/v1/auth/refresh', { refreshToken })

export const logout = (refreshToken: string) =>
  request.post<any, ApiResponse<void>>('/api/v1/auth/logout', { refreshToken })
