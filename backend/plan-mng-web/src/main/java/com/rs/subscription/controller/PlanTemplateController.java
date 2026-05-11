package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.service.PlanTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plan-templates")
@RequiredArgsConstructor
public class PlanTemplateController {

    private final PlanTemplateService planTemplateService;

    @GetMapping
    public ApiResponse<List<PlanTemplateResponse>> list(
            @RequestParam(required = false) String segment) {
        List<PlanTemplateResponse> result = (segment != null && !segment.isBlank())
                ? planTemplateService.listBySegment(segment)
                : planTemplateService.listAll();
        return ApiResponse.success(result, "Fetched plan templates");
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanTemplateResponse> get(@PathVariable Long id) {
        return ApiResponse.success(planTemplateService.getById(id), "Fetched plan template");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('plan:create')")
    public ApiResponse<PlanTemplateResponse> create(@Valid @RequestBody CreatePlanTemplateRequest request) {
        return ApiResponse.success(planTemplateService.create(request), "Created plan template");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<PlanTemplateResponse> update(@PathVariable Long id, @Valid @RequestBody CreatePlanTemplateRequest request) {
        return ApiResponse.success(planTemplateService.update(id, request), "Updated plan template");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('plan:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        planTemplateService.delete(id);
        return ApiResponse.success("Deleted plan template");
    }
}
