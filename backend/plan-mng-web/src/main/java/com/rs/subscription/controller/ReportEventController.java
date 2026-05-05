package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateAuthFailureEventRequest;
import com.rs.subscription.dto.request.CreateCertificateUsageRequest;
import com.rs.subscription.dto.request.CreateDocumentUploadEventRequest;
import com.rs.subscription.dto.response.ReportEventResponse;
import com.rs.subscription.service.ReportEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportEventController {

    private final ReportEventService reportEventService;

    @PostMapping("/api/v1/certificate-usages")
    @PreAuthorize("hasAuthority('report:event:create') or hasAuthority('plan:update')")
    public ApiResponse<ReportEventResponse> recordCertificateUsage(
        @Valid @RequestBody CreateCertificateUsageRequest request
    ) {
        return ApiResponse.success(
            reportEventService.recordCertificateUsage(request),
            "Recorded certificate usage event"
        );
    }

    @PostMapping("/api/v1/report-events/document-upload")
    @PreAuthorize("hasAuthority('report:event:create') or hasAuthority('plan:update')")
    public ApiResponse<ReportEventResponse> recordDocumentUpload(
        @Valid @RequestBody CreateDocumentUploadEventRequest request
    ) {
        return ApiResponse.success(
            reportEventService.recordDocumentUpload(request),
            "Recorded document upload event"
        );
    }

    @PostMapping("/api/v1/report-events/auth-failure")
    @PreAuthorize("hasAuthority('report:event:create') or hasAuthority('plan:update')")
    public ApiResponse<ReportEventResponse> recordAuthFailure(
        @Valid @RequestBody CreateAuthFailureEventRequest request
    ) {
        return ApiResponse.success(
            reportEventService.recordAuthFailure(request),
            "Recorded authentication failure event"
        );
    }
}
