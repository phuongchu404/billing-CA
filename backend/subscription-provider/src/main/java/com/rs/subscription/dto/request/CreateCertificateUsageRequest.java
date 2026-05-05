package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCertificateUsageRequest {
    @NotBlank
    private String certificateId;

    private Long userId;
    private Long subscriptionId;
    private Long groupPlanAssignmentId;

    private String usageType = "SIGNING";

    @Min(1)
    private Integer quantity = 1;

    private LocalDateTime usedAt;
    private String externalRequestId;
}
