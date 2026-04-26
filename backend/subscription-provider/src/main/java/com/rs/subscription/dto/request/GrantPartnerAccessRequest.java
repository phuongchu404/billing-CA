package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GrantPartnerAccessRequest {

    @NotBlank(message = "Partner user ID is required")
    @Size(max = 36)
    private String partnerUserId;

    @NotNull(message = "Group ID is required")
    private Long groupId;
}
