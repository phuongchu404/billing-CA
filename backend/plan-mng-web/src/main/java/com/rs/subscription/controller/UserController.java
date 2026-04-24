package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.*;
import com.rs.subscription.dto.request.ResetPasswordRequest;
import com.rs.subscription.dto.response.UserResponse;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;
    private final AdminAuditLogService auditLogService;

    @PostMapping("/api/v1/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create new user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest req,
                                             @AuthenticationPrincipal String adminId) {
        UserResponse user = userService.createUser(req, adminId);
        auditLogService.log(adminId, "CREATE_USER", "USER", user.getUserId(),
                "Created user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "User created successfully");
    }

    @GetMapping("/api/v1/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "List all users")
    public ApiResponse<PagedResponse<UserResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.listUsers(status, query, page, size), "Users retrieved successfully");
    }

    @GetMapping("/api/v1/admin/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getById(@PathVariable String userId) {
        return ApiResponse.success(userService.getUserById(userId), "User retrieved successfully");
    }

    @PutMapping("/api/v1/admin/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update user profile")
    public ApiResponse<UserResponse> update(@PathVariable String userId,
                                             @Valid @RequestBody UpdateUserRequest req,
                                             @AuthenticationPrincipal String adminId) {
        UserResponse user = userService.updateUser(userId, req);
        auditLogService.log(adminId, "UPDATE_USER", "USER", userId,
                "Updated profile for user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "User updated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deactivate user")
    public ApiResponse<Void> deactivate(@PathVariable String userId,
                                        @AuthenticationPrincipal String requesterId) {
        userService.deactivateUser(userId, requesterId);
        auditLogService.log(requesterId, "DEACTIVATE_USER", "USER", userId, "Deactivated user");
        return ApiResponse.success("User deactivated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/reactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Reactivate user")
    public ApiResponse<Void> reactivate(@PathVariable String userId,
                                        @AuthenticationPrincipal String adminId) {
        userService.reactivateUser(userId);
        auditLogService.log(adminId, "REACTIVATE_USER", "USER", userId, "Reactivated user");
        return ApiResponse.success("User reactivated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/unlock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Unlock user account")
    public ApiResponse<Void> unlock(@PathVariable String userId,
                                    @AuthenticationPrincipal String adminId) {
        userService.unlockUser(userId);
        auditLogService.log(adminId, "UNLOCK_USER", "USER", userId, "Unlocked user account");
        return ApiResponse.success("User unlocked successfully");
    }

    @DeleteMapping("/api/v1/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete user")
    public void delete(@PathVariable String userId,
                       @AuthenticationPrincipal String requesterId) {
        UserResponse user = userService.getUserById(userId);
        userService.deleteUser(userId, requesterId);
        auditLogService.log(requesterId, "DELETE_USER", "USER", userId,
                "Deleted user '" + user.getUsername() + "'");
    }

    @PutMapping("/api/v1/admin/users/{userId}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Assign roles to user")
    public ApiResponse<UserResponse> assignRoles(@PathVariable String userId,
                                                  @Valid @RequestBody AssignRolesRequest req,
                                                  @AuthenticationPrincipal String adminId) {
        UserResponse user = userService.assignRoles(userId, req.getRoleIds(), adminId);
        auditLogService.log(adminId, "ASSIGN_USER_ROLES", "USER", userId,
                "Assigned roles " + req.getRoleIds() + " to user '" + user.getUsername() + "'");
        return ApiResponse.success(user, "Roles updated successfully");
    }

    @PatchMapping("/api/v1/admin/users/{userId}/password")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Reset user password")
    public ApiResponse<Void> resetPassword(@PathVariable String userId,
                                           @Valid @RequestBody ResetPasswordRequest req,
                                           @AuthenticationPrincipal String adminId) {
        userService.resetPassword(userId, req.getNewPassword());
        auditLogService.log(adminId, "RESET_USER_PASSWORD", "USER", userId, "Reset user password");
        return ApiResponse.success("Password reset successfully");
    }

    @GetMapping("/api/v1/users/me")
    @Operation(summary = "Get own profile")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal String userId) {
        return ApiResponse.success(userService.getMyProfile(userId), "Profile retrieved successfully");
    }

    @PutMapping("/api/v1/users/me/password")
    @Operation(summary = "Change own password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String userId,
                                             @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userId, req);
        return ApiResponse.success("Password changed successfully");
    }
}
