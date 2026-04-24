package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the admin who performed the action */
    @Column(nullable = false, length = 150)
    private String actor;

    /** e.g. CREATE_USER, UPDATE_PLAN, DELETE_ROLE */
    @Column(nullable = false, length = 100)
    private String action;

    /** e.g. USER, ROLE, PLAN, PARTNER, SUBSCRIPTION */
    @Column(nullable = false, length = 50)
    private String entityType;

    /** Primary key / code of the affected entity */
    @Column(nullable = false, length = 150)
    private String entityId;

    /** Human-readable description */
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
