package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 200)
    private String fullName;
}
