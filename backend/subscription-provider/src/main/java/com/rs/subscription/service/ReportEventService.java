package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateAuthFailureEventRequest;
import com.rs.subscription.dto.request.CreateCertificateUsageRequest;
import com.rs.subscription.dto.request.CreateDocumentUploadEventRequest;
import com.rs.subscription.dto.response.ReportEventResponse;

public interface ReportEventService {
    ReportEventResponse recordCertificateUsage(CreateCertificateUsageRequest request);
    ReportEventResponse recordDocumentUpload(CreateDocumentUploadEventRequest request);
    ReportEventResponse recordAuthFailure(CreateAuthFailureEventRequest request);
}
