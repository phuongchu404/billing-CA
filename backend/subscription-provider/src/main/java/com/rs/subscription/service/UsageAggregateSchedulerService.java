package com.rs.subscription.service;

import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.entity.UsageAggregateRollupLog;
import com.rs.subscription.repository.UsageAggregateRepository;
import com.rs.subscription.repository.UsageAggregateRollupLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UsageAggregateSchedulerService {

    void rollupCurrentMonth();

    int rollupPeriod(String periodKey);
}
