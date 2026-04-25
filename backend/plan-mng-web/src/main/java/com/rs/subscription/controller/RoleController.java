package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.AssignPermissionsRequest;
import com.rs.subscription.dto.request.CreateRoleRequest;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse.ModuleGroupResponse;
import com.rs.subscription.dto.response.RoleResponse;
import com.rs.subscription.security.CurrentUser;
import com.rs.subscription.security.service.CustomUserDetails;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management")
public class RoleController {

    private final RoleService roleService;
    private final AdminAuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:view')")
    @Operation(summary = "List all roles")
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.success(roleService.listRoles(), "Roles retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('role:create')")
    @Operation(summary = "Create custom role")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody CreateRoleRequest req,
                                             @CurrentUser CustomUserDetails admin) {
        String adminId = admin.getUserId();
        RoleResponse role = roleService.createRole(req);
        auditLogService.log(adminId, "CREATE_ROLE", "ROLE", String.valueOf(role.getRoleId()),
                "Created role '" + role.getRoleName() + "'");
        return ApiResponse.success(role, "Role created successfully");
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Update role")
    public ApiResponse<RoleResponse> update(@PathVariable Long roleId,
                                             @Valid @RequestBody CreateRoleRequest req,
                                             @CurrentUser CustomUserDetails admin) {
        String adminId = admin.getUserId();
        RoleResponse role = roleService.updateRole(roleId, req);
        auditLogService.log(adminId, "UPDATE_ROLE", "ROLE", String.valueOf(roleId),
                "Updated role '" + role.getRoleName() + "'");
        return ApiResponse.success(role, "Role updated successfully");
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Delete custom role")
    public void delete(@PathVariable Long roleId, @CurrentUser CustomUserDetails admin) {
        String adminId = admin.getUserId();
        roleService.deleteRole(roleId);
        auditLogService.log(adminId, "DELETE_ROLE", "ROLE", String.valueOf(roleId), "Deleted role");
    }

    // ── Permissions ──

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role:view')")
    @Operation(summary = "List all permissions grouped by module and group")
    public ApiResponse<List<ModuleGroupResponse>> listPermissions() {
        return ApiResponse.success(roleService.listPermissionTree(), "Permissions retrieved");
    }

    @GetMapping("/permissions/matrix")
    @PreAuthorize("hasAuthority('role:view')")
    @Operation(summary = "Get role-permission matrix")
    public ApiResponse<RolePermissionMatrixResponse> getPermissionMatrix() {
        return ApiResponse.success(roleService.getPermissionMatrix(), "Permission matrix retrieved");
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Assign permissions to a role")
    public ApiResponse<List<Long>> assignPermissions(@PathVariable Long roleId,
                                                      @Valid @RequestBody AssignPermissionsRequest req,
                                                      @CurrentUser CustomUserDetails admin) {
        String adminId = admin.getUserId();
        roleService.assignPermissions(roleId, req.getPermissionIds());
        auditLogService.log(adminId, "ASSIGN_ROLE_PERMISSIONS", "ROLE", String.valueOf(roleId),
                "Updated permissions for role, count=" + (req.getPermissionIds() == null ? 0 : req.getPermissionIds().size()));
        return ApiResponse.success(roleService.getRolePermissionIds(roleId), "Permissions updated");
    }
}
