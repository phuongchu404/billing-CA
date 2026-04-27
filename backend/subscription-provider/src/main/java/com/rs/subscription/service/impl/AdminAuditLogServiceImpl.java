package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.response.AdminAuditLogResponse;
import com.rs.subscription.entity.AdminAuditLog;
import com.rs.subscription.repository.AdminAuditLogRepository;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final UserAccountRepository userAccountRepository;

    public void log(String actorUserId, String action, String entityType, String entityId, String details) {
        logByUserId(actorUserId, action, entityType, entityId, details);
    }

    /** Save a log when the actor username is already known (e.g. login/logout flows). */
    public void logDirect(String actorUsername, String action, String entityType, String entityId, String details) {
        adminAuditLogRepository.save(AdminAuditLog.builder()
            .actor(actorUsername)
            .action(action)
            .entityType(entityType)
            .entityId(entityId != null ? entityId : "")
            .details(details)
            .build());
    }

    /** Save a log when only the userId is available — resolves it to username first. */
    public void logByUserId(String actorUserId, String action, String entityType, String entityId, String details) {
        String actor = userAccountRepository.findById(actorUserId)
            .map(u -> u.getUsername())
            .orElse(actorUserId);
        logDirect(actor, action, entityType, entityId, details);
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
