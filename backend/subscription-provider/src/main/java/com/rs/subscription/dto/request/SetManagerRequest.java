package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SetManagerRequest {
    /** userId manager trực tiếp. Null hoặc blank = xoá manager. */
    @Positive(message = "Manager user ID must be positive")
    private Long managerUserId;
}


