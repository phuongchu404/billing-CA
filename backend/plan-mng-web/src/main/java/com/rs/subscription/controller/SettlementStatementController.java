package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateSettlementStatementRequest;
import com.rs.subscription.dto.response.SettlementStatementResponse;
import com.rs.subscription.service.SettlementStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlement-statements")
@RequiredArgsConstructor
public class SettlementStatementController {

    private final SettlementStatementService settlementStatementService;

    @GetMapping
    @PreAuthorize("hasAuthority('report:view') or hasAuthority('report:group:view')")
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

    /**
     * Xuất Excel đối soát.
     * @param groupId null = tất cả đại lý
     * @param month   "YYYY-MM", null = không lọc tháng
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('report:view','report:group:view','group:update')")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String month) {
        byte[] data = settlementStatementService.exportToExcel(groupId, month);
        String filename = "doi-soat"
            + (groupId != null ? "-group" + groupId : "")
            + (month != null ? "-" + month : "")
            + ".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }
}
