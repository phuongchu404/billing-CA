package com.rs.subscription.repository;

import com.rs.subscription.entity.ApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Page<ApprovalRequest> findByStatus(String status, Pageable pageable);

    Page<ApprovalRequest> findByRequestedBy(String requestedBy, Pageable pageable);

    Page<ApprovalRequest> findByStatusAndRequestedBy(String status, String requestedBy, Pageable pageable);

    List<ApprovalRequest> findAllByOrderByCreatedAtDesc();

    Optional<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<ApprovalRequest> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
