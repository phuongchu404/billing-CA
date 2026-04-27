package com.rs.subscription.dto.request;

import lombok.Data;

@Data
public class RejectApprovalRequest {
    private String rejectedBy;
    private String reason;
}
