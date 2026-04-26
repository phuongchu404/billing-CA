package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.ExpiringGroupRow;
import com.rs.subscription.dto.response.GroupReportResponse;
import com.rs.subscription.dto.response.IndividualReportResponse;
import com.rs.subscription.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;

    @GetMapping("/group")
    @PreAuthorize("hasAuthority('report:group:view')")
    public ApiResponse<GroupReportResponse> getGroupReport(
            @RequestParam(defaultValue = "2026-03") String periodKey) {
        return ApiResponse.success(reportService.getGroupReport(periodKey), "Fetched group report");
    }

    @GetMapping("/group/expiring-soon")
    @PreAuthorize("hasAuthority('report:group:view')")
    public ApiResponse<List<ExpiringGroupRow>> getExpiringSoon() {
        return ApiResponse.success(reportService.getExpiringSoon(), "Fetched expiring groups");
    }

    @GetMapping("/individual")
    @PreAuthorize("hasAuthority('report:individual:view')")
    public ApiResponse<IndividualReportResponse> getIndividualReport(
            @RequestParam(defaultValue = "2026-03") String periodKey) {
        return ApiResponse.success(reportService.getIndividualReport(periodKey), "Fetched individual report");
    }
}
