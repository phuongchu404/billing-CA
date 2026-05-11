package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.*;
import com.rs.subscription.dto.request.ResetPasswordRequest;
import com.rs.subscription.dto.response.UserResponse;
import com.rs.subscription.security.CurrentUser;
import com.rs.subscription.security.service.CustomUserDetails;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;
    private final AdminAuditLogService auditLogService;

    @PostMapping("/api/v1/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user:create')")
    @Operation(summary = "Create new user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest req,
                                             @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        UserResponse user = userService.createUser(req, String.valueOf(adminId));
        auditLogService.log(adminId, "CREATE_USER", "USER", user.getUserId(),
                "Created user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "User created successfully");
    }

    @GetMapping("/api/v1/admin/users")
    @PreAuthorize("hasAnyAuthority('user:view','group:assign:owner')")
    @Operation(summary = "List all users")
    public ApiResponse<PagedResponse<UserResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.listUsers(status, query, role, page, size), "Users retrieved successfully");
    }

    @GetMapping("/api/v1/admin/partner-users")
    @PreAuthorize("hasAnyAuthority('user:view','partner:access:grant')")
    @Operation(summary = "List users with ROLE_PARTNER")
    public ApiResponse<PagedResponse<UserResponse>> listPartnerUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return ApiResponse.success(userService.listUsersByRole("ROLE_PARTNER", page, size), "Partner users retrieved successfully");
    }

    @GetMapping("/api/v1/admin/users/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getById(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserById(userId), "User retrieved successfully");
    }

    @PutMapping("/api/v1/admin/users/{userId}")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Update user profile")
    public ApiResponse<UserResponse> update(@PathVariable Long userId,
                                             @Valid @RequestBody UpdateUserRequest req,
                                             @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        UserResponse user = userService.updateUser(userId, req);
        auditLogService.log(adminId, "UPDATE_USER", "USER", userId,
                "Updated profile for user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "User updated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/deactivate")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Deactivate user")
    public ApiResponse<Void> deactivate(@PathVariable Long userId,
                                        @CurrentUser CustomUserDetails requester) {
        Long requesterId = requester.getUserId();
        userService.deactivateUser(userId, requesterId);
        auditLogService.log(requesterId, "DEACTIVATE_USER", "USER", userId, "Deactivated user");
        return ApiResponse.success("User deactivated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/reactivate")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Reactivate user")
    public ApiResponse<Void> reactivate(@PathVariable Long userId,
                                        @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        userService.reactivateUser(userId);
        auditLogService.log(adminId, "REACTIVATE_USER", "USER", userId, "Reactivated user");
        return ApiResponse.success("User reactivated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/unlock")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Unlock user account")
    public ApiResponse<Void> unlock(@PathVariable Long userId,
                                    @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        userService.unlockUser(userId);
        auditLogService.log(adminId, "UNLOCK_USER", "USER", userId, "Unlocked user account");
        return ApiResponse.success("User unlocked successfully");
    }

    @DeleteMapping("/api/v1/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Delete user")
    public void delete(@PathVariable Long userId,
                       @CurrentUser CustomUserDetails requester) {
        Long requesterId = requester.getUserId();
        UserResponse user = userService.getUserById(userId);
        userService.deleteUser(userId, requesterId);
        auditLogService.log(requesterId, "DELETE_USER", "USER", userId,
                "Deleted user '" + user.getUsername() + "'");
    }

    @PutMapping("/api/v1/admin/users/{userId}/roles")
    @PreAuthorize("hasAuthority('user:update') and hasAuthority('role:update')")
    @Operation(summary = "Assign roles to user")
    public ApiResponse<UserResponse> assignRoles(@PathVariable Long userId,
                                                  @Valid @RequestBody AssignRolesRequest req,
                                                  @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        UserResponse user = userService.assignRoles(userId, req.getRoleIds(), adminId);
        auditLogService.log(adminId, "ASSIGN_USER_ROLES", "USER", userId,
                "Assigned roles " + req.getRoleIds() + " to user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "Roles updated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/password")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Reset user password")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId,
                                           @Valid @RequestBody ResetPasswordRequest req,
                                           @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        userService.resetPassword(userId, req.getNewPassword());
        auditLogService.log(adminId, "RESET_USER_PASSWORD", "USER", userId, "Reset user password");
        return ApiResponse.success("Password reset successfully");
    }

    @GetMapping("/api/v1/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get own profile")
    public ApiResponse<UserResponse> getMe(@CurrentUser CustomUserDetails user) {
        return ApiResponse.success(userService.getMyProfile(user.getUserId()), "Profile retrieved successfully");
    }

    @PutMapping("/api/v1/users/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change own password")
    public ApiResponse<Void> changePassword(@CurrentUser CustomUserDetails user,
                                             @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(user.getUserId(), req);
        return ApiResponse.success("Password changed successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/manager")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "Assign or clear manager for user")
    public ApiResponse<com.rs.subscription.dto.response.UserResponse> setManager(
            @PathVariable Long userId,
            @Valid @RequestBody com.rs.subscription.dto.request.SetManagerRequest req,
            @CurrentUser CustomUserDetails admin) {
        Long adminId = admin.getUserId();
        com.rs.subscription.dto.response.UserResponse user = userService.setManager(userId, req.getManagerUserId());
        auditLogService.log(adminId, "SET_USER_MANAGER", "USER", userId,
                "Set manager to: " + req.getManagerUserId());
        return ApiResponse.success(user, "Manager updated successfully");
    }

    @GetMapping("/api/v1/admin/users/{userId}/subordinates")
    @PreAuthorize("hasAnyAuthority('user:view','group:view:subordinates')")
    @Operation(summary = "List direct subordinates of a user")
    public ApiResponse<java.util.List<com.rs.subscription.dto.response.UserResponse>> getSubordinates(
            @PathVariable Long userId) {
        return ApiResponse.success(userService.getDirectSubordinates(userId), "Subordinates retrieved successfully");
    }
}




