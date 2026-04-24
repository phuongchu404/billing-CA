package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Partner code is required")
    @Size(max = 100)
    private String groupCode;

    @NotBlank(message = "Partner name is required")
    @Size(max = 200)
    private String groupName;

    @NotBlank(message = "Username is required")
    @Size(max = 100)
    private String username;

    /** Required on create; optional on update (omit to keep existing password). */
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Space-separated list of contact email addresses. */
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    /** Space-separated list of contact phone numbers. */
    private String contactPhone;

    /** Reference contract number. */
    @Size(max = 200)
    private String refContractNo;

    /** Space-separated list of Person In Charge email addresses. */
    private String picEmails;
}
