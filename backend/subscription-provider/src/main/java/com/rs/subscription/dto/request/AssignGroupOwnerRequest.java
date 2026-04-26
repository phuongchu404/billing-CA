package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignGroupOwnerRequest {
    /** userId nhân viên phụ trách. Null hoặc blank = xoá owner. */
    @Size(max = 36)
    private String ownerUserId;
}
