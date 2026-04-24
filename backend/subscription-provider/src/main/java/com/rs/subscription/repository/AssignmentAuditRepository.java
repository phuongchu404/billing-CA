package com.rs.subscription.repository;

import com.rs.subscription.entity.AssignmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentAuditRepository extends JpaRepository<AssignmentAudit, Long> {
    List<AssignmentAudit> findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(Long groupPlanAssignmentId);
    List<AssignmentAudit> findByRetailPlanScheduleRetailPlanScheduleIdOrderByCreatedAtDesc(Long retailPlanScheduleId);
}
