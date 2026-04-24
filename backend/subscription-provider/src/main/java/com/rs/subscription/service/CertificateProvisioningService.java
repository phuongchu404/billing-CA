package com.rs.subscription.service;

import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.dto.response.CertificateUsageResponse;
import com.rs.subscription.dto.response.UsageDailySummaryResponse;
import com.rs.subscription.entity.CertificateProvisioningRecord;
import com.rs.subscription.entity.CertificateProvisioningRecord.CertType;
import com.rs.subscription.entity.CertificateProvisioningRecord.ProvisioningStatus;
import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.integration.CertificateProvisioningResult;
import com.rs.subscription.integration.CertificateRequest;
import com.rs.subscription.integration.RsCoreClient;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateProvisioningService {

    private final CertificateProvisioningRepository certRepo;
    private final CertificateUsageRecordRepository usageRepo;
    private final SubscriptionRepository subscriptionRepository;
    private final RsCoreClient rsCoreClient;

    @Value("${rs.core.cert.max-retries:5}")
    private int maxRetries;

    @Transactional
    public void provisionForIndividual(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND, "Subscription not found", 404));
        String userId = sub.getUserId();
        if (userId == null) return;

        var existing = certRepo.findBySubscriptionSubscriptionIdAndUserId(subscriptionId, userId);
        if (existing.isPresent() && existing.get().getStatus() == ProvisioningStatus.COMPLETED) {
            log.info("Certificate already provisioned for user={}, subscription={}", userId, subscriptionId);
            return;
        }

        CertificateProvisioningRecord record = existing.orElseGet(() -> {
            CertificateProvisioningRecord r = CertificateProvisioningRecord.builder()
                .subscription(sub).userId(userId)
                .requestId(UUID.randomUUID().toString())
                .certType(CertType.INDIVIDUAL)
                .status(ProvisioningStatus.PENDING).retryCount(0).build();
            return certRepo.save(r);
        });

        callRsCore(record, sub);
    }

    @Transactional
    public CertificateProvisioningResponse provisionForGroupMember(String userId, Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND, "Subscription not found", 404));

        var existing = certRepo.findBySubscriptionSubscriptionIdAndUserId(subscriptionId, userId);
        if (existing.isPresent() && existing.get().getStatus() == ProvisioningStatus.COMPLETED) {
            return toResponse(existing.get());
        }

        CertificateProvisioningRecord record = existing.orElseGet(() -> {
            CertificateProvisioningRecord r = CertificateProvisioningRecord.builder()
                .subscription(sub).userId(userId)
                .requestId(UUID.randomUUID().toString())
                .certType(CertType.INDIVIDUAL_OF_ORGANIZATION)
                .status(ProvisioningStatus.PENDING).retryCount(0).build();
            return certRepo.save(r);
        });

        callRsCore(record, sub);
        return toResponse(certRepo.findById(record.getId()).orElse(record));
    }

    @Transactional
    public void retryPendingProvisions() {
        List<CertificateProvisioningRecord> eligible = certRepo.findRetryEligible(maxRetries);
        log.info("Retrying {} certificate provisions", eligible.size());
        for (CertificateProvisioningRecord record : eligible) {
            callRsCore(record, record.getSubscription());
        }
    }

    @Transactional
    public CertificateProvisioningResponse adminRetry(Long recordId) {
        CertificateProvisioningRecord record = certRepo.findById(recordId)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND, "Record not found: " + recordId, 404));
        record.setStatus(ProvisioningStatus.FAILED);
        record.setRetryCount(0);
        certRepo.save(record);
        callRsCore(record, record.getSubscription());
        return toResponse(certRepo.findById(recordId).orElse(record));
    }

    @Transactional(readOnly = true)
    public CertificateProvisioningResponse getMyCertificate(String userId) {
        return certRepo.findTopByUserIdOrderByCreatedAtDesc(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "No certificate record found for user: " + userId, 404));
    }

    @Transactional(readOnly = true)
    public List<CertificateProvisioningResponse> getBySubscription(Long subscriptionId) {
        return certRepo.findBySubscriptionSubscriptionId(subscriptionId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CertificateProvisioningResponse> getPendingRecords() {
        return certRepo.findByStatus(ProvisioningStatus.FAILED)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CertificateProvisioningResponse bindCertificate(Long subscriptionId, String userId, BindCertificateRequest req) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "Subscription not found: " + subscriptionId, 404));

        String effectiveUserId = (userId != null && !userId.isBlank()) ? userId : sub.getUserId();

        var existing = certRepo.findBySubscriptionSubscriptionIdAndUserId(subscriptionId, effectiveUserId);
        CertType resolvedCertType = parseCertType(req.getCertType());

        CertificateProvisioningRecord record = existing.orElseGet(() -> {
            CertificateProvisioningRecord r = CertificateProvisioningRecord.builder()
                .subscription(sub)
                .userId(effectiveUserId)
                .requestId(UUID.randomUUID().toString())
                .certType(resolvedCertType)
                .status(ProvisioningStatus.PENDING)
                .retryCount(0)
                .build();
            return certRepo.save(r);
        });

        record.setCertType(resolvedCertType);
        record.setCertificateId(req.getCertificateId());
        record.setKeyId(req.getKeyId());
        record.setIssuedAt(req.getIssuedAt() != null ? req.getIssuedAt() : LocalDateTime.now());
        record.setExpiresAt(req.getExpiresAt());
        record.setStatus(ProvisioningStatus.COMPLETED);
        record.setLastAttemptedAt(LocalDateTime.now());
        certRepo.save(record);

        log.info("Certificate bound manually for subscriptionId={}, userId={}, certId={}",
            subscriptionId, effectiveUserId, req.getCertificateId());
        return toResponse(record);
    }

    private void callRsCore(CertificateProvisioningRecord record, Subscription sub) {
        record.setLastAttemptedAt(LocalDateTime.now());
        record.setRequestId(UUID.randomUUID().toString());

        CertificateRequest req = CertificateRequest.builder()
            .requestId(record.getRequestId())
            .subscriptionId(sub.getSubscriptionId())
            .userId(record.getUserId())
            .fullName("Unknown")
            .email("unknown@rs.local")
            .validFrom(sub.getStartDate() != null ? sub.getStartDate().toString() : "")
            .validTo(sub.getEndDate() != null ? sub.getEndDate().toString() : "")
            .planCode(sub.getPlan().getPlanCode())
            .featureFlags(Map.of(
                "allowBulkSigning", sub.getPlan().getAllowBulkSigning(),
                "allowApiAccess", sub.getPlan().getAllowApiAccess()))
            .build();

        try {
            CertificateProvisioningResult result = rsCoreClient.createCertificate(req);
            record.setCertificateId(result.getCertificateId());
            record.setKeyId(result.getKeyId());
            record.setIssuedAt(LocalDateTime.now());
            record.setStatus(ProvisioningStatus.COMPLETED);
            certRepo.save(record);
            log.info("Certificate provisioned for user={}, certId={}", record.getUserId(), result.getCertificateId());
        } catch (Exception e) {
            log.warn("RS Core call failed for record {}: {}", record.getId(), e.getMessage());
            record.setRetryCount(record.getRetryCount() + 1);
            record.setFailureReason(e.getMessage());
            record.setStatus(record.getRetryCount() >= maxRetries
                ? ProvisioningStatus.FAILED_PERMANENT : ProvisioningStatus.FAILED);
            certRepo.save(record);
        }
    }

    private CertType parseCertType(Integer value) {
        if (value == null) return CertType.INDIVIDUAL;
        for (CertType type : CertType.values()) {
            if (type.getValue() == value) return type;
        }
        return CertType.INDIVIDUAL;
    }

    private CertificateProvisioningResponse toResponse(CertificateProvisioningRecord r) {
        CertificateProvisioningResponse res = new CertificateProvisioningResponse();
        res.setProvisioningRecordId(r.getId());
        res.setUserId(r.getUserId());
        res.setCertType(r.getCertType() != null ? r.getCertType().getValue() : CertType.INDIVIDUAL.getValue());
        res.setCertificateId(r.getCertificateId());
        res.setKeyId(r.getKeyId());
        res.setStatus(r.getStatus().name());
        res.setIssuedAt(r.getIssuedAt());
        res.setExpiresAt(r.getExpiresAt());
        res.setRetryCount(r.getRetryCount());
        res.setUsageCount(r.getUsageCount());
        res.setFailureReason(r.getFailureReason());
        res.setSubscriptionId(r.getSubscription().getSubscriptionId());
        res.setPlanName(r.getSubscription().getPlan().getPlanName());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }

    @Transactional(readOnly = true)
    public Page<CertificateProvisioningResponse> getAllCertificates(
            CertificateProvisioningRecord.ProvisioningStatus status,
            String userId,
            Pageable pageable) {
        return certRepo.findAllWithFilters(status, userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CertificateProvisioningResponse recordUsage(String certificateId, String userId) {
        int updated = certRepo.incrementUsageCount(certificateId);
        if (updated == 0) {
            throw new SmsException(ErrorCodes.CERTIFICATE_NOT_FOUND, "Certificate not found: " + certificateId, 404);
        }
        usageRepo.save(CertificateUsageRecord.builder()
            .certificateId(certificateId)
            .userId(userId)
            .build());
        return toResponse(certRepo.findByCertificateId(certificateId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<CertificateUsageResponse> getUsageHistory(String certificateId, Pageable pageable) {
        return usageRepo.findByCertificateIdOrderByUsedAtDesc(certificateId, pageable)
            .map(r -> {
                CertificateUsageResponse dto = new CertificateUsageResponse();
                dto.setId(r.getId());
                dto.setCertificateId(r.getCertificateId());
                dto.setUserId(r.getUserId());
                dto.setUsedAt(r.getUsedAt());
                return dto;
            });
    }

    @Transactional(readOnly = true)
    public List<UsageDailySummaryResponse> getDailyUsage(String certificateId, LocalDate from, LocalDate to) {
        List<Object[]> rows = usageRepo.findDailyUsage(
            certificateId,
            from.atStartOfDay(),
            to.plusDays(1).atStartOfDay());
        return rows.stream()
            .map(r -> new UsageDailySummaryResponse(
                (LocalDate) r[0],
                ((Number) r[1]).intValue(),
                ((Number) r[2]).intValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public CertificateProvisioningResponse getUsageCount(String certificateId) {
        return certRepo.findByCertificateId(certificateId)
            .map(this::toResponse)
            .orElseThrow(() -> new SmsException(ErrorCodes.CERTIFICATE_NOT_FOUND,
                "Certificate not found: " + certificateId, 404));
    }
}
