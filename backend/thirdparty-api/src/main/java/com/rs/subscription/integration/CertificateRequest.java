package com.rs.subscription.integration;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class CertificateRequest {
    private String requestId;
    private Long subscriptionId;
    private String userId;
    private String fullName;
    private String email;
    private String nationalId;
    private String phoneNumber;
    private String organizationName;
    private String validFrom;
    private String validTo;
    private String planCode;
    private Map<String, Object> featureFlags;
}
