package com.rs.subscription.repository;

import com.rs.subscription.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByExternalReference(String externalReference);
    boolean existsByExternalReference(String externalReference);
    List<PaymentRecord> findBySubscriptionSubscriptionIdOrderByPaidAtDesc(Long subscriptionId);
    List<PaymentRecord> findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByPaidAtDesc(Long groupPlanAssignmentId);
    List<PaymentRecord> findBySettlementStatementSettlementStatementIdOrderByPaidAtDesc(Long settlementStatementId);
}
