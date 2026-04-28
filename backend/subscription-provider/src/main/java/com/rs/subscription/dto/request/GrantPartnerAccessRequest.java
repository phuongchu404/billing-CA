package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GrantPartnerAccessRequest {

    @NotNull(message = "Partner user ID is required")
    @Positive(message = "Partner user ID must be positive")
    private Long partnerUserId;

    @NotNull(message = "Group ID is required")
    private Long groupId;
}


