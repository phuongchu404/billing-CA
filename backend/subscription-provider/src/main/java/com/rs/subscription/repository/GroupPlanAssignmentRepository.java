package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupPlanAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GroupPlanAssignmentRepository extends JpaRepository<GroupPlanAssignment, Long> {
    List<GroupPlanAssignment> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<GroupPlanAssignment> findByAssignmentStatusOrderByCreatedAtDesc(String assignmentStatus);
    Optional<GroupPlanAssignment> findFirstByGroupGroupIdAndPlanTemplatePlanTemplateIdAndAssignmentStatus(
        Long groupId,
        Long planTemplateId,
        String assignmentStatus);

    /** Lấy assignment đang ACTIVE của một group (thường chỉ có 1) */
    Optional<GroupPlanAssignment> findFirstByGroupGroupIdAndAssignmentStatusOrderByActivatedAtDesc(Long groupId, String status);

    /** Lịch sử: các assignment đã được duyệt trở lên của group */
    @Query("SELECT a FROM GroupPlanAssignment a WHERE a.group.groupId = :groupId " +
           "AND a.assignmentStatus IN ('APPROVED','ACTIVE','STOPPED','EXPIRED') " +
           "ORDER BY a.requestedAt DESC")
    List<GroupPlanAssignment> findHistoryByGroupId(@Param("groupId") Long groupId);

    /** Scheduler: APPROVED và đã đến ngày bắt đầu → cần chuyển sang ACTIVE */
    @Query("SELECT a FROM GroupPlanAssignment a WHERE a.assignmentStatus = 'APPROVED' AND a.applyFrom <= :today")
    List<GroupPlanAssignment> findApprovedReadyToActivate(@Param("today") LocalDate today);

    /** Scheduler: ACTIVE và đã qua ngày kết thúc → cần chuyển sang EXPIRED */
    @Query("SELECT a FROM GroupPlanAssignment a WHERE a.assignmentStatus = 'ACTIVE' AND a.applyTo < :today")
    List<GroupPlanAssignment> findActiveReadyToExpire(@Param("today") LocalDate today);

    /** Lấy ACTIVE assignments của một group (có thể có nhiều - multi-plan) */
    List<GroupPlanAssignment> findByGroupGroupIdAndAssignmentStatus(Long groupId, String status);

    /** Batch: ACTIVE assignments cho nhiều group cùng lúc (tránh N+1) */
    @Query("SELECT a FROM GroupPlanAssignment a WHERE a.group.groupId IN :groupIds AND a.assignmentStatus = :status")
    List<GroupPlanAssignment> findByGroupIdsAndStatus(
        @Param("groupIds") List<Long> groupIds,
        @Param("status") String status);

    /** Batch: ACTIVE assignments kèm PlanTemplate (JOIN FETCH tránh lazy-load N+1) */
    @Query("SELECT a FROM GroupPlanAssignment a JOIN FETCH a.planTemplate WHERE a.group.groupId IN :groupIds AND a.assignmentStatus = :status")
    List<GroupPlanAssignment> findByGroupIdsAndStatusWithPlan(
        @Param("groupIds") List<Long> groupIds,
        @Param("status") String status);

    /** Đại lý có gói sắp hết hạn trong ngưỡng và chưa có gói tiếp theo */
    @Query("SELECT a FROM GroupPlanAssignment a " +
           "WHERE a.assignmentStatus = 'ACTIVE' " +
           "AND a.applyTo BETWEEN :today AND :threshold " +
           "AND NOT EXISTS (" +
           "  SELECT a2 FROM GroupPlanAssignment a2 " +
           "  WHERE a2.group = a.group " +
           "  AND a2.groupPlanAssignmentId != a.groupPlanAssignmentId " +
           "  AND a2.assignmentStatus IN ('APPROVED', 'ACTIVE') " +
           "  AND a2.applyFrom > a.applyTo" +
           ")")
    List<GroupPlanAssignment> findExpiringSoonWithNoSuccessor(
        @Param("today") LocalDate today,
        @Param("threshold") LocalDate threshold);
}
