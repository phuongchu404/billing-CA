package com.rs.subscription.dto.request;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRetailPlanScheduleRequest {
    @NotNull
    private Long planTemplateId;

    private String scheduleStatus = CommercialEnums.ScheduleStatus.AVAILABLE.name();

    private LocalDate applyFrom;
    private LocalDate applyTo;

    @NotBlank
    private String requestedBy;
}
