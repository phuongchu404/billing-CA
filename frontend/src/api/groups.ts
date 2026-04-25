import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type {
  GroupListItem,
  GroupDetail,
  UpsertGroupRequest,
  PlanHistory,
  GroupPlanAssignment,
  AddGroupPlanRequest,
  CreateGroupPlanAssignmentRequest,
  ReviewAssignmentRequest,
  ProvisionGroupRequest,
  ProvisionGroupResponse,
} from '@/types/group'

// ---- Group CRUD ----
export const listGroups = () =>
  request.get<any, ApiResponse<GroupListItem[]>>('/api/v1/groups')

export const getGroup = (id: number) =>
  request.get<any, ApiResponse<GroupDetail>>(`/api/v1/groups/${id}`)

export const createGroup = (data: UpsertGroupRequest) =>
  request.post<any, ApiResponse<GroupDetail>>('/api/v1/groups', data)

export const updateGroup = (id: number, data: UpsertGroupRequest) =>
  request.put<any, ApiResponse<GroupDetail>>(`/api/v1/groups/${id}`, data)

export const suspendGroup = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/groups/${id}/suspend`)

export const activateGroup = (id: number) =>
  request.patch<any, ApiResponse<void>>(`/api/v1/groups/${id}/activate`)

// ---- Plan history ----
export const getGroupPlanHistory = (id: number) =>
  request.get<any, ApiResponse<PlanHistory[]>>(`/api/v1/groups/${id}/plan-history`)

// ---- Plan assignments ----
export const listGroupAssignments = (groupId: number) =>
  request.get<any, ApiResponse<GroupPlanAssignment[]>>(`/api/v1/groups/${groupId}/plan-assignments`)

export const createGroupAssignment = (groupId: number, data: CreateGroupPlanAssignmentRequest) =>
  request.post<any, ApiResponse<GroupPlanAssignment>>(`/api/v1/groups/${groupId}/plan-assignments`, data)

export const reviewAssignment = (assignmentId: number, data: ReviewAssignmentRequest) =>
  request.post<any, ApiResponse<GroupPlanAssignment>>(`/api/v1/groups/plan-assignments/${assignmentId}/review`, data)

// ---- Add plan to existing group (tạo plan template + assignment trong 1 transaction) ----
export const addGroupPlan = (groupId: number, data: AddGroupPlanRequest) =>
  request.post<any, ApiResponse<GroupPlanAssignment>>(`/api/v1/groups/${groupId}/add-plan`, data)

// ---- Provision (tạo group + plan + assignment trong 1 transaction) ----
export const provisionGroup = (data: ProvisionGroupRequest) =>
  request.post<any, ApiResponse<ProvisionGroupResponse>>('/api/v1/groups/provision', data)
