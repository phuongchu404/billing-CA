package com.rs.subscription.service;

import com.rs.subscription.aop.TrackAdminAudit;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.response.AdminAuditLogResponse;
import com.rs.subscription.entity.AdminAuditLog;
import com.rs.subscription.repository.AdminAuditLogRepository;
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
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    /**
     * Record an admin action. actorId is the userId (UUID) from the JWT principal;
     * this method resolves it to username for readability.
     */
    @TrackAdminAudit(
        actor = "#p0",
        action = "#p1",
        entityType = "#p2",
        entityId = "#p3",
        details = "#p4",
        resolveActorFromUserId = true
    )
    public void log(String actorId, String action, String entityType, String entityId, String details) {
        log.debug("AdminAudit queued: actorId={} action={} {}#{}", actorId, action, entityType, entityId);
    }

    /**
     * Record an action when the username is already known (avoids an extra DB lookup).
     */
    @TrackAdminAudit(
        actor = "#p0",
        action = "#p1",
        entityType = "#p2",
        entityId = "#p3",
        details = "#p4"
    )
    public void logDirect(String actorUsername, String action, String entityType, String entityId, String details) {
        log.debug("AdminAudit queued: actor={} action={} {}#{}", actorUsername, action, entityType, entityId);
    }

    public PagedResponse<AdminAuditLogResponse> getAdminAuditLogs(
            String actor, String action, String entityType,
            LocalDateTime from, LocalDateTime to,
            int page, int size) {
        Page<AdminAuditLog> result = adminAuditLogRepository.findWithFilters(
                actor, action, entityType, from, to, PageRequest.of(page, size));
        return PagedResponse.<AdminAuditLogResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    private AdminAuditLogResponse toResponse(AdminAuditLog l) {
        AdminAuditLogResponse r = new AdminAuditLogResponse();
        r.setId(l.getId());
        r.setActor(l.getActor());
        r.setAction(l.getAction());
        r.setEntityType(l.getEntityType());
        r.setEntityId(l.getEntityId());
        r.setDetails(l.getDetails());
        r.setCreatedAt(l.getCreatedAt());
        return r;
    }
}
