package com.rs.subscription.repository;

import com.rs.subscription.entity.UsageAggregateRollupLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsageAggregateRollupLogRepository extends JpaRepository<UsageAggregateRollupLog, Long> {
    Optional<UsageAggregateRollupLog> findTopByPeriodKeyOrderByRunAtDesc(String periodKey);
}
