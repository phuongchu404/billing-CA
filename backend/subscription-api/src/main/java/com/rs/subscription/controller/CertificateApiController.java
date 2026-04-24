package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.service.CertificateProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "Certificate binding")
public class CertificateApiController {

    private final CertificateProvisioningService certService;

    @PostMapping("/api/v1/subscriptions/{subscriptionId}/certificate/bind")
    @Operation(summary = "Bind an externally issued certificate to a subscription")
    public ApiResponse<CertificateProvisioningResponse> bindCertificate(
            @PathVariable Long subscriptionId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BindCertificateRequest body) {
        return ApiResponse.success(
            certService.bindCertificate(subscriptionId, userId, body),
            "Certificate bound successfully");
    }

    @PostMapping("/api/v1/certificates/{certificateId}/usage")
    @Operation(summary = "Record a usage event for a certificate")
    public ApiResponse<CertificateProvisioningResponse> recordUsage(
            @PathVariable String certificateId,
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(
            certService.recordUsage(certificateId, userId),
            "Usage recorded successfully");
    }

    @GetMapping("/api/v1/certificates/{certificateId}/usage")
    @Operation(summary = "Get usage count for a certificate")
    public ApiResponse<CertificateProvisioningResponse> getUsage(
            @PathVariable String certificateId,
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(
            certService.getUsageCount(certificateId),
            "Usage count retrieved");
    }
}
