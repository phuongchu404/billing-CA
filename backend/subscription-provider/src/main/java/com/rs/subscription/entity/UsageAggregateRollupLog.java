package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_aggregate_rollup_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageAggregateRollupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String periodKey;

    @Column(nullable = false)
    private LocalDateTime runAt;

    @Column(nullable = false)
    private int groupsUpdated;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    @PrePersist
    void onCreate() {
        if (runAt == null) runAt = LocalDateTime.now();
        if (status == null) status = "SUCCESS";
    }
}
