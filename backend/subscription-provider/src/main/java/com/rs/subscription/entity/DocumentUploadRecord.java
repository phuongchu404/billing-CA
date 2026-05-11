package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "document_upload_records",
    indexes = {
        @Index(name = "idx_dur_subscription_uploaded", columnList = "subscription_id, uploaded_at"),
        @Index(name = "idx_dur_user_uploaded", columnList = "user_id, uploaded_at"),
        @Index(name = "idx_dur_certificate_uploaded", columnList = "certificate_id, uploaded_at"),
        @Index(name = "idx_dur_status_uploaded", columnList = "upload_status, uploaded_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Subscription subscription;

    @Column(name = "certificate_id", length = 200)
    private String certificateId;

    @Column(name = "document_id", nullable = false, length = 200)
    private String documentId;

    @Column(name = "upload_status", nullable = false, length = 30)
    private String uploadStatus;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
        if (uploadStatus == null) uploadStatus = "SUCCESS";
        createdAt = LocalDateTime.now();
    }
}
