package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @Column(length = 36)
    private String userId;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(unique = true, nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private Integer failedLoginAttempts;

    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;

    @Column(length = 36)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
        if (status == null) status = UserStatus.ACTIVE;
        if (authProvider == null) authProvider = AuthProvider.LOCAL;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AuthProvider { LOCAL, SSO }
    public enum UserStatus { ACTIVE, INACTIVE, LOCKED }
}
