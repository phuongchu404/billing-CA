package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CertificateUsageResponse {
    private Long id;
    private String certificateId;
    private String userId;
    private LocalDateTime usedAt;
}
