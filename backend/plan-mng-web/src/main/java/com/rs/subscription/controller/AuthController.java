package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.*;
import com.rs.subscription.dto.response.LoginResponse;
import com.rs.subscription.security.JwtService;
import com.rs.subscription.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Local auth endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                             HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String ua = httpRequest.getHeader("User-Agent");
        return ApiResponse.success(authService.login(req, ip, ua), "Login successful");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ApiResponse.success(authService.refreshToken(req.getRefreshToken()), "Token refreshed successfully");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return ApiResponse.success("Logged out successfully");
    }

    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWKS endpoint - public key for token verification")
    public Map<String, Object> jwks() {
        return jwtService.getJwks();
    }
}
