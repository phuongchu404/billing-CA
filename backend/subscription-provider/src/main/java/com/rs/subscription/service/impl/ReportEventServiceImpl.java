package com.rs.subscription.service.impl;

import com.rs.subscription.dto.request.CreateAuthFailureEventRequest;
import com.rs.subscription.dto.request.CreateCertificateUsageRequest;
import com.rs.subscription.dto.request.CreateDocumentUploadEventRequest;
import com.rs.subscription.dto.response.ReportEventResponse;
import com.rs.subscription.entity.CertificateAuthFailureRecord;
import com.rs.subscription.entity.CertificateProvisioningRecord;
import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.DocumentUploadRecord;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.CertificateAuthFailureRecordRepository;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.DocumentUploadRecordRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.service.ReportEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportEventServiceImpl implements ReportEventService {

    private final CertificateProvisioningRepository provisioningRepository;
    private final CertificateUsageRecordRepository usageRecordRepository;
    private final DocumentUploadRecordRepository documentUploadRecordRepository;
    private final CertificateAuthFailureRecordRepository authFailureRecordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final GroupPlanAssignmentRepository groupPlanAssignmentRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public ReportEventResponse recordCertificateUsage(CreateCertificateUsageRequest request) {
        CertificateProvisioningRecord cert = findCompletedCertificate(request.getCertificateId());
        Subscription subscription = resolveSubscription(request.getSubscriptionId(), cert);
        GroupPlanAssignment assignment = resolveAssignment(request.getGroupPlanAssignmentId(), cert);
        Long userId = request.getUserId() != null ? request.getUserId() : cert.getUserId();
        String usageType = normalizeUsageType(request.getUsageType());

        CertificateUsageRecord record = CertificateUsageRecord.builder()
            .certificateId(cert.getCertificateId())
            .userId(userId)
            .subscription(subscription)
            .groupPlanAssignment(assignment)
            .usageType(usageType)
            .quantity(request.getQuantity() == null ? 1 : request.getQuantity())
            .usedAt(request.getUsedAt() == null ? LocalDateTime.now() : request.getUsedAt())
            .build();
        record = usageRecordRepository.save(record);

        if (CommercialEnums.UsageType.SIGNING.name().equals(usageType)) {
            provisioningRepository.incrementUsageCount(cert.getCertificateId());
            if (subscription != null) {
                int used = subscription.getSigningQuotaUsed() == null ? 0 : subscription.getSigningQuotaUsed();
                subscription.setSigningQuotaUsed(used + record.getQuantity());
                subscriptionRepository.save(subscription);
            }
        }
        evictIndividualReportCache();

        ReportEventResponse response = new ReportEventResponse();
        response.setId(record.getId());
        response.setEventType("CERTIFICATE_USAGE");
        response.setUserId(record.getUserId());
        response.setSubscriptionId(subscription != null ? subscription.getSubscriptionId() : null);
        response.setGroupPlanAssignmentId(assignment != null ? assignment.getGroupPlanAssignmentId() : null);
        response.setCertificateId(record.getCertificateId());
        response.setUsageType(record.getUsageType());
        response.setQuantity(record.getQuantity());
        response.setEventAt(record.getUsedAt());
        return response;
    }

    @Override
    @Transactional
    public ReportEventResponse recordDocumentUpload(CreateDocumentUploadEventRequest request) {
        CertificateProvisioningRecord cert = request.getCertificateId() == null || request.getCertificateId().isBlank()
            ? null
            : findCompletedCertificate(request.getCertificateId());
        Subscription subscription = resolveSubscription(request.getSubscriptionId(), cert);
        Long userId = resolveUserId(request.getUserId(), cert);

        DocumentUploadRecord record = DocumentUploadRecord.builder()
            .userId(userId)
            .subscription(subscription)
            .certificateId(cert != null ? cert.getCertificateId() : request.getCertificateId())
            .documentId(request.getDocumentId())
            .uploadStatus(normalizeUploadStatus(request.getUploadStatus()))
            .uploadedAt(request.getUploadedAt() == null ? LocalDateTime.now() : request.getUploadedAt())
            .build();
        record = documentUploadRecordRepository.save(record);
        evictIndividualReportCache();

        ReportEventResponse response = new ReportEventResponse();
        response.setId(record.getId());
        response.setEventType("DOCUMENT_UPLOAD");
        response.setUserId(record.getUserId());
        response.setSubscriptionId(subscription != null ? subscription.getSubscriptionId() : null);
        response.setCertificateId(record.getCertificateId());
        response.setDocumentId(record.getDocumentId());
        response.setStatus(record.getUploadStatus());
        response.setEventAt(record.getUploadedAt());
        return response;
    }

    @Override
    @Transactional
    public ReportEventResponse recordAuthFailure(CreateAuthFailureEventRequest request) {
        CertificateProvisioningRecord cert = request.getCertificateId() == null || request.getCertificateId().isBlank()
            ? null
            : findCompletedCertificate(request.getCertificateId());
        Subscription subscription = resolveSubscription(request.getSubscriptionId(), cert);
        Long userId = resolveUserId(request.getUserId(), cert);

        CertificateAuthFailureRecord record = CertificateAuthFailureRecord.builder()
            .userId(userId)
            .subscription(subscription)
            .certificateId(cert != null ? cert.getCertificateId() : request.getCertificateId())
            .failureType(normalizeFailureType(request.getFailureType()))
            .reasonCode(request.getReasonCode())
            .failedAt(request.getFailedAt() == null ? LocalDateTime.now() : request.getFailedAt())
            .build();
        record = authFailureRecordRepository.save(record);
        evictIndividualReportCache();

        ReportEventResponse response = new ReportEventResponse();
        response.setId(record.getId());
        response.setEventType("AUTH_FAILURE");
        response.setUserId(record.getUserId());
        response.setSubscriptionId(subscription != null ? subscription.getSubscriptionId() : null);
        response.setCertificateId(record.getCertificateId());
        response.setFailureType(record.getFailureType());
        response.setReasonCode(record.getReasonCode());
        response.setEventAt(record.getFailedAt());
        return response;
    }

    private CertificateProvisioningRecord findCompletedCertificate(String certificateId) {
        CertificateProvisioningRecord cert = provisioningRepository.findByCertificateId(certificateId)
            .orElseThrow(() -> new SmsException(ErrorCodes.CERTIFICATE_NOT_FOUND,
                "Certificate not found: " + certificateId, 404));
        if (!CommercialEnums.ProvisioningStatus.COMPLETED.name().equals(cert.getStatus())) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Certificate is not completed: " + cert.getStatus(), 409);
        }
        return cert;
    }

    private Subscription resolveSubscription(Long subscriptionId, CertificateProvisioningRecord cert) {
        if (subscriptionId != null) {
            return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                    "Subscription not found: " + subscriptionId, 404));
        }
        return cert != null ? cert.getSubscription() : null;
    }

    private GroupPlanAssignment resolveAssignment(Long assignmentId, CertificateProvisioningRecord cert) {
        if (assignmentId != null) {
            return groupPlanAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new SmsException(ErrorCodes.ASSIGNMENT_NOT_FOUND,
                    "Group plan assignment not found: " + assignmentId, 404));
        }
        return cert != null ? cert.getGroupPlanAssignment() : null;
    }

    private Long resolveUserId(Long userId, CertificateProvisioningRecord cert) {
        Long resolved = userId != null ? userId : (cert != null ? cert.getUserId() : null);
        if (resolved == null) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "userId is required when certificateId is not provided", 400);
        }
        return resolved;
    }

    private String normalizeUsageType(String usageType) {
        String value = usageType == null || usageType.isBlank()
            ? CommercialEnums.UsageType.SIGNING.name()
            : usageType.trim().toUpperCase();
        return CommercialEnums.normalize(value, CommercialEnums.UsageType.class, "usageType");
    }

    private String normalizeUploadStatus(String status) {
        String value = status == null || status.isBlank() ? "SUCCESS" : status.trim().toUpperCase();
        if (!value.equals("SUCCESS") && !value.equals("FAILED")) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "uploadStatus must be SUCCESS or FAILED", 400);
        }
        return value;
    }

    private String normalizeFailureType(String failureType) {
        String value = failureType == null ? "" : failureType.trim().toUpperCase();
        if (!value.equals("PIN") && !value.equals("OTP") && !value.equals("MOC")) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "failureType must be PIN, OTP or MOC", 400);
        }
        return value;
    }

    private void evictIndividualReportCache() {
        var cache = cacheManager.getCache("individualReport");
        if (cache != null) {
            cache.clear();
        }
    }
}
