package com.rs.subscription.service;

import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequestStep;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

public interface ApprovalNotificationService {

    void notifySubmitted(ApprovalRequest approval);

    void notifyStepApproved(ApprovalRequest approval, int nextLevel);

    void notifyFullyApproved(ApprovalRequest approval);

    void notifyRejected(ApprovalRequest approval, ApprovalRequestStep rejectedStep);

    void notifyRevisionRequested(ApprovalRequest approval, String reason);
}
