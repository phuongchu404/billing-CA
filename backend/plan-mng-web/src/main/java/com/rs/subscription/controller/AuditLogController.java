package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.response.AdminAuditLogResponse;
import com.rs.subscription.service.AdminAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Admin audit logs")
public class AuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('audit-log:view')")
    @Operation(summary = "Get admin audit logs with optional filters")
    public ApiResponse<PagedResponse<AdminAuditLogResponse>> list(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
                adminAuditLogService.getAdminAuditLogs(actor, action, entityType, from, to, page, size),
                "Audit logs retrieved successfully");
    }
}
