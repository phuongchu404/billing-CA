package com.rs.subscription.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class IndividualPlanConfigSummaryResponse {
    private List<IndividualPlanConfigListItemResponse> list;
    private CurrentPlanInfo currentPlan;
    private NextPlanInfo nextPlan;
    private String lastUpdated;

    @Data
    public static class CurrentPlanInfo {
        private String name;
        private String applyUntil;
    }

    @Data
    public static class NextPlanInfo {
        private String name;
        private String applyFrom;
    }
}
