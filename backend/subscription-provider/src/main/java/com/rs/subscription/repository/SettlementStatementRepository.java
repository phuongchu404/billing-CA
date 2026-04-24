package com.rs.subscription.repository;

import com.rs.subscription.entity.SettlementStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementStatementRepository extends JpaRepository<SettlementStatement, Long> {
    List<SettlementStatement> findByGroupGroupIdOrderByCreatedAtDesc(Long groupId);
    List<SettlementStatement> findByStatusOrderByCreatedAtDesc(String status);
}
