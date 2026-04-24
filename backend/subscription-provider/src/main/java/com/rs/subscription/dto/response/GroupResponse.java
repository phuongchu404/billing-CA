package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupResponse {
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String username;
    private String contactEmail;
    private String contactPhone;
    private String refContractNo;
    private String picEmails;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private Long memberCount;
}
