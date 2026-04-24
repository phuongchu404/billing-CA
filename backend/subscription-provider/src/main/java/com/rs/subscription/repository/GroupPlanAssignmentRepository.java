package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupPlanAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupPlanAssignmentRepository extends JpaRepository<GroupPlanAssignment, Long> {
    List<GroupPlanAssignment> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<GroupPlanAssignment> findByAssignmentStatusOrderByCreatedAtDesc(String assignmentStatus);

    /** Lấy assignment đang ACTIVE của một group (thường chỉ có 1) */
    Optional<GroupPlanAssignment> findFirstByGroupGroupIdAndAssignmentStatusOrderByActivatedAtDesc(Long groupId, String status);

    /** Lịch sử: các assignment đã kết thúc (ACTIVE hoặc STOPPED) của group */
    @Query("SELECT a FROM GroupPlanAssignment a WHERE a.group.groupId = :groupId " +
           "AND a.assignmentStatus IN ('ACTIVE','STOPPED','EXPIRED') " +
           "ORDER BY a.requestedAt DESC")
    List<GroupPlanAssignment> findHistoryByGroupId(@Param("groupId") Long groupId);
}
