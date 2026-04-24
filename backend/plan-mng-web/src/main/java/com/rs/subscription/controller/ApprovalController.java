package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.ReviewApprovalRequest;
import com.rs.subscription.dto.request.SubmitApprovalRequest;
import com.rs.subscription.dto.response.ApprovalRequestResponse;
import com.rs.subscription.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Approval request management")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    @Operation(summary = "List approval requests")
    public ApiResponse<PagedResponse<ApprovalRequestResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(approvalService.listApprovals(status, page, size), "OK");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get approval request detail")
    public ApiResponse<ApprovalRequestResponse> get(@PathVariable Long id) {
        return ApiResponse.success(approvalService.getApproval(id), "OK");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a new approval request")
    public ApiResponse<ApprovalRequestResponse> submit(
            @Valid @RequestBody SubmitApprovalRequest req,
            @AuthenticationPrincipal String actor) {
        return ApiResponse.success(approvalService.submit(req, actor), "Request submitted");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Approve a pending request")
    public ApiResponse<ApprovalRequestResponse> approve(
            @PathVariable Long id,
            @RequestBody ReviewApprovalRequest req,
            @AuthenticationPrincipal String actor) {
        return ApiResponse.success(approvalService.approve(id, req, actor), "Request approved");
    }

    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deny a pending request")
    public ApiResponse<ApprovalRequestResponse> deny(
            @PathVariable Long id,
            @RequestBody ReviewApprovalRequest req,
            @AuthenticationPrincipal String actor) {
        return ApiResponse.success(approvalService.deny(id, req, actor), "Request denied");
    }
}
