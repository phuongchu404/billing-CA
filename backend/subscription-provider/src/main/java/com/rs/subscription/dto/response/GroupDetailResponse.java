package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDetailResponse {
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String status;
    /** Danh sách email nhân sự phụ trách (contactType = PIC) */
    private List<String> picEmails;
    /** Danh sách email đại diện đại lý (contactType = CONTRACT hoặc BILLING) */
    private List<String> contactEmails;
    private String refContractNo;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long ownerUserId;
    private String ownerName;
}


