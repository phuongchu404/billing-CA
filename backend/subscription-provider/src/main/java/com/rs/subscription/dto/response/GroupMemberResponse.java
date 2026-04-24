package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GroupMemberResponse {
    private Long id;
    private Long groupId;
    private String userId;
    private String role;
    private LocalDateTime joinedAt;
    private String addedBy;
    private LocalDate memberStartDate;
    private LocalDate memberEndDate;
}
