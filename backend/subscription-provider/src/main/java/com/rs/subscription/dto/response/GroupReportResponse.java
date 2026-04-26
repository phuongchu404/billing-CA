package com.rs.subscription.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Collections;

@Data
public class GroupReportResponse {
    private GroupStatsSummary stats;
    private List<String> agencies;
    private CertDataByType certData;
    private List<Integer> signingData;
    private List<GrowthItem> growthData;
    private List<RatioItem> ratioData;
    private List<ExpiringGroupRow> expiringRows;
    private String lastUpdated;

    @Data
    public static class GroupStatsSummary {
        private int activePartners;
        private int newCts;
        private int signings;
        private int expiringSoon;
        private int newCtsPct;
        private int signingsPct;
    }

    @Data
    public static class CertDataByType {
        private List<Integer> individual;
        private List<Integer> organization;
        private List<Integer> individualOfOrg;
    }

    @Data
    public static class GrowthItem {
        private int current;
        private int prev;
        private int growth;
    }

    @Data
    public static class RatioItem {
        private String name;
        private double individual;
        private double organization;
        private double individualOfOrg;
    }
}
