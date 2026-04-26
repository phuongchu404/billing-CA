package com.rs.subscription.service;

import com.rs.subscription.dto.request.GrantPartnerAccessRequest;
import com.rs.subscription.dto.response.PartnerGroupAccessResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.PartnerGroupAccess;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PartnerGroupAccessRepository;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerGroupAccessService {

    private final PartnerGroupAccessRepository partnerGroupAccessRepository;
    private final UserAccountRepository userAccountRepository;
    private final GroupRepository groupRepository;

    /** Cấp quyền đối tác xem báo cáo của group. */
    @Transactional
    public PartnerGroupAccessResponse grant(GrantPartnerAccessRequest request, String grantedBy) {
        UserAccount partner = userAccountRepository.findById(request.getPartnerUserId())
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                "Partner user not found: " + request.getPartnerUserId(), 400));

        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND,
                "Group not found: " + request.getGroupId(), 404));

        // Nếu đã có access record còn hiệu lực thì trả về luôn (idempotent)
        partnerGroupAccessRepository.findActiveAccess(request.getPartnerUserId(), request.getGroupId())
            .ifPresent(existing -> { throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "Partner already has active access to this group", 409); });

        PartnerGroupAccess access = PartnerGroupAccess.builder()
            .partner(partner)
            .group(group)
            .grantedBy(grantedBy)
            .build();

        return toResponse(partnerGroupAccessRepository.save(access));
    }

    /** Thu hồi quyền đối tác (soft-delete bằng revokedAt). */
    @Transactional
    public void revoke(Long accessId, String revokedBy) {
        PartnerGroupAccess access = partnerGroupAccessRepository.findById(accessId)
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                "Access record not found: " + accessId, 404));

        if (access.getRevokedAt() != null) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "Access has already been revoked", 400);
        }
        access.setRevokedAt(LocalDateTime.now());
        partnerGroupAccessRepository.save(access);
    }

    /** Thu hồi theo partner + group (tiện dụng hơn cho UI). */
    @Transactional
    public void revokeByPartnerAndGroup(String partnerUserId, Long groupId) {
        PartnerGroupAccess access = partnerGroupAccessRepository
            .findActiveAccess(partnerUserId, groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                "No active access found for this partner/group combination", 404));
        access.setRevokedAt(LocalDateTime.now());
        partnerGroupAccessRepository.save(access);
    }

    /** Danh sách groups mà partner được xem (còn hiệu lực). */
    @Transactional(readOnly = true)
    public List<PartnerGroupAccessResponse> listActiveForPartner(String partnerUserId) {
        return partnerGroupAccessRepository.findActiveByPartner(partnerUserId)
            .stream().map(this::toResponse).toList();
    }

    /** Toàn bộ lịch sử cấp quyền cho partner (kể cả đã thu hồi). */
    @Transactional(readOnly = true)
    public List<PartnerGroupAccessResponse> listHistoryForPartner(String partnerUserId) {
        return partnerGroupAccessRepository.findByPartnerUserIdOrderByGrantedAtDesc(partnerUserId)
            .stream().map(this::toResponse).toList();
    }

    private PartnerGroupAccessResponse toResponse(PartnerGroupAccess a) {
        PartnerGroupAccessResponse r = new PartnerGroupAccessResponse();
        r.setId(a.getId());
        r.setPartnerUserId(a.getPartner().getUserId());
        r.setPartnerName(a.getPartner().getFullName());
        r.setGroupId(a.getGroup().getGroupId());
        r.setGroupCode(a.getGroup().getGroupCode());
        r.setGroupName(a.getGroup().getGroupName());
        r.setGrantedBy(a.getGrantedBy());
        r.setGrantedAt(a.getGrantedAt());
        r.setRevokedAt(a.getRevokedAt());
        r.setActive(a.isActive());
        return r;
    }
}
