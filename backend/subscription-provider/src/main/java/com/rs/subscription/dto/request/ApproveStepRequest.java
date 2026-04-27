package com.rs.subscription.dto.request;

import lombok.Data;

@Data
public class ApproveStepRequest {
    private String approvedBy;
    private String comment;
}
