package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_group_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerGroupAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_user_id", nullable = false)
    private UserAccount partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(length = 36, nullable = false)
    private String grantedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    private LocalDateTime revokedAt;

    @Column(length = 36)
    private String revokedBy;

    @PrePersist
    void onCreate() {
        grantedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}


