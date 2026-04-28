package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AssignGroupOwnerRequest {
    /** userId nhân viên phụ trách. Null hoặc blank = xoá owner. */
    @Positive(message = "Owner user ID must be positive")
    private Long ownerUserId;
}


