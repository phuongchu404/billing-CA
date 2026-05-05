package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateDocumentUploadEventRequest {
    private Long userId;
    private Long subscriptionId;
    private String certificateId;

    @NotBlank
    private String documentId;

    private String uploadStatus = "SUCCESS";
    private LocalDateTime uploadedAt;
}
