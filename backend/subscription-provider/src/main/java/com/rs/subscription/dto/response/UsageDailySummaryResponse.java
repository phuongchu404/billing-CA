package com.rs.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UsageDailySummaryResponse {
    private LocalDate usageDate;
    private int usageCount;
    private int distinctUsers;
}
