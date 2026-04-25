package com.rs.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String refreshToken;
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private java.util.List<String> roles;
    private java.util.List<String> permissions;
}
