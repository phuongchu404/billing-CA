package com.rs.subscription.repository;

import com.rs.subscription.entity.CertificateProvisioningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CertificateProvisioningRepository extends JpaRepository<CertificateProvisioningRecord, Long> {
    Optional<CertificateProvisioningRecord> findBySubscriptionSubscriptionIdAndUserId(Long subscriptionId, Long userId);
    List<CertificateProvisioningRecord> findByStatus(String status);

    @Query("SELECT c FROM CertificateProvisioningRecord c WHERE c.status = 'FAILED' AND c.retryCount < :maxRetries")
    List<CertificateProvisioningRecord> findRetryEligible(@Param("maxRetries") int maxRetries);

    Optional<CertificateProvisioningRecord> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    List<CertificateProvisioningRecord> findBySubscriptionSubscriptionId(Long subscriptionId);
    Optional<CertificateProvisioningRecord> findByCertificateId(String certificateId);

    @Query(value = "SELECT c FROM CertificateProvisioningRecord c " +
                   "JOIN FETCH c.subscription s JOIN FETCH s.planTemplate p " +
                   "WHERE (:status IS NULL OR c.status = :status) " +
                   "AND (:userId IS NULL OR c.userId = :userId)",
           countQuery = "SELECT COUNT(c) FROM CertificateProvisioningRecord c " +
                        "WHERE (:status IS NULL OR c.status = :status) " +
                        "AND (:userId IS NULL OR c.userId = :userId)")
    Page<CertificateProvisioningRecord> findAllWithFilters(
        @Param("status") String status,
        @Param("userId") Long userId,
        Pageable pageable);

    @Modifying
    @Query("UPDATE CertificateProvisioningRecord c SET c.usageCount = c.usageCount + 1 WHERE c.certificateId = :certificateId")
    int incrementUsageCount(@Param("certificateId") String certificateId);

    @Query("SELECT COUNT(c) FROM CertificateProvisioningRecord c WHERE c.subscription.subscriberType = :type AND c.createdAt >= :since")
    long countBySubscriberTypeAndCreatedAtAfter(@Param("type") String type, @Param("since") LocalDateTime since);

    /** Batch: đếm chứng thư theo assignmentId + cert_type cho nhiều assignment cùng lúc (tránh N+1) */
    @Query("SELECT c.groupPlanAssignment.groupPlanAssignmentId, c.certType, COUNT(c) " +
           "FROM CertificateProvisioningRecord c " +
           "WHERE c.groupPlanAssignment.groupPlanAssignmentId IN :assignmentIds " +
           "AND c.status = 'COMPLETED' " +
           "AND c.issuedAt >= :from AND c.issuedAt < :to " +
           "GROUP BY c.groupPlanAssignment.groupPlanAssignmentId, c.certType")
    List<Object[]> countCompletedByCertTypeGroupedByAssignment(
        @Param("assignmentIds") List<Long> assignmentIds,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    /** Đếm chứng thư theo cert_type cho GROUP assignments trong khoảng thời gian */
    @Query("SELECT c.certType, COUNT(c) FROM CertificateProvisioningRecord c " +
           "WHERE c.groupPlanAssignment.groupPlanAssignmentId IN :assignmentIds " +
           "AND c.status = 'COMPLETED' " +
           "AND c.issuedAt >= :from AND c.issuedAt < :to " +
           "GROUP BY c.certType")
    List<Object[]> countCompletedByCertTypeForAssignments(
        @Param("assignmentIds") List<Long> assignmentIds,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    /** Đếm chứng thư INDIVIDUAL theo tuần và cert_type trong tháng */
    @Query(nativeQuery = true,
           value = "SELECT WEEK(c.issued_at, 1) - WEEK(DATE_FORMAT(c.issued_at, '%Y-%m-01'), 1) + 1 AS week_num, " +
                   "c.cert_type, COUNT(c.id) " +
                   "FROM certificate_provisioning_records c " +
                   "JOIN subscriptions s ON s.subscription_id = c.subscription_id " +
                   "WHERE s.subscriber_type = 'INDIVIDUAL' " +
                   "AND c.status = 'COMPLETED' " +
                   "AND c.issued_at >= :from AND c.issued_at < :to " +
                   "GROUP BY week_num, c.cert_type " +
                   "ORDER BY week_num, c.cert_type")
    List<Object[]> countWeeklyCertsByTypeForIndividual(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    /** Đếm khách hàng mới (subscription) theo tuần trong tháng */
    @Query(nativeQuery = true,
           value = "SELECT WEEK(s.created_at, 1) - WEEK(DATE_FORMAT(s.created_at, '%Y-%m-01'), 1) + 1 AS week_num, " +
                   "COUNT(DISTINCT s.user_id) " +
                   "FROM subscriptions s " +
                   "WHERE s.subscriber_type = 'INDIVIDUAL' " +
                   "AND s.created_at >= :from AND s.created_at < :to " +
                   "GROUP BY week_num " +
                   "ORDER BY week_num")
    List<Object[]> countWeeklyNewCustomers(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}



