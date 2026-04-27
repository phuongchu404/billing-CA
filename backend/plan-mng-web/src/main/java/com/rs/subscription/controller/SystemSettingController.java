package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.TestEmailRequest;
import com.rs.subscription.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Operational system settings")
public class SystemSettingController {

    private final MailService mailService;

    @PostMapping("/email/test")
    @PreAuthorize("hasAuthority('role:update')")
    @Operation(summary = "Send a test email using current configuration")
    public ApiResponse<String> testEmail(@Valid @RequestBody TestEmailRequest req) {
        mailService.sendTestEmail(req.getRecipient());
        return ApiResponse.success("Sent", "Test email sent to " + req.getRecipient());
    }
}
