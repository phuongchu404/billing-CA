package com.rs.subscription.service;

import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.AssignGroupPlanRequest;
import com.rs.subscription.dto.request.CreateSubscriptionRequest;
import com.rs.subscription.dto.response.AuditLogResponse;
import com.rs.subscription.dto.response.PartnerPlanActionResponse;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.dto.response.SubscriptionVerifyResponse;
import com.rs.subscription.entity.*;
import com.rs.subscription.entity.Subscription.SubscriptionStatus;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final GroupRepository groupRepository;
    private final PlanService planService;
    private final AuditLogService auditLogService;
    private final SubscriptionAuditLogRepository auditLogRepository;
    private final CertificateProvisioningService certService;
    private final CertificateProvisioningRepository certProvisioningRepository;

    @Value("${sms.cert.auto-provision.on-approval:false}")
    private boolean autoProvisionOnApproval;

    @Value("${sms.cert.auto-provision.on-zero-fee:false}")
    private boolean autoProvisionOnZeroFee;

    public SubscriptionService(
        SubscriptionRepository subscriptionRepository,
        GroupRepository groupRepository,
        PlanService planService,
        AuditLogService auditLogService,
        SubscriptionAuditLogRepository auditLogRepository,
        @Lazy CertificateProvisioningService certService,
        CertificateProvisioningRepository certProvisioningRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.groupRepository = groupRepository;
        this.planService = planService;
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.certService = certService;
        this.certProvisioningRepository = certProvisioningRepository;
    }

    public List<SubscriptionResponse> getMySubscriptions(String userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionResponse initiateIndividualSubscription(String userId, CreateSubscriptionRequest req) {
        Plan plan = planService.findPlanByCodeEntity(req.getPlanCode());
        if (!plan.getIsActive()) {
            throw new SmsException(ErrorCodes.PLAN_NOT_ASSIGNABLE, "Plan is not active", 400);
        }
        if (plan.getIsGroupPlan()) {
            throw new SmsException(ErrorCodes.PLAN_NOT_ASSIGNABLE, "This plan is for groups only", 400);
        }

        boolean zeroFee = plan.getPrice() != null && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
        LocalDate today = LocalDate.now();

        Subscription sub = Subscription.builder()
            .subscriberType(Subscription.SubscriberType.INDIVIDUAL)
            .userId(userId)
            .plan(plan)
            .status(zeroFee ? SubscriptionStatus.ACTIVE : SubscriptionStatus.PENDING)
            .startDate(zeroFee ? today : null)
            .endDate(zeroFee ? today.plusDays(plan.getValidityDays()) : null)
            .signingQuotaTotal(plan.getMaxSigningQuota())
            .signingQuotaUsed(0)
            .paymentReference(zeroFee ? "ZERO-FEE" : req.getPaymentReference())
            .activatedBy(zeroFee ? "SYSTEM" : null)
            .build();
        sub = subscriptionRepository.save(sub);

        if (zeroFee) {
            auditLogService.log(sub, "SYSTEM", null, "ACTIVE", "Zero-fee plan, auto-activated");
            if (autoProvisionOnZeroFee) {
                try { certService.provisionForIndividual(sub.getSubscriptionId()); }
                catch (Exception e) { log.warn("Certificate provisioning failed for subscription {}, will retry", sub.getSubscriptionId(), e); }
            }
        } else {
            auditLogService.log(sub, userId, null, "PENDING", "Individual subscription initiated");
        }
        return toResponse(sub);
    }

    @Transactional
    public void approveSubscription(Long id, String adminUserId) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.PENDING) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Only PENDING subscriptions can be approved", 422);
        }
        String paymentReference = "MANUAL-" + id;
        sub.setActivatedBy(adminUserId);
        activateIndividualSubscription(sub, paymentReference);
        auditLogService.log(sub, adminUserId, "PENDING", "ACTIVE", "Manually approved by admin");
    }

    @Transactional
    public void activateIndividualSubscription(Subscription subscription, String paymentReference) {
        LocalDate today = LocalDate.now();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(today);
        subscription.setEndDate(today.plusDays(subscription.getPlan().getValidityDays()));
        subscription.setPaymentReference(paymentReference);
        subscriptionRepository.save(subscription);
        auditLogService.log(subscription, "SYSTEM", "PENDING", "ACTIVE", "Payment confirmed: " + paymentReference);

        // Trigger certificate provisioning (configurable via sms.cert.auto-provision.on-approval)
        if (autoProvisionOnApproval) {
            try {
                certService.provisionForIndividual(subscription.getSubscriptionId());
            } catch (Exception e) {
                log.warn("Certificate provisioning failed for subscription {}, will retry", subscription.getSubscriptionId(), e);
            }
        }
    }

    @Transactional
    public SubscriptionResponse assignPlanToGroup(Long groupId, AssignGroupPlanRequest req, String adminUserId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + groupId, 404));

        Plan plan = planService.findPlanByCodeEntity(req.getPlanCode());
        if (!plan.getIsActive() || !plan.getIsGroupPlan()) {
            throw new SmsException(ErrorCodes.PLAN_NOT_ASSIGNABLE, "Plan is not assignable to groups", 400);
        }

        LocalDate today = LocalDate.now();
        Subscription sub = Subscription.builder()
            .subscriberType(Subscription.SubscriberType.GROUP)
            .group(group)
            .plan(plan)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(today)
            .endDate(today.plusDays(plan.getValidityDays()))
            .signingQuotaTotal(plan.getMaxSigningQuota())
            .signingQuotaUsed(0)
            .activatedBy(adminUserId)
            .build();

        sub = subscriptionRepository.save(sub);
        auditLogService.log(sub, adminUserId, null, "ACTIVE", "Plan assigned to group by admin");
        return toResponse(sub);
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        return toResponse(findById(id));
    }

    public List<SubscriptionResponse> getGroupSubscriptions(Long groupId) {
        return subscriptionRepository.findAllByGroupGroupId(groupId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<SubscriptionResponse> getUserSubscriptions(String userId) {
        return subscriptionRepository.findAllByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PagedResponse<SubscriptionResponse> listSubscriptions(String status, String query, String subscriberType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Subscription> result;
        boolean hasQuery = query != null && !query.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasType = subscriberType != null && !subscriberType.isBlank();

        if (hasType) {
            Subscription.SubscriberType type = Subscription.SubscriberType.valueOf(subscriberType.toUpperCase());
            if (hasQuery && hasStatus) {
                SubscriptionStatus st = SubscriptionStatus.valueOf(status.toUpperCase());
                result = subscriptionRepository.searchBySubscriberTypeAndStatusAndQuery(type, st, query.toLowerCase(), pageable);
            } else if (hasQuery) {
                result = subscriptionRepository.searchBySubscriberTypeAndQuery(type, query.toLowerCase(), pageable);
            } else if (hasStatus) {
                SubscriptionStatus st = SubscriptionStatus.valueOf(status.toUpperCase());
                result = subscriptionRepository.findBySubscriberTypeAndStatus(type, st, pageable);
            } else {
                result = subscriptionRepository.findBySubscriberType(type, pageable);
            }
        } else if (hasQuery && hasStatus) {
            SubscriptionStatus st = SubscriptionStatus.valueOf(status.toUpperCase());
            result = subscriptionRepository.searchByStatusAndQuery(st, query.toLowerCase(), pageable);
        } else if (hasQuery) {
            result = subscriptionRepository.searchByQuery(query.toLowerCase(), pageable);
        } else if (hasStatus) {
            SubscriptionStatus st = SubscriptionStatus.valueOf(status.toUpperCase());
            result = subscriptionRepository.findByStatus(st, pageable);
        } else {
            result = subscriptionRepository.findAll(pageable);
        }
        return PagedResponse.<SubscriptionResponse>builder()
            .content(result.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build();
    }

    @Transactional
    public void cancelSubscription(Long id, String actor) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE && sub.getStatus() != SubscriptionStatus.PENDING) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Cannot cancel subscription with status: " + sub.getStatus(), 422);
        }
        String oldStatus = sub.getStatus().name();
        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);
        auditLogService.log(sub, actor, oldStatus, "CANCELLED", "Cancelled by " + actor);
    }

    @Transactional
    public void suspendSubscription(Long id, String actor) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Can only suspend ACTIVE subscriptions", 422);
        }
        sub.setStatus(SubscriptionStatus.SUSPENDED);
        subscriptionRepository.save(sub);
        auditLogService.log(sub, actor, "ACTIVE", "SUSPENDED", "Suspended by admin");
    }

    @Transactional
    public void reactivateSubscription(Long id, String actor) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                "Can only reactivate SUSPENDED subscriptions", 422);
        }
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);
        auditLogService.log(sub, actor, "SUSPENDED", "ACTIVE", "Reactivated by admin");
    }

    public List<PartnerPlanActionResponse> getGroupPlanActions(Long groupId) {
        return auditLogRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
            .map(a -> {
                PartnerPlanActionResponse r = new PartnerPlanActionResponse();
                r.setId(a.getId());
                r.setSubscriptionId(a.getSubscription().getSubscriptionId());
                r.setPlanCode(a.getSubscription().getPlan().getPlanCode());
                r.setPlanName(a.getSubscription().getPlan().getPlanName());
                r.setAction(deriveAction(a.getOldStatus(), a.getNewStatus()));
                r.setActor(a.getActor());
                r.setOldStatus(a.getOldStatus());
                r.setNewStatus(a.getNewStatus());
                r.setReason(a.getReason());
                r.setCreatedAt(a.getCreatedAt());
                return r;
            }).collect(Collectors.toList());
    }

    private String deriveAction(String oldStatus, String newStatus) {
        if ("SUSPENDED".equals(newStatus)) return "PAUSE";
        if ("CANCELLED".equals(newStatus)) return "CANCEL";
        if ("EXPIRED".equals(newStatus)) return "EXPIRE";
        if ("ACTIVE".equals(newStatus) && "SUSPENDED".equals(oldStatus)) return "RESUME";
        if ("ACTIVE".equals(newStatus)) return "ASSIGN";
        if ("PENDING".equals(newStatus)) return "INITIATE";
        return newStatus != null ? newStatus : "UNKNOWN";
    }

    public List<AuditLogResponse> getAuditLog(Long subscriptionId) {
        return auditLogRepository.findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(subscriptionId)
            .stream().map(a -> {
                AuditLogResponse r = new AuditLogResponse();
                r.setId(a.getId());
                r.setSubscriptionId(subscriptionId);
                r.setActor(a.getActor());
                r.setOldStatus(a.getOldStatus());
                r.setNewStatus(a.getNewStatus());
                r.setReason(a.getReason());
                r.setCreatedAt(a.getCreatedAt());
                return r;
            }).collect(Collectors.toList());
    }

    @Transactional
    public void decrementQuota(Long subscriptionId, int count) {
        Subscription sub = findById(subscriptionId);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SmsException(ErrorCodes.SUBSCRIPTION_INACTIVE, "Subscription is not active", 422);
        }
        if (sub.getSigningQuotaUsed() + count > sub.getSigningQuotaTotal()) {
            throw new SmsException(ErrorCodes.QUOTA_EXHAUSTED, "Signing quota exhausted", 422);
        }
        sub.setSigningQuotaUsed(sub.getSigningQuotaUsed() + count);

        // Auto-expire COMBINED plans when quota is fully consumed
        Plan.PlanType planType = sub.getPlan().getPlanType();
        if (planType == Plan.PlanType.COMBINED
                && sub.getSigningQuotaUsed() >= sub.getSigningQuotaTotal()) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            auditLogService.log(sub, "SYSTEM", "ACTIVE", "EXPIRED", "Signing quota exhausted");
        } else {
            subscriptionRepository.save(sub);
        }
    }

    public SubscriptionVerifyResponse verifySubscription(Long subscriptionId, String userId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (sub == null) {
            SubscriptionVerifyResponse r = new SubscriptionVerifyResponse();
            r.setSubscriptionId(subscriptionId);
            r.setValid(false);
            r.setInvalidReason("Subscription not found");
            return r;
        }
        return buildVerifyResponse(sub, userId);
    }

    public SubscriptionVerifyResponse getActiveSubscriptionForUser(String userId) {
        Subscription sub = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .stream().findFirst()
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "No active subscription found for user: " + userId, 404));
        return buildVerifyResponse(sub, userId);
    }

    private SubscriptionVerifyResponse buildVerifyResponse(Subscription sub, String userId) {
        SubscriptionVerifyResponse r = new SubscriptionVerifyResponse();
        r.setSubscriptionId(sub.getSubscriptionId());
        r.setUserId(sub.getUserId());
        r.setStatus(sub.getStatus().name());
        r.setPlanCode(sub.getPlan().getPlanCode());
        r.setPlanName(sub.getPlan().getPlanName());
        r.setPlanType(sub.getPlan().getPlanType() != null ? sub.getPlan().getPlanType().name() : null);
        r.setStartDate(sub.getStartDate());
        r.setEndDate(sub.getEndDate());

        Integer total = sub.getSigningQuotaTotal();
        Integer used = sub.getSigningQuotaUsed();
        r.setSigningQuotaTotal(total);
        r.setSigningQuotaUsed(used);
        r.setSigningQuotaRemaining(total != null && used != null ? total - used : null);

        // Determine validity
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            r.setValid(false);
            r.setInvalidReason("Subscription status is " + sub.getStatus().name());
        } else if (userId != null && !userId.equals(sub.getUserId())) {
            r.setValid(false);
            r.setInvalidReason("Subscription does not belong to the given user");
        } else if (sub.getEndDate() != null && sub.getEndDate().isBefore(LocalDate.now())) {
            r.setValid(false);
            r.setInvalidReason("Subscription has expired");
        } else {
            r.setValid(true);
        }
        return r;
    }

    public Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "Subscription not found: " + id, 404));
    }

    public Subscription findByPaymentReference(String ref) {
        return subscriptionRepository.findByPaymentReference(ref)
            .orElseThrow(() -> new SmsException(ErrorCodes.PAYMENT_SUBSCRIPTION_MISMATCH,
                "No subscription found with payment reference: " + ref, 400));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setSubscriptionId(s.getSubscriptionId());
        r.setSubscriberType(s.getSubscriberType().name());
        r.setUserId(s.getUserId());
        r.setGroupId(s.getGroup() != null ? s.getGroup().getGroupId() : null);
        r.setPlanCode(s.getPlan().getPlanCode());
        r.setPlanName(s.getPlan().getPlanName());
        r.setStatus(s.getStatus().name());
        r.setStartDate(s.getStartDate());
        r.setEndDate(s.getEndDate());
        r.setPlanEffectiveFrom(s.getPlan().getEffectiveFrom());
        r.setPlanEffectiveTo(s.getPlan().getEffectiveTo());
        r.setPlanType(s.getPlan().getPlanType() != null ? s.getPlan().getPlanType().name() : null);
        r.setPlanValidityDays(s.getPlan().getValidityDays());
        r.setPlanValidityAmount(s.getPlan().getValidityAmount());
        r.setPlanValidityUnit(s.getPlan().getValidityUnit());
        r.setSigningQuotaTotal(s.getSigningQuotaTotal());
        r.setSigningQuotaUsed(s.getSigningQuotaUsed());
        r.setActivatedBy(s.getActivatedBy());
        r.setPaymentReference(s.getPaymentReference());
        certProvisioningRepository.findBySubscriptionSubscriptionId(s.getSubscriptionId()).stream()
            .filter(c -> c.getCertificateId() != null)
            .findFirst()
            .ifPresent(c -> r.setCertificateId(c.getCertificateId()));
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        SubscriptionResponse.FeatureFlagsDto ff = new SubscriptionResponse.FeatureFlagsDto();
        ff.setAllowBulkSigning(s.getPlan().getAllowBulkSigning());
        ff.setAllowApiAccess(s.getPlan().getAllowApiAccess());
        r.setFeatureFlags(ff);
        return r;
    }
}
