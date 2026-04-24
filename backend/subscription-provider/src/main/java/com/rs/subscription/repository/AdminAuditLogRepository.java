package com.rs.subscription.repository;

import com.rs.subscription.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    @Query("SELECT l FROM AdminAuditLog l WHERE " +
           "(:actor IS NULL OR l.actor = :actor) AND " +
           "(:action IS NULL OR l.action = :action) AND " +
           "(:entityType IS NULL OR l.entityType = :entityType) AND " +
           "(:from IS NULL OR l.createdAt >= :from) AND " +
           "(:to IS NULL OR l.createdAt <= :to) " +
           "ORDER BY l.createdAt DESC")
    Page<AdminAuditLog> findWithFilters(
            @Param("actor") String actor,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
