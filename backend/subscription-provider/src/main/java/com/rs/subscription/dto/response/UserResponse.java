package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String authProvider;
    private String status;
    private List<RoleResponse> roles;
    private Long managerUserId;
    private String managerName;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}


