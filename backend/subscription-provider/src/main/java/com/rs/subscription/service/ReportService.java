package com.rs.subscription.service;

import com.rs.subscription.dto.response.ExpiringGroupRow;
import com.rs.subscription.dto.response.GroupReportResponse;
import com.rs.subscription.dto.response.IndividualReportResponse;
import com.rs.subscription.entity.CertificateProvisioningRecord.CertType;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReportService {

    GroupReportResponse getGroupReport(String periodKey);

    List<ExpiringGroupRow> getExpiringSoon();

    IndividualReportResponse getIndividualReport(String periodKey);
}
