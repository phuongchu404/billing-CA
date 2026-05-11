package com.rs.subscription.service.impl;

import com.rs.subscription.dto.request.BindCertificateRequest;
import com.rs.subscription.dto.response.CertificateProvisioningResponse;
import com.rs.subscription.dto.response.CertificateUsageResponse;
import com.rs.subscription.dto.response.UsageDailySummaryResponse;
import com.rs.subscription.entity.CertificateProvisioningRecord;
import com.rs.subscription.entity.CertificateProvisioningRecord.CertType;
import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.integration.CertificateRequest;
import com.rs.subscription.integration.CertificateProvisioningResult;
import com.rs.subscription.integration.RsCoreClient;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.service.CertificateProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateProvisioningServiceImpl implements CertificateProvisioningService {

    private static final int MAX_RETRY = 3;

    private final CertificateProvisioningRepository provisioningRepository;
    private final CertificateUsageRecordRepository usageRecordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserAccountRepository userAccountRepository;
    private final RsCoreClient rsCoreClient;

    @Override
    @Transactional
    public CertificateProvisioningResponse provision(Long subscriptionId, Long userId) {
        Subscription subscription = findActiveSubscription(subscriptionId);
        checkQuota(subscription);

        var existingOpt = provisioningRepository.findBySubscriptionSubscriptionIdAndUserId(subscriptionId, userId);
        if (existingOpt.isPresent()) {
            CertificateProvisioningRecord existing = existingOpt.get();
            if (CommercialEnums.ProvisioningStatus.COMPLETED.name().equals(existing.getStatus())
                    && existing.getExpiresAt() != null
                    && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
                return toResponse(existing);
            }
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Certificate provisioning record already exists for subscriptionId=" + subscriptionId + ", userId=" + userId, 409);
        }

        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED, "User not found: " + userId, 404));

        PlanPricingRule rule = subscription.getPricingRule();
        LocalDate validFrom = LocalDate.now();
        LocalDate validTo = computeValidTo(validFrom, rule);
        String requestId = UUID.randomUUID().toString();

        CertificateProvisioningRecord record = CertificateProvisioningRecord.builder()
            .subscription(subscription)
            .groupPlanAssignment(subscription.getGroupPlanAssignment())
            .pricingRule(rule)
            .userId(userId)
            .requestId(requestId)
            .status(CommercialEnums.ProvisioningStatus.PENDING.name())
            .certType(resolveCertType(rule))
            .build();
        record = provisioningRepository.save(record);

        CertificateRequest certRequest = CertificateRequest.builder()
            .requestId(requestId)
            .subscriptionId(subscriptionId)
            .userId(userId.toString())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .validFrom(validFrom.toString())
            .validTo(validTo.toString())
            .planCode(subscription.getPlanTemplate().getPlanCode())
            .featureFlags(buildFeatureFlags(subscription.getPlanTemplate()))
            .build();

        return callRsCoreAndUpdate(record, certRequest, subscription, userId);
    }

    @Override
    @Transactional
    public CertificateProvisioningResponse bindCertificate(Long subscriptionId, Long userId, BindCertificateRequest request) {
        Subscription subscription = findActiveSubscription(subscriptionId);
        checkQuota(subscription);
        ensureNoExistingProvisioning(subscriptionId, userId);
        ensureCertificateIdAvailable(request.getCertificateId(), null);

        PlanPricingRule rule = subscription.getPricingRule();
        String requestId = UUID.randomUUID().toString();

        CertificateProvisioningRecord record = CertificateProvisioningRecord.builder()
            .subscription(subscription)
            .groupPlanAssignment(subscription.getGroupPlanAssignment())
            .pricingRule(rule)
            .userId(userId)
            .requestId(requestId)
            .status(CommercialEnums.ProvisioningStatus.COMPLETED.name())
            .certType(request.getCertType() != null ? CertType.values()[request.getCertType() - 1] : resolveCertType(rule))
            .certificateId(request.getCertificateId())
            .keyId(request.getKeyId())
            .issuedAt(request.getIssuedAt() != null ? request.getIssuedAt() : LocalDateTime.now())
            .expiresAt(request.getExpiresAt())
            .build();
        record = provisioningRepository.save(record);

        subscription.setSigningQuotaUsed(subscription.getSigningQuotaUsed() + 1);
        subscriptionRepository.save(subscription);

        usageRecordRepository.save(CertificateUsageRecord.builder()
            .certificateId(request.getCertificateId())
            .userId(userId)
            .subscription(subscription)
            .groupPlanAssignment(subscription.getGroupPlanAssignment())
            .usageType(CommercialEnums.UsageType.CERTIFICATE_CREATED.name())
            .quantity(1)
            .build());

        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateProvisioningResponse getMyCertificate(Long userId) {
        return provisioningRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new SmsException(ErrorCodes.CERTIFICATE_NOT_FOUND,
                "No certificate found for user: " + userId, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateProvisioningResponse> getBySubscription(Long subscriptionId) {
        return provisioningRepository.findBySubscriptionSubscriptionId(subscriptionId)
            .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CertificateProvisioningResponse adminRetry(Long recordId) {
        CertificateProvisioningRecord record = provisioningRepository.findById(recordId)
            .orElseThrow(() -> new SmsException(ErrorCodes.CERTIFICATE_NOT_FOUND,
                "Certificate record not found: " + recordId, 404));

        if (!CommercialEnums.ProvisioningStatus.FAILED.name().equals(record.getStatus())) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Only FAILED records can be retried, current status: " + record.getStatus(), 409);
        }
        if (record.getRetryCount() >= MAX_RETRY) {
            throw new SmsException(ErrorCodes.RS_CORE_PROVISIONING_FAILED_PERMANENT,
                "Max retry attempts (" + MAX_RETRY + ") reached for record: " + recordId, 422);
        }

        Subscription subscription = record.getSubscription();
        UserAccount user = userAccountRepository.findById(record.getUserId())
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                "User not found: " + record.getUserId(), 404));

        PlanPricingRule rule = record.getPricingRule();
        LocalDate validFrom = LocalDate.now();
        LocalDate validTo = computeValidTo(validFrom, rule);

        CertificateRequest certRequest = CertificateRequest.builder()
            .requestId(record.getRequestId())
            .subscriptionId(subscription.getSubscriptionId())
            .userId(record.getUserId().toString())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .validFrom(validFrom.toString())
            .validTo(validTo.toString())
            .planCode(subscription.getPlanTemplate().getPlanCode())
            .featureFlags(buildFeatureFlags(subscription.getPlanTemplate()))
            .build();

        return callRsCoreAndUpdate(record, certRequest, subscription, record.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateProvisioningResponse> getPendingRecords() {
        List<CertificateProvisioningRecord> pending = provisioningRepository.findByStatus(CommercialEnums.ProvisioningStatus.PENDING.name());
        List<CertificateProvisioningRecord> failed = provisioningRepository.findRetryEligible(MAX_RETRY);
        return java.util.stream.Stream.concat(pending.stream(), failed.stream())
            .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CertificateProvisioningResponse> getAllCertificates(String status, Long userId, Pageable pageable) {
        return provisioningRepository.findAllWithFilters(status, userId, pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CertificateUsageResponse> getUsageHistory(String certId, Pageable pageable) {
        return usageRecordRepository.findByCertificateIdOrderByUsedAtDesc(certId, pageable)
            .map(r -> {
                CertificateUsageResponse resp = new CertificateUsageResponse();
                resp.setId(r.getId());
                resp.setCertificateId(r.getCertificateId());
                resp.setUserId(r.getUserId());
                resp.setUsedAt(r.getUsedAt());
                return resp;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsageDailySummaryResponse> getDailyUsage(String certId, LocalDate from, LocalDate to) {
        return usageRecordRepository.findDailyUsage(certId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
            .stream()
            .map(row -> new UsageDailySummaryResponse(
                (LocalDate) row[0],
                ((Number) row[1]).intValue(),
                ((Number) row[2]).intValue()))
            .toList();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private CertificateProvisioningResponse callRsCoreAndUpdate(
            CertificateProvisioningRecord record,
            CertificateRequest certRequest,
            Subscription subscription,
            Long userId) {
        try {
            CertificateProvisioningResult result = rsCoreClient.createCertificate(certRequest);
            ensureCertificateIdAvailable(result.getCertificateId(), record.getId());

            record.setStatus(CommercialEnums.ProvisioningStatus.COMPLETED.name());
            record.setCertificateId(result.getCertificateId());
            record.setKeyId(result.getKeyId());
            record.setIssuedAt(parseDateTime(result.getIssuedAt()));
            record.setExpiresAt(parseDateTime(result.getExpiresAt()));
            record.setLastAttemptedAt(LocalDateTime.now());
            record = provisioningRepository.save(record);

            subscription.setSigningQuotaUsed(subscription.getSigningQuotaUsed() + 1);
            subscriptionRepository.save(subscription);

            usageRecordRepository.save(CertificateUsageRecord.builder()
                .certificateId(result.getCertificateId())
                .userId(userId)
                .subscription(subscription)
                .groupPlanAssignment(subscription.getGroupPlanAssignment())
                .usageType(CommercialEnums.UsageType.CERTIFICATE_CREATED.name())
                .quantity(1)
                .build());

            log.info("Certificate provisioned: recordId={}, certId={}, userId={}",
                record.getId(), result.getCertificateId(), userId);

        } catch (Exception ex) {
            int newRetry = record.getRetryCount() + 1;
            record.setRetryCount(newRetry);
            record.setStatus(newRetry >= MAX_RETRY
                ? CommercialEnums.ProvisioningStatus.FAILED_PERMANENT.name()
                : CommercialEnums.ProvisioningStatus.FAILED.name());
            record.setFailureReason(ex.getMessage());
            record.setLastAttemptedAt(LocalDateTime.now());
            provisioningRepository.save(record);
            log.error("Certificate provisioning failed: recordId={}, attempt={}, error={}",
                record.getId(), newRetry, ex.getMessage());
            throw new SmsException(ErrorCodes.RS_CORE_UNAVAILABLE,
                "Certificate provisioning failed: " + ex.getMessage(), 502);
        }

        return toResponse(record);
    }

    private Subscription findActiveSubscription(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "Subscription not found: " + subscriptionId, 404));
        if (!CommercialEnums.SubscriptionStatus.ACTIVE.name().equals(sub.getStatus())) {
            throw new SmsException(ErrorCodes.SUBSCRIPTION_INACTIVE,
                "Subscription is not active: " + sub.getStatus(), 409);
        }
        return sub;
    }

    private void checkQuota(Subscription subscription) {
        int total = subscription.getSigningQuotaTotal() == null ? 0 : subscription.getSigningQuotaTotal();
        int used = subscription.getSigningQuotaUsed() == null ? 0 : subscription.getSigningQuotaUsed();
        if (total > 0 && used >= total) {
            throw new SmsException(ErrorCodes.QUOTA_EXHAUSTED,
                "Certificate quota exhausted: used=" + used + ", total=" + total, 422);
        }
    }

    private void ensureNoExistingProvisioning(Long subscriptionId, Long userId) {
        provisioningRepository.findBySubscriptionSubscriptionIdAndUserId(subscriptionId, userId)
            .ifPresent(existing -> {
                throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Certificate provisioning record already exists for subscriptionId=" + subscriptionId + ", userId=" + userId, 409);
            });
    }

    private void ensureCertificateIdAvailable(String certificateId, Long currentRecordId) {
        if (certificateId == null || certificateId.isBlank()) return;
        provisioningRepository.findByCertificateId(certificateId)
            .filter(existing -> currentRecordId == null || !existing.getId().equals(currentRecordId))
            .ifPresent(existing -> {
                throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Certificate already exists: " + certificateId, 409);
            });
    }

    private LocalDate computeValidTo(LocalDate from, PlanPricingRule rule) {
        if (rule == null) return from.plusMonths(12);
        int value = rule.getCertificateValidityValue();
        return switch (rule.getCertificateValidityUnit()) {
            case "YEAR" -> from.plusYears(value);
            case "DAY"  -> from.plusDays(value);
            default     -> from.plusMonths(value);
        };
    }

    private CertType resolveCertType(PlanPricingRule rule) {
        if (rule == null) return CertType.INDIVIDUAL;
        String subjectType = rule.getSubjectType();
        if (CommercialEnums.SubjectType.ORGANIZATION.name().equals(subjectType)) return CertType.ORGANIZATION;
        if (CommercialEnums.SubjectType.INDIVIDUAL_OF_ORG.name().equals(subjectType)) {
            return CertType.INDIVIDUAL_OF_ORGANIZATION;
        }
        return CertType.INDIVIDUAL;
    }

    private Map<String, Object> buildFeatureFlags(PlanTemplate template) {
        return Map.of(
            "allowBulkSigning", template.getAllowBulkSigning(),
            "allowApiAccess",   template.getAllowApiAccess()
        );
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e2) {
                log.warn("Cannot parse datetime value from RS Core: {}", value);
                return null;
            }
        }
    }

    private CertificateProvisioningResponse toResponse(CertificateProvisioningRecord r) {
        CertificateProvisioningResponse resp = new CertificateProvisioningResponse();
        resp.setProvisioningRecordId(r.getId());
        resp.setUserId(r.getUserId());
        resp.setCertType(r.getCertType() != null ? r.getCertType().getValue() : null);
        resp.setCertificateId(r.getCertificateId());
        resp.setKeyId(r.getKeyId());
        resp.setStatus(r.getStatus());
        resp.setIssuedAt(r.getIssuedAt());
        resp.setExpiresAt(r.getExpiresAt());
        resp.setRetryCount(r.getRetryCount());
        resp.setUsageCount(r.getUsageCount());
        resp.setFailureReason(r.getFailureReason());
        resp.setSubscriptionId(r.getSubscription() != null ? r.getSubscription().getSubscriptionId() : null);
        resp.setPlanName(r.getSubscription() != null ? r.getSubscription().getPlanTemplate().getPlanName() : null);
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }

}
