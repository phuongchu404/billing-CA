package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^ROLE_.*", message = "Role name must start with ROLE_")
    @Size(max = 100)
    private String roleName;

    @NotBlank(message = "Display name is required")
    @Size(max = 200)
    private String displayName;

    private String description;
}
