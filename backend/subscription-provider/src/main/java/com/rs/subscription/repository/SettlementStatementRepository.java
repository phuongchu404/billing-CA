package com.rs.subscription.repository;

import com.rs.subscription.entity.SettlementStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SettlementStatementRepository extends JpaRepository<SettlementStatement, Long> {
    List<SettlementStatement> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<SettlementStatement> findByStatusOrderByCreatedAtDesc(String status);

    /** Export query: lọc theo groupId và/hoặc khoảng tháng. Null = không lọc. */
    @Query("SELECT s FROM SettlementStatement s " +
           "WHERE (:groupId IS NULL OR s.group.groupId = :groupId) " +
           "AND (:start IS NULL OR s.fromDate >= :start) " +
           "AND (:end IS NULL OR s.fromDate <= :end) " +
           "ORDER BY s.group.groupCode ASC, s.createdAt DESC")
    List<SettlementStatement> findForExport(
        @Param("groupId") Long groupId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
}
