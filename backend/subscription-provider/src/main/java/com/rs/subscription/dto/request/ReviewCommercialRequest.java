package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCommercialRequest {
    @NotBlank
    private String actor;

    @NotBlank
    private String decision;

    private String note;
}
