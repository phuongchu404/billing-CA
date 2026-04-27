package com.rs.subscription.service;

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

public interface AdminAuditLogService {

    void log(String actorUserId, String action, String entityType, String entityId, String details);

    void logDirect(String actorUsername, String action, String entityType, String entityId, String details);

    void logByUserId(String actorUserId, String action, String entityType, String entityId, String details);

    PagedResponse<AdminAuditLogResponse> getAdminAuditLogs(
                String actor, String action, String entityType,
                LocalDateTime from, LocalDateTime to,
                int page, int size);
}
