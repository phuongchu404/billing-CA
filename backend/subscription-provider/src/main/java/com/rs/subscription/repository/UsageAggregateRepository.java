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

    /** Batch: usage per assignment — trả về [scopeId, ctsCreated, signingUsed] cho từng assignment */
    @Query(value = "SELECT u.scope_id, COALESCE(SUM(u.certificates_created), 0), COALESCE(SUM(u.signing_used), 0) " +
                   "FROM usage_aggregates u " +
                   "WHERE u.aggregate_scope = 'GROUP_ASSIGNMENT' AND u.scope_id IN :assignmentIds " +
                   "GROUP BY u.scope_id",
           nativeQuery = true)
    List<Object[]> sumUsagePerAssignment(@Param("assignmentIds") List<Long> assignmentIds);

    /** Lấy tất cả usage records cho một assignment cụ thể */
    List<UsageAggregate> findByAggregateScopeAndScopeId(String aggregateScope, Long scopeId);

    /** Lấy tất cả GROUP scope aggregates cho một tháng cụ thể */
    @Query("SELECT u FROM UsageAggregate u WHERE u.aggregateScope = 'GROUP' AND u.periodType = 'MONTH' AND u.periodKey = :periodKey")
    List<UsageAggregate> findGroupMonthlyByPeriodKey(@Param("periodKey") String periodKey);

    @Query("SELECT COALESCE(SUM(u.certificatesCreated), 0), COALESCE(SUM(u.signingUsed), 0) " +
           "FROM UsageAggregate u " +
           "WHERE u.aggregateScope = 'GROUP' AND u.periodType = 'MONTH' AND u.periodKey = :periodKey")
    Object[] sumGroupMonthlyByPeriodKey(@Param("periodKey") String periodKey);
}
