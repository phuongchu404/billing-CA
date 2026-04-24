package com.rs.subscription.repository;

import com.rs.subscription.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByPlanCode(String planCode);
    List<Plan> findByIsActiveTrue();
    @Query("SELECT p FROM Plan p WHERE p.isActive = true AND p.isGroupPlan = false")
    List<Plan> findActiveIndividualPlans();
    @Query("SELECT p FROM Plan p WHERE p.isActive = true AND p.isGroupPlan = true")
    List<Plan> findActiveGroupPlans();
    boolean existsByPlanCode(String planCode);
}
