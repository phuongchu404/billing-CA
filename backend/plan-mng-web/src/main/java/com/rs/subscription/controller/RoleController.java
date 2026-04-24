package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.AssignPermissionsRequest;
import com.rs.subscription.dto.request.CreateRoleRequest;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse;
import com.rs.subscription.dto.response.RoleResponse;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Roles", description = "Role management")
public class RoleController {

    private final RoleService roleService;
    private final AdminAuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List all roles")
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.success(roleService.listRoles(), "Roles retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create custom role")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody CreateRoleRequest req,
                                             @AuthenticationPrincipal String adminId) {
        RoleResponse role = roleService.createRole(req);
        auditLogService.log(adminId, "CREATE_ROLE", "ROLE", String.valueOf(role.getRoleId()),
                "Created role '" + role.getRoleName() + "'");
        return ApiResponse.success(role, "Role created successfully");
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update role")
    public ApiResponse<RoleResponse> update(@PathVariable Long roleId,
                                             @Valid @RequestBody CreateRoleRequest req,
                                             @AuthenticationPrincipal String adminId) {
        RoleResponse role = roleService.updateRole(roleId, req);
        auditLogService.log(adminId, "UPDATE_ROLE", "ROLE", String.valueOf(roleId),
                "Updated role '" + role.getRoleName() + "'");
        return ApiResponse.success(role, "Role updated successfully");
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete custom role")
    public void delete(@PathVariable Long roleId, @AuthenticationPrincipal String adminId) {
        roleService.deleteRole(roleId);
        auditLogService.log(adminId, "DELETE_ROLE", "ROLE", String.valueOf(roleId), "Deleted role");
    }

    // ── Permission Matrix ──

    @GetMapping("/permissions/matrix")
    @Operation(summary = "Get role-permission matrix")
    public ApiResponse<RolePermissionMatrixResponse> getPermissionMatrix() {
        return ApiResponse.success(roleService.getPermissionMatrix(), "Permission matrix retrieved");
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permissions to a role")
    public ApiResponse<List<Long>> assignPermissions(@PathVariable Long roleId,
                                                      @Valid @RequestBody AssignPermissionsRequest req,
                                                      @AuthenticationPrincipal String adminId) {
        roleService.assignPermissions(roleId, req.getPermissionIds());
        auditLogService.log(adminId, "ASSIGN_ROLE_PERMISSIONS", "ROLE", String.valueOf(roleId),
                "Updated permissions for role, count=" + (req.getPermissionIds() == null ? 0 : req.getPermissionIds().size()));
        return ApiResponse.success(roleService.getRolePermissionIds(roleId), "Permissions updated");
    }
}
