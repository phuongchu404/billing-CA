package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupContactRequest {
    private Long groupId;

    @NotBlank
    private String contactType;

    @NotBlank
    @Email
    private String email;

    private String fullName;
    private String phone;
    private Boolean isPrimary = false;
    private Boolean receiveUsageAlert = false;
    private Boolean isActive = true;
}
