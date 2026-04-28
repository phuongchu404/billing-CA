package com.rs.subscription.repository;

import com.rs.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByOrderByCreatedAtDesc();
    List<Subscription> findBySubscriberTypeOrderByCreatedAtDesc(String subscriberType);
    List<Subscription> findByStatusOrderByCreatedAtDesc(String status);
    List<Subscription> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);
    Subscription findFirstByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(Long groupPlanAssignmentId);
    Subscription findFirstByRetailPlanScheduleRetailPlanScheduleIdAndUserIdOrderByCreatedAtDesc(Long retailPlanScheduleId, Long userId);

    @Query("SELECT COUNT(DISTINCT s.userId) FROM Subscription s " +
           "WHERE s.subscriberType = 'INDIVIDUAL' AND s.status = 'ACTIVE' AND s.userId IS NOT NULL")
    long countActiveIndividualCustomers();
}



