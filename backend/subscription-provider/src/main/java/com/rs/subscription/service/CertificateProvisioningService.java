package com.rs.subscription.service;

import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.dto.response.CertificateUsageResponse;
import com.rs.subscription.dto.response.UsageDailySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CertificateProvisioningService {

    CertificateProvisioningResponse provision(Long subscriptionId, Long userId);

    CertificateProvisioningResponse bindCertificate(Long subscriptionId, Long userId, BindCertificateRequest request);

    CertificateProvisioningResponse getMyCertificate(Long userId);

    List<CertificateProvisioningResponse> getBySubscription(Long subscriptionId);

    CertificateProvisioningResponse adminRetry(Long recordId);

    List<CertificateProvisioningResponse> getPendingRecords();

    Page<CertificateProvisioningResponse> getAllCertificates(String status, Long userId, Pageable pageable);

    Page<CertificateUsageResponse> getUsageHistory(String certId, Pageable pageable);

    List<UsageDailySummaryResponse> getDailyUsage(String certId, LocalDate from, LocalDate to);
}
