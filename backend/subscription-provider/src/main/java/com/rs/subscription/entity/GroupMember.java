package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import com.rs.subscription.enums.CommercialEnums;
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

    @Convert(converter = UuidBinaryConverter.class)
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private String userId;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false, length = 100)
    private String addedBy;

    @Column
    private LocalDate memberStartDate;

    @Column
    private LocalDate memberEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_assignment_id")
    private GroupPlanAssignment sourceAssignment;

    @PrePersist
    void onCreate() {
        joinedAt = LocalDateTime.now();
        if (role == null) role = CommercialEnums.MemberRole.MEMBER.name();
    }
}
