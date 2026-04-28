package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import com.rs.subscription.enums.AuthEnums;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime revokedAt;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 45)
    private String ipAddress;

    @PrePersist
    void onCreate() {
        issuedAt = LocalDateTime.now();
        if (status == null) status = AuthEnums.TokenStatus.ACTIVE.name();
    }
}


