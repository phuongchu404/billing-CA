package com.rs.subscription.dto.request;

import lombok.Data;

@Data
public class RevisionApprovalRequest {
    private String requestedBy;
    private String reason;
}
