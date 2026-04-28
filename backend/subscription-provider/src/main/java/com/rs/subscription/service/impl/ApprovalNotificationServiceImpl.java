package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequestStep;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gửi email notification cho từng sự kiện trong luồng phê duyệt.
 * Tất cả phương thức đều @Async để không block luồng xử lý chính.
 *
 * Mapping role name (trong bảng roles) ↔ approval level:
 *   APPROVAL_L1  →  Trưởng phòng  (Level 1)
 *   APPROVAL_L2  →  Giám đốc      (Level 2)
 *   APPROVAL_L3  →  CFO           (Level 3)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalNotificationServiceImpl implements ApprovalNotificationService {

    private static final String[] LEVEL_PERMISSION_KEYS = {"approval:level1", "approval:level2", "approval:level3"};
    private static final String[] LEVEL_DISPLAY = {"Trưởng phòng", "Giám đốc", "CFO"};

    private final MailService mailService;
    private final UserAccountRepository userAccountRepository;

    // ─── Submit: gửi cho approver Level 1 ────────────────────────────────────

    @Async
    public void notifySubmitted(ApprovalRequest approval) {
        sendToApproversAtLevel(approval, 1);
    }

    // ─── Step approved, chuyển cấp tiếp theo ─────────────────────────────────

    @Async
    public void notifyStepApproved(ApprovalRequest approval, int nextLevel) {
        sendToApproversAtLevel(approval, nextLevel);
    }

    // ─── Fully approved → notify Sale (requester) ─────────────────────────────

    @Async
    public void notifyFullyApproved(ApprovalRequest approval) {
        String requesterEmail = resolveRequesterEmail(approval.getRequestedBy());
        if (requesterEmail == null) return;

        String subject = "[Billing] Request #" + approval.getId() + " đã được phê duyệt hoàn tất";
        String body = buildBody(
            "Request của bạn đã được phê duyệt toàn bộ cấp và sẽ được kích hoạt.",
            approval
        );
        mailService.send(requesterEmail, subject, body);
    }

    // ─── Rejected → notify Sale ───────────────────────────────────────────────

    @Async
    public void notifyRejected(ApprovalRequest approval, ApprovalRequestStep rejectedStep) {
        String requesterEmail = resolveRequesterEmail(approval.getRequestedBy());
        if (requesterEmail == null) return;

        String subject = "[Billing] Request #" + approval.getId() + " bị từ chối";
        String body = buildBody(
            "Request của bạn bị từ chối tại cấp " + rejectedStep.getStepLevel()
                + " (" + levelDisplayName(rejectedStep.getStepLevel()) + ").\n"
                + "Lý do: " + (rejectedStep.getComment() != null ? rejectedStep.getComment() : "Không có ghi chú"),
            approval
        );
        mailService.send(requesterEmail, subject, body);
    }

    // ─── Revision → notify Sale ───────────────────────────────────────────────

    @Async
    public void notifyRevisionRequested(ApprovalRequest approval, String reason) {
        String requesterEmail = resolveRequesterEmail(approval.getRequestedBy());
        if (requesterEmail == null) return;

        String subject = "[Billing] Request #" + approval.getId() + " cần chỉnh sửa";
        String body = buildBody(
            "Approver yêu cầu bạn chỉnh sửa request.\n"
                + "Lý do: " + (reason != null ? reason : "Không có ghi chú")
                + "\n\nVui lòng đăng nhập hệ thống, chỉnh sửa và resubmit.",
            approval
        );
        mailService.send(requesterEmail, subject, body);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void sendToApproversAtLevel(ApprovalRequest approval, int level) {
        if (level < 1 || level > LEVEL_PERMISSION_KEYS.length) return;

        String permissionKey = LEVEL_PERMISSION_KEYS[level - 1];
        List<UserAccount> approvers = userAccountRepository
            .findActiveManagersByUsernameAndPermissionKey(approval.getRequestedBy(), permissionKey);
        if (approvers.isEmpty()) {
            log.warn("Không tìm thấy approver cấp {} trong tuyến quản lý của user {}, fallback theo permission {} cho approval request #{}",
                level, approval.getRequestedBy(), permissionKey, approval.getId());
            approvers = userAccountRepository.findActiveUsersByPermissionKey(permissionKey);
        }

        if (approvers.isEmpty()) {
            log.warn("Không tìm thấy approver nào có permission {} cho approval request #{}", permissionKey, approval.getId());
            return;
        }

        String subject = "[Billing] Cần phê duyệt — Request #" + approval.getId()
            + " (Cấp " + level + " — " + levelDisplayName(level) + ")";

        String body = buildBody(
            "Có một yêu cầu phê duyệt đang chờ bạn xử lý tại cấp " + level
                + " (" + levelDisplayName(level) + ")."
                + "\n\nVui lòng đăng nhập hệ thống để xem chi tiết và thực hiện phê duyệt.",
            approval
        );

        for (UserAccount approver : approvers) {
            mailService.send(approver.getEmail(), subject, body);
        }
    }

    private String resolveRequesterEmail(String username) {
        return userAccountRepository
            .findByUsernameAndStatus(username, "ACTIVE")
            .map(UserAccount::getEmail)
            .orElseGet(() -> {
                log.warn("Không tìm thấy email cho user: {}", username);
                return null;
            });
    }

    private String buildBody(String message, ApprovalRequest approval) {
        return message + "\n\n"
            + "─────────────────────────────\n"
            + "Thông tin request:\n"
            + "  Mã request  : #" + approval.getId() + "\n"
            + "  Loại KH     : " + segmentLabel(approval.getCustomerSegment()) + "\n"
            + "  Người tạo   : " + approval.getRequestedBy() + "\n"
            + "  Mô tả       : " + approval.getDescription() + "\n"
            + "  Giá trị HĐ  : " + formatAmount(approval) + "\n"
            + "  Số cấp duyệt: " + approval.getTotalLevels() + " cấp\n"
            + "─────────────────────────────\n"
            + "Hệ thống Billing — Subscription Management";
    }

    private String segmentLabel(String segment) {
        if ("GROUP".equals(segment)) return "Khách hàng đại lý";
        return "Khách hàng phổ thông";
    }

    private String formatAmount(ApprovalRequest approval) {
        if (approval.getContractValue() == null) return "N/A";
        return String.format("%,.0f VND", approval.getContractValue());
    }

    private String levelDisplayName(int level) {
        if (level >= 1 && level <= LEVEL_DISPLAY.length) return LEVEL_DISPLAY[level - 1];
        return "Cấp " + level;
    }
}
