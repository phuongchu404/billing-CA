package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.ApproveStepRequest;
import com.rs.subscription.dto.request.RejectApprovalRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.request.RevisionApprovalRequest;
import com.rs.subscription.dto.request.SubmitApprovalRequest;
import com.rs.subscription.dto.response.ApprovalLevelConfigResponse;
import com.rs.subscription.dto.response.ApprovalRequestResponse;
import com.rs.subscription.dto.response.MultiLevelApprovalResponse;
import com.rs.subscription.service.ApprovalRequestService;
import com.rs.subscription.service.MultiLevelApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approval-requests")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;
    private final MultiLevelApprovalService multiLevelApprovalService;

    // ─── Legacy single-level (giữ tương thích) ────────────────────────────────

    @GetMapping("/legacy")
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<List<ApprovalRequestResponse>> listLegacy() {
        return ApiResponse.success(approvalRequestService.listAll(), "Fetched approval requests");
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('subscription:update')")
    public ApiResponse<ApprovalRequestResponse> review(
        @PathVariable Long id,
        @RequestBody ReviewCommercialRequest request
    ) {
        return ApiResponse.success(approvalRequestService.review(id, request), "Reviewed approval request");
    }

    // ─── Multi-level approval ────────────────────────────────────────────────

    /**
     * GET /api/v1/approval-requests
     * Danh sách tất cả approval requests (multi-level view).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<List<MultiLevelApprovalResponse>> list() {
        return ApiResponse.success(multiLevelApprovalService.listAll(), "Fetched approval requests");
    }

    /**
     * GET /api/v1/approval-requests/{id}
     * Chi tiết approval request kèm danh sách steps.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<MultiLevelApprovalResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(multiLevelApprovalService.getById(id), "Fetched approval request");
    }

    /**
     * POST /api/v1/approval-requests/{id}/submit
     * Sale submit request → chuyển DRAFT → IN_APPROVAL, tạo steps.
     *
     * Áp dụng cho cả:
     *   - Khách hàng phổ thông (INDIVIDUAL / RetailPlanSchedule)
     *   - Khách hàng đại lý (GROUP / GroupPlanAssignment)
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('subscription:update')")
    public ApiResponse<MultiLevelApprovalResponse> submit(
        @PathVariable Long id,
        @RequestBody SubmitApprovalRequest request
    ) {
        return ApiResponse.success(multiLevelApprovalService.submit(id, request), "Submitted for approval");
    }

    /**
     * POST /api/v1/approval-requests/{id}/approve
     * Approver duyệt step hiện tại.
     * - Level 1 (Trưởng phòng): requiredApprovalLevel = LEVEL_1
     * - Level 2 (Giám đốc)    : requiredApprovalLevel = LEVEL_2
     * - Level 3 (CFO)         : requiredApprovalLevel = LEVEL_3
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')")
    public ApiResponse<MultiLevelApprovalResponse> approve(
        @PathVariable Long id,
        @RequestBody ApproveStepRequest request
    ) {
        return ApiResponse.success(multiLevelApprovalService.approveStep(id, request), "Step approved");
    }

    /**
     * POST /api/v1/approval-requests/{id}/reject
     * Từ chối toàn bộ request tại bất kỳ cấp nào.
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')")
    public ApiResponse<MultiLevelApprovalResponse> reject(
        @PathVariable Long id,
        @RequestBody RejectApprovalRequest request
    ) {
        return ApiResponse.success(multiLevelApprovalService.reject(id, request), "Request rejected");
    }

    /**
     * POST /api/v1/approval-requests/{id}/revision
     * Yêu cầu Sale sửa lại → reset steps, trả về NEED_REVISION.
     */
    @PostMapping("/{id}/revision")
    @PreAuthorize("hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')")
    public ApiResponse<MultiLevelApprovalResponse> requestRevision(
        @PathVariable Long id,
        @RequestBody RevisionApprovalRequest request
    ) {
        return ApiResponse.success(multiLevelApprovalService.requestRevision(id, request), "Revision requested");
    }

    /**
     * POST /api/v1/approval-requests/{id}/resubmit
     * Sale resubmit sau khi đã sửa (NEED_REVISION → IN_APPROVAL).
     */
    @PostMapping("/{id}/resubmit")
    @PreAuthorize("hasAuthority('subscription:update')")
    public ApiResponse<MultiLevelApprovalResponse> resubmit(
        @PathVariable Long id,
        @RequestBody SubmitApprovalRequest request
    ) {
        return ApiResponse.success(multiLevelApprovalService.resubmit(id, request), "Resubmitted for approval");
    }

    // ─── Level config ────────────────────────────────────────────────────────

    /**
     * GET /api/v1/approval-requests/level-configs
     * Xem cấu hình số cấp duyệt theo loại khách hàng và ngưỡng giá trị.
     */
    @GetMapping("/level-configs")
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<List<ApprovalLevelConfigResponse>> listLevelConfigs() {
        return ApiResponse.success(multiLevelApprovalService.listLevelConfigs(), "Fetched level configs");
    }
}
