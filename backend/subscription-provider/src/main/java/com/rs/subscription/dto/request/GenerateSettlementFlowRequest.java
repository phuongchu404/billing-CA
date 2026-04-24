package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GenerateSettlementFlowRequest {
    @NotBlank
    private String actor;

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;

    private Boolean finalizeNow = false;
    private String currency = "VND";
}
