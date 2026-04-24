package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(unique = true, nullable = false, length = 100)
    private String permissionKey;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false, length = 100)
    private String moduleGroup;

    @Column(nullable = false, length = 100)
    private String groupName;

    @Column(nullable = false)
    private Integer sortOrder;
}
