package com.rs.subscription.service;

import com.rs.subscription.dto.response.ReportHealthResponse;

public interface ReportHealthService {
    ReportHealthResponse getHealth(String periodKey);
}
