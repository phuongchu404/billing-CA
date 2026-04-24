package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
import com.rs.subscription.dto.response.PlanHistoryResponse;
import com.rs.subscription.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller quản lý Group (khách hàng đại lý).
 * Tất cả endpoints được permitAll trong ApiSecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /** Danh sách tất cả đại lý kèm thông tin gói cước hiện tại và usage */
    @GetMapping
    public ApiResponse<List<GroupListItemResponse>> listAll() {
        return ApiResponse.success(groupService.listAll(), "Fetched groups");
    }

    /** Chi tiết một đại lý */
    @GetMapping("/{id}")
    public ApiResponse<GroupDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(groupService.getById(id), "Fetched group");
    }

    /** Tạo đại lý mới (hệ thống tự sinh groupCode) */
    @PostMapping
    public ApiResponse<GroupDetailResponse> create(@Valid @RequestBody UpsertGroupRequest request) {
        return ApiResponse.success(groupService.create(request), "Created group");
    }

    /** Cập nhật thông tin đại lý */
    @PutMapping("/{id}")
    public ApiResponse<GroupDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpsertGroupRequest request
    ) {
        return ApiResponse.success(groupService.update(id, request), "Updated group");
    }

    /** Tạm dừng đại lý (ACTIVE → INACTIVE) */
    @PatchMapping("/{id}/suspend")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        groupService.suspend(id);
        return ApiResponse.success("Group suspended");
    }

    /** Kích hoạt lại đại lý (INACTIVE → ACTIVE) */
    @PatchMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        groupService.activate(id);
        return ApiResponse.success("Group activated");
    }

    /** Lịch sử áp dụng gói cước của đại lý */
    @GetMapping("/{id}/plan-history")
    public ApiResponse<List<PlanHistoryResponse>> getPlanHistory(@PathVariable Long id) {
        return ApiResponse.success(groupService.getPlanHistory(id), "Fetched plan history");
    }
}
