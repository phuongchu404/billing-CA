package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentSchedulerServiceImpl implements AssignmentSchedulerService {

    private final GroupPlanAssignmentRepository assignmentRepository;
    private final AssignmentAuditRepository auditRepository;

    /**
     * Chạy lúc 00:05 mỗi ngày.
     * APPROVED + applyFrom <= hôm nay → ACTIVE
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void activateApprovedAssignments() {
        LocalDate today = LocalDate.now();
        List<GroupPlanAssignment> readyList = assignmentRepository.findApprovedReadyToActivate(today);
        if (readyList.isEmpty()) return;

        log.info("[Scheduler] Kích hoạt {} assignment(s) sang ACTIVE tại {}", readyList.size(), today);
        for (GroupPlanAssignment entity : readyList) {
            String oldStatus = entity.getAssignmentStatus();
            entity.setAssignmentStatus("ACTIVE");
            entity.setActivatedAt(LocalDateTime.now());
            assignmentRepository.save(entity);
            saveAudit(entity, oldStatus, "ACTIVE", "ACTIVATE", "scheduler", null);
        }
    }

    /**
     * Chạy lúc 00:10 mỗi ngày.
     * ACTIVE + applyTo < hôm nay → EXPIRED
     */
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void expireActiveAssignments() {
        LocalDate today = LocalDate.now();
        List<GroupPlanAssignment> expiredList = assignmentRepository.findActiveReadyToExpire(today);
        if (expiredList.isEmpty()) return;

        log.info("[Scheduler] Hết hạn {} assignment(s) sang EXPIRED tại {}", expiredList.size(), today);
        for (GroupPlanAssignment entity : expiredList) {
            String oldStatus = entity.getAssignmentStatus();
            entity.setAssignmentStatus("EXPIRED");
            entity.setStoppedAt(LocalDateTime.now());
            assignmentRepository.save(entity);
            saveAudit(entity, oldStatus, "EXPIRED", "EXPIRE", "scheduler", "Tự động hết hạn theo applyTo");
        }
    }

    private void saveAudit(GroupPlanAssignment entity, String oldStatus, String newStatus,
                           String action, String actor, String note) {
        AssignmentAudit audit = new AssignmentAudit();
        audit.setGroupPlanAssignment(entity);
        audit.setAssignmentType("GROUP_PLAN");
        audit.setAction(action);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setActor(actor);
        audit.setNote(note);
        auditRepository.save(audit);
    }
}
