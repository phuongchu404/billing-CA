package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import com.rs.subscription.enums.AuthEnums;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(nullable = false, length = 30)
    private String authProvider;

    @Column(nullable = false, length = 30)
    private String status;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
        if (status == null) status = AuthEnums.UserStatus.ACTIVE.name();
        if (authProvider == null) authProvider = AuthEnums.AuthProvider.LOCAL.name();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public List<Role> getRoles() {
        return userRoles.stream()
            .map(UserRole::getRole)
            .collect(Collectors.toList());
    }

    public void setRoles(List<Role> roles) {
        this.userRoles.clear();
        if (roles == null) {
            return;
        }
        roles.forEach(role -> this.userRoles.add(UserRole.of(this, role)));
    }
}
