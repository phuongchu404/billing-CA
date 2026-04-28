package com.rs.subscription.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 200)
    private String fullName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Positive(message = "Manager user ID must be positive")
    private Long managerUserId;

    @AssertTrue(message = "Passwords do not match")
    private boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) return true;
        return password.equals(confirmPassword);
    }

    private List<Long> roleIds;
}
