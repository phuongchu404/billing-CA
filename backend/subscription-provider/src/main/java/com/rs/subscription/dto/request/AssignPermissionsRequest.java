package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotNull
    private List<Long> permissionIds;
}
