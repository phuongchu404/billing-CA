package com.rs.subscription.repository;

import com.rs.subscription.entity.AssignmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentAuditRepository extends JpaRepository<AssignmentAudit, Long> {
    List<AssignmentAudit> findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(Long groupPlanAssignmentId);
    List<AssignmentAudit> findByRetailPlanScheduleRetailPlanScheduleIdOrderByCreatedAtDesc(Long retailPlanScheduleId);

    @Query("SELECT a FROM AssignmentAudit a WHERE a.retailPlanSchedule.retailPlanScheduleId IN :ids ORDER BY a.createdAt DESC")
    List<AssignmentAudit> findByRetailPlanScheduleIdIn(@Param("ids") List<Long> ids);
}
