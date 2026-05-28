package com.rs.subscription.service;

import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;

public interface IndividualUsageTrackingService {

    IndividualUsageTrackingResponse getUsageTracking(
            String purchasedAt, String ctsType, String ctsDuration,
            String ctsStatus, String plan,
            int page, int size, String sortBy, String sortDir);
}
