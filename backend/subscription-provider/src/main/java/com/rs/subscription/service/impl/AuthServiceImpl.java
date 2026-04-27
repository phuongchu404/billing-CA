package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.LoginRequest;
import com.rs.subscription.dto.response.LoginResponse;
import com.rs.subscription.entity.RefreshToken;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.AuthEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.RefreshTokenRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.security.JwtService;
import com.rs.subscription.security.service.CustomUserDetails;
import com.rs.subscription.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditLogService adminAuditLogService;

    @Value("${sms.auth.jwt.refresh-token-ttl:604800}")
    private long refreshTokenTtl;

    @Value("${sms.auth.jwt.access-token-ttl:3600}")
    private long accessTokenTtl;

    @Value("${sms.auth.password.lockout-threshold:5}")
    private int lockoutThreshold;

    @Value("${sms.auth.password.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public LoginResponse login(LoginRequest req, String ipAddress, String userAgent) {
        UserAccount user = userAccountRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new SmsException(ErrorCodes.UNAUTHORIZED, "Invalid username or password", 401));

        if (AuthEnums.UserStatus.LOCKED.name().equals(user.getStatus())) {
            if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
                user.setStatus(AuthEnums.UserStatus.ACTIVE.name());
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            } else {
                throw new SmsException(ErrorCodes.ACCOUNT_LOCKED,
                    "Account temporarily locked until " + user.getLockedUntil(), 403);
            }
        }

        if (AuthEnums.UserStatus.INACTIVE.name().equals(user.getStatus())) {
            throw new SmsException(ErrorCodes.ACCOUNT_INACTIVE, "Account has been deactivated", 403);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= lockoutThreshold) {
                user.setStatus(AuthEnums.UserStatus.LOCKED.name());
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                log.warn("Account locked: {}", user.getUsername());
            }
            userAccountRepository.save(user);
            throw new SmsException(ErrorCodes.UNAUTHORIZED, "Invalid username or password", 401);
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(user);

        CustomUserDetails userDetails = loadSecurityUser(user);
        String accessToken = jwtService.generateAccessToken(user, resolvePermissions(userDetails));
        String refreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken rt = RefreshToken.builder()
            .token(refreshTokenValue)
            .user(user)
            .status(AuthEnums.TokenStatus.ACTIVE.name())
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenTtl))
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        refreshTokenRepository.save(rt);

        adminAuditLogService.logDirect(user.getUsername(), "LOGIN", "USER", user.getUserId(),
                "Login from IP " + ipAddress);

        return buildLoginResponse(user, userDetails, accessToken, refreshTokenValue);
    }

    @Transactional
    public LoginResponse refreshToken(String tokenValue) {
        RefreshToken rt = refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new SmsException(ErrorCodes.INVALID_REFRESH_TOKEN, "Invalid refresh token", 401));

        if (!AuthEnums.TokenStatus.ACTIVE.name().equals(rt.getStatus())) {
            throw new SmsException(ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token revoked or expired", 401);
        }
        if (LocalDateTime.now().isAfter(rt.getExpiresAt())) {
            rt.setStatus(AuthEnums.TokenStatus.EXPIRED.name());
            refreshTokenRepository.save(rt);
            throw new SmsException(ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token has expired", 401);
        }

        CustomUserDetails userDetails = loadSecurityUser(rt.getUser());
        String newAccessToken = jwtService.generateAccessToken(rt.getUser(), resolvePermissions(userDetails));

        return buildLoginResponse(rt.getUser(), userDetails, newAccessToken, tokenValue);
    }

    private CustomUserDetails loadSecurityUser(UserAccount user) {
        return (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getUserId());
    }

    private List<String> resolvePermissions(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> resolveRoles(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .distinct()
                .collect(Collectors.toList());
    }

    private LoginResponse buildLoginResponse(UserAccount user, CustomUserDetails userDetails,
                                             String accessToken, String refreshTokenValue) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl)
                .refreshToken(refreshTokenValue)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(resolveRoles(userDetails))
                .permissions(resolvePermissions(userDetails))
                .build();
    }

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(rt -> {
            adminAuditLogService.logDirect(rt.getUser().getUsername(), "LOGOUT", "USER",
                    rt.getUser().getUserId(), "User logged out");
            rt.setStatus(AuthEnums.TokenStatus.REVOKED.name());
            rt.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(rt);
        });
    }
}
