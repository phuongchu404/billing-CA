package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.ApprovalRequestResponse;
import com.rs.subscription.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approval-requests")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    @GetMapping
    public ApiResponse<List<ApprovalRequestResponse>> list() {
        return ApiResponse.success(approvalRequestService.listAll(), "Fetched approval requests");
    }

    @PostMapping("/{id}/review")
    public ApiResponse<ApprovalRequestResponse> review(@PathVariable Long id, @Valid @RequestBody ReviewCommercialRequest request) {
        return ApiResponse.success(approvalRequestService.review(id, request), "Reviewed approval request");
    }
}
