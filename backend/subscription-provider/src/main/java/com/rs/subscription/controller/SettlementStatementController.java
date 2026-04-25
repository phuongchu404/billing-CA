package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateSettlementStatementRequest;
import com.rs.subscription.dto.response.SettlementStatementResponse;
import com.rs.subscription.service.SettlementStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlement-statements")
@RequiredArgsConstructor
public class SettlementStatementController {

    private final SettlementStatementService settlementStatementService;

    @GetMapping
    @PreAuthorize("hasAuthority('report:view')")
    public ApiResponse<List<SettlementStatementResponse>> list(@RequestParam(required = false) Long groupId) {
        if (groupId != null) {
            return ApiResponse.success(settlementStatementService.listByGroup(groupId), "Fetched settlement statements");
        }
        return ApiResponse.success(settlementStatementService.listAll(), "Fetched settlement statements");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('group:update')")
    public ApiResponse<SettlementStatementResponse> create(@Valid @RequestBody CreateSettlementStatementRequest request) {
        return ApiResponse.success(settlementStatementService.create(request), "Created settlement statement");
    }
}
