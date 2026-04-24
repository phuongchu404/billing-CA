package com.rs.subscription.integration;

import lombok.Data;

@Data
public class CertificateProvisioningResult {
    private String certificateId;
    private String keyId;
    private String issuedAt;
    private String expiresAt;
}
