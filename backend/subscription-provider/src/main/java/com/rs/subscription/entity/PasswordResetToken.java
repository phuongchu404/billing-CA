package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false, length = 36) private String token;
    @Convert(converter = UuidBinaryConverter.class)
    @Column(nullable = false, columnDefinition = "BINARY(16)") private String userId;
    @Column(nullable = false) private Boolean used;
    @Column(nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); if (used == null) used = false; }
}
