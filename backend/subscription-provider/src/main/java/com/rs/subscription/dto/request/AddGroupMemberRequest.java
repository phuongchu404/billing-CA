package com.rs.subscription.dto.request;

import com.rs.subscription.entity.GroupMember.MemberRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddGroupMemberRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    private MemberRole role = MemberRole.MEMBER;
}
