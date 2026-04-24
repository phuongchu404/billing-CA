package com.rs.subscription.repository;

import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.Subscription.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Subscription> findByUserIdAndStatusInOrderByCreatedAtDesc(String userId, Collection<SubscriptionStatus> statuses);

    @Query("SELECT s FROM Subscription s WHERE s.group.groupId = :groupId AND s.status = :status ORDER BY s.createdAt DESC")
    List<Subscription> findByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.group.groupId = :groupId ORDER BY s.createdAt DESC")
    List<Subscription> findAllByGroupGroupId(@Param("groupId") Long groupId);

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :date AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :from AND :to AND s.status = 'ACTIVE'")
    List<Subscription> findExpiringSoon(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :from AND :to AND s.status = 'ACTIVE' AND s.subscriberType = :type")
    List<Subscription> findExpiringSoonBySubscriberType(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("type") Subscription.SubscriberType type);

    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    long countByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE LOWER(COALESCE(s.userId,'')) LIKE %:q% OR LOWER(s.plan.planCode) LIKE %:q%")
    Page<Subscription> searchByQuery(@Param("q") String q, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND (LOWER(COALESCE(s.userId,'')) LIKE %:q% OR LOWER(s.plan.planCode) LIKE %:q%)")
    Page<Subscription> searchByStatusAndQuery(@Param("status") SubscriptionStatus status, @Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriberType = 'INDIVIDUAL' AND s.status = :status")
    long countIndividualByStatus(@Param("status") SubscriptionStatus status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriberType = 'GROUP' AND s.status = :status")
    long countGroupByStatus(@Param("status") SubscriptionStatus status);

    Page<Subscription> findBySubscriberType(Subscription.SubscriberType subscriberType, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.subscriberType = :type AND s.status = :status")
    Page<Subscription> findBySubscriberTypeAndStatus(@Param("type") Subscription.SubscriberType type, @Param("status") SubscriptionStatus status, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.subscriberType = :type AND (LOWER(COALESCE(s.userId,'')) LIKE %:q% OR LOWER(s.plan.planCode) LIKE %:q%)")
    Page<Subscription> searchBySubscriberTypeAndQuery(@Param("type") Subscription.SubscriberType type, @Param("q") String q, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.subscriberType = :type AND s.status = :status AND (LOWER(COALESCE(s.userId,'')) LIKE %:q% OR LOWER(s.plan.planCode) LIKE %:q%)")
    Page<Subscription> searchBySubscriberTypeAndStatusAndQuery(@Param("type") Subscription.SubscriberType type, @Param("status") SubscriptionStatus status, @Param("q") String q, Pageable pageable);

    Optional<Subscription> findByPaymentReference(String paymentReference);

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findAllByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriberType = :type AND s.createdAt >= :since")
    long countBySubscriberTypeAndCreatedAtAfter(@Param("type") Subscription.SubscriberType type, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.endDate BETWEEN :from AND :to AND s.status = 'ACTIVE' AND s.subscriberType = :type")
    long countExpiringSoonBySubscriberType(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("type") Subscription.SubscriberType type);
}
