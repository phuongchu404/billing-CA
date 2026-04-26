package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartnerGroupAccessResponse {
    private Long id;
    private String partnerUserId;
    private String partnerName;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String grantedBy;
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
    private boolean active;
}
