package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.dto.response.CertificateUsageResponse;
import com.rs.subscription.dto.response.UsageDailySummaryResponse;
import com.rs.subscription.security.CurrentUser;
import com.rs.subscription.security.service.CustomUserDetails;
import com.rs.subscription.service.CertificateProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "Certificate provisioning and usage")
public class CertificateController {

    private final CertificateProvisioningService certService;

    @PostMapping("/api/v1/certificates/request")
    @Operation(summary = "Yêu cầu cấp chứng thư cho subscription")
    public ApiResponse<CertificateProvisioningResponse> requestCertificate(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam Long subscriptionId) {
        return ApiResponse.success(
            certService.provision(subscriptionId, currentUser.getUserId()),
            "Certificate provisioning initiated");
    }

    @GetMapping("/api/v1/certificates/me")
    @Operation(summary = "Xem chứng thư hiện tại của tôi")
    public ApiResponse<CertificateProvisioningResponse> getMyCertificate(
            @CurrentUser CustomUserDetails currentUser) {
        return ApiResponse.success(
            certService.getMyCertificate(currentUser.getUserId()),
            "Certificate status retrieved");
    }

    @PostMapping("/api/v1/subscriptions/{subscriptionId}/certificate/bind")
    @PreAuthorize("hasAuthority('plan:update')")
    @Operation(summary = "Admin: gắn chứng thư thủ công vào subscription")
    public ApiResponse<CertificateProvisioningResponse> bindCertificate(
            @PathVariable Long subscriptionId,
            @RequestParam(required = false) Long userId,
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody BindCertificateRequest body) {
        Long targetUserId = userId != null ? userId : currentUser.getUserId();
        return ApiResponse.success(
            certService.bindCertificate(subscriptionId, targetUserId, body),
            "Certificate bound successfully");
    }

    @GetMapping("/api/v1/subscriptions/{subscriptionId}/certificates")
    @PreAuthorize("hasAuthority('plan:view')")
    @Operation(summary = "Admin: danh sách chứng thư của một subscription")
    public ApiResponse<List<CertificateProvisioningResponse>> getBySubscription(
            @PathVariable Long subscriptionId) {
        return ApiResponse.success(
            certService.getBySubscription(subscriptionId),
            "Certificates retrieved successfully");
    }

    @PostMapping("/api/v1/admin/certificates/{recordId}/retry")
    @PreAuthorize("hasAuthority('plan:update')")
    @Operation(summary = "Admin: thử lại cấp chứng thư bị lỗi")
    public ApiResponse<CertificateProvisioningResponse> retry(@PathVariable Long recordId) {
        return ApiResponse.success(
            certService.adminRetry(recordId),
            "Certificate retry initiated");
    }

    @GetMapping("/api/v1/admin/certificates/pending")
    @PreAuthorize("hasAuthority('plan:view')")
    @Operation(summary = "Admin: danh sách chứng thư đang chờ xử lý hoặc bị lỗi")
    public ApiResponse<List<CertificateProvisioningResponse>> getPending() {
        return ApiResponse.success(
            certService.getPendingRecords(),
            "Pending certificates retrieved");
    }

    @GetMapping("/api/v1/admin/certificates")
    @PreAuthorize("hasAuthority('plan:view')")
    @Operation(summary = "Admin: danh sách tất cả chứng thư (có filter)")
    public ApiResponse<PagedResponse<CertificateProvisioningResponse>> getAllCertificates(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CertificateProvisioningResponse> result = certService.getAllCertificates(
            status, userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.success(PagedResponse.<CertificateProvisioningResponse>builder()
            .content(result.getContent())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build(), "Certificates retrieved");
    }

    @GetMapping("/api/v1/admin/certificates/{certId}/usage-history")
    @PreAuthorize("hasAuthority('plan:view')")
    @Operation(summary = "Admin: lịch sử sử dụng của một chứng thư")
    public ApiResponse<PagedResponse<CertificateUsageResponse>> getUsageHistory(
            @PathVariable String certId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CertificateUsageResponse> result = certService.getUsageHistory(
            certId, PageRequest.of(page, size));
        return ApiResponse.success(PagedResponse.<CertificateUsageResponse>builder()
            .content(result.getContent())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build(), "Usage history retrieved");
    }

    @GetMapping("/api/v1/admin/certificates/{certId}/usage-daily")
    @PreAuthorize("hasAuthority('plan:view')")
    @Operation(summary = "Admin: tổng hợp lượt sử dụng theo ngày")
    public ApiResponse<List<UsageDailySummaryResponse>> getDailyUsage(
            @PathVariable String certId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(
            certService.getDailyUsage(certId, from, to),
            "Daily usage retrieved");
    }
}
