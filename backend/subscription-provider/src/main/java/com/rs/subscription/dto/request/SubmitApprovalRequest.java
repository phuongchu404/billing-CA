package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitApprovalRequest {

    @NotBlank(message = "Request type is required")
    private String requestType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;
}
