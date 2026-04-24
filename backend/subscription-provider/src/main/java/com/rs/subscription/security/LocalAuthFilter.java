package com.rs.subscription.security;

import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.service.TokenRedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final TokenRedisService tokenRedisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.validateAndGetClaims(token);
            String authProvider = (String) claims.get("auth_provider");

            if ("LOCAL".equals(authProvider) && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!tokenRedisService.isAccessTokenValid(token)) {
                    // Token not in Redis whitelist — session was cleared (e.g. backend restart or explicit logout)
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
                    return;
                }
                String userId = claims.getSubject();
                userAccountRepository.findById(userId).ifPresent(user -> {
                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                        .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        } catch (JwtException e) {
            // Token signature invalid (e.g. keys rotated after restart) — reject immediately
            log.debug("Invalid JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
