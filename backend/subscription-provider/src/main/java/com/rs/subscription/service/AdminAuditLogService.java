package com.rs.subscription.service;

import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.response.AdminAuditLogResponse;
import com.rs.subscription.entity.AdminAuditLog;
import com.rs.subscription.repository.AdminAuditLogRepository;
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
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * Record an admin action. actorId is the userId (UUID) from the JWT principal;
     * this method resolves it to username for readability.
     */
    public void log(String actorId, String action, String entityType, String entityId, String details) {
        String actorName = userAccountRepository.findById(actorId)
                .map(u -> u.getUsername())
                .orElse(actorId);
        persist(actorName, action, entityType, entityId, details);
    }

    /**
     * Record an action when the username is already known (avoids an extra DB lookup).
     */
    public void logDirect(String actorUsername, String action, String entityType, String entityId, String details) {
        persist(actorUsername, action, entityType, entityId, details);
    }

    private void persist(String actorName, String action, String entityType, String entityId, String details) {
        AdminAuditLog entry = AdminAuditLog.builder()
                .actor(actorName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        adminAuditLogRepository.save(entry);
        log.debug("AdminAudit: actor={} action={} {}#{}", actorName, action, entityType, entityId);
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
