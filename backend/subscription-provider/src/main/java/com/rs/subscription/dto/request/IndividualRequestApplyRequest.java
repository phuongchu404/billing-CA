package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IndividualRequestApplyRequest {
    @NotBlank(message = "Ngày bắt đầu không được để trống")
    private String applyFrom;
    @NotBlank(message = "Ngày kết thúc không được để trống")
    private String applyUntil;
    private String requestedBy;
}
