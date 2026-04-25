package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.TestEmailRequest;
import com.rs.subscription.dto.response.SystemSettingResponse;
import com.rs.subscription.service.MailService;
import com.rs.subscription.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Operational system settings")
public class SystemSettingController {

    private final SystemSettingService settingService;
    private final MailService mailService;

    @GetMapping("/{category}")
    @PreAuthorize("hasAuthority('role:view')")
    @Operation(summary = "Get settings for a category (EMAIL, NOTIFICATION)")
    public ApiResponse<List<SystemSettingResponse>> getCategory(@PathVariable String category) {
        return ApiResponse.success(settingService.getCategory(category), "OK");
    }

    @PutMapping("/{category}")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Update settings for a category")
    public ApiResponse<List<SystemSettingResponse>> updateCategory(
            @PathVariable String category,
            @RequestBody Map<String, String> updates) {
        return ApiResponse.success(settingService.updateCategory(category, updates), "Settings saved");
    }

    @PostMapping("/email/test")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Send a test email using current configuration")
    public ApiResponse<String> testEmail(@Valid @RequestBody TestEmailRequest req) {
        mailService.sendTestEmail(req.getRecipient());
        return ApiResponse.success("Sent", "Test email sent to " + req.getRecipient());
    }
}
