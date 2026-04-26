package com.rs.subscription.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class IndividualReportResponse {
    private IndividualStatsSummary stats;
    private List<String> weeks;
    private List<Integer> newCustChart;
    private ChartByType ctsChart;
    private ChartByType signingChart;
    private FailureChart failureChart;

    @Data
    public static class IndividualStatsSummary {
        private int activeCustomers;
        private int newCts;
        private int signings;
        private int uploads;
        private int uploadPct;
    }

    @Data
    public static class ChartByType {
        private List<Integer> individual;
        private List<Integer> organization;
        private List<Integer> individualOfOrg;
    }

    @Data
    public static class FailureChart {
        private List<Integer> pin;
        private List<Integer> otp;
        private List<Integer> moc;
    }
}
