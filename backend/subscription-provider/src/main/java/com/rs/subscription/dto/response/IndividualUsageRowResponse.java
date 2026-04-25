package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class IndividualUsageRowResponse {
    private Long id;
    private String account;
    private String purchasedAt;
    /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
    private String ctsType;
    private Integer ctsDuration;
    /** ACTIVE | PENDING_ACTIVATE | PENDING_APPROVE | REVOKED | EXPIRED */
    private String ctsStatus;
    private long signings;
    private String plan;
    private long fee;
}
