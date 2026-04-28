package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CertificateProvisioningResponse {
    private Long provisioningRecordId;
    private Long userId;
    private Integer certType;
    private String certificateId;
    private String keyId;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Integer retryCount;
    private Integer usageCount;
    private String failureReason;
    private Long subscriptionId;
    private String planName;
    private LocalDateTime createdAt;
}


