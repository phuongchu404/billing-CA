package com.rs.subscription.repository;

import com.rs.subscription.entity.ApprovalRequestStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestStepRepository extends JpaRepository<ApprovalRequestStep, Long> {

    List<ApprovalRequestStep> findByApprovalRequestIdOrderByStepLevelAsc(Long approvalRequestId);

    Optional<ApprovalRequestStep> findByApprovalRequestIdAndStepLevel(Long approvalRequestId, Integer stepLevel);

    List<ApprovalRequestStep> findByApprovalRequestIdAndStatus(Long approvalRequestId, String status);
}
