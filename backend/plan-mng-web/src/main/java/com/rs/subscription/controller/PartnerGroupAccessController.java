package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.GrantPartnerAccessRequest;
import com.rs.subscription.dto.response.PartnerGroupAccessResponse;
import com.rs.subscription.security.CurrentUser;
import com.rs.subscription.security.service.CustomUserDetails;
import com.rs.subscription.service.PartnerGroupAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý quyền truy cập báo cáo cho đối tác.
 * Admin cấp / thu hồi quyền cho từng partner trên từng group.
 */
@RestController
@RequestMapping("/api/v1/partner-access")
@RequiredArgsConstructor
public class PartnerGroupAccessController {

    private final PartnerGroupAccessService partnerGroupAccessService;

    /** Cấp quyền xem báo cáo group cho đối tác */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('partner:access:grant')")
    public ApiResponse<PartnerGroupAccessResponse> grant(
        @Valid @RequestBody GrantPartnerAccessRequest request,
        @CurrentUser CustomUserDetails admin
    ) {
        return ApiResponse.success(
            partnerGroupAccessService.grant(request, admin.getUserId()),
            "Access granted"
        );
    }

    /** Thu hồi quyền theo accessId */
    @DeleteMapping("/{accessId}")
    @PreAuthorize("hasAuthority('partner:access:revoke')")
    public ApiResponse<Void> revoke(
        @PathVariable Long accessId,
        @CurrentUser CustomUserDetails admin
    ) {
        partnerGroupAccessService.revoke(accessId, admin.getUserId());
        return ApiResponse.success("Access revoked");
    }

    /** Thu hồi quyền theo partner + group (tiện dụng cho UI) */
    @DeleteMapping("/partner/{partnerUserId}/group/{groupId}")
    @PreAuthorize("hasAuthority('partner:access:revoke')")
    public ApiResponse<Void> revokeByPartnerAndGroup(
        @PathVariable String partnerUserId,
        @PathVariable Long groupId
    ) {
        partnerGroupAccessService.revokeByPartnerAndGroup(partnerUserId, groupId);
        return ApiResponse.success("Access revoked");
    }

    /** Danh sách groups đối tác còn được xem */
    @GetMapping("/partner/{partnerUserId}")
    @PreAuthorize("hasAuthority('partner:access:grant') or (isAuthenticated() and #partnerUserId == authentication.principal.userId)")
    public ApiResponse<List<PartnerGroupAccessResponse>> listActive(
        @PathVariable String partnerUserId
    ) {
        return ApiResponse.success(
            partnerGroupAccessService.listActiveForPartner(partnerUserId),
            "Fetched active partner access"
        );
    }

    /** Lịch sử cấp/thu hồi quyền cho đối tác */
    @GetMapping("/partner/{partnerUserId}/history")
    @PreAuthorize("hasAuthority('partner:access:grant')")
    public ApiResponse<List<PartnerGroupAccessResponse>> listHistory(
        @PathVariable String partnerUserId
    ) {
        return ApiResponse.success(
            partnerGroupAccessService.listHistoryForPartner(partnerUserId),
            "Fetched partner access history"
        );
    }
}
