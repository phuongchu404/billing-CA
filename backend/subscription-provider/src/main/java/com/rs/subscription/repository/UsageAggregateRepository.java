package com.rs.subscription.repository;

import com.rs.subscription.entity.UsageAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsageAggregateRepository extends JpaRepository<UsageAggregate, Long> {
    List<UsageAggregate> findByAggregateScopeOrderByPeriodKeyDesc(String aggregateScope);
    List<UsageAggregate> findByAggregateScopeAndScopeIdOrderByPeriodKeyDesc(String aggregateScope, Long scopeId);
    Optional<UsageAggregate> findByAggregateScopeAndScopeIdAndPeriodTypeAndPeriodKey(
        String aggregateScope, Long scopeId, String periodType, String periodKey
    );

    /** Tổng hợp ctsCreated và signingUsed cho tất cả scope_id trong danh sách assignmentIds */
    @Query("SELECT COALESCE(SUM(u.certificatesCreated), 0), COALESCE(SUM(u.signingUsed), 0) " +
           "FROM UsageAggregate u " +
           "WHERE u.aggregateScope = 'GROUP_ASSIGNMENT' AND u.scopeId IN :assignmentIds")
    Object[] sumUsageByAssignmentIds(@Param("assignmentIds") List<Long> assignmentIds);

    /** Lấy tất cả usage records cho một assignment cụ thể */
    List<UsageAggregate> findByAggregateScopeAndScopeId(String aggregateScope, Long scopeId);
}
