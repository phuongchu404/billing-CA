package com.rs.subscription.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class IndividualUsageTrackingResponse {
    private IndividualUsageStatsResponse stats;
    private List<IndividualUsageRowResponse> list;
    private String lastUpdated;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    @Data
    public static class IndividualUsageStatsResponse {
        private long accounts;
        private long plansBought;
        private long signings;
        private long ctsIndividual;
        private long ctsOrg;
        private long ctsIndividualOfOrg;
    }
}
