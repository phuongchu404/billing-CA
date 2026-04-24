package com.rs.subscription.repository;

import com.rs.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByOrderByCreatedAtDesc();
    List<Subscription> findBySubscriberTypeOrderByCreatedAtDesc(String subscriberType);
    List<Subscription> findByStatusOrderByCreatedAtDesc(String status);
    List<Subscription> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId);
    Subscription findFirstByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(Long groupPlanAssignmentId);
    Subscription findFirstByRetailPlanScheduleRetailPlanScheduleIdAndUserIdOrderByCreatedAtDesc(Long retailPlanScheduleId, String userId);
}
