package com.rs.subscription.dto.request;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddGroupMemberRequest {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    private String role = CommercialEnums.MemberRole.MEMBER.name();
}


