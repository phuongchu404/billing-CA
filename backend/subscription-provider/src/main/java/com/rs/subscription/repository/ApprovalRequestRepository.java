package com.rs.subscription.repository;

import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequest.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Page<ApprovalRequest> findByStatus(ApprovalStatus status, Pageable pageable);

    Page<ApprovalRequest> findByRequestedBy(String requestedBy, Pageable pageable);

    Page<ApprovalRequest> findByStatusAndRequestedBy(ApprovalStatus status, String requestedBy, Pageable pageable);
}
