package com.rs.subscription.repository;

import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CertificateUsageRecordRepository extends JpaRepository<CertificateUsageRecord, Long> {

    Page<CertificateUsageRecord> findByCertificateIdOrderByUsedAtDesc(String certificateId, Pageable pageable);

    @Query("""
        SELECT CAST(r.usedAt AS LocalDate) AS usageDate,
               COUNT(r.id)               AS usageCount,
               COUNT(DISTINCT r.userId)  AS distinctUsers
        FROM CertificateUsageRecord r
        WHERE r.certificateId = :certId
          AND r.usedAt >= :from
          AND r.usedAt < :to
        GROUP BY CAST(r.usedAt AS LocalDate)
        ORDER BY CAST(r.usedAt AS LocalDate) DESC
        """)
    List<Object[]> findDailyUsage(
            @Param("certId") String certId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        SELECT COUNT(u) FROM CertificateUsageRecord u
        WHERE u.usedAt >= :since
          AND u.certificateId IN (
              SELECT c.certificateId FROM CertificateProvisioningRecord c
              WHERE c.subscription.subscriberType = :type
                AND c.certificateId IS NOT NULL
          )
        """)
    long countBySubscriberTypeAndUsedAtAfter(@Param("type") String type, @Param("since") LocalDateTime since);

    List<CertificateUsageRecord> findBySubscriptionGroupGroupIdAndUsedAtGreaterThanEqualAndUsedAtLessThanOrderByUsedAtAsc(
            Long groupId,
            LocalDateTime from,
            LocalDateTime to);

    /** Đếm lượt ký INDIVIDUAL theo tuần và cert_type trong tháng */
    @Query(nativeQuery = true,
           value = "SELECT WEEK(cur.used_at, 1) - WEEK(DATE_FORMAT(cur.used_at, '%Y-%m-01'), 1) + 1 AS week_num, " +
                   "cpr.cert_type, COUNT(cur.id) " +
                   "FROM certificate_usage_records cur " +
                   "JOIN certificate_provisioning_records cpr ON cpr.certificate_id = cur.certificate_id " +
                   "JOIN subscriptions s ON s.subscription_id = cur.subscription_id " +
                   "WHERE s.subscriber_type = 'INDIVIDUAL' " +
                   "AND cur.usage_type = 'SIGNING' " +
                   "AND cur.used_at >= :from AND cur.used_at < :to " +
                   "GROUP BY week_num, cpr.cert_type " +
                   "ORDER BY week_num, cpr.cert_type")
    List<Object[]> countWeeklySigningsByTypeForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    /** Batch: đếm lượt ký theo assignmentId + cert_type cho nhiều assignment cùng lúc. */
    @Query("SELECT c.groupPlanAssignment.groupPlanAssignmentId, c.certType, COUNT(u) " +
           "FROM CertificateUsageRecord u " +
           "JOIN CertificateProvisioningRecord c ON c.certificateId = u.certificateId " +
           "WHERE c.groupPlanAssignment.groupPlanAssignmentId IN :assignmentIds " +
           "AND u.usageType = 'SIGNING' " +
           "AND u.usedAt >= :from AND u.usedAt < :to " +
           "GROUP BY c.groupPlanAssignment.groupPlanAssignmentId, c.certType")
    List<Object[]> countSigningsByCertTypeGroupedByAssignment(
        @Param("assignmentIds") List<Long> assignmentIds,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(u) FROM CertificateUsageRecord u " +
           "WHERE u.usageType = 'SIGNING' AND u.usedAt >= :from AND u.usedAt < :to")
    long countSignings(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(u) FROM CertificateUsageRecord u " +
           "WHERE u.usageType = 'SIGNING' " +
           "AND u.groupPlanAssignment IS NOT NULL " +
           "AND u.usedAt >= :from AND u.usedAt < :to")
    long countGroupSignings(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Modifying
    @Query(value = """
        INSERT INTO certificate_usage_daily (certificate_id, usage_date, usage_count, distinct_users)
        SELECT certificate_id,
               DATE(used_at)        AS usage_date,
               COUNT(*)             AS usage_count,
               COUNT(DISTINCT user_id) AS distinct_users
        FROM certificate_usage_records
        WHERE used_at >= :from AND used_at < :to
        GROUP BY certificate_id, DATE(used_at)
        ON DUPLICATE KEY UPDATE
            usage_count    = VALUES(usage_count),
            distinct_users = VALUES(distinct_users),
            updated_at     = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertDailyRollup(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
