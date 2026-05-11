package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartnerAccountRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    private String email;

    @Size(max = 200)
    private String fullName;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}
