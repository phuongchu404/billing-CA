package com.rs.subscription.repository;

import com.rs.subscription.entity.CertificateAuthFailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CertificateAuthFailureRecordRepository extends JpaRepository<CertificateAuthFailureRecord, Long> {

    @Query(nativeQuery = true,
           value = "SELECT WEEK(f.failed_at, 1) - WEEK(DATE_FORMAT(f.failed_at, '%Y-%m-01'), 1) + 1 AS week_num, " +
                   "f.failure_type, COUNT(f.id) " +
                   "FROM certificate_auth_failure_records f " +
                   "JOIN subscriptions s ON s.subscription_id = f.subscription_id " +
                   "WHERE s.subscriber_type = 'INDIVIDUAL' " +
                   "AND f.failed_at >= :from AND f.failed_at < :to " +
                   "GROUP BY week_num, f.failure_type " +
                   "ORDER BY week_num, f.failure_type")
    List<Object[]> countWeeklyFailuresByTypeForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}
