package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAuthFailureEventRequest {
    private Long userId;
    private Long subscriptionId;
    private String certificateId;

    @NotBlank
    private String failureType;

    private String reasonCode;
    private LocalDateTime failedAt;
}
