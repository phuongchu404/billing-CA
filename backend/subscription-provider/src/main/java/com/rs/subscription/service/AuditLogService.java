package com.rs.subscription.service;

import com.rs.subscription.aop.TrackSubscriptionAudit;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.response.AuditLogResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.SubscriptionAuditLog;
import com.rs.subscription.repository.SubscriptionAuditLogRepository;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final SubscriptionAuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;

    @TrackSubscriptionAudit(
        subscriptionId = "#p0.subscriptionId",
        actor = "#p1",
        reason = "#p4",
        resolveActorFromUserId = true
    )
    public void log(Subscription subscription, String actor, String oldStatus, String newStatus, String reason) {
        String actorName = userAccountRepository.findById(actor)
                .map(u -> u.getUsername())
                .orElse(actor);
        log.info("Audit: subscription={} actor={} {} -> {}", subscription.getSubscriptionId(), actorName, oldStatus, newStatus);
    }

    public PagedResponse<AuditLogResponse> getSystemAuditLogs(
            String actor, Long subscriptionId, LocalDateTime from, LocalDateTime to,
            int page, int size) {
        Page<SubscriptionAuditLog> result = auditLogRepository.findWithFilters(
                actor, subscriptionId, from, to, PageRequest.of(page, size));
        return PagedResponse.<AuditLogResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    private AuditLogResponse toResponse(SubscriptionAuditLog a) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(a.getId());
        r.setSubscriptionId(a.getSubscription().getSubscriptionId());
        r.setActor(a.getActor());
        r.setOldStatus(a.getOldStatus());
        r.setNewStatus(a.getNewStatus());
        r.setReason(a.getReason());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
