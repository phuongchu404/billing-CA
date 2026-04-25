package com.rs.subscription.dto.request;

import lombok.Data;

@Data
public class IndividualApproveRequest {
    private String applyFrom;
    private String applyUntil;
    private String approvedBy;
}
