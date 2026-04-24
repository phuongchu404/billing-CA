package com.rs.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenRepository.PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Entity
    @Table(name = "password_reset_tokens")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class PasswordResetToken {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(unique = true, nullable = false, length = 36) private String token;
        @Column(nullable = false, length = 36) private String userId;
        @Column(nullable = false) private Boolean used;
        @Column(nullable = false) private LocalDateTime expiresAt;
        @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
        @PrePersist void onCreate() { createdAt = LocalDateTime.now(); if (used == null) used = false; }
    }
}
