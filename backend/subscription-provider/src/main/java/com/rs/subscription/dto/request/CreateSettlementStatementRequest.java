package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSettlementStatementRequest {
    @NotNull
    private Long groupId;

    private LocalDate fromDate;
    private LocalDate toDate;
    private String status = "DRAFT";
    private Integer totalCertificates = 0;
    private Integer totalSignings = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String currency = "VND";

    @NotBlank
    private String generatedBy;
}
