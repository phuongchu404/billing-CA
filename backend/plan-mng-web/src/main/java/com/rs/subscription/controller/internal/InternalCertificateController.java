package com.rs.subscription.controller.internal;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.request.InternalBindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.service.CertificateProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/certificates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Certificates", description = "Internal service-to-service certificate endpoints")
public class InternalCertificateController {

    private final CertificateProvisioningService certService;

    @Value("${rs.core.service-token}")
    private String expectedServiceToken;

    @PostMapping("/bind")
    @Operation(summary = "Bind an externally issued certificate to a subscription (service-to-service)")
    public ApiResponse<CertificateProvisioningResponse> bindCertificate(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @Valid @RequestBody InternalBindCertificateRequest body) {
        validateServiceToken(serviceToken);

        BindCertificateRequest req = new BindCertificateRequest();
        req.setCertificateId(body.getCertificateId());
        req.setKeyId(body.getKeyId());
        req.setIssuedAt(body.getIssuedAt());
        req.setExpiresAt(body.getExpiresAt());

        return ApiResponse.success(
            certService.bindCertificate(body.getSubscriptionId(), body.getUserId(), req),
            "Certificate bound successfully");
    }

    private void validateServiceToken(String serviceToken) {
        if (serviceToken == null || !serviceToken.equals(expectedServiceToken)) {
            log.warn("Invalid or missing X-Service-Token on /internal/certificates/bind");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service token");
        }
    }
}
