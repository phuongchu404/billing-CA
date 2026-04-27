package com.rs.subscription.service;

import com.rs.subscription.dto.response.IndividualUsageRowResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public interface IndividualUsageTrackingService {

    IndividualUsageTrackingResponse getUsageTracking();
}
