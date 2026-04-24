package com.rs.subscription.repository;

import com.rs.subscription.entity.SubscriptionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionAuditLogRepository extends JpaRepository<SubscriptionAuditLog, Long> {
    List<SubscriptionAuditLog> findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    @Query("SELECT l FROM SubscriptionAuditLog l WHERE l.subscription.group.groupId = :groupId ORDER BY l.createdAt DESC")
    List<SubscriptionAuditLog> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") Long groupId);

    @Query("SELECT l FROM SubscriptionAuditLog l WHERE " +
           "(:actor IS NULL OR l.actor = :actor) AND " +
           "(:subscriptionId IS NULL OR l.subscription.subscriptionId = :subscriptionId) AND " +
           "(:from IS NULL OR l.createdAt >= :from) AND " +
           "(:to IS NULL OR l.createdAt <= :to) " +
           "ORDER BY l.createdAt DESC")
    Page<SubscriptionAuditLog> findWithFilters(
            @Param("actor") String actor,
            @Param("subscriptionId") Long subscriptionId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
