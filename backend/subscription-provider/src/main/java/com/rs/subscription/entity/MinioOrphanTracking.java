package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "minio_orphan_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinioOrphanTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String bucket;

    @Column(name = "object_name", nullable = false, length = 1000)
    private String objectName;

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /** true = file đã được gắn vào entity, sẽ không bị xóa bởi cleanup job. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean confirmed = false;
}
