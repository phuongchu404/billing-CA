//package com.rs.subscription.controller;
//
//import com.rs.subscription.dto.ApiResponse;
//import com.rs.subscription.dto.PagedResponse;
//import com.rs.subscription.dto.request.BindCertificateRequest;
//import com.rs.subscription.dto.response.CertificateProvisioningResponse;
//import com.rs.subscription.dto.response.CertificateUsageResponse;
//import com.rs.subscription.dto.response.UsageDailySummaryResponse;
//import com.rs.subscription.entity.CertificateProvisioningRecord;
//import com.rs.subscription.repository.GroupMemberRepository;
//import com.rs.subscription.repository.SubscriptionRepository;
//import com.rs.subscription.entity.Subscription;
//import com.rs.subscription.exception.ErrorCodes;
//import com.rs.subscription.exception.SmsException;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@Tag(name = "Certificates", description = "Certificate provisioning")
//public class CertificateController {
//
//    private final CertificateProvisioningService certService;
//    private final SubscriptionRepository subscriptionRepository;
//    private final GroupMemberRepository groupMemberRepository;
//
//    @PostMapping("/api/v1/certificates/request")
//    @Operation(summary = "Request certificate provisioning")
//    public ApiResponse<CertificateProvisioningResponse> requestCertificate(
//            @AuthenticationPrincipal String userId,
//            @RequestParam(required = false) Long subscriptionId) {
//        if (subscriptionId != null) {
//            return ApiResponse.success(
//                certService.provisionForGroupMember(userId, subscriptionId),
//                "Certificate provisioning initiated");
//        }
//
//        // Find most recent active individual subscription
//        var individualSubs = subscriptionRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
//            userId, java.util.List.of(Subscription.SubscriptionStatus.ACTIVE));
//        if (!individualSubs.isEmpty()) {
//            Subscription chosen = individualSubs.get(0);
//            return ApiResponse.success(
//                certService.provisionForGroupMember(userId, chosen.getSubscriptionId()),
//                "Certificate provisioning initiated");
//        }
//
//        // Check group membership
//        var memberships = groupMemberRepository.findByUserId(userId);
//        for (var membership : memberships) {
//            var groupSub = subscriptionRepository.findByGroupIdAndStatus(
//                membership.getGroup().getGroupId(), Subscription.SubscriptionStatus.ACTIVE)
//                .stream().findFirst();
//            if (groupSub.isPresent()) {
//                return ApiResponse.success(
//                    certService.provisionForGroupMember(userId, groupSub.get().getSubscriptionId()),
//                    "Certificate provisioning initiated");
//            }
//        }
//
//        throw new SmsException(ErrorCodes.NO_ACTIVE_GROUP_SUBSCRIPTION,
//            "No active subscription found for user: " + userId, 403);
//    }
//
//    @PostMapping("/api/v1/subscriptions/{subscriptionId}/certificate/bind")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "Bind an externally issued certificate to a subscription")
//    public ApiResponse<CertificateProvisioningResponse> bindCertificate(
//            @PathVariable Long subscriptionId,
//            @RequestParam(required = false) String userId,
//            @Valid @RequestBody BindCertificateRequest body) {
//        return ApiResponse.success(
//            certService.bindCertificate(subscriptionId, userId, body),
//            "Certificate bound successfully");
//    }
//
//    @GetMapping("/api/v1/certificates/me")
//    @Operation(summary = "Get my certificate provisioning status")
//    public ApiResponse<CertificateProvisioningResponse> getMyCertificate(
//            @AuthenticationPrincipal String userId) {
//        return ApiResponse.success(certService.getMyCertificate(userId), "Certificate status retrieved");
//    }
//
//    @GetMapping("/api/v1/subscriptions/{subscriptionId}/certificates")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "List certificates for subscription")
//    public ApiResponse<List<CertificateProvisioningResponse>> getBySubscription(
//            @PathVariable Long subscriptionId) {
//        return ApiResponse.success(certService.getBySubscription(subscriptionId), "Certificates retrieved successfully");
//    }
//
//    @PostMapping("/api/v1/admin/certificates/{recordId}/retry")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "Manually retry failed certificate provisioning")
//    public ApiResponse<CertificateProvisioningResponse> retry(@PathVariable Long recordId) {
//        return ApiResponse.success(certService.adminRetry(recordId), "Certificate retry initiated");
//    }
//
//    @GetMapping("/api/v1/admin/certificates/pending")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "List pending/failed certificate records")
//    public ApiResponse<List<CertificateProvisioningResponse>> getPending() {
//        return ApiResponse.success(certService.getPendingRecords(), "Pending certificates retrieved");
//    }
//
//    @GetMapping("/api/v1/admin/certificates")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "List all certificates with optional filters")
//    public ApiResponse<PagedResponse<CertificateProvisioningResponse>> getAllCertificates(
//            @RequestParam(required = false) CertificateProvisioningRecord.ProvisioningStatus status,
//            @RequestParam(required = false) String userId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<CertificateProvisioningResponse> result = certService.getAllCertificates(
//                status, userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
//        return ApiResponse.success(PagedResponse.<CertificateProvisioningResponse>builder()
//                .content(result.getContent())
//                .totalElements(result.getTotalElements())
//                .totalPages(result.getTotalPages())
//                .page(page)
//                .size(size)
//                .build(), "Certificates retrieved");
//    }
//
//    @GetMapping("/api/v1/admin/certificates/{certId}/usage-history")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "Paginated usage event history for a certificate")
//    public ApiResponse<PagedResponse<CertificateUsageResponse>> getUsageHistory(
//            @PathVariable String certId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        Page<CertificateUsageResponse> result = certService.getUsageHistory(
//            certId, PageRequest.of(page, size));
//        return ApiResponse.success(PagedResponse.<CertificateUsageResponse>builder()
//                .content(result.getContent())
//                .totalElements(result.getTotalElements())
//                .totalPages(result.getTotalPages())
//                .page(page)
//                .size(size)
//                .build(), "Usage history retrieved");
//    }
//
//    @GetMapping("/api/v1/admin/certificates/{certId}/usage-daily")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @Operation(summary = "Daily rollup usage summary for a certificate")
//    public ApiResponse<List<UsageDailySummaryResponse>> getDailyUsage(
//            @PathVariable String certId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
//        return ApiResponse.success(certService.getDailyUsage(certId, from, to), "Daily usage retrieved");
//    }
//}
