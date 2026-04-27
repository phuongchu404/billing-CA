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

public interface AuditLogService {

    void log(Subscription subscription, String actor, String oldStatus, String newStatus, String reason);

    PagedResponse<AuditLogResponse> getSystemAuditLogs(
                String actor, Long subscriptionId, LocalDateTime from, LocalDateTime to,
                int page, int size);
}
