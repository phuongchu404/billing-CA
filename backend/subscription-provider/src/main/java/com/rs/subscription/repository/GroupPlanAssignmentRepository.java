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
}
