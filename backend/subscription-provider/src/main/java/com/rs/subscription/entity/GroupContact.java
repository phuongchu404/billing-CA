package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "group_contacts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_contact_type_email", columnNames = {"group_id", "contact_type", "email"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupContactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 30)
    private String contactType;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(length = 200)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false)
    private Boolean isPrimary;

    @Column(nullable = false)
    private Boolean receiveUsageAlert;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (isPrimary == null) isPrimary = false;
        if (receiveUsageAlert == null) receiveUsageAlert = false;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
