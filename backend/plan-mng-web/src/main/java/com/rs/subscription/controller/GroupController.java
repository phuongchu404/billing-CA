package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.AddGroupMemberRequest;
import com.rs.subscription.dto.request.CreateGroupRequest;
import com.rs.subscription.dto.response.GroupMemberResponse;
import com.rs.subscription.dto.response.GroupResponse;
import com.rs.subscription.dto.response.PartnerPlanActionResponse;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.GroupService;
import com.rs.subscription.service.SubscriptionService;
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
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Partner management")
public class GroupController {

    private final GroupService groupService;
    private final AdminAuditLogService auditLogService;
    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "List all partners")
    public ApiResponse<List<GroupResponse>> list() {
        return ApiResponse.success(groupService.listGroups(), "Partners retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create new partner")
    public ApiResponse<GroupResponse> create(@Valid @RequestBody CreateGroupRequest req,
                                              @AuthenticationPrincipal String userId) {
        GroupResponse group = groupService.createGroup(req, userId);
        auditLogService.log(userId, "CREATE_PARTNER", "PARTNER", String.valueOf(group.getGroupId()),
                "Created partner '" + group.getGroupName() + "'");
        return ApiResponse.success(group, "Partner created successfully");
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "Get partner details")
    public ApiResponse<GroupResponse> get(@PathVariable Long groupId) {
        return ApiResponse.success(groupService.getGroupById(groupId), "Partner retrieved successfully");
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update partner info")
    public ApiResponse<GroupResponse> update(@PathVariable Long groupId,
                                              @Valid @RequestBody CreateGroupRequest req,
                                              @AuthenticationPrincipal String adminId) {
        GroupResponse group = groupService.updateGroup(groupId, req);
        auditLogService.log(adminId, "UPDATE_PARTNER", "PARTNER", String.valueOf(groupId),
                "Updated partner '" + group.getGroupName() + "'");
        return ApiResponse.success(group, "Partner updated successfully");
    }

    @PatchMapping("/{groupId}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deactivate partner")
    public ApiResponse<Void> deactivate(@PathVariable Long groupId,
                                         @AuthenticationPrincipal String adminId) {
        groupService.deactivateGroup(groupId);
        auditLogService.log(adminId, "DEACTIVATE_PARTNER", "PARTNER", String.valueOf(groupId),
                "Deactivated partner");
        return ApiResponse.success("Partner deactivated successfully");
    }

    @PatchMapping("/{groupId}/activate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Activate partner")
    public ApiResponse<Void> activate(@PathVariable Long groupId,
                                       @AuthenticationPrincipal String adminId) {
        groupService.activateGroup(groupId);
        auditLogService.log(adminId, "ACTIVATE_PARTNER", "PARTNER", String.valueOf(groupId),
                "Activated partner");
        return ApiResponse.success("Partner activated successfully");
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "List partner members")
    public ApiResponse<List<GroupMemberResponse>> getMembers(@PathVariable Long groupId) {
        return ApiResponse.success(groupService.getMembers(groupId), "Members retrieved successfully");
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "Add member to partner")
    public ApiResponse<GroupMemberResponse> addMember(@PathVariable Long groupId,
                                                       @Valid @RequestBody AddGroupMemberRequest req,
                                                       @AuthenticationPrincipal String userId) {
        GroupMemberResponse member = groupService.addMember(groupId, req, userId);
        auditLogService.log(userId, "ADD_PARTNER_MEMBER", "PARTNER", String.valueOf(groupId),
                "Added member '" + req.getUserId() + "' to partner");
        return ApiResponse.success(member, "Member added successfully");
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "Remove member from partner")
    public ApiResponse<Void> removeMember(@PathVariable Long groupId, @PathVariable String memberId,
                                           @AuthenticationPrincipal String adminId) {
        groupService.removeMember(groupId, memberId);
        auditLogService.log(adminId, "REMOVE_PARTNER_MEMBER", "PARTNER", String.valueOf(groupId),
                "Removed member '" + memberId + "' from partner");
        return ApiResponse.success("Member removed successfully");
    }

    @GetMapping("/{groupId}/subscription")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "Get partner active subscription")
    public ApiResponse<SubscriptionResponse> getSubscription(@PathVariable Long groupId) {
        return ApiResponse.success(groupService.getGroupActiveSubscription(groupId), "Subscription retrieved successfully");
    }

    @GetMapping("/{groupId}/subscriptions")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "List all subscriptions for a partner")
    public ApiResponse<List<SubscriptionResponse>> getSubscriptions(@PathVariable Long groupId) {
        return ApiResponse.success(subscriptionService.getGroupSubscriptions(groupId), "Subscriptions retrieved successfully");
    }

    @GetMapping("/{groupId}/plan-actions")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR')")
    @Operation(summary = "List plan action history for a partner")
    public ApiResponse<List<PartnerPlanActionResponse>> getPlanActions(@PathVariable Long groupId) {
        return ApiResponse.success(subscriptionService.getGroupPlanActions(groupId), "Plan actions retrieved successfully");
    }
}
