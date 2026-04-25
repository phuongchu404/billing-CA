package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class IndividualPlanConfigListItemResponse {
    private Long id;
    private String name;
    /** AVAILABLE | UNAVAILABLE | PENDING | APPROVED | APPLYING */
    private String status;
    private String applyFrom;
    private String applyUntil;
    private String updatedAt;
}
