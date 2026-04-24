package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "`groups`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(unique = true, nullable = false, length = 100)
    private String groupCode;

    @Column(nullable = false, length = 200)
    private String groupName;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /** BCrypt-hashed password. */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contactEmail;

    @Column(columnDefinition = "TEXT")
    private String contactPhone;

    @Column(length = 200)
    private String refContractNo;

    @Column(columnDefinition = "TEXT")
    private String picEmails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status;

    @Column(nullable = false, length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> members;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = GroupStatus.ACTIVE;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum GroupStatus { ACTIVE, INACTIVE }
}
