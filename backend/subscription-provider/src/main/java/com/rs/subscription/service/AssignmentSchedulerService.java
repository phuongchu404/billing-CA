package com.rs.subscription.service;

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

public interface AssignmentSchedulerService {

    void activateApprovedAssignments();

    void expireActiveAssignments();
}
