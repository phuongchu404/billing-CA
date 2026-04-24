package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AssignRolesRequest {
    @NotNull
    private List<Long> roleIds;
}
