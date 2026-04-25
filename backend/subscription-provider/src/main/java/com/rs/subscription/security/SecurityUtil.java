package com.rs.subscription.security;

import com.rs.subscription.security.service.CustomUserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Optional<CustomUserDetails> getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                        return userDetails;
                    }
                    return null;
                });
    }

    public static Optional<String> getCurrentUserId() {
        return getCurrentUser().map(CustomUserDetails::getUserId);
    }

    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(CustomUserDetails::getUsername);
    }
}
