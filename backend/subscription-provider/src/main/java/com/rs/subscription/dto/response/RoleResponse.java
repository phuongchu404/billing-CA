package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleResponse {
    private Long roleId;
    private String roleName;
    private String displayName;
    private String description;
    private Boolean isSystemRole;
    private LocalDateTime createdAt;
}
