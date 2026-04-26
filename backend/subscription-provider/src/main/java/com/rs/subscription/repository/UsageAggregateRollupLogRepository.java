package com.rs.subscription.repository;

import com.rs.subscription.entity.UsageAggregateRollupLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageAggregateRollupLogRepository extends JpaRepository<UsageAggregateRollupLog, Long> {
}
