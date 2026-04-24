package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateGroupContactRequest;
import com.rs.subscription.dto.request.CreateGroupPlanAssignmentRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.GroupContactResponse;
import com.rs.subscription.dto.response.GroupPlanAssignmentResponse;
import com.rs.subscription.service.GroupContactService;
import com.rs.subscription.service.GroupPlanAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupCommercialController {

    private final GroupContactService groupContactService;
    private final GroupPlanAssignmentService groupPlanAssignmentService;

    @GetMapping("/{groupId}/contacts")
    public ApiResponse<List<GroupContactResponse>> listContacts(@PathVariable Long groupId) {
        return ApiResponse.success(groupContactService.listByGroup(groupId), "Fetched group contacts");
    }

    @PostMapping("/{groupId}/contacts")
    public ApiResponse<GroupContactResponse> createContact(
        @PathVariable Long groupId,
        @Valid @RequestBody CreateGroupContactRequest request
    ) {
        request.setGroupId(groupId);
        return ApiResponse.success(groupContactService.create(request), "Created group contact");
    }

    @PutMapping("/contacts/{contactId}")
    public ApiResponse<GroupContactResponse> updateContact(
        @PathVariable Long contactId,
        @Valid @RequestBody CreateGroupContactRequest request
    ) {
        return ApiResponse.success(groupContactService.update(contactId, request), "Updated group contact");
    }

    @DeleteMapping("/contacts/{contactId}")
    public ApiResponse<Void> deleteContact(@PathVariable Long contactId) {
        groupContactService.delete(contactId);
        return ApiResponse.success("Deleted group contact");
    }

    @GetMapping("/plan-assignments")
    public ApiResponse<List<GroupPlanAssignmentResponse>> listAllAssignments() {
        return ApiResponse.success(groupPlanAssignmentService.listAll(), "Fetched group plan assignments");
    }

    @GetMapping("/plan-assignments/{assignmentId}")
    public ApiResponse<GroupPlanAssignmentResponse> getAssignment(@PathVariable Long assignmentId) {
        return ApiResponse.success(groupPlanAssignmentService.getById(assignmentId), "Fetched group plan assignment");
    }

    @GetMapping("/{groupId}/plan-assignments")
    public ApiResponse<List<GroupPlanAssignmentResponse>> listAssignments(@PathVariable Long groupId) {
        return ApiResponse.success(groupPlanAssignmentService.listByGroup(groupId), "Fetched group plan assignments");
    }

    @PostMapping("/{groupId}/plan-assignments")
    public ApiResponse<GroupPlanAssignmentResponse> createAssignment(
        @PathVariable Long groupId,
        @Valid @RequestBody CreateGroupPlanAssignmentRequest request
    ) {
        request.setGroupId(groupId);
        return ApiResponse.success(groupPlanAssignmentService.create(request), "Created group plan assignment");
    }

    @PostMapping("/plan-assignments/{assignmentId}/review")
    public ApiResponse<GroupPlanAssignmentResponse> reviewAssignment(
        @PathVariable Long assignmentId,
        @Valid @RequestBody ReviewCommercialRequest request
    ) {
        return ApiResponse.success(groupPlanAssignmentService.review(assignmentId, request), "Reviewed group plan assignment");
    }
}
