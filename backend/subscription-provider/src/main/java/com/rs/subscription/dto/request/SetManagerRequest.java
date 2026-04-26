package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetManagerRequest {
    /** userId manager trực tiếp. Null hoặc blank = xoá manager. */
    @Size(max = 36)
    private String managerUserId;
}
