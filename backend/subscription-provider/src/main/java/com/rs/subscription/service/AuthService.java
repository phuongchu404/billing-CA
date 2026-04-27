package com.rs.subscription.service;

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

public interface AuthService {

    LoginResponse login(LoginRequest req, String ipAddress, String userAgent);

    LoginResponse refreshToken(String tokenValue);

    void logout(String tokenValue);
}
