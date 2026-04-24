package com.rs.subscription.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BindCertificateRequest {
    @NotBlank(message = "Certificate ID is required")
    private String certificateId;

    @NotBlank(message = "Key ID is required")
    private String keyId;

    private Integer certType;

    private LocalDateTime issuedAt;

    @NotNull(message = "Certificate expiry date is required")
    private LocalDateTime expiresAt;

    @AssertTrue(message = "expiresAt must be after issuedAt")
    private boolean isExpiresAtAfterIssuedAt() {
        if (issuedAt == null || expiresAt == null) return true;
        return expiresAt.isAfter(issuedAt);
    }
}
