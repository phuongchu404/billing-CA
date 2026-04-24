package com.rs.subscription.repository;

import com.rs.subscription.entity.CertificateProvisioningRecord;
import com.rs.subscription.entity.CertificateProvisioningRecord.ProvisioningStatus;
import com.rs.subscription.entity.Subscription;
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
    Optional<CertificateProvisioningRecord> findBySubscriptionSubscriptionIdAndUserId(Long subscriptionId, String userId);
    List<CertificateProvisioningRecord> findByStatus(ProvisioningStatus status);

    @Query("SELECT c FROM CertificateProvisioningRecord c WHERE c.status = 'FAILED' AND c.retryCount < :maxRetries")
    List<CertificateProvisioningRecord> findRetryEligible(@Param("maxRetries") int maxRetries);

    Optional<CertificateProvisioningRecord> findTopByUserIdOrderByCreatedAtDesc(String userId);
    List<CertificateProvisioningRecord> findBySubscriptionSubscriptionId(Long subscriptionId);
    Optional<CertificateProvisioningRecord> findByCertificateId(String certificateId);

    @Query(value = "SELECT c FROM CertificateProvisioningRecord c " +
                   "JOIN FETCH c.subscription s JOIN FETCH s.plan p " +
                   "WHERE (:status IS NULL OR c.status = :status) " +
                   "AND (:userId IS NULL OR c.userId LIKE %:userId%)",
           countQuery = "SELECT COUNT(c) FROM CertificateProvisioningRecord c " +
                        "WHERE (:status IS NULL OR c.status = :status) " +
                        "AND (:userId IS NULL OR c.userId LIKE %:userId%)")
    Page<CertificateProvisioningRecord> findAllWithFilters(
        @Param("status") ProvisioningStatus status,
        @Param("userId") String userId,
        Pageable pageable);

    @Modifying
    @Query("UPDATE CertificateProvisioningRecord c SET c.usageCount = c.usageCount + 1 WHERE c.certificateId = :certificateId")
    int incrementUsageCount(@Param("certificateId") String certificateId);

    @Query("SELECT COUNT(c) FROM CertificateProvisioningRecord c WHERE c.subscription.subscriberType = :type AND c.createdAt >= :since")
    long countBySubscriberTypeAndCreatedAtAfter(@Param("type") Subscription.SubscriberType type, @Param("since") LocalDateTime since);
}
