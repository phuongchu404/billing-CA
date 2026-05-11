package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.AssignGroupOwnerRequest;
import com.rs.subscription.dto.request.ProvisionGroupRequest;
import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
import com.rs.subscription.dto.response.GroupListResponse;
import com.rs.subscription.dto.response.PlanHistoryResponse;
import com.rs.subscription.dto.response.ProvisionGroupResponse;
import com.rs.subscription.service.GroupProvisioningService;
import com.rs.subscription.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupProvisioningService groupProvisioningService;

    /** Danh sách đại lý — tự động lọc theo scope của user hiện tại */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('group:view','group:view:own','group:view:subordinates')")
    public ApiResponse<GroupListResponse> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String applyUntil,
            @RequestParam(required = false) String updatedAt) {
        return ApiResponse.success(groupService.listAll(status, applyUntil, updatedAt), "Fetched groups");
    }

    /** Chi tiết một đại lý */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('group:view','group:view:own','group:view:subordinates','report:view:partner')")
    public ApiResponse<GroupDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(groupService.getById(id), "Fetched group");
    }

    /** Tạo đại lý mới */
    @PostMapping
    @PreAuthorize("hasAuthority('group:create')")
    public ApiResponse<GroupDetailResponse> create(@Valid @RequestBody UpsertGroupRequest request) {
        return ApiResponse.success(groupService.create(request), "Created group");
    }

    /** Tạo đại lý + gói cước + gán cùng lúc trong một transaction */
    @PostMapping("/provision")
    @PreAuthorize("hasAuthority('group:create')")
    public ApiResponse<ProvisionGroupResponse> provision(@Valid @RequestBody ProvisionGroupRequest request) {
        return ApiResponse.success(groupProvisioningService.provision(request), "Provisioned group with plan");
    }

    /** Cập nhật thông tin đại lý */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('group:update')")
    public ApiResponse<GroupDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpsertGroupRequest request
    ) {
        return ApiResponse.success(groupService.update(id, request), "Updated group");
    }

    /** Gán nhân viên phụ trách cho group (admin / manager) */
    @PatchMapping("/{id}/owner")
    @PreAuthorize("hasAuthority('group:assign:owner')")
    public ApiResponse<GroupDetailResponse> assignOwner(
        @PathVariable Long id,
        @Valid @RequestBody AssignGroupOwnerRequest request
    ) {
        return ApiResponse.success(groupService.assignOwner(id, request.getOwnerUserId()), "Owner assigned");
    }

    /** Tạm dừng đại lý */
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('group:update')")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        groupService.suspend(id);
        return ApiResponse.success("Group suspended");
    }

    /** Kích hoạt lại đại lý */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('group:update')")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        groupService.activate(id);
        return ApiResponse.success("Group activated");
    }

    /** Lịch sử áp dụng gói cước của đại lý */
    @GetMapping("/{id}/plan-history")
    @PreAuthorize("hasAnyAuthority('group:view','group:view:own','group:view:subordinates','report:view:partner')")
    public ApiResponse<List<PlanHistoryResponse>> getPlanHistory(@PathVariable Long id) {
        return ApiResponse.success(groupService.getPlanHistory(id), "Fetched plan history");
    }
}



