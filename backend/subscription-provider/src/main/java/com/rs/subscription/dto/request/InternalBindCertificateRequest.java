package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InternalBindCertificateRequest {
    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    private Long userId;

    @NotBlank(message = "Certificate ID is required")
    private String certificateId;

    @NotBlank(message = "Key ID is required")
    private String keyId;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
}


