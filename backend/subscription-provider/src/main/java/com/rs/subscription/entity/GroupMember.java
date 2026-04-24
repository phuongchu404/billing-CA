package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false, length = 100)
    private String addedBy;

    /** Populated only when the group plan uses INDIVIDUAL_START validity mode. */
    @Column
    private LocalDate memberStartDate;

    @Column
    private LocalDate memberEndDate;

    @PrePersist
    void onCreate() {
        joinedAt = LocalDateTime.now();
        if (role == null) role = MemberRole.MEMBER;
    }

    public enum MemberRole { OPERATOR, MEMBER }
}
